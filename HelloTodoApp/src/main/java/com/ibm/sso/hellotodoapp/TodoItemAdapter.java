package com.ibm.sso.hellotodoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;


public class TodoItemAdapter extends BaseAdapter implements ListAdapter {

	private List<TodoItem> mTodoItems;
	private Context mContext;

	public TodoItemAdapter(Context context, List<TodoItem> todoItems) {
		mContext = context;
		mTodoItems = todoItems;
	}

	public int getCount() {
		return mTodoItems.size();
	}

	public Object getItem(int position) {
		return mTodoItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			// Inflate ListView row layout
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.todoitem, parent, false);
		}

		// Get Views from layout
		ImageButton button = (ImageButton) convertView.findViewById(R.id.isdone);
		TextView text = (TextView) convertView.findViewById(R.id.itemtext);

		// Get relevant TodoItem
		TodoItem todoItem = mTodoItems.get(position);

		// Set TodoItem text
		String name = todoItem.getText();
		text.setText(name);

		// Set TodoItem completed image
		if (todoItem.isDone()) {
			button.setImageResource(R.drawable.checkbox1);
		}else {
			button.setImageResource(R.drawable.checkbox2);
		}

		return convertView;
	}

}
