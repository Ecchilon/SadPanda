package com.ecchilon.sadpanda.overview;

import java.util.List;

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
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.RxRoboFragment;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.api.GalleryLoader;
import com.ecchilon.sadpanda.search.SearchController;
import com.ecchilon.sadpanda.search.SearchDialogFragment;
import com.ecchilon.sadpanda.util.MenuBuilder;
import com.google.inject.Inject;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.trello.rxlifecycle.RxLifecycle;
import lombok.NonNull;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

/**
 * A fragment representing a list of Items. Large screen devices (such as tablets) are supported by replacing the
 * ListView with a GridView. interface.
 */
public class OverviewFragment extends RxRoboFragment implements MenuBuilder.GalleryMenuClickListener {

	private static final String TAG = "OverviewFragment";
	private Subscription pageSubscription = Subscriptions.empty();

	public enum SearchType {
		NONE,
		SIMPLE,
		ADVANCED
	}

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

	@InjectView(R.id.swipe_container)
	private SwipeRefreshLayout refreshLayout;

	private final OverviewAdapter adapter = new OverviewAdapter();

	@Inject
	private GalleryLoader galleryLoader;

	@Inject
	private MenuBuilder menuBuilder;

	private LinearLayoutManager layoutManager;
	private String mQueryUrl;
	private SearchType mSearchType;

	private Integer favoritesCategory;

	private final Observable<List<GalleryEntry>> pageObservable = Observable.defer(
			() -> RxRecyclerView.scrollEvents(entryListView)
					.compose(bindToLifecycle())
					.filter(this::canLoadPage)
					.scan(0, (page, event) -> page + 1)
					.flatMap(page -> galleryLoader.loadPage(mQueryUrl, page)));

	public static OverviewFragment newInstance(@NonNull String url, @Nullable Integer favoritesCategory,
			@Nullable String query, SearchType searchType) {
		OverviewFragment fragment = new OverviewFragment();
		if (url != null) {
			Bundle args = new Bundle();
			args.putString(URL_KEY, url);
			if (favoritesCategory != null) {
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

		if (getArguments().containsKey(IS_FAVORITE_LIST_KEY)) {
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

		entryListView.setAdapter(adapter);
		layoutManager = new LinearLayoutManager(getContext());
		entryListView.setLayoutManager(layoutManager);
		loadNewData();

		refreshLayout.setOnRefreshListener(this::refresh);

		registerForContextMenu(entryListView);
	}

	private void loadNewData() {
		pageSubscription.unsubscribe();

		pageSubscription = pageObservable.observeOn(AndroidSchedulers.mainThread())
				.subscribe(galleryEntries -> {
					if (refreshLayout != null) {
						refreshLayout.setRefreshing(false);
					}

					showLoading();
					adapter.addItems(galleryEntries);

					if (adapter.getItemCount() == 0) {
						showEmpty();
					}
				}, throwable -> {
					Log.e(TAG, "Couldn't retrieve gallery items", throwable);
					if (refreshLayout != null) {
						refreshLayout.setRefreshing(false);
					}
					showEmpty();
				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh_menu:
				refresh();
				return true;
			case R.id.action_search:
				showSearchFragment();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void refresh() {
		adapter.clear();
		loadNewData();

		showLoading();
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

	private boolean canLoadPage(RecyclerViewScrollEvent event) {
		int visibleItemCount = layoutManager.getChildCount();
		int totalItemCount = layoutManager.getItemCount();
		int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
		return !galleryLoader.isLoading() && galleryLoader.isHasMoreItems()
				&& visibleItemCount + firstVisibleItem >= totalItemCount;
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
