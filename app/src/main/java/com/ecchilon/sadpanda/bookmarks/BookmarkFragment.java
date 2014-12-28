package com.ecchilon.sadpanda.bookmarks;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class BookmarkFragment extends RoboFragment implements AbsListView.OnItemClickListener {

	@InjectView(R.id.bookmark_list)
	private ListView mBookmarksList;

	@InjectView(android.R.id.empty)
	private View mEmptyView;

	@Inject
	private BookmarkController mBookmarkController;

	@Inject
	private ObjectMapper mObjectMapper;

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
		try {
			viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, mObjectMapper.writeValueAsString(
					parent.getItemAtPosition(position)));
		}
		catch (IOException e) {
			Toast.makeText(getActivity(), R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("BookmarkFragment", "Failed to write gallery entry", e);
			return;
		}
		startActivity(viewerIntent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				GalleryEntry entry = (GalleryEntry) mBookmarksList.getItemAtPosition(info.position);
				mBookmarkController.removeBookmark(entry);
				mBookmarksList.setAdapter(new BookmarksAdapter(mBookmarkController.getBookmarks()));
		}

		return super.onContextItemSelected(item);
	}
}
