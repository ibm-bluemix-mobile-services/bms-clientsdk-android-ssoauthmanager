package com.ibm.bluemix.sso.authorizationmanager;

import android.content.Context;

public interface SSOAuthenticationListener {
	void onAuthenticationChallengeReceived (Context context, SSOAuthenticationManager authManager);
}
