package com.ibm.sso.hellotodoapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		final Activity ctx = this;

		TimerTask tt = new TimerTask() {
			@Override
			public void run () {
				Intent i = new Intent(ctx, MainActivity.class);
				ctx.startActivity(i);
				ctx.finish();
			}
		};

		new Timer().schedule(tt, 1000);


	}

}
