package com.ecchilon.sadpanda.auth;

import static com.ecchilon.sadpanda.util.NetUtils.assertNotMainThread;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import android.content.SharedPreferences;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


public class ExhentaiAuth {
	public static final String DOMAIN = "exhentai.org";
	public static final String SITE_URL = "http://exhentai.org";

	public enum ExhentaiResult {
		NO_USERNAME("You must enter a username"),
		USER_NOT_FOUND("You must already have registered for an account before you can log in"),
		NO_PASSWORD("Your password field was not complete"),
		INCORRECT_AUTH("Username or password incorrect"),
		CONNECTION_FAILURE("Failed to connect"),
		UNKNOWN("Unknown error occurred trying to log you in"),
		SUCCESS("Logged in successfully");


		@Getter
		private String errorMessage;

		private ExhentaiResult(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

	public static final String MEMBER_KEY = "pandaMemberKey";
	public static final String HASH_KEY = "pandaHashKey";
	public static final String SESSION_KEY = "pandaSessionKey";

	public static final String IPB_MEMBER_ID = "ipb_member_id";
	public static final String IPB_PASS_HASH = "ipb_pass_hash";
	public static final String IPB_SESSION_ID = "ipb_session_id";

	private static final String login = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";

	private final HttpClient mClient;
	private final HttpContext mHttpContext;
	private final SharedPreferences mSharedPrefs;

	@Inject
	public ExhentaiAuth(HttpClient client, HttpContext mHttpContext, SharedPreferences mSharedPrefs) {
		this.mClient = client;
		this.mHttpContext = mHttpContext;
		this.mSharedPrefs = mSharedPrefs;
	}

	public void logout() {
		assertNotMainThread();

		getCookieStore().clear();

		mSharedPrefs.edit()
				.remove(MEMBER_KEY)
				.remove(HASH_KEY)
				.remove(SESSION_KEY)
				.apply();
	}

	public ExhentaiResult login(final String username, String password) {
		assertNotMainThread();
		List<NameValuePair> nvps = Lists.newArrayList();
		nvps.add(new BasicNameValuePair("UserName", username));
		nvps.add(new BasicNameValuePair("PassWord", password));
		nvps.add(new BasicNameValuePair("CookieDate", "1"));
		UrlEncodedFormEntity encodedFormEntity;

		try {
			encodedFormEntity = new UrlEncodedFormEntity(nvps);
		}
		catch (UnsupportedEncodingException e) {
			return ExhentaiResult.INCORRECT_AUTH;
		}

		HttpPost post = new HttpPost(login);
		post.setEntity(encodedFormEntity);

		HttpResponse response;
		try {
			response = mClient.execute(post, mHttpContext);
		}
		catch (IOException e) {
			return ExhentaiResult.CONNECTION_FAILURE;
		}

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			return ExhentaiResult.CONNECTION_FAILURE;
		}

		String body;
		try {
			body = EntityUtils.toString(response.getEntity());
		}
		catch (IOException e) {
			return ExhentaiResult.CONNECTION_FAILURE;
		}

		if (body.contains("You are now logged in as: " + username)) {
			final CookieStore cookieStore = getCookieStore();

			//alter cookies to get access to exhentai
			List<Cookie> cookies = Lists.newArrayList(cookieStore.getCookies());
			cookieStore.clear();

			String memberId = "", passHash = "", sessionId = "";

			for (Cookie cookie : cookies) {
				String name = cookie.getName();
				if(IPB_MEMBER_ID.equals(name)) {
					memberId = cookie.getValue();
				}
				else if(IPB_PASS_HASH.equals(name)) {
					passHash = cookie.getValue();
				}
				else if(IPB_SESSION_ID.equals(name)) {
					sessionId = cookie.getValue();
				}

				cookieStore.addCookie(new PandaHttpCookie(cookie));
			}

			if(memberId == null || passHash == null || sessionId == null) {
				cookieStore.clear();
				return ExhentaiResult.INCORRECT_AUTH;
			}

			mSharedPrefs.edit()
					.putString(MEMBER_KEY, memberId)
					.putString(HASH_KEY, passHash)
					.putString(SESSION_KEY, sessionId)
					.apply();

			return ExhentaiResult.SUCCESS;
		}
		else {
			for (ExhentaiResult error : ExhentaiResult.values()) {
				if (body.contains(error.getErrorMessage())) {
					return error;
				}
			}
		}

		return ExhentaiResult.UNKNOWN;
	}

	public boolean isLoggedIn() {
		for (Cookie cookie : getCookieStore().getCookies()) {
			if (cookie.getDomain().contains(DOMAIN)) {
				return true;
			}
		}

		return false;
	}

	private CookieStore getCookieStore() {
		return (CookieStore) mHttpContext.getAttribute(ClientContext.COOKIE_STORE);
	}

	private interface CookieFunction {
		void run(Cookie cookie);
	}

	public static class PandaHttpCookie extends BasicClientCookie {

		public PandaHttpCookie(String name, String value) {
			super(name, value);

			setPath("/");
			setDomain(DOMAIN);
			setAttribute("url", SITE_URL);
		}

		public PandaHttpCookie(Cookie cookie) {
			this(cookie.getName(), cookie.getValue());
		}
	}
}

