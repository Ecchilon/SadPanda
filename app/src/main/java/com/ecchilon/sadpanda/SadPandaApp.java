package com.ecchilon.sadpanda;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.util.Log;
import com.squareup.picasso.Picasso;
import lombok.Getter;

public class SadPandaApp extends Application {

	private static final String TAG = "SadPandaApp";

	private static final long HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10 MiB

	@Getter
	private static Exception lastException;

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

	public static class PicassoListener implements Picasso.Listener {
		@Override
		public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
			Log.e("Picasso", "Failed to load image for " + uri.toString(), exception);
			lastException = exception;
		}
	}
}
