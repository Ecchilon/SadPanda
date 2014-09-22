package com.ecchilon.sadpanda;

import android.content.Context;

import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
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
        AsyncHttpClient httpClient = new AsyncHttpClient ();

        CookieStore cookieStore = new PersistentCookieStore(mContext);
        httpClient.setCookieStore(cookieStore);

        ExhentaiAuth auth = new ExhentaiAuth(httpClient);

        bind(AsyncHttpClient.class).toInstance(httpClient);
        bind(ExhentaiAuth.class).toInstance(auth);
    }
}
