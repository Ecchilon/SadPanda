package com.ecchilon.sadpanda;

import java.util.List;

import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.imageviewer.ImageEntry;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;
import com.google.inject.Inject;

public class PageLoadTaskFactory {

	private final DataLoader mDataLoader;

	@Inject
	public PageLoadTaskFactory(DataLoader dataLoader) {
		mDataLoader = dataLoader;
	}

	public PageLoadTask create(GalleryEntry entry, int page) {
		return new PageLoadTask(mDataLoader, entry, page);
	}

	public static class PageLoadTask extends AsyncResultTask<Void, Void, List<ImageEntry>> {

		private final DataLoader mDataLoader;
		private final GalleryEntry mGalleryEntry;
		private final int mPage;

		public PageLoadTask(DataLoader dataLoader, GalleryEntry mGalleryEntry, int mPage) {
			super(false);
			this.mDataLoader = dataLoader;
			this.mGalleryEntry = mGalleryEntry;
			this.mPage = mPage;
		}

		@Override
		protected List<ImageEntry> call(Void... params) throws Exception {
			return mDataLoader.getPhotoList(mGalleryEntry, mPage);
		}
	}
}
