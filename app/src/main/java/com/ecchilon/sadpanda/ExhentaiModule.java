package com.ecchilon.sadpanda;

import android.content.Context;

import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.client.CookieStore;

/**
 * Created by Alex on 20-9-2014.
 */
public class ExhentaiModule extends AbstractModule {

    private final Context mContext;

    @Inject
    public ExhentaiModule(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void configure() {
        bind(ExhentaiAuth.class).in(Singleton.class);
    }

    @Provides
    private AsyncHttpClient getAsyncClient() {
        AsyncHttpClient httpClient = new AsyncHttpClient ();

        CookieStore cookieStore = new PersistentCookieStore(mContext);
        httpClient.setCookieStore(cookieStore);
        return httpClient;
    }
}
