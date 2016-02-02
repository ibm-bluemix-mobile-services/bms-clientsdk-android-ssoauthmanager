package com.ibm.sso.hellotodoapp;

public interface TodoResponseListener<T> {
	void onSuccess (T response);
	void onFailure (String msg);
}
