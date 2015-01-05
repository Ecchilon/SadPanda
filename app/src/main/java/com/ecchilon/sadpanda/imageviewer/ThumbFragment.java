package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;
import com.google.inject.Inject;
import com.paging.listview.PagingGridView;
import com.paging.listview.PagingView;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class ThumbFragment extends RoboFragment implements AbsListView.OnItemClickListener, PagingView.Pagingable {

	public interface OnThumbSelectedListener {
		void onThumbSelected(int position);
	}

	private static final String GRID_STATE_KEY = "gridStateKey";
	private static final int THUMB_COUNT = 40;

	public static final String GALLERY_ENTRY_KEY = "galleryEntryKey";

	public static ThumbFragment newInstance(@NonNull String entryString) {
		Bundle args = new Bundle();
		args.putString(GALLERY_ENTRY_KEY, entryString);

		ThumbFragment fragment = new ThumbFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@InjectView(R.id.thumb_overview)
	private PagingGridView mThumbOverview;

	@Inject
	private DataLoader mDataLoader;

	@Inject
	private ObjectMapper mObjectMapper;

	private GalleryEntry mGalleryEntry;
	private OnThumbSelectedListener mListener;
	private int mCurrentPage = 0;

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

		mListener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		ThumbAdapter adapter = new ThumbAdapter();
		mThumbOverview.setAdapter(adapter);
		mThumbOverview.setPagingableListener(this);
		mThumbOverview.setHasMoreItems(true);
		mThumbOverview.setOnItemClickListener(this);
		if(savedInstanceState != null) {
			mThumbOverview.onRestoreInstanceState(savedInstanceState.getParcelable(GRID_STATE_KEY));
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

	@Override
	public void onLoadMoreItems() {
		PageLoadTask task = new PageLoadTask();
		task.setListener(new AsyncResultTask.Callback<List<ImageEntry>>() {
			@Override
			public void onSuccess(List<ImageEntry> result) {
				mCurrentPage++;
				mThumbOverview.onFinishLoading(result.size() == THUMB_COUNT, result);
			}

			@Override
			public void onError(Exception e) {
				mThumbOverview.onFinishLoading(false, null);
			}
		});
		task.execute(mCurrentPage);
	}

	private class PageLoadTask extends AsyncResultTask<Integer, Void, List<ImageEntry>> {

		public PageLoadTask() {
			super(false);
		}

		@Override
		protected List<ImageEntry> call(Integer... params) throws Exception {
			return mDataLoader.getPhotoList(mGalleryEntry, params[0]);
		}
	}
}
