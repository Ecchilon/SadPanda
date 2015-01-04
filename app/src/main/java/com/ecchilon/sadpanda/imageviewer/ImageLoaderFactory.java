package com.ecchilon.sadpanda.imageviewer;

import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;

public class ImageLoaderFactory {

	private final DataLoader mDataLoader;

	@Inject
	public ImageLoaderFactory(DataLoader dataLoader) {
		this.mDataLoader = dataLoader;
	}

	public ImageLoader getImageLoader(GalleryEntry entry) {
		return new ImageLoader(mDataLoader, entry);
	}
}
