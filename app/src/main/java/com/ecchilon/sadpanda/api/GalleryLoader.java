package com.ecchilon.sadpanda.api;

import java.util.List;

import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import lombok.Getter;
import rx.Observable;

public class GalleryLoader {

	private final DataLoader dataLoader;
	@Getter
	private boolean isLoading = false;
	@Getter
	private boolean hasMoreItems = true;

	@Inject
	GalleryLoader(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public Observable<List<GalleryEntry>> loadPage(String base, int page) {
		isLoading = true;
		return dataLoader.getGalleryIndex(base, page).doOnNext(galleryEntries -> {
			isLoading = false;
			if (galleryEntries.size() < DataLoader.GALLERIES_PER_PAGE) {
				hasMoreItems = false;
			}
		});
	}
}
