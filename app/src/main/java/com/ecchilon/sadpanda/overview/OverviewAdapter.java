package com.ecchilon.sadpanda.overview;

import static com.ecchilon.sadpanda.data.OverviewPresenter.State.END;
import static com.ecchilon.sadpanda.data.OverviewPresenter.State.LOADED;
import static com.ecchilon.sadpanda.data.OverviewPresenter.State.LOADING;

import javax.inject.Inject;

import java.util.Collection;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.data.OverviewPresenter;
import com.ecchilon.sadpanda.util.MenuBuilder;
import com.google.common.collect.Lists;

public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int LOADING_VIEW_TYPE = 13;
	private static final int END_VIEW_TYPE = 14;
	private static final int GALLERY_VIEW_TYPE = 15;

	private List<GalleryEntry> items = Lists.newArrayList();

	private OverviewPresenter.State state;

	private final MenuBuilder menuBuilder;

	@Inject
	OverviewAdapter(MenuBuilder menuBuilder) {
		this.menuBuilder = menuBuilder;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		if (viewType == GALLERY_VIEW_TYPE) {
			return new EntryViewHolder(layoutInflater.inflate(R.layout.gallery_entry, parent, false), menuBuilder);
		}
		else if (viewType == LOADING_VIEW_TYPE) {
			return new EmptyViewHolder(layoutInflater.inflate(R.layout.loading_footer, parent, false));
		}
		else {
			return new EmptyViewHolder(layoutInflater.inflate(R.layout.end_footer, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (getItemViewType(position) == GALLERY_VIEW_TYPE) {
			((EntryViewHolder) holder).bindGalleryEntry(items.get(position));
		}
	}

	@Override
	public int getItemCount() {
		return state == LOADING || state == END ? items.size() + 1 : items.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (state != LOADING && state != END) {
			return GALLERY_VIEW_TYPE;
		}
		else if (state == LOADING) {
			return position == items.size() ? LOADING_VIEW_TYPE : GALLERY_VIEW_TYPE;
		}
		else {
			return position == items.size() ? END_VIEW_TYPE : GALLERY_VIEW_TYPE;
		}
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

	public void setState(OverviewPresenter.State state) {
		if (this.state == state) {
			return;
		}

		if ((this.state == LOADED || this.state == null) && (state == LOADING || state == END)) {
			notifyItemInserted(items.size());
		}
		else if ((this.state == LOADING || this.state == END) && (state == LOADED || state == null)) {
			notifyItemRemoved(items.size());
		}
		else {
			notifyItemChanged(items.size());
		}

		this.state = state;
	}

	private static class EmptyViewHolder extends RecyclerView.ViewHolder {
		public EmptyViewHolder(View view) {
			super(view);
		}
	}
}
