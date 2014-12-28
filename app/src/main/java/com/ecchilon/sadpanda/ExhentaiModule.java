package com.ecchilon.sadpanda;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.bookmarks.BookmarkController;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.squareup.picasso.Picasso;
import org.apache.http.client.CookieStore;

/**
 * Created by Alex on 20-9-2014.
 */
public class ExhentaiModule extends AbstractModule {

    private final Context mContext;

    @Inject
    public ExhentaiModule(Context context) {
        this.mContext = context;
    }

    @Override
    protected void configure() {
        bind(ExhentaiAuth.class).in(Singleton.class);
        bind(BookmarkController.class).in(Singleton.class);

        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener(new PicassoListener());

        Picasso.setSingletonInstance(builder.build());
    }

    @Provides
    private AsyncHttpClient getClient() {
        AsyncHttpClient httpClient = new AsyncHttpClient ();

        CookieStore cookieStore = new PersistentCookieStore(mContext);
        httpClient.setCookieStore(cookieStore);
        return httpClient;
    }

    private class PicassoListener implements Picasso.Listener {

        @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            Log.e("Picasso", "Failed to load image for " + uri.toString(), exception);
        }
    }
}
