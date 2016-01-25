package com.ecchilon.sadpanda.overview;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.ecchilon.sadpanda.search.SearchController;
import com.ecchilon.sadpanda.search.SearchDialogFragment;
import com.ecchilon.sadpanda.util.MenuBuilder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.paging.listview.PagingListView;
import lombok.NonNull;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import rx.android.schedulers.AndroidSchedulers;

/**
 * A fragment representing a list of Items. Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView. interface.
 */
public class OverviewFragment extends RoboFragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout
		.OnRefreshListener, PagingListView.Pagingable, MenuBuilder.GalleryMenuClickListener {

	private static final String TAG = "OverviewFragment";

	public enum SearchType {
		NONE,
		SIMPLE,
		ADVANCED
	}

	private static final int GALLERY_BATCH_SIZE = 25;

	private static final String STORED_ENTRIES_KEY = "OverviewStoredEntries";
	private static final String STORED_POSITION_KEY = "OverviewStoredPosition";
	private static final String STORED_PAGE_KEY = "OverviewStoredPage";
	private static final String STORED_MORE_ITEMS_KEY = "OverviewStoredHasMoreItems";

	private static final String IS_FAVORITE_LIST_KEY = "favoriteListKey";
	private static final String SEARCH_TYPE_KEY = "SearchTypeKey";
	private static final String URL_KEY = "overviewUrlKey";
	private static final String QUERY_KEY = "overviewQueryKey";

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
	private MenuBuilder menuBuilder;

	private String mQueryUrl;
	private SearchType mSearchType;

	private int mCurrentPage = 0;

	private Integer favoritesCategory;

	public static OverviewFragment newInstance(@NonNull String url, @Nullable Integer favoritesCategory,
			@Nullable String query, SearchType searchType) {
		OverviewFragment fragment = new OverviewFragment();
		if (url != null) {
			Bundle args = new Bundle();
			args.putString(URL_KEY, url);
			if(favoritesCategory != null) {
				args.putInt(IS_FAVORITE_LIST_KEY, favoritesCategory);
			}
			if (query != null) {
				args.putString(QUERY_KEY, query);
			}
			args.putSerializable(SEARCH_TYPE_KEY, searchType);
			fragment.setArguments(args);
		}

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if(getArguments().containsKey(IS_FAVORITE_LIST_KEY)) {
			favoritesCategory = getArguments().getInt(IS_FAVORITE_LIST_KEY);
		}
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

		registerForContextMenu(mListView);
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
		String query = null;
		if (getArguments().containsKey(QUERY_KEY)) {
			query = getArguments().getString(QUERY_KEY);
		}

		SearchDialogFragment fragment = SearchDialogFragment.newInstance(query, mQueryUrl, mSearchType);
		fragment.show(getFragmentManager(), "Search");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v == mListView) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			GalleryEntry entry = mAdapter.getItem(info.position);
			menuBuilder.buildMenu(menu, getActivity(), entry, info.position, favoritesCategory);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//because #onContextItemSelected also gets triggered on invisible fragments...
		if (getUserVisibleHint()) {
			GalleryEntry entry = mAdapter.getItem(item.getOrder());
			menuBuilder.onMenuItemSelected(item, entry, this);
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

		showLoading();
	}

	@Override
	public void onLoadMoreItems() {
		mDataLoader.getGalleryIndex(mQueryUrl, mCurrentPage++)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(galleryEntries -> {
					if (mRefreshLayout != null) {
						mRefreshLayout.setRefreshing(false);
					}

					//TODO apparently not always batch size?
					mListView.onFinishLoading(galleryEntries.size() >= GALLERY_BATCH_SIZE,
							galleryEntries);

					if (mAdapter.getCount() == 0) {
						showEmpty();
					}
				}, throwable -> {
					Log.e(TAG, "Couldn't retrieve gallery items", throwable);
					if(mRefreshLayout != null) {
						mRefreshLayout.setRefreshing(false);
					}
					showEmpty();
				});
	}

	@Override
	public void viewByUploader(String uploader) {
		openSearchWithUrl(uploader, SearchController.getUploaderUrl(uploader));
	}

	@Override
	public void viewByTag(String tag) {
		openSearchWithUrl(tag, SearchController.getDefaultUrl(tag));
	}

	private void openSearchWithUrl(String query, String url) {
		getContext().startActivity(SearchActivity.newInstance(getContext(), query, url));
	}

	@Override
	public void onAddedToFavorites(int category) {
		Snackbar.make(mListView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onRemovedFromFavorites() {
		Snackbar.make(mListView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onFailedToRemoveFavorite() {
		Snackbar.make(mListView, R.string.favorite_removed_failed, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onFailedToAddFavorite(int category) {
		Snackbar.make(mListView, R.string.favorite_added_failed, Snackbar.LENGTH_SHORT).show();
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
				((AppCompatActivity) getActivity()).getSupportActionBar()
						.setSubtitle(mSubTitle + (mCurrentDisplayedPage + 1));
			}
		}
	}
}
