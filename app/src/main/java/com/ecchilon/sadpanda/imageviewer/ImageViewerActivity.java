package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_image)
public class ImageViewerActivity extends RoboActionBarActivity implements ImageViewerFragment.VisibilityToggler {

	@Inject
	private ObjectMapper mObjectMapper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String entryString = getIntent().getStringExtra(ImageViewerFragment.GALLERY_ITEM_KEY);

		GalleryEntry galleryEntry;
		try {
			galleryEntry = mObjectMapper.readValue(entryString, GalleryEntry.class);
		}
		catch (IOException e) {
			Toast.makeText(this, R.string.entry_parsing_failure, Toast.LENGTH_SHORT).show();
			Log.e("ImageViewerFragment", "Failed to parse gallery entry", e);
			finish();
			return;
		}

		getSupportActionBar().setTitle(galleryEntry.getTitle());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, ImageViewerFragment.newInstance(entryString))
					.commit();
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

	private boolean lowProfile = false;

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
				onBackPressed();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
