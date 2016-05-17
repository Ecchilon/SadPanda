package com.ecchilon.sadpanda.api;

import java.util.List;

import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import lombok.Getter;
import rx.Observable;

public class GalleryScroller {

	private final GalleryClient galleryClient;

	@Getter
	private boolean isLoading = false;
	@Getter
	private boolean hasMoreItems = true;

	@Inject
	GalleryScroller(GalleryClient galleryClient) {
		this.galleryClient = galleryClient;
	}

	public Observable<List<GalleryEntry>> loadPage(String base, int page) {
		isLoading = true;
		return galleryClient.getGalleryIndex(base, page).doOnNext(galleryEntries -> {
			isLoading = false;
			if (galleryEntries.isEmpty()) {
				hasMoreItems = false;
			}
		});
	}
}
