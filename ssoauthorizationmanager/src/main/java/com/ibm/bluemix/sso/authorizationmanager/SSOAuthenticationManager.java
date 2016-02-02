package com.ibm.bluemix.sso.authorizationmanager;

import android.content.Context;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class SSOAuthenticationManager {

	private static final Logger logger = Logger.getLogger(SSOAuthenticationManager.class.getName());
	private final Context context;
	private final SSOAuthenticationListener authenticationListener;
	private final ResponseListener responseListener;
	private final String authUrl;
	private final String azUrl;

	public SSOAuthenticationManager (Context context,
									 SSOAuthenticationListener authenticationListener,
									 ResponseListener responseListener,
									 String authUrl,
									 String azUrl){
		this.context = context;
		this.authenticationListener = authenticationListener;
		this.responseListener = responseListener;
		this.authUrl = authUrl;
		this.azUrl = azUrl;
	}

	protected void startAuthentication() {
		logger.debug("startAuthentication");
		authenticationListener.onAuthenticationChallengeReceived(context, this);
	}

	public void submitCredentials(final JSONObject credentials){
		logger.debug("submitCredentials ::" + credentials.toString());

		String requestUrl = authUrl + "?PolicyId=urn:ibm:security:authentication:asf:basicldapuser";
		logger.debug("sending initAuthFlowRequest :: " + requestUrl);

		Request initAuthFlowRequest = new Request(requestUrl, Request.POST);
		initAuthFlowRequest.addHeader("Content-Type", "application/json");
		initAuthFlowRequest.addHeader("Accept", "application/json");

		initAuthFlowRequest.send(context, new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("initAuthFlowRequest onSuccess ::" + response.getResponseText());
				JSONObject responseJson;
				try {
					responseJson = new JSONObject(response.getResponseText());
					String stateId = responseJson.getString("state");
					login(stateId, credentials);
				} catch (JSONException e) {
					responseListener.onFailure(null, e, null);
				}
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					logger.error("initAuthFlowRequest onFailure :: " + t.getMessage());
					responseListener.onFailure(null, t, null);
				} else if (extendedInfo!=null){
					logger.error("initAuthFlowRequest onFailure :: " + extendedInfo.toString());
					responseListener.onFailure(null, null, extendedInfo);
				} else {
					logger.error("initAuthFlowRequest onFailure :: " + response.getStatus() + " " + response.getResponseText());
					responseListener.onFailure(response, null, null);
				}
			}
		});

	}

	private void login(final String stateId, final JSONObject credentials){
		String requestUrl = authUrl + "?StateId=" + stateId;
		logger.debug("sending loginRequest :: " + requestUrl);

		Request loginRequest = new Request(requestUrl, Request.PUT);
		loginRequest.addHeader("Content-Type", "application/json");
		loginRequest.addHeader("Accept", "application/json");

		loginRequest.send(context, credentials.toString(), new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("loginRequest onSuccess ::" + response.getStatus() + " " + response.getResponseText());
				if (response.getStatus() == 204){
					authorize();
				} else {
					try {
						JSONObject responseJson = new JSONObject(response.getResponseText());
						responseListener.onFailure(null, null, responseJson);
					} catch (JSONException e){
						responseListener.onFailure(null, e, null);
					}
				}
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					logger.error("loginRequest onFailure :: " + t.getMessage());
					responseListener.onFailure(null, t, null);
				} else if (extendedInfo!=null){
					logger.error("loginRequest onFailure :: " + extendedInfo.toString());
					responseListener.onFailure(null, null, extendedInfo);
				} else {
					logger.error("loginRequest onFailure :: " + response.getStatus() + " " + response.getResponseText());
					responseListener.onFailure(response, null, null);
				}
			}
		});
	}

	private void authorize(){
		logger.debug("sending azRequest :: " + azUrl);
		Request azRequest = new Request(azUrl, Request.GET);

		azRequest.send(context, new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.debug("azRequest onSuccess failure ::" + response.getStatus() + " " + response.getResponseText());
				responseListener.onFailure(response, null, null);
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				if (t!=null){
					if (t.getMessage().contains("sso-callback-success")){
						logger.debug("Authentication+authorization success");
						responseListener.onSuccess(null);
					} else {
						logger.error("azRequest onFailure :: " + t.getMessage());
						responseListener.onFailure(null, t, null);
					}
				} else if (extendedInfo!=null){
					logger.error("azRequest onFailure :: " + extendedInfo.toString());
					responseListener.onFailure(null, null, extendedInfo);
				} else {
					logger.error("azRequest onFailure :: " + response.getStatus() + " " + response.getResponseText());
					responseListener.onFailure(response, null, null);
				}
			}
		});
	}
}
