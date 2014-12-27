package com.ecchilon.sadpanda.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.gson.Gson;
import com.google.inject.Inject;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class BookmarkFragment extends RoboFragment implements AbsListView.OnItemClickListener {

	@InjectView(R.id.bookmark_list)
	private ListView mBookmarksList;

	@InjectView(android.R.id.empty)
	private View mEmptyView;

	@Inject
	private BookmarkController mBookmarkController;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_bookmarks, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mBookmarksList.setOnItemClickListener(this);
		mBookmarksList.setAdapter(new BookmarksAdapter(mBookmarkController.getBookmarks()));
		mBookmarksList.setEmptyView(mEmptyView);
		registerForContextMenu(mBookmarksList);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if(v == mBookmarksList) {
			menu.add(0, 0, 0, R.string.remove_bookmark);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent viewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
		viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, new Gson().toJson(parent.getItemAtPosition(position)));
		startActivity(viewerIntent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				AdapterViewCompat.AdapterContextMenuInfo info = (AdapterViewCompat.AdapterContextMenuInfo) item.getMenuInfo();
				GalleryEntry entry = (GalleryEntry) mBookmarksList.getItemAtPosition(info.position);
				mBookmarkController.removeBookmark(entry);
		}

		return super.onContextItemSelected(item);
	}
}
