package com.ecchilon.sadpanda.favorites;

import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;

public class RemoveFavoriteTask extends AsyncResultTask<Void, Void, GalleryEntry> {

	private final GalleryEntry mEntry;
	private final DataLoader mDataLoader;

	public RemoveFavoriteTask(GalleryEntry entry, DataLoader dataLoader) {
		super(false);
		this.mEntry = entry;
		this.mDataLoader = dataLoader;
	}

	@Override
	protected GalleryEntry call(Void... voids) throws Exception {
		mDataLoader.removeGalleryFromFavorites(mEntry);
		return mEntry;
	}
}
