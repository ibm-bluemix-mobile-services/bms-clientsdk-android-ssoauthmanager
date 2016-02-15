package com.ibm.bluemix.sso.authorizationmanager;

import android.content.Context;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthorizationManager;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.UserIdentity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class SSOAuthorizationManager implements AuthorizationManager {

	private static final Logger logger = Logger.getLogger(SSOAuthorizationManager.class.getName());
	private static final String WWW_AUTHENTICATE_HEADER_NAME = "Www-Authenticate";
	private static final String SSO_AUTH_HEADER_SCOPE_BEARER = "Bearer scope=\"ssoAuthentication\"";
	private static final String AUTHORIZATION_URL_HEADER_NAME = "X-Sso-Authorization-Url";
	private static final String AUTHENTICATION_URL_HEADER_NAME = "X-Sso-Authentication-Url";

	private static SSOAuthorizationManager instance = null;
	private static String authorizationUrl = null;
	private static String authenticationUrl = null;
	//private static SSOWebView authWebView = null;
	private static SSOAuthenticationListener authenticationListener = null;

	private SSOAuthorizationManager(){}

	public static SSOAuthorizationManager getInstance () {
		if (null == instance){
			logger.info("Creating a new instance");
			instance = new SSOAuthorizationManager();
		}
		return instance;
	}

	public void init(SSOAuthenticationListener authenticationListener){
		SSOAuthorizationManager.authenticationListener = authenticationListener;
	}

	public boolean isAuthorizationRequired(int responseCode, Map<String, List<String>> headers){

//		if (headers.containsKey("Set-Cookie")){
//			String setCookieHeader = headers.get("Set-Cookie").get(0);
//			logger.debug("Synchronizing cookie :: " + setCookieHeader);
//			String appRoute = BMSClient.getInstance().getBluemixAppRoute();
//			CookieManager.getInstance().setCookie(appRoute, setCookieHeader);
//		}

		if (!headers.containsKey(WWW_AUTHENTICATE_HEADER_NAME) ||
				!headers.containsKey(AUTHORIZATION_URL_HEADER_NAME) ||
				!headers.containsKey(AUTHENTICATION_URL_HEADER_NAME)) {
			return false;
		}

		String authHeader = headers.get(WWW_AUTHENTICATE_HEADER_NAME).get(0);

		if (responseCode == 401 && authHeader.equals(SSO_AUTH_HEADER_SCOPE_BEARER)) {
			authorizationUrl = headers.get(AUTHORIZATION_URL_HEADER_NAME).get(0);
			authenticationUrl = headers.get(AUTHENTICATION_URL_HEADER_NAME).get(0);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isAuthorizationRequired (HttpURLConnection urlConnection) throws IOException {
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public String getCachedAuthorizationHeader () {
		return null;
	}

	@Override
	public void obtainAuthorization (Context context, final ResponseListener listener, Object... params) {
		logger.debug("obtainAuthorizationHeader");

		SSOAuthenticationManager authManager = new SSOAuthenticationManager(context,
				authenticationListener, listener, authenticationUrl, authorizationUrl);

		authManager.startAuthentication();

//		final Activity activity = (Activity) context;
//
//		activity.runOnUiThread(new Runnable() {
//			@Override
//			public void run () {
//				authWebView = new SSOWebView(activity, listener);
//				authWebView.clearCache(true);
//				authWebView.clearHistory();
//				authWebView.setWebViewClient(new SSOWebViewClient(authWebView));
//				authWebView.setWebChromeClient(new SSOWebChromeClient(authWebView));
//				authWebView.getSettings().setJavaScriptEnabled(true);
//				authWebView.loadUrl(loginUrl);
//			}
//		});

	}

	@Override
	public void clearAuthorizationData () {
		BMSClient.getInstance().getCookieManager().getCookieStore().removeAll();
	}

	@Override
	public UserIdentity getUserIdentity () {
		return null;
	}

	@Override
	public DeviceIdentity getDeviceIdentity () {
		return null;
	}

	@Override
	public AppIdentity getAppIdentity () {
		return null;
	}

}
