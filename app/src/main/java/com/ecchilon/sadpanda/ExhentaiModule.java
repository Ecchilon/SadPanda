package com.ecchilon.sadpanda;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.squareup.picasso.Picasso;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class ExhentaiModule extends AbstractModule {

	@Inject
	private Context context;

	public ExhentaiModule(Application application) {
		context = application;
	}

	@Override
	protected void configure() {
		bind(ExhentaiAuth.class).in(Singleton.class);

		Picasso.Builder builder = new Picasso.Builder(context);
		builder.listener(new SadPandaApp.PicassoListener());

		Picasso.setSingletonInstance(builder.build());
	}

	@Provides
	@Singleton
	private OkHttpClient client(Cache cache) {
		return new OkHttpClient.Builder().cache(cache).build();
	}

	@Provides
	private Cache cache(Context context) {
		File cacheDir;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cacheDir = context.getExternalCacheDir();
		}
		else {
			cacheDir = context.getCacheDir();
		}
		int cacheSize = 10 * 1024 * 1024;
		return new Cache(new File(cacheDir, "http"), cacheSize);
	}
}
