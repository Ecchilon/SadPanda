package com.ecchilon.sadpanda;


import android.app.Application;
import android.content.Context;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ExhentaiModule extends AbstractModule {

	@Inject
	private Context context;

	public ExhentaiModule(Application application) {
		context = application;
	}

	@Override
	protected void configure() {
		bind(ExhentaiAuth.class).in(Singleton.class);

		Provider<ExhentaiAuth> provider = getProvider(ExhentaiAuth.class);
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(chain -> {
					Request newRequest = chain.request().newBuilder()
							.addHeader("Cookie", provider.get().getSessionCookie())
							.build();
					return chain.proceed(newRequest);
				})
				.build();

		Picasso.Builder builder = new Picasso.Builder(context);
		builder.listener(new SadPandaApp.PicassoListener());
		builder.downloader(new OkHttp3Downloader(client));

		Picasso.setSingletonInstance(builder.build());
	}

	@Provides
	@Singleton
	private OkHttpClient client() {
		return new OkHttpClient();
	}
}
