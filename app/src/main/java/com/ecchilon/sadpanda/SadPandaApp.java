package com.ecchilon.sadpanda;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.util.Log;

public class SadPandaApp extends Application {

	private static final String TAG = "SadPandaApp";

	private static final long HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10 MiB

	@Override
	public void onCreate() {
		super.onCreate();

		File httpCacheDir = new File(getCacheDir(), "http");

		try {
			HttpResponseCache.install(httpCacheDir, HTTP_CACHE_SIZE);
		}
		catch (IOException e) {
			Log.e(TAG, "Failed to create http cache!", e);
		}
	}
}
