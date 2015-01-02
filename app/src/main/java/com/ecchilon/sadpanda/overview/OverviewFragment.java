package com.ecchilon.sadpanda.overview;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.ApiCallException;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.favorites.FavoritesTaskFactory;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.ecchilon.sadpanda.search.SearchDialogFragment;
import com.ecchilon.sadpanda.util.AsyncTaskResult;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.paging.listview.PagingListView;
import lombok.NonNull;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * A fragment representing a list of Items. Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView. interface.
 */
public class OverviewFragment extends RoboFragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout
		.OnRefreshListener, PagingListView.Pagingable {

	public enum SearchType {
		NONE,
		SIMPLE,
		ADVANCED
	}

	private static final int GALLERY_BATCH_SIZE = 25;

	private static final int ADD_MENU_ID = 12414;
	private static final int REMOVE_MENU_ID = 14252345;

	private static final String MENU_INFO_POS_KEY = "menuInfoPosKey";

	private static final String STORED_ENTRIES_KEY = "OverviewStoredEntries";
	private static final String STORED_POSITION_KEY = "OverviewStoredPosition";
	private static final String STORED_PAGE_KEY = "OverviewStoredPage";
	private static final String STORED_MORE_ITEMS_KEY = "OverviewStoredHasMoreItems";

	public static final String SEARCH_TYPE_KEY = "SearchTypeKey";
	public static final String URL_KEY = "ExhentaiURL";

	@InjectView(R.id.overview_list)
	private PagingListView mListView;

	@InjectView(android.R.id.empty)
	private View mLoadingView;

	@InjectView(R.id.no_content)
	private View mNoContentView;

	@Nullable
	@InjectView(R.id.swipe_container)
	private SwipeRefreshLayout mRefreshLayout;

	private OverviewAdapter mAdapter;

	@Inject
	private ObjectMapper mObjectMapper;

	@Inject
	private DataLoader mDataLoader;

	@Inject
	private FavoritesTaskFactory mTaskFactory;

	private String mQueryUrl;
	private SearchType mSearchType;

	private int mCurrentPage = 0;

	public static OverviewFragment newInstance(@NonNull String url, SearchType searchType) {
		OverviewFragment fragment = new OverviewFragment();
		if (url != null) {
			Bundle args = new Bundle();
			args.putString(URL_KEY, url);
			args.putSerializable(SEARCH_TYPE_KEY, searchType);
			fragment.setArguments(args);
		}

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mQueryUrl = getArguments().getString(URL_KEY);
		mSearchType = (SearchType) getArguments().getSerializable(SEARCH_TYPE_KEY);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.overview, menu);
		if (mSearchType == SearchType.NONE) {
			menu.removeItem(R.id.action_search);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_overview_gallery, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState == null) {
			mAdapter = new OverviewAdapter();
			mListView.setAdapter(mAdapter);
			mListView.setHasMoreItems(true);
		}
		else {
			List<GalleryEntry> entries;
			try {
				entries = mObjectMapper.readValue(savedInstanceState.getString(STORED_ENTRIES_KEY),
						new TypeReference<List<GalleryEntry>>() {
						});
			}
			catch (IOException e) {
				entries = Lists.newArrayList();
			}
			mAdapter = new OverviewAdapter(entries);
			mCurrentPage = savedInstanceState.getInt(STORED_PAGE_KEY, 0);
			mListView.setAdapter(mAdapter);
			mListView.smoothScrollToPosition(savedInstanceState.getInt(STORED_POSITION_KEY, 0));
			mListView.setHasMoreItems(savedInstanceState.getBoolean(STORED_MORE_ITEMS_KEY, true));
		}

		if (mAdapter.getCount() == 0 && !mListView.hasMoreItems()) {
			showEmpty();
		}
		else {
			showLoading();
		}

		mListView.setOnItemClickListener(this);
		mListView.setPagingableListener(this);
		mListView.setOnScrollListener(new PagedOnScrollListener());


		if (mRefreshLayout != null) {
			mRefreshLayout.setOnRefreshListener(this);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		registerForContextMenu(mListView);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterForContextMenu(mListView);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		try {
			outState.putString(STORED_ENTRIES_KEY, mObjectMapper.writeValueAsString(mAdapter.getItems()));
		}
		catch (IOException ignored) {
		}

		outState.putInt(STORED_POSITION_KEY, mListView.getFirstVisiblePosition());
		outState.putInt(STORED_PAGE_KEY, mCurrentPage);
		outState.putBoolean(STORED_MORE_ITEMS_KEY, mListView.hasMoreItems());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh_menu:
				onRefresh();
				return true;
			case R.id.action_search:
				showSearchFragment();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showSearchFragment() {
		Bundle args = new Bundle();
		args.putSerializable(SEARCH_TYPE_KEY, mSearchType);

		SearchDialogFragment fragment = new SearchDialogFragment();
		fragment.setArguments(args);
		fragment.show(getFragmentManager(), "Search");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v == mListView) {
			SubMenu favorites = menu.addSubMenu(0, ADD_MENU_ID, 0, R.string.add_to_favorites);

			// This uses an intent to store the position because google refuses to fix bugs:
			// https://code.google.com/p/android/issues/detail?id=7139
			Intent posIntent = getPositionIntent((AdapterView.AdapterContextMenuInfo) menuInfo);
			for (int i = 0; i < 10; i++) {
				favorites.add(0, i, 0, Integer.toString(i)).setIntent(posIntent);
			}
			menu.add(0, REMOVE_MENU_ID, 0, R.string.remove_bookmark);
		}
	}

	private Intent getPositionIntent(AdapterView.AdapterContextMenuInfo menuInfo) {
		Intent posIntent = new Intent();
		posIntent.putExtra(MENU_INFO_POS_KEY, menuInfo.position);
		return posIntent;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//because #onContextItemSelected also gets triggered on invisible fragments...
		if (getUserVisibleHint()) {
			switch (item.getItemId()) {
				case ADD_MENU_ID:
					// Add menu id itself gets ignored
					return true;
				case REMOVE_MENU_ID:
					AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
					GalleryEntry entry = mAdapter.getItem(info.position);
					mTaskFactory.getRemoveFavoriteTask(entry).execute();
					return true;
				default:
					// The item id indicates which category to add to
					// See #onCreateContextMenu comment as to why intent is used
					int pos = item.getIntent().getIntExtra(MENU_INFO_POS_KEY, -1);
					if (pos == -1) {
						throw new IllegalArgumentException("Gallery item position was not set for this menu item!");
					}

					GalleryEntry addEntry = mAdapter.getItem(pos);
					mTaskFactory.getAddFavoriteTask(addEntry, item.getItemId(), null).execute();
					return true;
			}
		}
		return false;
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

		showLoading();
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

				if (mRefreshLayout != null) {
					mRefreshLayout.setRefreshing(false);
				}

				if (entryList.isSuccessful()) {
					mListView.onFinishLoading(entryList.getResult().size() >= GALLERY_BATCH_SIZE,
							entryList.getResult());
				}

				if (mAdapter.getCount() == 0) {
					showEmpty();
				}

				//TODO show reload for page on failure
			}
		}.execute();
	}

	private void showEmpty() {
		mListView.setEmptyView(mNoContentView);
		mLoadingView.setVisibility(View.GONE);
	}

	private void showLoading() {
		mListView.setEmptyView(mLoadingView);
		mNoContentView.setVisibility(View.GONE);
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

			if (newDisplayedPage != mCurrentDisplayedPage) {
				mCurrentDisplayedPage = newDisplayedPage;
				((ActionBarActivity) getActivity()).getSupportActionBar()
						.setSubtitle(mSubTitle + (mCurrentDisplayedPage + 1));
			}
		}
	}
}
