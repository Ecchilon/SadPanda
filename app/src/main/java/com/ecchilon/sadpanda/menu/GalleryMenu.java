package com.ecchilon.sadpanda.menu;

import static android.view.MenuItem.OnMenuItemClickListener;

import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.GalleryEntry;

public class GalleryMenu {
	public interface MenuItemSelectedListener {
		void onMenuItemSelected(String item, boolean uploader);
	}

	private final GalleryEntry mEntry;
	private ContextMenuBuilder mContextMenu;
	private MenuItemSelectedListener mListener;

	public GalleryMenu(GalleryEntry entry, Context context) {
		this.mEntry = entry;
		mContextMenu = new ContextMenuBuilder(context);
		mContextMenu
				.setOnCreateMenuListener(new OnCreateMenuListener())
				.setOnMenuItemClickListener(new PopupMenuItemClickListener())
				.setHeaderTitle(entry.getTitle());
	}

	public void setOnMenuItemSelectedListener(MenuItemSelectedListener listener) {
		this.mListener = listener;
	}

	public boolean show() {
		return mContextMenu.show() != null;
	}

	private class OnCreateMenuListener implements ContextMenuBuilder.OnCreateMenuListener {
		@Override
		public void onCreateMenu(ContextMenu menu, Context context) {
			String uploader = context.getString(R.string.uploader);
			String tag = context.getString(R.string.tag);

			if (mEntry.getUploader() != null) {
				menu.add(0, 0, 0, uploader + " - " + mEntry.getUploader());
			}
			String[] tags = mEntry.getTags();
			for (int i = 0; i < tags.length; i++) {
				menu.add(0, i + 1, 0, tag + " - " + tags[i]);
			}
		}
	}

	private class PopupMenuItemClickListener implements OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			if (item.getItemId() == 0) {
				if (mListener != null) {
					mListener.onMenuItemSelected(mEntry.getUploader(), true);
				}
			}
			else {
				int tagPos = item.getItemId() - 1;
				if (mListener != null) {
					mListener.onMenuItemSelected(mEntry.getTags()[tagPos], false);
				}
			}

			return true;
		}
	}
}
