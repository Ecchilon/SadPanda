package com.ecchilon.sadpanda.auth;

import static com.ecchilon.sadpanda.auth.ExhentaiAuth.ExhentaiError.INCORRECT_AUTH;
import static com.ecchilon.sadpanda.auth.ExhentaiAuth.ExhentaiError.UNKNOWN;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

	private static final String USERNAME_KEY = "pandaUserNameKey";
	public static final String MEMBER_KEY = "pandaMemberKey";
	public static final String HASH_KEY = "pandaHashKey";
	public static final String SESSION_KEY = "pandaSessionKey";

	public static final String IPB_MEMBER_ID = "ipb_member_id";
	public static final String IPB_PASS_HASH = "ipb_pass_hash";
	public static final String IPB_SESSION_ID = "ipb_session_id";

	private static final String LOGIN = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";

	private final OkHttpClient client;
	private final SharedPreferences sharedPreferences;

	private final AtomicReference<String> sessionCookie = new AtomicReference<>();

	@Inject
	public ExhentaiAuth(OkHttpClient client, SharedPreferences sharedPreferences) {
		this.client = client;
		this.sharedPreferences = sharedPreferences;
	}

	public void logout() {
		sessionCookie.set(null);

		sharedPreferences.edit()
				.remove(MEMBER_KEY)
				.remove(HASH_KEY)
				.remove(SESSION_KEY)
				.remove(USERNAME_KEY)
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
					String body = null;
					try {
						body = response.body().string();
					}
					catch (IOException e) {
						throw OnErrorThrowable.from(e);
					}
					if (body.contains("You are now logged in as: " + username)) {
						List<String> cookies = Lists.newArrayList();
						for(String cookie : response.headers("Set-Cookie")) {
							Collections.addAll(cookies, cookie.split(";"));
						}

						String memberId = null, passHash = null, sessionId = null;

						for (String cookie : cookies) {
							String[] kvPair = cookie.split("=");
							if(kvPair.length != 2) {
								continue;
							}
							String key = kvPair[0];
							String value = kvPair[1];
							if(IPB_MEMBER_ID.equals(key)) {
								memberId = value;
							}
							else if(IPB_PASS_HASH.equals(key)) {
								passHash = value;
							}
							else if(IPB_SESSION_ID.equals(key)) {
								sessionId = value;
							}
						}

						if((memberId == null || passHash == null || sessionId == null)) {
							throw OnErrorThrowable.from(new AuthException(INCORRECT_AUTH));
						}

						sharedPreferences.edit()
								.putString(MEMBER_KEY, memberId)
								.putString(HASH_KEY, passHash)
								.putString(SESSION_KEY, sessionId)
								.putString(USERNAME_KEY, username)
								.apply();

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
		return sharedPreferences.contains(SESSION_KEY);
	}

	public String getUserName() {
		return sharedPreferences.getString(USERNAME_KEY, null);
	}

	public String getSessionCookie() {
		synchronized (sessionCookie) {
			String cookie = sessionCookie.get();
			if(cookie == null) {
				cookie = createCookie();
				sessionCookie.set(cookie);
			}

			return cookie;
		}
	}

	private String createCookie() {
		String[] cookies = new String[] {
				getCookieKeyValue(IPB_MEMBER_ID, MEMBER_KEY),
				getCookieKeyValue(IPB_PASS_HASH, HASH_KEY),
				getCookieKeyValue(IPB_SESSION_ID, SESSION_KEY)
		};

		return TextUtils.join(";", cookies);
	}

	private String getCookieKeyValue(String key, String preferenceKey) {
		return key + "=" + sharedPreferences.getString(preferenceKey, "");
	}
}

