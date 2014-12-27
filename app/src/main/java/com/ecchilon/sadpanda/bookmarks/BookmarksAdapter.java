package com.ecchilon.sadpanda.bookmarks;

import static com.ecchilon.sadpanda.util.ViewConstructor.createOverviewItem;

import java.util.List;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.ecchilon.sadpanda.overview.GalleryEntry;

public class BookmarksAdapter extends BaseAdapter {

	private final List<GalleryEntry> mEntryList;

	public BookmarksAdapter(@NonNull List<GalleryEntry> entryList) {
		this.mEntryList = entryList;
	}

	@Override
	public int getCount() {
		return mEntryList.size();
	}

	@Override
	public GalleryEntry getItem(int position) {
		return mEntryList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createOverviewItem(getItem(position), convertView, parent);
	}
}
