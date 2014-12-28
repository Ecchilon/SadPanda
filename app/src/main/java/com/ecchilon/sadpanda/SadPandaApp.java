package com.ecchilon.sadpanda;

import android.app.Application;
import roboguice.RoboGuice;

public class SadPandaApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		RoboGuice.getOrCreateBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this), new ExhentaiModule(this));
	}
}
