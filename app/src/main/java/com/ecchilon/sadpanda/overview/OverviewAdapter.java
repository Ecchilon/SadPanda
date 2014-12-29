package com.ecchilon.sadpanda.overview;

import static com.ecchilon.sadpanda.util.ViewConstructor.createOverviewItem;

import android.view.View;
import android.view.ViewGroup;
import com.paging.listview.PagingBaseAdapter;

/**
 * Created by Alex on 21-9-2014.
 */
public class OverviewAdapter extends PagingBaseAdapter<GalleryEntry> {

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public GalleryEntry getItem(int position) {
		return items.get(position);
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
