package com.ecchilon.sadpanda.auth;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import android.util.Log;
import com.ecchilon.sadpanda.net.PersistentCookieStore;
import com.google.inject.Inject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
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

	private static final String TAG = "ExhentaiAuth";

	private static final String logout =
			"https://forums.e-hentai.org/index.php?act=Login&CODE=03&k=cabd579871bc89de01a8d057b75bfd19";
	private static final String login = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";
	private static final String Domain = ".exhentai.org";

	public interface AuthListener {
		public void onSuccess();

		public void onFailure(ExhentaiError error);
	}

	private final OkHttpClient mClient;

	@Inject
	public ExhentaiAuth(OkHttpClient client) {
		this.mClient = client;
	}

	public void logout(final AuthListener listener) {
		Request request = new Request.Builder().url(logout).get().build();

		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				listener.onFailure(ExhentaiError.CONNECTION_FAILURE);
			}

			@Override
			public void onResponse(Response response) throws IOException {
				PersistentCookieStore cookieStore = getCookieStore();

				//Remove exhentai cookies
				Iterator<HttpCookie> iterator = cookieStore.getCookies().iterator();
				while (iterator.hasNext()) {
					HttpCookie cookie = iterator.next();

					if ((cookie.getName().contains("ipb_")
							|| cookie.getName().contains("uconfig"))
							&& cookie.getDomain().contains(Domain)) {
						iterator.remove();
					}
				}

				listener.onSuccess();
			}
		});
	}

	public void login(final String username, String password, final AuthListener listener) {
		Request request = new Request.Builder()
				.url(login)
				.post(new FormEncodingBuilder()
						.add("UserName", username)
						.add("PassWord", password)
						.add("CookieDate", "1").build())
				.build();

		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				listener.onFailure(ExhentaiError.CONNECTION_FAILURE);
			}

			@Override
			public void onResponse(Response response) throws IOException {
				String responseString = response.body().string();
				if (responseString.contains("You are now logged in as: " + username)) {
					PersistentCookieStore cookieStore = getCookieStore();

					//alter cookies to get access to exhentai
					HttpCookie[] cookies = new HttpCookie[cookieStore.getCookies().size()];
					cookieStore.getCookies().toArray(cookies);
					for (HttpCookie cookie : cookies) {
						if (cookie.getName().contains("ipb_") || cookie.getName().contains("uconfig")) {
							HttpCookie newCookie = new HttpCookie(
									cookie.getName(), cookie.getValue());
							newCookie.setDomain(Domain);
							newCookie.setPath("/");
							try {
								cookieStore.add(new URI(newCookie.getDomain()), newCookie);
							}
							catch (URISyntaxException e) {
								Log.d(TAG, "Failed to add new cookie for login", e);
								listener.onFailure(ExhentaiError.CONNECTION_FAILURE);
							}
							try {
								cookieStore.remove(new URI(cookie.getDomain()), cookie);
							}
							catch (URISyntaxException e) {
								Log.d(TAG, "Failed to remove old cookie for login", e);
								listener.onFailure(ExhentaiError.CONNECTION_FAILURE);
							}
						}
					}

					listener.onSuccess();
				}
				else {
					for (ExhentaiError error : ExhentaiError.values()) {
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
		for (HttpCookie cookie : getCookieStore().getCookies()) {
			if (cookie.getDomain().contains(Domain)) {
				return true;
			}
		}

		return false;
	}

	private PersistentCookieStore getCookieStore() {
		return (PersistentCookieStore) ((CookieManager) mClient.getCookieHandler()).getCookieStore();
	}
}

