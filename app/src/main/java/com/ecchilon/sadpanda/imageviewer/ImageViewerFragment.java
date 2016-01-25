package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.data.ImageLoader;
import com.ecchilon.sadpanda.imageviewer.data.ImageLoaderFactory;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.overview.OverviewFragment;
import com.ecchilon.sadpanda.overview.SearchActivity;
import com.ecchilon.sadpanda.search.SearchController;
import com.ecchilon.sadpanda.util.MenuBuilder;
import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class ImageViewerFragment extends RoboFragment implements MenuBuilder.GalleryMenuClickListener {

	public interface VisibilityToggler {
		void toggleVisibility(boolean delayUI);
	}

	public interface PageSelectedListener {
		void onPageSelected(int page);
	}

	private static final String TAG = "ImageViewerFragment";
	private static final String PAGER_STATE_KEY = "lastPageKey";

	public static final String PAGE_NUMBER_KEY = "pageNumberKey";
	public static final String GALLERY_ITEM_KEY = "ExhentaiGallery";

	public static ImageViewerFragment newInstance(@NonNull String entryString, @Nullable Integer page) {
		ImageViewerFragment fragment = new ImageViewerFragment();
		Bundle args = new Bundle();
		args.putString(GALLERY_ITEM_KEY, entryString);
		if(page != null) {
			args.putInt(PAGE_NUMBER_KEY, page);
		}

		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous and next wizard
	 * steps.
	 */
	@InjectView(R.id.pager)
	private GestureViewPager viewPager;

	@Inject
	private ObjectMapper mObjectMapper;
	@Inject
	private ImageLoaderFactory mImageLoaderFactory;
	@Inject
	private MenuBuilder menuBuilder;

	private VisibilityToggler mVisibilityToggler;
	private PageSelectedListener mPageListener;

	private GalleryEntry galleryEntry;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			galleryEntry = mObjectMapper.readValue(getArguments().getString(GALLERY_ITEM_KEY), GalleryEntry.class);
		}
		catch (IOException e) {
			Toast.makeText(getActivity(), R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Failed to parse gallery entry", e);
			getActivity().finish();
		}

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_image_viewer, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mVisibilityToggler = (VisibilityToggler) activity;
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Activity " + activity.getClass().getSimpleName() +
					" must implement ImageViewerFragment.VisibilityToggler");
		}

		try {
			mPageListener = (PageSelectedListener) activity;
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Activity " + activity.getClass().getSimpleName() +
					" must implement ImageViewerFragment.PageSelectedListener");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menuBuilder.buildMenu(menu, getContext(), galleryEntry, 0, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mVisibilityToggler.toggleVisibility(false);

		initImageViewPager(savedInstanceState);
	}

	private void initImageViewPager(Bundle savedInstanceState) {
		ImageLoader loader = mImageLoaderFactory.getImageLoader(galleryEntry);
		PagerAdapter pagerAdapter =
				new ScreenSlidePagerAdapter(getChildFragmentManager(), loader, galleryEntry, createArguments());
		viewPager.setAdapter(pagerAdapter);
		viewPager.setGestureDetector(new GestureDetector(getActivity(), new SingleTapListener()));
		viewPager.setOffscreenPageLimit(2);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		if(savedInstanceState != null) {
			viewPager.onRestoreInstanceState(savedInstanceState.getParcelable(PAGER_STATE_KEY));
		}
		else if(getArguments() != null && getArguments().containsKey(PAGE_NUMBER_KEY)) {
			viewPager.setCurrentItem(getArguments().getInt(PAGE_NUMBER_KEY), false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuBuilder.onMenuItemSelected(item, galleryEntry, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		 outState.putParcelable(PAGER_STATE_KEY, viewPager.onSaveInstanceState());
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
		Snackbar.make(viewPager, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onRemovedFromFavorites() {
		Snackbar.make(viewPager, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onFailedToRemoveFavorite() {
		Snackbar.make(viewPager, R.string.favorite_removed_failed, Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onFailedToAddFavorite(int category) {
		Snackbar.make(viewPager, R.string.favorite_added_failed, Snackbar.LENGTH_SHORT).show();
	}

	private Bundle createArguments() {
		Bundle arguments = new Bundle();

		SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

		ImageScale scale =
				ImageScale.getScale(sharedPreferences.getString(getString(R.string.pref_image_scaling_key), null));
		arguments.putSerializable(ScreenSlidePageFragment.IMAGE_SCALE_KEY, scale);

		float maxZoom = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_max_zoom_key), "2.5"));
		arguments.putFloat(ScreenSlidePageFragment.MAX_ZOOM_KEY, maxZoom);

		return arguments;
	}

	private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageScrolled(int i, float v, int i1) {

		}

		@Override
		public void onPageSelected(int i) {
			mPageListener.onPageSelected(i);
		}

		@Override
		public void onPageScrollStateChanged(int i) {

		}
	}

	private class SingleTapListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			mVisibilityToggler.toggleVisibility(true);
			return super.onSingleTapConfirmed(e);
		}
	}

}
