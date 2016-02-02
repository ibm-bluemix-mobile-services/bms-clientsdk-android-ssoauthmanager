package com.ibm.sso.hellotodoapp;


import org.json.JSONException;
import org.json.JSONObject;

public class TodoItem{
	private int id;
	private String text;
	private boolean isDone;

	public TodoItem(int id, String text, boolean isDone){
		this.id = id;
		this.text = text;
		this.isDone = isDone;
	}

	public TodoItem(JSONObject jsonObject){
		try{
			this.id = jsonObject.getInt("id");
			this.text = jsonObject.getString("text");
			this.isDone = jsonObject.getBoolean("isDone");
		} catch (JSONException e){
			throw new RuntimeException(e);
		}
	}
	public TodoItem(String text){
		this(0, text, false);
	}

	public int getId () {
		return id;
	}

	public String getText () {
		return text;
	}

	public boolean isDone () {
		return isDone;
	}

	public JSONObject toJSONObject(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", this.id);
			obj.put("text", this.text);
			obj.put("isDone", this.isDone);
			return obj;
		} catch (JSONException e) {
			throw new RuntimeException("Should never happen");
		}
	}
}
