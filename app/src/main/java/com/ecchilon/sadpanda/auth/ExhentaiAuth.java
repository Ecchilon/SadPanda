package com.ecchilon.sadpanda.auth;

import com.google.inject.Inject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import lombok.Getter;


public class ExhentaiAuth {
    public enum ExhentaiError {
        NO_USERNAME("You must enter a username"),
        USER_NOT_FOUND("You must already have registered for an account before you can log in"),
        NO_PASSWORD("Your password field was not complete"),
        INCORRECT_AUTH("Username or password incorrect"),
        CONNECTION_FAILURE("Failed to connect");


        @Getter
        private String errorMessage;

        private ExhentaiError(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    private static final String logout = "https://forums.e-hentai.org/index.php?act=Login&CODE=03&k=cabd579871bc89de01a8d057b75bfd19";
    private static final String login = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";
    private static final String Domain = ".exhentai.org";

    public interface AuthListener {
        public void onSuccess();

        public void onFailure(ExhentaiError error);
    }

    private final AsyncHttpClient mClient;

    @Inject
    public ExhentaiAuth(AsyncHttpClient client) {
        this.mClient = client;
    }

    public void logout(final AuthListener listener) {
        mClient.get(logout, new RequestParams(), new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                CookieStore cookieStore = (CookieStore) mClient.getHttpContext().getAttribute(ClientContext.COOKIE_STORE);

                //Remove exhentai cookies
                Iterator<Cookie> iterator = cookieStore.getCookies().iterator();
                while (iterator.hasNext()) {
                    Cookie cookie = iterator.next();

                    if ((cookie.getName().contains("ipb_")
                            || cookie.getName().contains("uconfig"))
                            && cookie.getDomain().contains(Domain)) {
                        iterator.remove();
                    }
                }

                listener.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                listener.onFailure(ExhentaiError.CONNECTION_FAILURE);
            }
        });
    }

    public void login(final String username, String password, final AuthListener listener) {
        RequestParams params = new RequestParams();
        params.put("UserName", username);
        params.put("PassWord", password);
        params.put("CookieDate", "1");

        mClient.post(login, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFailure(ExhentaiError.CONNECTION_FAILURE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                //Check whether we've been successfully logged in to E-Hentai
                if(responseString.contains("You are now logged in as: " + username)) {
                    PersistentCookieStore cookieStore = getCookieStore();

                    //alter cookies to get access to exhentai
                    Cookie[] cookies = new Cookie[cookieStore.getCookies().size()];
                    cookieStore.getCookies().toArray(cookies);
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().contains("ipb_") || cookie.getName().contains("uconfig")) {
                            BasicClientCookie newCookie = new BasicClientCookie(
                                    cookie.getName(), cookie.getValue());
                            newCookie.setDomain(Domain);
                            newCookie.setPath("/");
                            newCookie.setAttribute("url", "http://exhentai.org");
                            cookieStore.addCookie(newCookie);
                            cookieStore.deleteCookie(cookie);
                        }
                    }

                    listener.onSuccess();
                }
                else {
                    for(ExhentaiError error : ExhentaiError.values()) {
                        if (responseString.contains(error.getErrorMessage())) {
                            listener.onFailure(error);
                            return;
                        }
                    }
                }
            }
        });
    }

    public boolean isLoggedIn() {
        for (Cookie cookie :  getCookieStore().getCookies()) {
            if (cookie.getDomain().contains(Domain))
                return true;
        }

        return false;
    }

    private PersistentCookieStore getCookieStore() {
        return (PersistentCookieStore) mClient.getHttpContext().getAttribute(ClientContext.COOKIE_STORE);
    }
}

