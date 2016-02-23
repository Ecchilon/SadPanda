package com.ecchilon.sadpanda.imageviewer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.RoboAppCompatActivity;
import com.ecchilon.sadpanda.api.DataLoader;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_image)
public class ImageViewerActivity extends RoboAppCompatActivity implements ImageViewerFragment.VisibilityToggler,
		ThumbFragment.OnThumbSelectedListener, ImageViewerFragment.PageSelectedListener {

	private static final String ACTIVITY_STATE_KEY = "activityStateKey";

	private boolean lowProfile = false;

	private boolean mThumbMode = false;

	private Handler uiHandler = new Handler();
	private Runnable hideTask = () -> getWindow().getDecorView().setSystemUiVisibility(View
			.SYSTEM_UI_FLAG_LOW_PROFILE);

	private int mCurrentPage = 0;

	private static final String GALLERY_ID_KEY = "galleryIdKey";
	private static final String GALLERY_TOKEN_KEY = "galleryTokenKey";

	private DataLoader.GalleryIdToken galleryIdToken;

	public static Intent newInstance(Context context, long galleryId, @NonNull String galleryToken) {
		Intent intent = new Intent(context, ImageViewerActivity.class);
		intent.putExtra(GALLERY_ID_KEY, galleryId);
		intent.putExtra(GALLERY_TOKEN_KEY, galleryToken);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (savedInstanceState != null) {
			mThumbMode = savedInstanceState.getBoolean(ACTIVITY_STATE_KEY);
		}

		if (!(getIntent().hasExtra(GALLERY_ID_KEY) && getIntent().hasExtra(GALLERY_TOKEN_KEY))) {
			Intent intent = getIntent();
			Uri data = intent.getData();
			if (data != null) {
				galleryIdToken = DataLoader.getGalleryId(data.toString());
				if (galleryIdToken != null) {
					loadFragment();
				}
				else {
					Toast.makeText(this, R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
					finish();
				}
			}
			else {
				Toast.makeText(this, R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		else {
			galleryIdToken = new DataLoader.GalleryIdToken(getIntent().getLongExtra(GALLERY_ID_KEY, 0),
					getIntent().getStringExtra(GALLERY_TOKEN_KEY));
			loadFragment();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private void loadFragment() {
		Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.container);
		if (activeFragment != null &&
				((activeFragment instanceof ThumbFragment && mThumbMode) || (
						activeFragment instanceof ImageViewerFragment && !mThumbMode))) {
			//fragment is the correct fragment
			return;
		}

		Fragment newFragment = mThumbMode ?
				ThumbFragment.newInstance(galleryIdToken, mCurrentPage) :
				ImageViewerFragment.newInstance(galleryIdToken, mCurrentPage);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, newFragment)
				.commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(ACTIVITY_STATE_KEY, mThumbMode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.image_activity, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.show_overview);

		item.setIcon(mThumbMode ? R.drawable.ic_action_picture : R.drawable.ic_action_view_as_grid);
		item.setTitle(mThumbMode ? R.string.show_image : R.string.show_overview);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void toggleVisibility(boolean hideUIDelayed) {
		if (!lowProfile) {
			getSupportActionBar().hide();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				uiHandler.postDelayed(hideTask, hideUIDelayed ? 1000 : 0);
			}
		}
		else {
			getSupportActionBar().show();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				//Remove callback in case this function was called before it could fire
				uiHandler.removeCallbacks(hideTask);
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}

		lowProfile = !lowProfile;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent upIntent = NavUtils.getParentActivityIntent(this);
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					// We're not part of the app's task, so we create a new one
					TaskStackBuilder.create(this)
							.addNextIntentWithParentStack(upIntent)
							.startActivities();
				}
				else {
					// We're part of the app's task, so we navigate back (create up intent destroys existing parent
					// intent forcing a reload of the page)
					onBackPressed();
				}
				break;
			case R.id.show_overview:
				toggleGridview();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void toggleGridview() {
		mThumbMode = !mThumbMode;

		invalidateOptionsMenu();

		loadFragment();
	}

	@Override
	public void onThumbSelected(int position) {
		mCurrentPage = position;
		toggleGridview();
	}

	@Override
	public void onPageSelected(int page) {
		mCurrentPage = page;
	}
}
