package com.ecchilon.sadpanda.favorites;

import static com.ecchilon.sadpanda.favorites.FavoritesTaskFactory.*;

import android.os.AsyncTask;
import com.ecchilon.sadpanda.api.ApiCallException;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;
import lombok.Setter;

public class AddFavoriteTask extends AsyncResultTask<Void, Void, GalleryEntry> {

	private final GalleryEntry mEntry;
	private final String mNote;
	private final int mCategory;
	private final DataLoader mDataLoader;

	public AddFavoriteTask(GalleryEntry entry, String note, int category, DataLoader dataLoader) {
		super(false);
		this.mEntry = entry;
		this.mNote = note;
		this.mCategory = category;
		this.mDataLoader = dataLoader;
	}

	@Override
	protected GalleryEntry call(Void... voids) throws Exception {
		mDataLoader.addGalleryToFavorites(mCategory, mNote, mEntry);
		return mEntry;
	}
}
