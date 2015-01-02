package com.ecchilon.sadpanda.favorites;

import android.os.AsyncTask;
import com.ecchilon.sadpanda.api.ApiCallException;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;

public class RemoveFavoriteTask extends AsyncTask<Void, Void, Boolean> {

	private final GalleryEntry mEntry;
	private final DataLoader mDataLoader;

	public RemoveFavoriteTask(GalleryEntry entry, DataLoader dataLoader) {
		this.mEntry = entry;
		this.mDataLoader = dataLoader;
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		try {
			return mDataLoader.removeGalleryFromFavorites(mEntry);
		}
		catch (ApiCallException e) {
			return false;
		}
	}
}
