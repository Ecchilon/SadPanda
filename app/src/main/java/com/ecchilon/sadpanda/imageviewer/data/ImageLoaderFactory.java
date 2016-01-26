package com.ecchilon.sadpanda.imageviewer.data;

import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;

public class ImageLoaderFactory {

	private final ImageEntryCache imageEntryCache;
	private final GalleryPageCache galleryPageCache;

	@Inject
	ImageLoaderFactory(ImageEntryCache imageEntryCache, GalleryPageCache galleryPageCache) {
		this.imageEntryCache = imageEntryCache;
		this.galleryPageCache = galleryPageCache;
	}

	public ImageLoader getImageLoader(GalleryEntry entry) {
		return new ImageLoader(entry, imageEntryCache, galleryPageCache);
	}
}
