package com.ibm.sso.hellotodoapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ibm.bluemix.sso.authorizationmanager.SSOAuthenticationListener;
import com.ibm.bluemix.sso.authorizationmanager.SSOAuthenticationManager;
import com.ibm.bluemix.sso.authorizationmanager.SSOAuthorizationManager;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
		SSOAuthenticationListener,
		SwipeRefreshLayout.OnRefreshListener,
		ShakeDetector.Listener{
	private static final Logger logger = Logger.getLogger("MainActivity");
	private static final String appRoute = "http://hellotodo-with-sso.mybluemix.net";
	private static final String appGuid = "";
	private ProgressDialog progressDialog;
	private TodoItemAdapter todoItemAdapter;
	private List<TodoItem> todoItemList;
	private ListView todoItemsListView;
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Loading");
		progressDialog.setTitle("HelloTodo");
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setOnRefreshListener(this);

		todoItemList = new ArrayList<>();
		todoItemAdapter = new TodoItemAdapter(this, todoItemList);
		todoItemsListView = (ListView)findViewById(R.id.todoItemsListView);
		todoItemsListView.setAdapter(todoItemAdapter);
		todoItemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick (AdapterView<?> adapterView, View view, int i, long l) {
				logger.debug("onItemLongClick :: " + i);
				onDeleteClicked(i);
				return true;
			}
		});

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		ShakeDetector sd = new ShakeDetector(this);
		sd.setSensitivity(ShakeDetector.SENSITIVITY_HARD);
		sd.start(sensorManager);

		try {
			BMSClient.getInstance().initialize(this, appRoute, appGuid, BMSClient.REGION_US_SOUTH);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		SSOAuthorizationManager.getInstance().init(this);
		BMSClient.getInstance().setAuthorizationManager(SSOAuthorizationManager.getInstance());

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addNewItemButton);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View view) {
				logger.debug("addNewItemButton clicked");
				addNewItemClicked();
			}
		});

		loadTodoList();
	}

	public void loadTodoList () {
		logger.debug("loadTodoList");
		showProgressDialog(true);

		TodoFacade.getItems(this, new TodoResponseListener<JSONArray>() {
			@Override
			public void onSuccess (JSONArray itemsList) {
				showProgressDialog(false);
				populateList(itemsList);
			}

			@Override
			public void onFailure (String errorMsg) {
				showProgressDialog(false);
				showToast(errorMsg);
			}
		});
	}

	public void populateList(JSONArray itemsList){
		logger.debug("populateList");
		todoItemList.clear();
		try {
			for (int i = 0; i < itemsList.length(); i++) {
				JSONObject item = itemsList.getJSONObject(i);
				TodoItem todoItem = new TodoItem(item);
				todoItemList.add(todoItem);
			}
		} catch (JSONException e){
			logger.error("Failed parsing JSON");
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				todoItemAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onAuthenticationChallengeReceived (Context context, final SSOAuthenticationManager authManager) {
		logger.debug("onAuthenticationChallengeReceived");
		showProgressDialog(false);

		final LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		final EditText username = new EditText(this);
		final EditText password = new EditText(this);
		username.setHint("Enter username");
		password.setHint("Enter password");

		password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		linearLayout.addView(username);
		linearLayout.addView(password);

//		username.setText("demo");
//		password.setText("demo");

		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Enter username and password");
		alertBuilder.setView(linearLayout);

		alertBuilder.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
			@Override
			public void onClick (DialogInterface dialogInterface, int i) {
				logger.debug("onLoginClicked");
				showProgressDialog(true);
				JSONObject credentials = new JSONObject();
				try {
					credentials.put("username",username.getText().toString());
					credentials.put("password",password.getText().toString());
				} catch (JSONException e) {
					throw new RuntimeException("Should never happen");
				}

				authManager.submitCredentials(credentials);
			}
		});

		this.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				alertBuilder.show();
				username.requestFocus();
			}
		});
	}

	@Override
	public void onRefresh () {
		logger.debug("onRefresh");
		swipeRefreshLayout.setRefreshing(false);
		loadTodoList();
	}

	public void addNewItemClicked(){
		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Create new todo item");
		final EditText todoTextInput = new EditText(this);
		todoTextInput.setHint("todo item text");
		alertBuilder.setView(todoTextInput);
		alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick (DialogInterface dialogInterface, int i) {
				logger.debug("new item :: save");
				String todoText = todoTextInput.getText().toString();
				showProgressDialog(true);
				TodoFacade.addNewItem(MainActivity.this, todoText, new TodoResponseListener() {
					@Override
					public void onSuccess (Object response) {
						loadTodoList();
					}

					@Override
					public void onFailure (String msg) {
						showProgressDialog(false);
						showToast(msg);
					}
				});

			}
		});

		alertBuilder.setNegativeButton("Cancel", null);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				alertBuilder.show();
				todoTextInput.requestFocus();
			}
		});

	}

	public void showToast(final String text){
		this.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
			}
		});
	}

	public void showProgressDialog(final boolean show){
		this.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				if (show) {
					progressDialog.show();
				} else {
					progressDialog.dismiss();
				}
			}
		});
	}

	public void onEditClicked (View view) {
		logger.debug("onEditClicked");
		int index = todoItemsListView.getPositionForView(view);
		final TodoItem item = todoItemList.get(index);

		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Update todo item");

		final EditText todoTextInput = new EditText(this);
		todoTextInput.setHint("todo item text");
		todoTextInput.setText(item.getText());
		alertBuilder.setView(todoTextInput);
		alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick (DialogInterface dialogInterface, int i) {
				logger.debug("new item :: save");
				String todoText = todoTextInput.getText().toString();
				TodoItem updatedItem = new TodoItem(item.getId(), todoText, item.isDone());
				showProgressDialog(true);
				TodoFacade.updateItem(MainActivity.this, updatedItem, new TodoResponseListener() {
					@Override
					public void onSuccess (Object response) {
						loadTodoList();
					}

					@Override
					public void onFailure (String msg) {
						showProgressDialog(false);
						showToast(msg);
					}
				});

			}
		});

		this.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				alertBuilder.show();
				todoTextInput.requestFocus();
			}
		});
	}

	public void onToggleClicked (View view) {
		logger.debug("onToggleClicked");
		int index = todoItemsListView.getPositionForView(view);
		final TodoItem item = todoItemList.get(index);
		TodoItem updatedItem = new TodoItem(item.getId(), item.getText(), !item.isDone());
		showProgressDialog(true);
		TodoFacade.updateItem(MainActivity.this, updatedItem, new TodoResponseListener() {
			@Override
			public void onSuccess (Object response) {
				loadTodoList();
			}

			@Override
			public void onFailure (String msg) {
				showProgressDialog(false);
				showToast(msg);
			}
		});

	}

	public void onDeleteClicked(int index){
		logger.debug("onDeleteClicked");
		final TodoItem item = todoItemList.get(index);

		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Delete todo item?");
		alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick (DialogInterface dialogInterface, int i) {
				showProgressDialog(true);
				TodoFacade.deleteItem(MainActivity.this, item.getId(), new TodoResponseListener() {
					@Override
					public void onSuccess (Object response) {
						loadTodoList();
					}

					@Override
					public void onFailure (String msg) {
						showProgressDialog(false);
						showToast(msg);
					}
				});
			}
		});

		alertBuilder.setNegativeButton("NO", null);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				alertBuilder.show();
			}
		});
	}

	public void hearShake() {
		SSOAuthorizationManager.getInstance().clearAuthorizationData();
		Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
	}
}
