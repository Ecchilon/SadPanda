package com.ecchilon.sadpanda.overview;

import java.util.Collection;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.ecchilon.sadpanda.R;
import com.google.common.collect.Lists;

public class OverviewAdapter extends RecyclerView.Adapter<EntryViewHolder> {

	private List<GalleryEntry> items = Lists.newArrayList();

	@Override
	public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new EntryViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_entry, parent, false));
	}

	@Override
	public void onBindViewHolder(EntryViewHolder holder, int position) {
		holder.bindGalleryEntry(items.get(position));
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void addItems(Collection<? extends GalleryEntry> additionalItems) {
		int position = items.size();
		items.addAll(additionalItems);
		notifyItemRangeInserted(position, additionalItems.size());
	}

	public GalleryEntry getItem(int position) {
		return items.get(position);
	}

	public void clear() {
		int size = items.size();
		items.clear();
		notifyItemRangeRemoved(0, size);
	}
}
