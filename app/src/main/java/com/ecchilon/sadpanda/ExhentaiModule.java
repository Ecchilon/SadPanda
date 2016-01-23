package com.ecchilon.sadpanda;

import android.app.Application;
import android.content.Context;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.squareup.picasso.Picasso;
import okhttp3.OkHttpClient;

/**
 * Created by Alex on 20-9-2014.
 */
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
	private OkHttpClient client() {
		return new OkHttpClient();
	}
}
