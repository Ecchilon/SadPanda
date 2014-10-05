package com.ecchilon.sadpanda.overview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.ImageViewerActivity;
import com.ecchilon.sadpanda.imageviewer.ImageViewerFragment;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

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

    private static ImmutableMap<String,String> QUERY_PARAMS = ImmutableMap.<String, String>builder()
            .put("f_doujinshi", "1")
            .put("f_manga", "1")
            .put("f_artistcg", "1")
            .put("f_gamecg", "1")
            .put("f_western", "1")
            .put("f_non-h", "1")
            .put("f_imageset", "1")
            .put("f_cosplay", "1")
            .put("f_asianporn", "1")
            .put("f_misc", "1")
            .put("f_apply", "Apply+Filter").build();

    private static final String SEARCH_PARAM = "f_search";
    private static final String AUTHORITY = "exhentai.org";
    private static final String SCHEME = "http";
    private static final String FRONT_PAGE_URL =  SCHEME + "://" + AUTHORITY + "/";

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
            try {
                buildAdapter();
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(getActivity(), R.string.query_error, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            mAdapter = new OverviewAdapter(FRONT_PAGE_URL, getActivity());
        }

        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(mAdapter);
    }

    private void buildAdapter() throws UnsupportedEncodingException {
        String query = getArguments().getString(QUERY_KEY).trim();
        Uri.Builder builder = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path("/");
        for(Map.Entry<String, String> queryParam : QUERY_PARAMS.entrySet()) {
            builder.appendQueryParameter(queryParam.getKey(), queryParam.getValue());
        }

        builder.appendQueryParameter(SEARCH_PARAM, URLEncoder.encode(query, "UTF-8"));

        Uri uri = builder.build();
        mAdapter = new OverviewAdapter(uri.toString(), getActivity());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent viewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
        viewerIntent.putExtra(ImageViewerFragment.GALLERY_ITEM_KEY, new Gson().toJson(mAdapter.getItem(position)));
        startActivity(viewerIntent);
    }
}
