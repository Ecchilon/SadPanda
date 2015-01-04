package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
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
import com.ecchilon.sadpanda.favorites.AddFavoriteTask;
import com.ecchilon.sadpanda.favorites.FavoritesTaskFactory;
import com.ecchilon.sadpanda.favorites.RemoveFavoriteTask;
import com.ecchilon.sadpanda.menu.FavoritesMenu;
import com.ecchilon.sadpanda.menu.GalleryMenu;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.overview.OverviewFragment;
import com.ecchilon.sadpanda.overview.SearchActivity;
import com.ecchilon.sadpanda.search.SearchController;
import com.ecchilon.sadpanda.util.AddFavoriteCallback;
import com.ecchilon.sadpanda.util.RemoveFavoriteCallback;
import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Created by Alex on 21-9-2014.
 */
public class ImageViewerFragment extends RoboFragment implements FavoritesMenu.FavoriteCategorySelectedListener,
		GalleryMenu.MenuItemSelectedListener {

	private static final String TAG = "ImageViewerFragment";

	public interface VisibilityToggler {
		void toggleVisibility(boolean delayUI);
	}

	public static final String GALLERY_ITEM_KEY = "ExhentaiGallery";

	public static ImageViewerFragment newInstance(String entryString) {
		ImageViewerFragment fragment = new ImageViewerFragment();
		Bundle args = new Bundle();
		args.putString(GALLERY_ITEM_KEY, entryString);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous and next wizard
	 * steps.
	 */
	@InjectView(R.id.pager)
	private GestureViewPager mPager;

	@Inject
	private ObjectMapper mObjectMapper;
	@Inject
	private ImageLoaderFactory mImageLoaderFactory;
	@Inject
	private FavoritesTaskFactory mFavoritesTaskFactory;

	private VisibilityToggler mVisibilityToggler;

	private GalleryEntry mGalleryEntry;

	private GalleryMenu mGalleryMenu;
	private FavoritesMenu mFavoritesMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.gallery, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_favorite:
				mFavoritesMenu.show();
				return true;
			case R.id.remove_favorite:
				RemoveFavoriteTask task = mFavoritesTaskFactory.getRemoveFavoriteTask(mGalleryEntry);
				task.setListener(new RemoveFavoriteCallback(getActivity()));
				task.execute();
			case R.id.view_by:
				mGalleryMenu.show();
				return true;
		}

		return super.onOptionsItemSelected(item);
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
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mVisibilityToggler.toggleVisibility(false);

		try {
			mGalleryEntry = mObjectMapper.readValue(getArguments().getString(GALLERY_ITEM_KEY), GalleryEntry.class);
		}
		catch (IOException e) {
			Toast.makeText(getActivity(), R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("ImageViewerFragment", "Failed to parse gallery entry", e);
			getActivity().finish();
			return;
		}

		mGalleryMenu = new GalleryMenu(mGalleryEntry, getActivity());
		mGalleryMenu.setOnMenuItemSelectedListener(this);

		mFavoritesMenu = new FavoritesMenu(mGalleryEntry, getActivity());
		mFavoritesMenu.setFavoritesCategorySelectedListener(this);

		ImageLoader loader = mImageLoaderFactory.getImageLoader(mGalleryEntry);

		PagerAdapter mPagerAdapter =
				new ScreenSlidePagerAdapter(getChildFragmentManager(), loader, mGalleryEntry, createArguments());
		mPager.setAdapter(mPagerAdapter);
		mPager.setGestureDetector(new GestureDetector(getActivity(), new SingleTapListener()));
		mPager.setOffscreenPageLimit(2);
	}

	@Override
	public void onFavoriteCategorySelected(int category) {
		AddFavoriteTask task = mFavoritesTaskFactory.getAddFavoriteTask(mGalleryEntry, category, null);
		task.setListener(new AddFavoriteCallback(getActivity()));
		task.execute();
	}

	@Override
	public void onMenuItemSelected(String item, boolean uploader) {
		SearchController controller = new SearchController();
		String url = uploader ? controller.getUploaderUrl(item) : controller.getUrl(item);

		Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
		searchIntent.putExtra(OverviewFragment.URL_KEY, url);
		getActivity().startActivity(searchIntent);
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

	private class SingleTapListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			mVisibilityToggler.toggleVisibility(true);
			return super.onSingleTapConfirmed(e);
		}
	}
}
