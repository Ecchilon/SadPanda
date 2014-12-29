package com.ecchilon.sadpanda.overview;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.ApiCallException;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.bookmarks.BookmarkController;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.ecchilon.sadpanda.util.AsyncTaskResult;
import com.google.inject.Inject;
import com.paging.listview.PagingListView;
import lombok.NonNull;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * A fragment representing a list of Items. Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView. interface.
 */
public class OverviewFragment extends RoboFragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout
		.OnRefreshListener, PagingListView.Pagingable {

	private static final int GALLERY_BATCH_SIZE = 25;
	private static final int TOAST_TITLE_MAX_LENGTH = 50;

	public static final String URL_KEY = "ExhentaiURL";
	public static final String QUERY_KEY = "ExhentaiQuery";

	@InjectView(R.id.overview_list)
	private PagingListView mListView;
	@InjectView(R.id.swipe_container)
	private SwipeRefreshLayout mRefreshLayout;

	private OverviewAdapter mAdapter;

	@Inject
	private ObjectMapper mObjectMapper;

	@Inject
	private BookmarkController mBookmarkController;

	@Inject
	private DataLoader mDataLoader;

	private String mQueryUrl;

	private int mCurrentPage = 0;

	public static OverviewFragment newInstance(@NonNull String url) {
		OverviewFragment fragment = new OverviewFragment();
		if (url != null) {
			Bundle args = new Bundle();
			args.putString(URL_KEY, url);
			fragment.setArguments(args);
		}

		return fragment;
	}

	public OverviewFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		mQueryUrl = getArguments().getString(URL_KEY);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.overview, menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_gallery, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mAdapter = new OverviewAdapter();

		mListView.setAdapter(mAdapter);
		mListView.setHasMoreItems(true);
		mListView.setEmptyView(view.findViewById(android.R.id.empty));
		mListView.setOnItemClickListener(this);
		mListView.setPagingableListener(this);
		mListView.setOnScrollListener(new PagedOnScrollListener());

		registerForContextMenu(mListView);
		mRefreshLayout.setOnRefreshListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh_menu:
				onRefresh();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v == mListView) {
			int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
			GalleryEntry entry = mAdapter.getItem(position);
			menu.add(0, 0, 0, R.string.add_bookmark).setEnabled(!mBookmarkController.hasBookmark(entry));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				GalleryEntry entry = mAdapter.getItem(info.position);
				mBookmarkController.addBookmark(entry);
		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent viewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
		try {
			viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, mObjectMapper.writeValueAsString(
					mAdapter.getItem(position)));
		}
		catch (IOException e) {
			Toast.makeText(getActivity(), R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("BookmarkFragment", "Failed to write gallery entry", e);
			return;
		}
		startActivity(viewerIntent);
	}

	@Override
	public void onRefresh() {
		mAdapter.removeAllItems();
		mListView.setHasMoreItems(true);
		mCurrentPage = 0;
	}

	@Override
	public void onLoadMoreItems() {
		new AsyncTask<Void, Void, AsyncTaskResult<List<GalleryEntry>>>() {

			@Override
			protected AsyncTaskResult<List<GalleryEntry>>

			doInBackground(Void... params) {
				try {
					List<GalleryEntry> result = mDataLoader.getGalleryIndex(mQueryUrl, mCurrentPage++);
					return new AsyncTaskResult<List<GalleryEntry>>(result);
				}
				catch (ApiCallException e) {
					return new AsyncTaskResult<List<GalleryEntry>>(e);
				}
			}

			@Override
			protected void onPostExecute(AsyncTaskResult<List<GalleryEntry>> entryList) {
				super.onPostExecute(entryList);

				mRefreshLayout.setRefreshing(false);

				if (entryList.isSuccessful()) {
					mListView.onFinishLoading(entryList.getResult().size() >= GALLERY_BATCH_SIZE,
							entryList.getResult());
				}

				//TODO show reload for page on failure
			}
		}.execute();
	}

	private class PagedOnScrollListener implements AbsListView.OnScrollListener {

		private int mCurrentDisplayedPage = -1;
		private final String mSubTitle;

		private PagedOnScrollListener() {
			mSubTitle = getString(R.string.current_page) + " ";
		}


		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			//assumes GALLERY_BATCH_SIZE items for each page to determine current subtitle

			int newDisplayedPage = firstVisibleItem / GALLERY_BATCH_SIZE;

			if(newDisplayedPage != mCurrentDisplayedPage) {
				mCurrentDisplayedPage = newDisplayedPage;
				((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(mSubTitle + (mCurrentDisplayedPage + 1));
			}
		}
	}
}
