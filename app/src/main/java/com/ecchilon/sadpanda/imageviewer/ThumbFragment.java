package com.ecchilon.sadpanda.imageviewer;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.RxRoboFragment;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.api.GalleryClient;
import com.ecchilon.sadpanda.imageviewer.data.GalleryPageCache;
import com.google.inject.Inject;
import com.squareup.picasso.Picasso;
import lombok.Getter;
import roboguice.inject.InjectView;
import rx.android.schedulers.AndroidSchedulers;

public class ThumbFragment extends RxRoboFragment implements AbsListView.OnItemClickListener {

	public interface OnThumbSelectedListener {
		void onThumbSelected(int position);
	}

	private static final String GRID_STATE_KEY = "gridStateKey";

	private static final String GALLERY_ID_KEY = "galleryIdKey";
	private static final String GALLERY_TOKEN_KEY = "galleryTokenKey";

	public static final String CURRENT_PAGE_KEY = "currentPageKey";

	@Getter
	private static final Object picassoTag = new Object();

	public static ThumbFragment newInstance(DataLoader.GalleryIdToken galleryIdToken, int currentPage) {
		Bundle args = new Bundle();
		args.putLong(GALLERY_ID_KEY, galleryIdToken.getGalleryId());
		args.putString(GALLERY_TOKEN_KEY, galleryIdToken.getGalleryToken());
		args.putInt(CURRENT_PAGE_KEY, currentPage);

		ThumbFragment fragment = new ThumbFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@InjectView(R.id.thumb_overview)
	private GridView mThumbOverview;

	@Inject
	private GalleryClient galleryClient;

	@Inject
	private GalleryPageCache galleryPageCache;

	private int mCurrentPage;
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.thumb_overview, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mThumbOverview.setOnItemClickListener(this);

		galleryClient.getEntry(getArguments().getLong(GALLERY_ID_KEY), getArguments().getString(GALLERY_TOKEN_KEY))
				.compose(bindToLifecycle())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(entry -> {
					ThumbAdapter adapter = new ThumbAdapter(galleryPageCache, entry);
					mThumbOverview.setAdapter(adapter);
				}, throwable -> {
					Toast.makeText(getContext(), R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
					getActivity().finish();
				});

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
