package com.ecchilon.sadpanda.overview;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.inject.Inject;
import lombok.NonNull;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import rx.android.schedulers.AndroidSchedulers;

/**
 * A fragment representing a list of Items. Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView. interface.
 */
public class OverviewFragment extends RoboFragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout
		.OnRefreshListener, MenuBuilder.GalleryMenuClickListener {

	private static final String TAG = "OverviewFragment";

	public enum SearchType {
		NONE,
		SIMPLE,
		ADVANCED
	}

	private static final int GALLERY_BATCH_SIZE = 25;

	private static final String IS_FAVORITE_LIST_KEY = "favoriteListKey";
	private static final String SEARCH_TYPE_KEY = "SearchTypeKey";
	private static final String URL_KEY = "overviewUrlKey";
	private static final String QUERY_KEY = "overviewQueryKey";

	@InjectView(R.id.overview_list)
	private RecyclerView entryListView;

	@InjectView(android.R.id.empty)
	private View loadingView;

	@InjectView(R.id.no_content)
	private View noContentView;

	@Nullable
	@InjectView(R.id.swipe_container)
	private SwipeRefreshLayout refreshLayout;

	private final OverviewAdapter adapter = new OverviewAdapter();

	@Inject
	private ObjectMapper mObjectMapper;

	@Inject
	private DataLoader dataLoader;

	@Inject
	private MenuBuilder menuBuilder;

	private String mQueryUrl;
	private SearchType mSearchType;

	private int currentPage = 0;

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

		onLoadMoreItems();

		entryListView.setAdapter(adapter);
		entryListView.setLayoutManager(new LinearLayoutManager(getContext()));
		showLoading();

		if (refreshLayout != null) {
			refreshLayout.setOnRefreshListener(this);
		}

		registerForContextMenu(entryListView);
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
		if (v == entryListView) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			GalleryEntry entry = adapter.getItem(info.position);
			menuBuilder.buildMenu(menu, getActivity(), entry, info.position, favoritesCategory);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//because #onContextItemSelected also gets triggered on invisible fragments...
		if (getUserVisibleHint()) {
			GalleryEntry entry = adapter.getItem(item.getOrder());
			menuBuilder.onMenuItemSelected(item, entry, this);
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent viewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
		try {
			viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, mObjectMapper.writeValueAsString(
					adapter.getItem(position)));
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
		adapter.clear();
		currentPage = 0;

		showLoading();
	}

	public void onLoadMoreItems() {
		dataLoader.getGalleryIndex(mQueryUrl, currentPage++)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(galleryEntries -> {
					if (refreshLayout != null) {
						refreshLayout.setRefreshing(false);
					}

					adapter.addItems(galleryEntries);

					if (adapter.getItemCount() == 0) {
						showEmpty();
					}
				}, throwable -> {
					Log.e(TAG, "Couldn't retrieve gallery items", throwable);
					if(refreshLayout != null) {
						refreshLayout.setRefreshing(false);
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
		Snackbar.make(entryListView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onRemovedFromFavorites() {
		Snackbar.make(entryListView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onFailedToRemoveFavorite() {
		Snackbar.make(entryListView, R.string.favorite_removed_failed, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onFailedToAddFavorite(int category) {
		Snackbar.make(entryListView, R.string.favorite_added_failed, Snackbar.LENGTH_SHORT).show();
	}

	private void showEmpty() {
		//TODO empty
		loadingView.setVisibility(View.GONE);
	}

	private void showLoading() {
		//TODO loading
		noContentView.setVisibility(View.GONE);
	}

}
