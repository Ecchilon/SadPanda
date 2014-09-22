package com.ecchilon.sadpanda.overview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringEscapeUtils;

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
public class OverviewFragment extends RoboFragment implements AbsListView.OnItemClickListener {

    private static final String DEFAULT_QUERY =
            "?f_doujinshi=1" +
            "&f_manga=1" +
            "&f_artistcg=1" +
            "&f_gamecg=1" +
            "&f_western=1" +
            "&f_non-h=1" +
            "&f_imageset=1" +
            "&f_cosplay=1" +
            "&f_asianporn=1" +
            "&f_misc=1" +
            "&f_search=%s" +
            "&f_apply=Apply+Filter";

    private static final String FRONT_PAGE_URL = "http://exhentai.org/";

    public static final String QUERY_KEY = "ExhentaiQuery";

    @InjectView(android.R.id.list)
    private AbsListView mListView;

    private OverviewAdapter mAdapter;

    public static OverviewFragment newInstance(String query) {
        OverviewFragment fragment = new OverviewFragment();
        if(query != null) {
            Bundle args = new Bundle();
            args.putString(QUERY_KEY, query);
            fragment.setArguments(args);
        }

        return fragment;
    }

    public OverviewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_entry, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(QUERY_KEY)) {
            buildAdapter();
        }
        else {
            mAdapter = new OverviewAdapter(FRONT_PAGE_URL, getActivity());
        }

        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(mAdapter);
    }

    private void buildAdapter() {
        String query = getArguments().getString(QUERY_KEY);
        String formattedQuery = FRONT_PAGE_URL +
                String.format(DEFAULT_QUERY, StringEscapeUtils.escapeHtml4(query));
        mAdapter = new OverviewAdapter(formattedQuery, getActivity());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent viewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
        viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, new Gson().toJson(mAdapter.getItem(position)));
        startActivity(viewerIntent);
    }
}
