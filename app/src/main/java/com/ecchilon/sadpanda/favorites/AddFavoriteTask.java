package com.ecchilon.sadpanda.favorites;

import android.os.AsyncTask;
import com.ecchilon.sadpanda.api.ApiCallException;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;

public class AddFavoriteTask extends AsyncTask<Void, Void, Boolean> {

	private final GalleryEntry mEntry;
	private final String mNote;
	private final int mCategory;
	private final DataLoader mDataLoader;

	public AddFavoriteTask(GalleryEntry entry, String note, int category, DataLoader dataLoader) {
		this.mEntry = entry;
		this.mNote = note;
		this.mCategory = category;
		this.mDataLoader = dataLoader;
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		try {
			return mDataLoader.addGalleryToFavorites(mCategory, mNote, mEntry);
		}
		catch (ApiCallException e) {
			return false;
		}
	}
}
