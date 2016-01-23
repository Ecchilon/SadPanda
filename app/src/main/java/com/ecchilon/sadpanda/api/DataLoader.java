package com.ecchilon.sadpanda.api;

import static com.ecchilon.sadpanda.util.NetUtils.assertNotMainThread;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.support.annotation.NonNull;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.imageviewer.ImageEntry;
import com.ecchilon.sadpanda.imageviewer.ThumbEntry;
import com.ecchilon.sadpanda.overview.Category;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.util.Strings;


/**
 * Created by SkyArrow on 2014/2/19.
 *
 * @author alex on 2014/9/23.
 */
public class DataLoader {
	public static final int PHOTO_PER_PAGE = 40;
	private static final String FAVORITES_URL_EX = "http://exhentai.org/gallerypopups.php?gid=%d&t=%s&act=addfav";
	private static final String API_URL_EX = "http://exhentai.org/api.php";
	private static final String GALLERY_URL_EX = "http://exhentai.org/g/%d/%s";
	private static final String PHOTO_URL_EX = "http://exhentai.org/s/%s/%d-%d";
	private static final String GALLERY_PATTERN = "http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/";

	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private static final Pattern pPhotoUrl = Pattern.compile(
			"width:(\\d+)px; height:(\\d+)px; background:transparent url\\((.+?)\\) -(\\d+)px 0 no-repeat\"><a "
					+ "href=\"http://exhentai\\.org/s/(\\w+?)/\\d+-(\\d+)");
	private static final Pattern pShowkey = Pattern.compile("var showkey.*=.*\"([\\w-]+?)\";");
	private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"(.+)/(.+?)\"");
	private static final Pattern pGalleryHref = Pattern.compile("<a href=\"" + GALLERY_PATTERN + "\" onmouseover");
	private static final Pattern pGalleryUrl = Pattern.compile(GALLERY_PATTERN);

	private final OkHttpClient client;
	private final ExhentaiAuth auth;

	@Inject
	DataLoader(OkHttpClient client, ExhentaiAuth auth) {
		this.client = client;
		this.auth = auth;
	}

	private JSONObject callApi(JSONObject json) throws ApiCallException {
		assertNotMainThread();

		Request request = new Request.Builder()
				.addHeader("Accept", "application/json")
				.addHeader("Cookie", auth.getSessionCookie())
				.url(API_URL_EX)
				.post(RequestBody.create(JSON, json.toString()))
				.build();

		Response response;
		String responseStr;
		try {
			response = client.newCall(request).execute();
			responseStr = response.body().string();
		}
		catch (IOException e) {
			throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
		}

		JSONObject result;
		try {
			result = new JSONObject(responseStr);

			if (result.has("error")) {
				String error = result.getString("error");

				if (error.equals("Key mismatch")) {
					throw new ApiCallException(ApiErrorCode.SHOWKEY_INVALID, response);
				}
				else {
					throw new ApiCallException(ApiErrorCode.API_ERROR, response);
				}
			}

			return result;
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}
	}

	public JSONObject callApi(String method, JSONObject json) throws ApiCallException {
		try {
			json.put("method", method);
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}

		return callApi(json);
	}

	public List<ImageEntry> getPhotoList(GalleryEntry gallery, int page) throws ApiCallException {
		assertNotMainThread();
		try {
			String url = getGalleryUrl(gallery, page);

			Request request = new Request.Builder()
					.addHeader("Cookie", auth.getSessionCookie())
					.url(url)
					.get()
					.build();
			String content = client.newCall(request).execute().body().string();

			List<ImageEntry> list = new ArrayList<ImageEntry>();
			long galleryId = gallery.getGalleryId();
			Matcher matcher = pPhotoUrl.matcher(content);

			while (matcher.find()) {
				int width = Integer.parseInt(matcher.group(1));
				int height = Integer.parseInt(matcher.group(2));
				String thumbUrl = matcher.group(3);
				int offset = Integer.parseInt(matcher.group(4));

				ThumbEntry thumb = new ThumbEntry()
						.setWidth(width)
						.setHeight(height)
						.setUrl(thumbUrl)
						.setOffset(offset);

				String token = matcher.group(5);
				int photoPage = Integer.parseInt(matcher.group(6));

				ImageEntry photo = new ImageEntry()
						.setGalleryId(galleryId)
						.setToken(token)
						.setPage(photoPage)
						.setThumbEntry(thumb);

				list.add(photo);
			}

			return list;
		}
		catch (IOException e) {
			throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
		}
	}

	public ImageEntry getPhotoInfo(GalleryEntry gallery, ImageEntry photo) throws ApiCallException {
		String src = photo.getSrc();

		if (src != null && !Strings.isEmpty(src)) {
			return photo;
		}

		try {
			JSONObject json = getPhotoRaw(gallery, photo);
			Matcher matcher = pImageSrc.matcher(json.getString("i3"));
			String filename = "";

			while (matcher.find()) {
				filename = StringEscapeUtils.unescapeHtml4(matcher.group(2));
				src = StringEscapeUtils.unescapeHtml4(matcher.group(1) + "/" + filename);
			}

			if (Strings.isEmpty(pShowkey) || Strings.isEmpty(filename)) {
				throw new ApiCallException(ApiErrorCode.PHOTO_NOT_FOUND);
			}

			photo.setFilename(filename);
			photo.setSrc(src);

			return photo;
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}
	}

	public JSONObject getPhotoRaw(GalleryEntry gallery, ImageEntry photo) throws ApiCallException {
		try {
			String showkey = gallery.getShowkey();

			if (showkey == null || Strings.isEmpty(showkey)) {
				showkey = getShowkey(gallery, photo);
			}

			JSONObject json = new JSONObject();

			json.put("gid", gallery.getGalleryId());
			json.put("page", photo.getPage());
			json.put("imgkey", photo.getToken());
			json.put("showkey", showkey);

			return callApi("showpage", json);
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}
	}

	private String getShowkey(GalleryEntry gallery, ImageEntry entry) throws ApiCallException {
		assertNotMainThread();
		try {
			String url = getImagePageUrl(entry);

			Request request = new Request.Builder()
					.addHeader("Cookie", auth.getSessionCookie())
					.url(url)
					.get()
					.build();
			Response response = client.newCall(request).execute();
			String content = response.body().string();

			if (content.contains("This gallery is pining for the fjords")) {
				throw new ApiCallException(ApiErrorCode.GALLERY_PINNED, response);
			}
			else if (content.equals("Invalid page.")) {
				List<ImageEntry> list = getPhotoList(gallery, entry.getPage() / PHOTO_PER_PAGE);
				entry = list.get(0);
				request = new Request.Builder()
						.addHeader("Cookie", auth.getSessionCookie())
						.url(getImagePageUrl(entry))
						.get()
						.build();
				response = client.newCall(request).execute();
				content = response.body().string();

				if (content.equals("Invalid page.")) {
					throw new ApiCallException(ApiErrorCode.SHOWKEY_EXPIRED, response);
				}
			}

			Matcher matcher = pShowkey.matcher(content);
			String showkey = "";

			while (matcher.find()) {
				showkey = matcher.group(1);
			}

			if (Strings.isEmpty(showkey)) {
				throw new ApiCallException(ApiErrorCode.SHOWKEY_NOT_FOUND, response);
			}
			else {
				gallery.setShowkey(showkey);

				return showkey;
			}
		}
		catch (IOException e) {
			throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
		}
	}

	public List<GalleryEntry> getGalleryIndex(String base, boolean cache) throws ApiCallException {
		return getGalleryIndex(base, 0);
	}

	public List<GalleryEntry> getGalleryIndex(String base, int page) throws ApiCallException {
		return getGalleryIndex(base, page, true);
	}

	public List<GalleryEntry> getGalleryIndex(String base, int page, boolean cache) throws ApiCallException {
		String url = getGalleryIndexUrl(base, page);

		try {
			Request.Builder builder = new Request.Builder()
					.addHeader("Cookie", auth.getSessionCookie())
					.url(url)
					.get();
			if (!cache) {
				builder.cacheControl(CacheControl.FORCE_NETWORK);
			}
			String html = client.newCall(builder.build()).execute().body().string();
			Matcher matcher = pGalleryHref.matcher(html);
			JSONArray gidlist = new JSONArray();

			while (matcher.find()) {
				long id = Long.parseLong(matcher.group(2));
				String token = matcher.group(3);
				JSONArray arr = new JSONArray();

				arr.put(id);
				arr.put(token);

				gidlist.put(arr);
			}

			return getGalleryList(gidlist);
		}
		catch (IOException e) {
			throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
		}
	}

	public String getGalleryIndexUrl(String base, int page) {
		Uri.Builder builder = Uri.parse(base).buildUpon();
		builder.appendQueryParameter("page", Integer.toString(page));

		return builder.build().toString();
	}

	private List<GalleryEntry> getGalleryList(JSONArray gidlist) throws ApiCallException {
		List<GalleryEntry> galleryList = new ArrayList<GalleryEntry>();

		if (gidlist.length() == 0) {
			return galleryList;
		}

		try {
			JSONObject obj = new JSONObject();
			obj.put("gidlist", gidlist);

			JSONObject json = callApi("gdata", obj);

			JSONArray gmetadata = json.getJSONArray("gmetadata");

			for (int i = 0, len = gmetadata.length(); i < len; i++) {
				JSONObject data = gmetadata.getJSONObject(i);
				long id = data.getLong("gid");

				if (data.getBoolean("expunged")) {
					continue;
				}

				if (data.has("error")) {
					String error = data.getString("token");

					if (error.equals("Key missing, or incorrect key provided.")) {
						throw new ApiCallException(ApiErrorCode.TOKEN_INVALID);
					}
					else {
						throw new ApiCallException(ApiErrorCode.API_ERROR, error);
					}
				}

				GalleryEntry gallery = new GalleryEntry();

				gallery.setGalleryId(id);
				gallery.setToken(data.getString("token"));
				gallery.setTitle(data.getString("title"));
				gallery.setTitle_jpn(data.getString("title_jpn"));
				gallery.setCategory(Category.fromName(data.getString("category")));
				gallery.setThumb(data.getString("thumb"));
				gallery.setFileCount(data.getInt("filecount"));
				gallery.setRating((float) data.getDouble("rating"));
				gallery.setUploader(data.getString("uploader"));
				JSONArray tags = data.getJSONArray("tags");
				String[] tagString = new String[tags.length()];
				for (int j = 0; j < tags.length(); j++) {
					tagString[j] = tags.getString(j);
				}

				gallery.setTags(tagString);
				gallery.setCreated(new Date(data.getLong("posted") * 1000));
				gallery.setFilesize(Long.parseLong(data.getString("filesize")));

				galleryList.add(gallery);
			}

			return galleryList;
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}
	}

	public GalleryEntry getGallery(String url) throws ApiCallException {
		Matcher matcher = pGalleryUrl.matcher(url);

		if (matcher.find()) {
			long id = Long.parseLong(matcher.group(2));
			String token = matcher.group(3);
			return getGallery(id, token);
		}
		else {
			throw new ApiCallException(ApiErrorCode.TOKEN_OR_PAGE_INVALID);
		}
	}

	public GalleryEntry getGallery(long id, String token) throws ApiCallException {
		JSONArray gidlist = new JSONArray();
		JSONArray arr = new JSONArray();

		arr.put(id);
		arr.put(token);
		gidlist.put(arr);

		List<GalleryEntry> galleryList = getGalleryList(gidlist);

		if (galleryList == null) {
			return null;
		}
		else {
			return galleryList.get(0);
		}
	}

	public void removeGalleryFromFavorites(GalleryEntry entry) throws ApiCallException {
		try {
			updateFavorite("favdel", "", entry);
		}
		catch (IOException e) {
			throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
		}
	}

	public void addGalleryToFavorites(int favoritesCategory, String favNote,
			GalleryEntry entry) throws ApiCallException {
		try {
			if (favNote == null) {
				favNote = "";
			}

			updateFavorite(Integer.toString(favoritesCategory), favNote, entry);
		}
		catch (IOException e) {
			throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
		}
	}

	private void updateFavorite(@NonNull String favCat, @NonNull String favNote,
			GalleryEntry entry) throws IOException, ApiCallException {
		assertNotMainThread();

		String submitValue = favCat.equals("favdel") ? "Apply+Changes" : "Add+to+Favorites";
		RequestBody body = new FormBody.Builder()
				.addEncoded("favcat", favCat)
				.addEncoded("favnote", favNote)
				.add("submit", submitValue)
				.build();

		Request request = new Request.Builder()
				.url(String.format(FAVORITES_URL_EX, entry.getGalleryId(), entry.getToken()))
				.addHeader("Cookie", auth.getSessionCookie())
				.post(body)
				.build();

		Response response = client.newCall(request).execute();

		if (response.isSuccessful()) {
			throw new ApiCallException(ApiErrorCode.API_ERROR, response);
		}
	}

	public JSONArray getGalleryTokenList(JSONArray pageList) throws ApiCallException {
		try {
			JSONObject obj = new JSONObject();
			obj.put("pagelist", pageList);

			JSONObject json = callApi("gtoken", obj);

			if (json == null) {
				throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
			}
			else {
				if (json.has("tokenlist")) {
					return json.getJSONArray("tokenlist");
				}
				else {
					throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
				}
			}
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}
	}

	public String getGalleryToken(long id, String photoToken, int page) throws ApiCallException {
		try {
			JSONArray pagelist = new JSONArray();
			JSONArray arr = new JSONArray();

			arr.put(id);
			arr.put(photoToken);
			arr.put(page);
			pagelist.put(arr);

			JSONArray tokenlist = getGalleryTokenList(pagelist);
			JSONObject tokenObj = tokenlist.getJSONObject(0);

			if (tokenObj == null) {
				throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
			}
			else {
				if (tokenObj.has("token")) {
					return tokenObj.getString("token");
				}
				else if (tokenObj.has("error")) {
					String error = tokenObj.getString("error");

					if (error.equals("Invalid page.")) {
						throw new ApiCallException(ApiErrorCode.TOKEN_OR_PAGE_INVALID);
					}
					else {
						throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND, error);
					}
				}
				else {
					throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
				}
			}
		}
		catch (JSONException e) {
			throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
		}
	}

	private static String getGalleryUrl(GalleryEntry galleryEntry, int page) {
		String base = String.format(GALLERY_URL_EX, galleryEntry.getGalleryId(), galleryEntry.getToken());
		Uri.Builder builder = Uri.parse(base).buildUpon();
		builder.appendQueryParameter("p", Integer.toString(page));

		return builder.build().toString();
	}

	private static String getImagePageUrl(ImageEntry entry) {
		return String.format(PHOTO_URL_EX, entry.getToken(), entry.getGalleryId(), entry.getPage());
	}
}
