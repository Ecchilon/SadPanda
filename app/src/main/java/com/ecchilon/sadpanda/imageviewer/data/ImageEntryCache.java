package com.ecchilon.sadpanda.imageviewer.data;

import android.util.SparseArray;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class ImageEntryCache {

	private final DataLoader dataLoader;

	private final SparseArray<Observable<ImageEntry>> imageEntries = new SparseArray<>();

	@Inject
	ImageEntryCache(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public Observable<ImageEntry> getFullImageEntry(GalleryEntry galleryEntry, ImageEntry imageEntry, int page) {
		Observable<ImageEntry> entry = imageEntries.get(page);
		if(entry == null) {
			ConnectableObservable<ImageEntry> replay = dataLoader.getPhotoInfo(galleryEntry, imageEntry).subscribeOn(
					Schedulers.io())
					.replay();
			replay.connect();
			imageEntries.put(page, entry = replay);
		}
		return entry;
	}
}
