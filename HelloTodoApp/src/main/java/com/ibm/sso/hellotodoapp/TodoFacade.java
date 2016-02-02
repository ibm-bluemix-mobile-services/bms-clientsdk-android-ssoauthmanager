package com.ibm.sso.hellotodoapp;


import android.content.Context;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TodoFacade {

	private static final Logger logger = Logger.getLogger(TodoFacade.class.getName());

	public static void getItems(Context context, final TodoResponseListener<JSONArray> listener){
		logger.debug("getItems");
		Request request = new Request("/api/Items", Request.GET);
		request.send(context, new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("onSuccess :: " + response.getResponseText());
				try {
					listener.onSuccess(new JSONArray(response.getResponseText()));
				} catch (JSONException e){
					logger.error("failed to parse response", e);
					listener.onFailure("Invalid server response");
				}
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					logger.error("onFailure :: " + t.getMessage());
				} else if (extendedInfo!=null){
					logger.error("onFailure :: " + extendedInfo.toString());
					try {
						String errorMessage = extendedInfo.getString("message");
						listener.onFailure(errorMessage);
						return;
					} catch (JSONException e){
						logger.error("onFailure :: Failed parsing response",e);
					}
				} else {
					logger.error("onFailure :: " + response.getStatus() + " " + response.getResponseText());
				}
				listener.onFailure("Invalid server response");
			}
		});
	}

	public static void addNewItem(Context context, String text, final TodoResponseListener listener) {
		logger.debug("addNewItem :: " + text);
		TodoItem newItem = new TodoItem(text);

		Request request = new Request("/api/Items", Request.POST);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Accept", "application/json");

		request.send(context, newItem.toJSONObject().toString(), new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("onSuccess");
				listener.onSuccess(null);
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					logger.error("onFailure :: " + t.getMessage());
				} else if (extendedInfo!=null){
					logger.error("onFailure :: " + extendedInfo.toString());
					try {
						String errorMessage = extendedInfo.getString("message");
						listener.onFailure(errorMessage);
						return;
					} catch (JSONException e){
						logger.error("onFailure :: Failed parsing response",e);
					}
				} else {
					logger.error("onFailure :: " + response.getStatus() + " " + response.getResponseText());
				}
				listener.onFailure("Invalid server response");
			}
		});
	}

	public static void updateItem(Context context, TodoItem item, final TodoResponseListener listener) {
		logger.debug("updateItem :: " + item.toJSONObject().toString());

		Request request = new Request("/api/Items", Request.PUT);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Accept", "application/json");

		request.send(context, item.toJSONObject().toString(), new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("onSuccess");
				listener.onSuccess(null);
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					logger.error("onFailure :: " + t.getMessage());
				} else if (extendedInfo!=null){
					logger.error("onFailure :: " + extendedInfo.toString());
					try {
						String errorMessage = extendedInfo.getString("message");
						listener.onFailure(errorMessage);
						return;
					} catch (JSONException e){
						logger.error("onFailure :: Failed parsing response",e);
					}
				} else {
					logger.error("onFailure :: " + response.getStatus() + " " + response.getResponseText());
				}
				listener.onFailure("Invalid server response");
			}
		});
	}

	public static void deleteItem(Context context, int itemId, final TodoResponseListener listener) {
		logger.debug("deleteItem :: " + itemId);

		Request request = new Request("/api/Items/" + itemId, Request.DELETE);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Accept", "application/json");

		request.send(context, new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("onSuccess");
				listener.onSuccess(null);
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					logger.error("onFailure :: " + t.getMessage());
				} else if (extendedInfo!=null){
					logger.error("onFailure :: " + extendedInfo.toString());
					try {
						String errorMessage = extendedInfo.getString("message");
						listener.onFailure(errorMessage);
						return;
					} catch (JSONException e){
						logger.error("onFailure :: Failed parsing response",e);
					}
				} else {
					logger.error("onFailure :: " + response.getStatus() + " " + response.getResponseText());
				}
				listener.onFailure("Invalid server response");
			}
		});
	}


}
