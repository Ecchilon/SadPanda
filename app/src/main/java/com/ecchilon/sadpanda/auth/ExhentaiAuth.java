package com.ecchilon.sadpanda.auth;

import static com.ecchilon.sadpanda.auth.ExhentaiAuth.ExhentaiError.UNKNOWN;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.Getter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

public class ExhentaiAuth {

	public enum ExhentaiError {
		NO_USERNAME("You must enter a username"),
		USER_NOT_FOUND("You must already have registered for an account before you can log in"),
		NO_PASSWORD("Your password field was not complete"),
		INCORRECT_AUTH("Username or password incorrect"),
		CONNECTION_FAILURE("Failed to connect"),
		UNKNOWN("Unknown error occurred trying to log you in");

		@Getter
		private final String errorMessage;

		ExhentaiError(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

	public static class AuthException extends RuntimeException {
		@Getter
		private final ExhentaiError error;

		AuthException(ExhentaiError error) {
			this.error = error;
		}
	}

	public static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE_PREFERENCES = "cookie_prefs";

	private static final String USERNAME_KEY = "pandaUserNameKey";

	private static final String LOGIN = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";

	private final OkHttpClient client;
	private final SharedPreferences sharedPreferences;

	private final AtomicReference<String> sessionCookie = new AtomicReference<>();

	@Inject
	public ExhentaiAuth(OkHttpClient client, Context context) {
		this.client = client;
		this.sharedPreferences = context.getSharedPreferences(COOKIE_PREFERENCES, Context.MODE_PRIVATE);
	}

	public void logout() {
		sessionCookie.set(null);

		sharedPreferences.edit()
				.clear()
				.apply();
	}

	public Observable<Void> login(final String username, String password) {
		return Observable.just(username)
				.map(user -> {
					RequestBody requestBody = new FormBody.Builder()
							.addEncoded("UserName", username)
							.addEncoded("PassWord", password)
							.add("CookieDate", "1")
							.build();

					Request request = new Request.Builder()
							.url(LOGIN)
							.post(requestBody)
							.build();

					try {
						return client.newCall(request).execute();
					}
					catch (IOException e) {
						throw OnErrorThrowable.from(e);
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.computation())
				.map(response -> {
					String body;
					try {
						body = response.body().string();
					}
					catch (IOException e) {
						throw OnErrorThrowable.from(e);
					}
					if (body.contains("You are now logged in as: ")) {
						addCookies(response.headers(SET_COOKIE));
						sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
						return null;
					}
					else {
						for (ExhentaiError error : ExhentaiError.values()) {
							if (body.contains(error.getErrorMessage())) {
								throw OnErrorThrowable.from(new AuthException(error));
							}
						}
					}

					throw OnErrorThrowable.from(new AuthException(UNKNOWN));
				});
	}

	public boolean isLoggedIn() {
		return sharedPreferences.contains(USERNAME_KEY);
	}

	public String getUserName() {
		return sharedPreferences.getString(USERNAME_KEY, null);
	}

	public void addCookies(Collection<String> cookies) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		boolean addedCookie = false;
		for (String entry : cookies) {
			for (HttpCookie cookie : HttpCookie.parse(entry)) {
				editor.putString(cookie.getName(), cookie.getValue());
				addedCookie = true;
			}
		}

		if (addedCookie) {
			sessionCookie.set(null);
		}

		editor.apply();
	}

	public String getSessionCookie() {
		synchronized (sessionCookie) {
			String cookie = sessionCookie.get();
			if (cookie == null) {
				cookie = createSessionCookie();
				sessionCookie.set(cookie);
			}

			return cookie;
		}
	}

	private String createSessionCookie() {
		Map<String, ?> cookieMap = sharedPreferences.getAll();
		List<String> cookies = Lists.newArrayListWithExpectedSize(cookieMap.size());
		for (Map.Entry<String, ?> entry : cookieMap.entrySet()) {
			cookies.add(entry.getKey() + "=" + entry.getValue());
		}

		return TextUtils.join(";", cookies);
	}
}

