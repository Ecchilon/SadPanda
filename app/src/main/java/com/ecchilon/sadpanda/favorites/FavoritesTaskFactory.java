package com.ecchilon.sadpanda.favorites;

import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;

public class FavoritesTaskFactory {

	private final DataLoader mDataloader;

	@Inject
	public FavoritesTaskFactory(DataLoader mDataloader) {
		this.mDataloader = mDataloader;
	}

	public AddFavoriteTask getAddFavoriteTask(GalleryEntry entry, int favoritesCategory, String note) {
		return new AddFavoriteTask(entry, note,favoritesCategory, mDataloader);
	}
	public RemoveFavoriteTask getRemoveFavoriteTask(GalleryEntry entry) {
		return new RemoveFavoriteTask(entry, mDataloader);
	}
}
