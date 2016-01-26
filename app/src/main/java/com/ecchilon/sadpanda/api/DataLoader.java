package com.ecchilon.sadpanda.api;

import static com.ecchilon.sadpanda.api.ApiErrorCode.API_ERROR;
import static com.ecchilon.sadpanda.api.ApiErrorCode.GALLERY_PINNED;
import static com.ecchilon.sadpanda.api.ApiErrorCode.PHOTO_NOT_FOUND;
import static com.ecchilon.sadpanda.api.ApiErrorCode.SHOWKEY_INVALID;
import static com.ecchilon.sadpanda.api.ApiErrorCode.SHOWKEY_NOT_FOUND;
import static com.ecchilon.sadpanda.api.ApiErrorCode.TOKEN_INVALID;
import static com.ecchilon.sadpanda.api.ApiErrorCode.TOKEN_OR_PAGE_INVALID;
import static com.ecchilon.sadpanda.util.FuncUtils.not;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.support.annotation.NonNull;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.imageviewer.data.ImageEntry;
import com.ecchilon.sadpanda.imageviewer.data.ThumbEntry;
import com.ecchilon.sadpanda.overview.Category;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.common.collect.Lists;
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
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;


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

	private Observable<JSONObject> callApi(JSONObject json) {
		return Observable.just(json)
				.map(jsonObject -> {
					Request request = new Request.Builder()
							.addHeader("Accept", "application/json")
							.addHeader("Cookie", auth.getSessionCookie())
							.url(API_URL_EX)
							.post(RequestBody.create(JSON, json.toString()))
							.build();

					String responseStr;
					try {
						responseStr = client.newCall(request).execute().body().string();
					}
					catch (IOException e) {
							throw OnErrorThrowable.from(e);
					}

					JSONObject result;
					try {
						result = new JSONObject(responseStr);

						if (result.has("error")) {
							String error = result.getString("error");

							if (error.equals("Key mismatch")) {
									throw OnErrorThrowable.from(new ApiCallException(SHOWKEY_INVALID));
							}
							else {
								throw OnErrorThrowable.from(new ApiCallException(API_ERROR));
							}
						}

						return result;
					}
					catch (JSONException e) {
						throw OnErrorThrowable.from(e);
					}
				})
				.subscribeOn(Schedulers.io());
	}

	public Observable<JSONObject> callApi(String method, JSONObject json) {
		return Observable.just(json)
				.flatMap(jsonObject -> {
					try {
						json.put("method", method);
					}
					catch (JSONException e) {
						throw OnErrorThrowable.from(e);
					}
					return callApi(json);
				});
	}

	public Observable<List<ImageEntry>> getPhotoList(GalleryEntry gallery, int page) {
		return getContent(getGalleryUrl(gallery, page))
				.observeOn(Schedulers.computation())
				.map(content -> {
					List<ImageEntry> list = new ArrayList<>();
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
				});
	}

	public Observable<ImageEntry> getPhotoInfo(GalleryEntry gallery, ImageEntry photo) {
		return Observable.just(gallery)
				.flatMap(galleryEntry -> {
					String src = photo.getSrc();

					if (src != null && !Strings.isEmpty(src)) {
						return Observable.just(photo);
					}

					return getPhotoRaw(galleryEntry, photo)
							.observeOn(Schedulers.computation())
							.map(jsonObject -> {
								Matcher matcher = null;
								try {
									matcher = pImageSrc.matcher(jsonObject.getString("i3"));
								}
								catch (JSONException e) {
									throw OnErrorThrowable.from(e);
								}
								String filename = null;
								String source = null;
								while (matcher.find()) {
									filename = StringEscapeUtils.unescapeHtml4(matcher.group(2));
									source = StringEscapeUtils.unescapeHtml4(matcher.group(1) + "/" + filename);
								}

								if (Strings.isEmpty(pShowkey) || Strings.isEmpty(filename)) {
									throw OnErrorThrowable.from(new ApiCallException(PHOTO_NOT_FOUND));
								}

								photo.setFilename(filename);
								photo.setSrc(source);

								return photo;
							});
				});
	}

	public Observable<JSONObject> getPhotoRaw(GalleryEntry gallery, ImageEntry photo) {
		return Observable.just(gallery)
				.flatMap(galleryEntry -> {
					String showKey = galleryEntry.getShowkey();

					if (showKey == null || Strings.isEmpty(showKey)) {
						return getShowkey(galleryEntry, photo);
					}
					else {
						return Observable.just(showKey);
					}
				})
				.flatMap(showKey -> {
					JSONObject json = new JSONObject();
					try {
						json.put("gid", gallery.getGalleryId());
						json.put("page", photo.getPage());
						json.put("imgkey", photo.getToken());
						json.put("showkey", showKey);
					}
					catch (JSONException e) {
						throw OnErrorThrowable.from(e);
					}
					return callApi("showpage", json);
				});
	}

	private Observable<String> getShowkey(GalleryEntry gallery, ImageEntry entry) {
		return getContent(getImagePageUrl(entry))
				.observeOn(Schedulers.computation())
				.flatMap(content -> {

					if (content.contains("This gallery is pining for the fjords")) {
						throw OnErrorThrowable.from(new ApiCallException(GALLERY_PINNED));
					}
					else if (content.equals("Invalid page.")) {
						return getPhotoList(gallery, entry.getPage() / PHOTO_PER_PAGE)
								.flatMap(imageEntries -> getContent(getImagePageUrl(imageEntries.get(0))));
					}
					else {
						return Observable.just(content);
					}
				})
				.map(content -> {
					Matcher matcher = pShowkey.matcher(content);
					String showkey = "";

					while (matcher.find()) {
						showkey = matcher.group(1);
					}

					if (Strings.isEmpty(showkey)) {
						throw new ApiCallException(SHOWKEY_NOT_FOUND);
					}
					else {
						gallery.setShowkey(showkey);

						return showkey;
					}
				});
	}

	private Observable<String> getContent(String url) {
		return getContent(url, true);
	}

	private Observable<String> getContent(String url, boolean useCache) {
		return Observable.just(url).map(requestUrl -> {
			Request.Builder builder = new Request.Builder()
					.addHeader("Cookie", auth.getSessionCookie())
					.url(requestUrl)
					.get();
			if (!useCache) {
				builder.cacheControl(CacheControl.FORCE_NETWORK);
			}
			String content;
			try {
				content = client.newCall(builder.build()).execute().body().string();
			}
			catch (IOException e) {
				throw OnErrorThrowable.from(e);
			}

			return content;
		}).subscribeOn(Schedulers.io());
	}

	public Observable<List<GalleryEntry>> getGalleryIndex(String base) {
		return getGalleryIndex(base, 0);
	}

	public Observable<List<GalleryEntry>> getGalleryIndex(String base, int page) {
		return getGalleryIndex(base, page, true);
	}

	public Observable<List<GalleryEntry>> getGalleryIndex(String base, int page, boolean cache) {
		return getContent(getGalleryIndexUrl(base, page), cache)
				.observeOn(Schedulers.computation())
				.flatMap(content -> {
					Matcher matcher = pGalleryHref.matcher(content);
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
				});
	}

	public String getGalleryIndexUrl(String base, int page) {
		Uri.Builder builder = Uri.parse(base).buildUpon();
		builder.appendQueryParameter("page", Integer.toString(page));

		return builder.build().toString();
	}

	private Observable<List<GalleryEntry>> getGalleryList(JSONArray gidlist) throws ApiCallException {
		return Observable.just(gidlist)
				.flatMap(jsonArray -> {
					if (jsonArray.length() == 0) {
						return Observable.just(Lists.newArrayList());
					}

					JSONObject obj = new JSONObject();
					try {
						obj.put("gidlist", gidlist);
					}
					catch (JSONException e) {
						throw OnErrorThrowable.from(e);
					}
					return callApi("gdata", obj).observeOn(Schedulers.computation()).map(this::getEntriesFromJson);
				});
	}

	private List<GalleryEntry> getEntriesFromJson(JSONObject object) {
		try {
			JSONArray gmetadata = object.getJSONArray("gmetadata");
			List<GalleryEntry> galleryList = Lists.newArrayListWithCapacity(gmetadata.length());

			for (int i = 0, len = gmetadata.length(); i < len; i++) {
				JSONObject data = gmetadata.getJSONObject(i);
				long id = data.getLong("gid");

				if (data.getBoolean("expunged")) {
					continue;
				}

				if (data.has("error")) {
					String error = data.getString("token");

					if (error.equals("Key missing, or incorrect key provided.")) {
						throw new ApiCallException(TOKEN_INVALID);
					}
					else {
						throw new ApiCallException(API_ERROR, error);
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
			throw OnErrorThrowable.from(e);
		}
	}

	public Observable<GalleryEntry> getGallery(String url) {
		return Observable.just(url)
				.flatMap(galleryUrl -> {
					Matcher matcher = pGalleryUrl.matcher(url);

					if (matcher.find()) {
						long id = Long.parseLong(matcher.group(2));
						String token = matcher.group(3);
						return getGallery(id, token);
					}
					else {
						throw OnErrorThrowable.from(new ApiCallException(TOKEN_OR_PAGE_INVALID));
					}
				});
	}

	public Observable<GalleryEntry> getGallery(long id, String token) {
		JSONArray gidlist = new JSONArray();
		JSONArray arr = new JSONArray();

		arr.put(id);
		arr.put(token);
		gidlist.put(arr);

		return getGalleryList(gidlist)
				.filter(not(List::isEmpty))
				.map(galleryEntries -> galleryEntries.get(0));
	}

	public Observable<Void> removeGalleryFromFavorites(GalleryEntry entry) {
		return updateFavorite("favdel", "", entry);
	}

	public Observable<Void> addGalleryToFavorites(int favoritesCategory, String favNote,
			GalleryEntry entry) {
			if (favNote == null) {
				favNote = "";
			}

		return updateFavorite(Integer.toString(favoritesCategory), favNote, entry);
	}

	private Observable<Void> updateFavorite(@NonNull String favCat, @NonNull String favNote,
			GalleryEntry entry) {
		return Observable.just(entry)
				.map(galleryEntry -> {
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

					Response response = null;
					try {
						response = client.newCall(request).execute();
					}
					catch (IOException e) {
						throw OnErrorThrowable.from(e);
					}

					if (!response.isSuccessful()) {
						throw OnErrorThrowable.from(new ApiCallException(API_ERROR));
					}
					else {
						return (Void)null;
					}
				}).subscribeOn(Schedulers.io());
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
