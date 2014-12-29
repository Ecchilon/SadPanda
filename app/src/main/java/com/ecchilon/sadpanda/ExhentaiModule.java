package com.ecchilon.sadpanda;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.net.PersistentCookieStore;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

/**
 * Created by Alex on 20-9-2014.
 */
public class ExhentaiModule extends AbstractModule {

    private static final String HTTP_CACHE_FILE = "http-cache";
    private static final long MAX_CACHE_SIZE = 1024 * 1024 * 10;    //10MB

    private final Context mContext;

    @Inject
    public ExhentaiModule(Context context) {
        this.mContext = context;
    }

    @Override
    protected void configure() {
        bind(ExhentaiAuth.class).in(Singleton.class);

        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener(new PicassoListener());

        Picasso.setSingletonInstance(builder.build());
    }

    @Provides
    private OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();

        client.setCookieHandler(new CookieManager(
                new PersistentCookieStore(mContext),
                CookiePolicy.ACCEPT_ALL));

        File httpCache = new File(mContext.getCacheDir(), HTTP_CACHE_FILE);
        if(!httpCache.exists()) {
            httpCache.mkdir();
        }

        try {
            Cache cache = new Cache(httpCache, MAX_CACHE_SIZE);
            client.setCache(cache);
        }
        catch (IOException e) {

        }
        return client;
    }

    private class PicassoListener implements Picasso.Listener {

        @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            Log.e("Picasso", "Failed to load image for " + uri.toString(), exception);
        }
    }
}
