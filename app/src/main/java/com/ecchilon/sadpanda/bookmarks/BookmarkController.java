package com.ecchilon.sadpanda.bookmarks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import android.content.Context;
import android.util.Log;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class BookmarkController {

	private static final String BOOKMARK_FILE = "bookmarks.json";
	private static final String TAG = "BookmarkController";

	private Context mContext;

	private List<GalleryEntry> mBookmarks;
	private ObjectMapper mObjectMapper;

	@Inject
	public BookmarkController(Context context, ObjectMapper mapper) {
		this.mContext = context;
		this.mObjectMapper = mapper;

		readBookmarks();
	}

	public boolean addBookmark(GalleryEntry entry) {
		if (mBookmarks.contains(entry)) {
			return false;
		}

		mBookmarks.add(entry);
		return writeBookmarks();
	}

	public boolean addBookmark(GalleryEntry entry, int index) {
		if (mBookmarks.contains(entry)) {
			return false;
		}

		mBookmarks.add(index, entry);
		return writeBookmarks();
	}

	public boolean hasBookmark(GalleryEntry entry) {
		return mBookmarks.contains(entry);
	}

	public boolean removeBookmark(GalleryEntry entry) {
		return mBookmarks.remove(entry) && writeBookmarks();
	}

	public List<GalleryEntry> getBookmarks() {
		return Lists.newArrayList(mBookmarks);
	}

	private void readBookmarks() {
		if (bookmarkFileExists()) {
			try {
				mBookmarks = mObjectMapper.readValue(mContext.openFileInput(BOOKMARK_FILE),
						new TypeReference<List<GalleryEntry>>() {
						});
			}
			catch (Exception e) {
				Log.e(TAG, "Error loading bookmarks file", e);
				mBookmarks = Lists.newArrayList();
			}
		}
		else {
			mBookmarks = Lists.newArrayList();
		}
	}

	private boolean writeBookmarks() {
		OutputStreamWriter stream = null;
		try {
			stream = new OutputStreamWriter(mContext.openFileOutput(BOOKMARK_FILE, Context.MODE_PRIVATE));
			String json = mObjectMapper.writeValueAsString(mBookmarks);
			stream.write(json);
		}
		catch (IOException e) {
			Log.e(TAG, "Error while writing bookmarks file", e);
			return false;
		}
		finally {
			if(stream != null) {
				try {
					stream.close();
				}
				catch (IOException e) {
					Log.e(TAG, "Error while closing writing stream", e);
				}
			}
		}

		return true;
	}

	private boolean bookmarkFileExists() {
		String[] files = mContext.fileList();
		for (String file : files) {
			if (file.equals(BOOKMARK_FILE)) {
				return true;
			}
		}

		return false;
	}
}
