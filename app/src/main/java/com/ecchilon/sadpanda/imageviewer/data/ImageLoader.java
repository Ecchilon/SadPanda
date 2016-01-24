package com.ecchilon.sadpanda.imageviewer.data;

import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import rx.Observable;

public class ImageLoader {

	private final ImageEntryCache imageEntryCache;
	private final GalleryPageCache galleryPageCache;
	private final GalleryEntry entry;

	public ImageLoader(GalleryEntry entry, ImageEntryCache imageEntryCache, GalleryPageCache galleryPageCache) {
		this.entry = entry;
		this.imageEntryCache = imageEntryCache;
		this.galleryPageCache = galleryPageCache;
	}

	public Observable<ImageEntry> getImage(int page) {
		int galleryPageIndex = page / DataLoader.PHOTO_PER_PAGE;
		return galleryPageCache.getGalleryPage(entry, galleryPageIndex)
				.map(imageEntries -> imageEntries.get(page % DataLoader.PHOTO_PER_PAGE))
				.flatMap(imageEntry -> imageEntryCache.getFullImageEntry(entry, imageEntry, page));
	}
}
