package com.ecchilon.sadpanda;

import static com.ecchilon.sadpanda.auth.ExhentaiAuth.HASH_KEY;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.IPB_MEMBER_ID;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.IPB_PASS_HASH;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.IPB_SESSION_ID;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.MEMBER_KEY;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.PandaHttpCookie;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.SESSION_KEY;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.squareup.picasso.Picasso;
import org.apache.http.client.CookieStore;
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

    @Inject
    private Context mContext;
    @Inject
    private SharedPreferences mSharedPrefs;

    public ExhentaiModule(Application application) {
        mContext = application;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Override
    protected void configure() {
        bind(ExhentaiAuth.class).in(Singleton.class);

        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener(new SadPandaApp.PicassoListener());

        Picasso.setSingletonInstance(builder.build());
    }

    @Provides
    @Singleton
    private HttpClient getClient() {
        return AndroidHttpClient.newInstance(USER_AGENT);
    }

    @Provides
    @Singleton
    private HttpContext getContext() {
        HttpContext context = new BasicHttpContext();
        CookieStore store = new BasicCookieStore();
        context.setAttribute(ClientContext.COOKIE_STORE, store);

        if(mSharedPrefs.contains(ExhentaiAuth.SESSION_KEY)) {
            String memberId = mSharedPrefs.getString(MEMBER_KEY, "");
            String passHash = mSharedPrefs.getString(HASH_KEY, "");
            String sessionId = mSharedPrefs.getString(SESSION_KEY, "");

            store.addCookie(new PandaHttpCookie(IPB_MEMBER_ID, memberId));
            store.addCookie(new PandaHttpCookie(IPB_PASS_HASH, passHash));
            store.addCookie(new PandaHttpCookie(IPB_SESSION_ID, sessionId));
        }

        return context;
    }


}
