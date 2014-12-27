package com.ecchilon.sadpanda.overview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.ecchilon.sadpanda.util.PagedScrollAdapter;
import com.google.gson.Gson;

import lombok.NonNull;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * interface.
 */
public class OverviewFragment extends RoboFragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, PagedScrollAdapter.PageLoadListener {

    public static final String URL_KEY = "ExhentaiURL";
    public static final String QUERY_KEY = "ExhentaiQuery";

    @InjectView(R.id.overview_list)
    private AbsListView mListView;
    @InjectView(R.id.swipe_container)
    private SwipeRefreshLayout mRefreshLayout;

    private OverviewAdapter mAdapter;

    public static OverviewFragment newInstance(@NonNull String url) {
        OverviewFragment fragment = new OverviewFragment();
        if(url != null) {
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.overview, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_entry, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String query = getArguments().getString(URL_KEY);

        mAdapter = new OverviewAdapter(query, getActivity());
        mAdapter.setPageLoadListener(this);

        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(mAdapter);

        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_menu:
                mAdapter.reload();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent viewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
        viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, new Gson().toJson(mAdapter.getItem(position)));
        startActivity(viewerIntent);
    }

    @Override
    public void onRefresh() {
        mAdapter.reload();
    }

    @Override
    public void onPageLoadStart(int page) {

    }

    @Override
    public void onPageLoadEnd(int page) {
        mRefreshLayout.setRefreshing(false);
    }
}
