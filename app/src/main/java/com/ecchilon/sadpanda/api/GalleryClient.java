package com.ecchilon.sadpanda.api;

import java.util.List;

import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rx.Observable;

@Singleton
public class GalleryClient {

	private static final int CACHE_SIZE = 100;

	private final ObservableLruCache<Long, GalleryEntry> galleryCache = new ObservableLruCache<>(CACHE_SIZE);

	private final DataLoader dataLoader;

	@Inject
	GalleryClient(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public Observable<List<GalleryEntry>> getGalleryIndex(String base, int page) {
		return dataLoader.getGalleryIndex(base, page).doOnNext(galleryEntries -> {
			for (GalleryEntry entry : galleryEntries) {
				addToCache(entry);
			}
		});
	}

	public Observable<GalleryEntry> getEntry(long entryId, String token) {
		return Observable.concat(galleryCache.get(entryId),
				dataLoader.getGallery(entryId, token).doOnNext(this::addToCache)).first();
	}

	private void addToCache(GalleryEntry entry) {
		galleryCache.put(entry.getGalleryId(), entry);
	}
}
