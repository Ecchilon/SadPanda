package com.ecchilon.sadpanda.imageviewer.data;

import java.util.List;

import android.util.SparseArray;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import rx.Observable;
import rx.observables.ConnectableObservable;

public class GalleryPageCache {

	private final DataLoader dataLoader;
	private final SparseArray<Observable<List<ImageEntry>>> galleryPages = new SparseArray<>();

	@Inject
	GalleryPageCache(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public Observable<List<ImageEntry>> getGalleryPage(GalleryEntry entry, int page) {
		int galleryPageIndex = page / DataLoader.PHOTO_PER_PAGE;
		Observable<List<ImageEntry>> galleryPage = galleryPages.get(galleryPageIndex);
		if(galleryPage == null) {
			//By setting getPhotoList up with a replay we ensure it acts as a cache to which other pages can subscribe as well
			ConnectableObservable<List<ImageEntry>> replay =
					dataLoader.getPhotoList(entry, galleryPageIndex).replay();
			replay.connect();
			galleryPages.put(galleryPageIndex, galleryPage = replay);
		}

		return galleryPage;
	}
}
