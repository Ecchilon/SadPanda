package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.ApiCallException;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;
import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_image)
public class ImageViewerActivity extends RoboActionBarActivity implements ImageViewerFragment.VisibilityToggler {

	private static final String GALLERY_ENTRY_KEY = "galleryEntryKey";

	private boolean lowProfile = false;

	@Inject
	private ObjectMapper mObjectMapper;

	@Inject
	private DataLoader mDataLoader;

	private GalleryEntry mGalleryEntry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			try {
				mGalleryEntry =
						mObjectMapper.readValue(savedInstanceState.getString(GALLERY_ENTRY_KEY), GalleryEntry.class);
			}
			catch (IOException e) {
				Log.w("ImageViewerFragment", "Failed to parse gallery entry during state restoration", e);
			}
		}

		if (mGalleryEntry == null) {
			Intent intent = getIntent();
			Uri data = intent.getData();
			if (data != null) {
				new GalleryLoadTask().execute(data);
				//TODO show loading screen?
			}
			else {
				String entryString = getIntent().getStringExtra(ImageViewerFragment.GALLERY_ITEM_KEY);

				try {
					mGalleryEntry = mObjectMapper.readValue(entryString, GalleryEntry.class);
				}
				catch (IOException e) {
					Toast.makeText(this, R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
					Log.e("ImageViewerFragment", "Failed to parse gallery entry", e);
					finish();
					return;
				}

				setGallery(mGalleryEntry);
			}
		}
	}

	private Handler uiHandler = new Handler();
	private Runnable hideTask = new Runnable() {
		@SuppressLint("NewApi")
		@Override
		public void run() {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	};


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mGalleryEntry != null) {
			try {
				outState.putString(GALLERY_ENTRY_KEY, mObjectMapper.writeValueAsString(mGalleryEntry));
			}
			catch (IOException ignored) {
			}
		}
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
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void setGallery(GalleryEntry galleryEntry) {
		getSupportActionBar().setTitle(galleryEntry.getTitle());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		try {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container,
							ImageViewerFragment.newInstance(mObjectMapper.writeValueAsString(galleryEntry)))
					.commit();
		}
		catch (IOException e) {
			Toast.makeText(this, R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("ImageViewerFragment", "Failed to parse gallery entry", e);
			finish();
		}
	}

	private class GalleryLoadTask extends AsyncResultTask<Uri, Void, GalleryEntry> {

		public GalleryLoadTask() {
			super(false);
		}

		@Override
		protected GalleryEntry call(Uri... params) throws Exception {
			return mDataLoader.getGallery(params[0].toString());
		}

		@Override
		protected void onSuccess(GalleryEntry entry) {
			setGallery(entry);
		}

		@Override
		protected void onError(Exception e) {
			Toast.makeText(ImageViewerActivity.this, R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("ImageViewerFragment", "Failed to parse gallery entry", e);
			finish();
		}
	}
}
