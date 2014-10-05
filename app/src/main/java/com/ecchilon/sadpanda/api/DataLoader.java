package com.ecchilon.sadpanda.api;

import android.net.Uri;
import android.os.Looper;

import com.ecchilon.sadpanda.imageviewer.ImageEntry;
import com.ecchilon.sadpanda.overview.Category;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import com.loopj.android.http.AsyncHttpClient;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roboguice.util.Strings;


/**
 * Created by SkyArrow on 2014/2/19.
 */
public class DataLoader {
    public static final int PHOTO_PER_PAGE = 40;
    private static final String API_URL_EX = "http://exhentai.org/api.php";
    private static final String GALLERY_URL_EX = "http://exhentai.org/g/%d/%s";
    private static final String PHOTO_URL_EX = "http://exhentai.org/s/%s/%d-%d";

    private static final Pattern pPhotoUrl = Pattern.compile("http://(g\\.e-|ex)hentai\\.org/s/(\\w+?)/(\\d+)-(\\d+)");
    private static final Pattern pShowkey = Pattern.compile("var showkey.*=.*\"([\\w-]+?)\";");
    private static final Pattern pImageSrc = Pattern.compile("<img id=\"img\" src=\"(.+)/(.+?)\"");
    private static final Pattern pGalleryURL = Pattern.compile("<a href=\"http://(g\\.e-|ex)hentai\\.org/g/(\\d+)/(\\w+)/\" onmouseover");

    private final HttpClient mHttpClient;
    private final HttpContext mHttpContext;

    @Inject
    private DataLoader(AsyncHttpClient httpClient) {
        mHttpClient = httpClient.getHttpClient();
        mHttpContext = httpClient.getHttpContext();
    }

    private HttpResponse getHttpResponse(HttpRequestBase httpRequest) throws IOException {
        return mHttpClient.execute(httpRequest, mHttpContext);
    }

    private JSONObject callApi(JSONObject json) throws ApiCallException {
        assertNotMainThread();
        String responseStr = "";

        try {
            HttpPost httpPost = new HttpPost(API_URL_EX);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json.toString()));

            HttpResponse response = getHttpResponse(httpPost);
            responseStr = readResponse(response);

            JSONObject result = new JSONObject(responseStr);

            if (result.has("error")) {
                String error = result.getString("error");

                if (error.equals("Key mismatch")) {
                    throw new ApiCallException(ApiErrorCode.SHOWKEY_INVALID, API_URL_EX, response);
                } else {
                    throw new ApiCallException(ApiErrorCode.API_ERROR, API_URL_EX, response);
                }
            }

            return result;
        } catch (IOException e) {
            throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    public JSONObject callApi(String method, JSONObject json) throws ApiCallException {
        try {
            json.put("method", method);
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }

        return callApi(json);
    }

    public List<ImageEntry> getPhotoList(GalleryEntry gallery, int page) throws ApiCallException {
        assertNotMainThread();
        try {
            String url = getGalleryUrl(gallery, page);

            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = getHttpResponse(httpGet);
            String content = readResponse(response);

            List<ImageEntry> list = new ArrayList<ImageEntry>();
            long galleryId = gallery.getGalleryId();
            Matcher matcher = pPhotoUrl.matcher(content);

            while (matcher.find()) {
                String token = matcher.group(2);
                int photoPage = Integer.parseInt(matcher.group(4));

                ImageEntry photo = new ImageEntry();

                photo.setGalleryId(galleryId);
                photo.setToken(token);
                photo.setPage(photoPage);

                list.add(photo);
            }

            return list;
        } catch (IOException e) {
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
        } catch (JSONException e) {
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
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    private String getShowkey(GalleryEntry gallery, ImageEntry entry) throws ApiCallException {
        assertNotMainThread();
        try {
            String url = getImagePageUrl(entry);

            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = getHttpResponse(httpGet);
            String content = readResponse(response);

            if (content.contains("This gallery is pining for the fjords")) {
                throw new ApiCallException(ApiErrorCode.GALLERY_PINNED, url, response);
            } else if (content.equals("Invalid page.")) {
                List<ImageEntry> list = getPhotoList(gallery, entry.getPage() / PHOTO_PER_PAGE);
                entry = list.get(0);
                httpGet = new HttpGet(getImagePageUrl(entry));
                response = getHttpResponse(httpGet);
                content = readResponse(response);

                if (content.equals("Invalid page.")) {
                    throw new ApiCallException(ApiErrorCode.SHOWKEY_EXPIRED, url, response);
                }
            }

            Matcher matcher = pShowkey.matcher(content);
            String showkey = "";

            while (matcher.find()) {
                showkey = matcher.group(1);
            }

            if (Strings.isEmpty(showkey)) {
                throw new ApiCallException(ApiErrorCode.SHOWKEY_NOT_FOUND, url, response);
            } else {
                gallery.setShowkey(showkey);

                return showkey;
            }
        } catch (IOException e) {
            throw new ApiCallException(ApiErrorCode.IO_ERROR, e);
        }
    }

    public List<GalleryEntry> getGalleryIndex(String base) throws ApiCallException {
        return getGalleryIndex(base, 0);
    }

    public List<GalleryEntry> getGalleryIndex(String base, int page) throws ApiCallException {
        String url = getGalleryIndexUrl(base, page);

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = getHttpResponse(httpGet);
            String html = readResponse(response);
            Matcher matcher = pGalleryURL.matcher(html);
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
        } catch (IOException e) {
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

        if (gidlist.length() == 0) return galleryList;

        try {
            JSONObject obj = new JSONObject();
            obj.put("gidlist", gidlist);

            JSONObject json = callApi("gdata", obj);

            JSONArray gmetadata = json.getJSONArray("gmetadata");

            for (int i = 0, len = gmetadata.length(); i < len; i++) {
                JSONObject data = gmetadata.getJSONObject(i);
                long id = data.getLong("gid");

                if (data.getBoolean("expunged")) continue;

                if (data.has("error")) {
                    String error = data.getString("token");

                    if (error.equals("Key missing, or incorrect key provided.")) {
                        throw new ApiCallException(ApiErrorCode.TOKEN_INVALID);
                    } else {
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
                for(int j = 0; j < tags.length(); j++) {
                    tagString[j] = tags.getString(j);
                }

                gallery.setTags(tagString);
                gallery.setCreated(new Date(data.getLong("posted") * 1000));
                gallery.setFilesize(Long.parseLong(data.getString("filesize")));

                galleryList.add(gallery);
            }

            return galleryList;
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
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
        } else {
            return galleryList.get(0);
        }
    }

    public JSONArray getGalleryTokenList(JSONArray pageList) throws ApiCallException {
        try {
            JSONObject obj = new JSONObject();
            obj.put("pagelist", pageList);

            JSONObject json = callApi("gtoken", obj);

            if (json == null) {
                throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
            } else {
                if (json.has("tokenlist")) {
                    return json.getJSONArray("tokenlist");
                } else {
                    throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
                }
            }
        } catch (JSONException e) {
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
            } else {
                if (tokenObj.has("token")) {
                    return tokenObj.getString("token");
                } else if (tokenObj.has("error")) {
                    String error = tokenObj.getString("error");

                    if (error.equals("Invalid page.")) {
                        throw new ApiCallException(ApiErrorCode.TOKEN_OR_PAGE_INVALID);
                    } else {
                        throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND, error);
                    }
                } else {
                    throw new ApiCallException(ApiErrorCode.TOKEN_NOT_FOUND);
                }
            }
        } catch (JSONException e) {
            throw new ApiCallException(ApiErrorCode.JSON_ERROR, e);
        }
    }

    private static String getGalleryUrl(GalleryEntry galleryEntry, int page) {
        String base = String.format(GALLERY_URL_EX, galleryEntry.getGalleryId(), galleryEntry.getToken());
        Uri.Builder builder = Uri.parse(base).buildUpon();
        builder.appendQueryParameter("p", Integer.toString(page));

        return builder.build().toString();
    }

    private static void assertNotMainThread() {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("API can't be called from UI Thread!");
        }
    }

    private static String getImagePageUrl(ImageEntry entry) {
        return String.format(PHOTO_URL_EX, entry.getToken(), entry.getGalleryId(), entry.getPage());
    }

    private static String readResponse(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            return EntityUtils.toString(entity);
        } else {
            return null;
        }
    }
}
