package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.data.GalleryPageCache;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import com.squareup.picasso.Picasso;
import lombok.Getter;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class ThumbFragment extends RoboFragment implements AbsListView.OnItemClickListener {

	public interface OnThumbSelectedListener {
		void onThumbSelected(int position);
	}

	private static final String GRID_STATE_KEY = "gridStateKey";

	public static final String GALLERY_ENTRY_KEY = "galleryEntryKey";

	public static final String CURRENT_PAGE_KEY = "currentPageKey";

	@Getter
	private static final Object picassoTag = new Object();

	public static ThumbFragment newInstance(@NonNull String entryString, int currentPage) {
		Bundle args = new Bundle();
		args.putString(GALLERY_ENTRY_KEY, entryString);
		args.putInt(CURRENT_PAGE_KEY, currentPage);

		ThumbFragment fragment = new ThumbFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@InjectView(R.id.thumb_overview)
	private GridView mThumbOverview;

	@Inject
	private GalleryPageCache galleryPageCache;

	@Inject
	private ObjectMapper mObjectMapper;

	private int mCurrentPage;
	private GalleryEntry mGalleryEntry;
	private OnThumbSelectedListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (OnThumbSelectedListener) activity;
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Activity " + activity.getLocalClassName()
					+ " should implement ThumbFragment.OnThumbSelectedListener!", e);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		Picasso.with(getActivity()).cancelTag(picassoTag);
		mListener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCurrentPage = getArguments().getInt(CURRENT_PAGE_KEY);

		try {
			mGalleryEntry = mObjectMapper.readValue(getArguments().getString(GALLERY_ENTRY_KEY), GalleryEntry.class);
		}
		catch (IOException e) {
			Toast.makeText(getActivity(), R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("ImageViewerFragment", "Failed to parse gallery entry", e);
			getActivity().finish();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.thumb_overview, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ThumbAdapter adapter = new ThumbAdapter(galleryPageCache, mGalleryEntry);
		mThumbOverview.setAdapter(adapter);
		mThumbOverview.setOnItemClickListener(this);
		if (savedInstanceState != null) {
			mThumbOverview.onRestoreInstanceState(savedInstanceState.getParcelable(GRID_STATE_KEY));
		}
		else {
			mThumbOverview.setSelection(mCurrentPage);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mListener.onThumbSelected(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(GRID_STATE_KEY, mThumbOverview.onSaveInstanceState());
	}
}
