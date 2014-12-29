package com.ecchilon.sadpanda;

import java.net.CookieHandler;
import java.net.CookiePolicy;

import android.app.Application;
import roboguice.RoboGuice;

public class SadPandaApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		java.net.CookieManager cookieManager = new java.net.CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);

		RoboGuice.getOrCreateBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this), new ExhentaiModule(this));
	}
}
