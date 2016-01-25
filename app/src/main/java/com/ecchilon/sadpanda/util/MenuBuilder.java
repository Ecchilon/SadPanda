package com.ecchilon.sadpanda.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import rx.schedulers.Schedulers;

public class MenuBuilder {

	public interface GalleryMenuClickListener {
		void viewByUploader(String uploader);

		void viewByTag(String tag);

		void onAddedToFavorites(int category);

		void onRemovedFromFavorites();

		void onFailedToRemoveFavorite();

		void onFailedToAddFavorite(int category);
	}

	private static final int FAVORITES_CATEGORY_COUNT = 10;

	private final DataLoader dataLoader;

	@Inject
	MenuBuilder(DataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public void buildMenu(Menu menu, Context context, GalleryEntry entry, int position, Integer currentFavorite) {
		SubMenu addFavorites = menu.addSubMenu(R.string.add_to);

		String favorites = context.getString(R.string.favorites);

		for (int i = 0; i < FAVORITES_CATEGORY_COUNT; i++) {
			if(currentFavorite != null && i == currentFavorite) {
				continue;
			}
			addFavorites.add(R.id.add_favorites_group, i, position, favorites + " " + i);
		}

		menu.add(Menu.NONE, R.id.remove_favorite, Menu.NONE, R.string.remove_favorite);

		SubMenu viewBy = menu.addSubMenu(R.string.view_entry);

		viewBy.add(R.id.view_by_group, R.id.view_uploader, position, entry.getUploader());
		for (String tag : entry.getTags()) {
			viewBy.add(R.id.view_by_group, Menu.NONE, position, tag);
		}
	}

	//TODO MVP implementation
	public boolean onMenuItemSelected(MenuItem item, GalleryEntry entry, @NonNull GalleryMenuClickListener listener) {
		int itemId = item.getItemId();
		switch (item.getGroupId()) {
			case R.id.view_by_group:
				if (itemId == R.id.view_uploader) {
					listener.viewByUploader(entry.getUploader());
				}
				else {
					listener.viewByTag(item.getTitle().toString());
				}
				return true;
			case R.id.add_favorites_group:
				dataLoader.addGalleryToFavorites(itemId, null, entry)
						.subscribeOn(Schedulers.io())
						.subscribe(res -> listener.onAddedToFavorites(itemId), throwable -> {
							Log.e(MenuBuilder.class.getSimpleName(), "Couldn't add gallery entry to favorites",
									throwable);
							listener.onFailedToAddFavorite(itemId);
						});
				return true;
			default:
				if (itemId == R.id.remove_favorite) {
					dataLoader.removeGalleryFromFavorites(entry)
							.subscribeOn(Schedulers.io())
							.subscribe(res -> listener.onRemovedFromFavorites(), throwable -> {
								Log.e(MenuBuilder.class.getSimpleName(), "Couldn't remove gallery entry from "
										+ "favorites",
										throwable);
								listener.onFailedToRemoveFavorite();
							});
					return true;
				}
				return false;
		}
	}
}
