package com.ecchilon.sadpanda.menu;

import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import lombok.Getter;

public class FavoritesMenu {
	private static final int FAVORITES_CATEGORY_COUNT = 10;

	public interface FavoriteCategorySelectedListener {
		void onFavoriteCategorySelected(int category);
	}

	@Getter
	private final GalleryEntry entry;
	private ContextMenuBuilder mContextMenu;
	private FavoriteCategorySelectedListener mListener;

	public FavoritesMenu(GalleryEntry entry, Context context) {
		this.entry = entry;
		mContextMenu = new ContextMenuBuilder(context);
		mContextMenu
				.setOnCreateMenuListener(new OnCreateMenuListener())
				.setOnMenuItemClickListener(new PopupMenuItemClickListener())
				.setHeaderTitle(R.string.add_to);
	}

	public void setFavoritesCategorySelectedListener(FavoriteCategorySelectedListener listener) {
		this.mListener = listener;
	}

	private class OnCreateMenuListener implements ContextMenuBuilder.OnCreateMenuListener {
		@Override
		public void onCreateMenu(ContextMenu contextMenu, Context context) {
			String favorites = context.getString(R.string.favorites);

			for(int i = 0; i < FAVORITES_CATEGORY_COUNT; i++) {
				contextMenu.add(0, i, 0, favorites + " " + i);
			}
		}
	}

	public boolean show() {
		return mContextMenu.show() != null;
	}

	private class PopupMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if(mListener != null) {
				mListener.onFavoriteCategorySelected(item.getItemId());
			}

			return true;
		}
	}
}
