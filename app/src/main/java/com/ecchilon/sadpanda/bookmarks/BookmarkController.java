package com.ecchilon.sadpanda.bookmarks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import android.content.Context;
import android.util.Log;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

public class BookmarkController {

	private static final String BOOKMARK_FILE = "bookmarks.json";
	private static final String TAG = "BookmarkController";

	private Context mContext;

	private List<GalleryEntry> mBookmarks;

	@Inject
	public BookmarkController(Context context) {
		this.mContext = context;

		readBookmarks();
	}

	public boolean addBookmark(GalleryEntry entry) {
		if(mBookmarks.contains(entry)) {
			return false;
		}

		mBookmarks.add(entry);
		return writeBookmarks();
	}

	public boolean addBookmark(GalleryEntry entry, int index) {
		if(mBookmarks.contains(entry)) {
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
				mBookmarks = new Gson().fromJson(new InputStreamReader(mContext.openFileInput(BOOKMARK_FILE)),
						new TypeToken<List<GalleryEntry>>() {
						}.getType());
			}
			catch (FileNotFoundException e) {
				Log.wtf(TAG, "File was not found even after checking it existed", e);
			}
		}
		else {
			mBookmarks = Lists.newArrayList();
		}
	}

	private boolean writeBookmarks() {
		try {
			new OutputStreamWriter(mContext.openFileOutput(BOOKMARK_FILE, Context.MODE_PRIVATE)).write(new Gson().toJson(mBookmarks));
		}
		catch (IOException e) {
			Log.e(TAG, "Error while writing bookmarks file", e);
			return false;
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
