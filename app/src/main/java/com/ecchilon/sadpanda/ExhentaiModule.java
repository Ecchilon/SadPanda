package com.ecchilon.sadpanda;

import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.squareup.picasso.Picasso;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * Created by Alex on 20-9-2014.
 */
public class ExhentaiModule extends AbstractModule {

    private static final String USER_AGENT = "Exhentai mobile";

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
    @Singleton
    private HttpClient getClient() {
        HttpClient httpClient = AndroidHttpClient.newInstance(USER_AGENT);

        return httpClient;
    }

    @Provides
    @Singleton
    private HttpContext getContext() {
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
        return context;
    }

    private class PicassoListener implements Picasso.Listener {
        @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            Log.e("Picasso", "Failed to load image for " + uri.toString(), exception);
        }
    }
}
