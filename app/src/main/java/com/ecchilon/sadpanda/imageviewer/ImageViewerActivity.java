package com.ecchilon.sadpanda.imageviewer;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import com.ecchilon.sadpanda.R;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_image)
public class ImageViewerActivity extends RoboActionBarActivity implements ImageViewerFragment.VisibilityToggler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(savedInstanceState == null) {
            String entryString = getIntent().getStringExtra(ImageViewerFragment.GALLERY_ITEM_KEY);

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
    public void toggleVisibility(boolean hideUIDelayed){
        if(!lowProfile){
            getSupportActionBar().hide();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                uiHandler.postDelayed(hideTask, hideUIDelayed? 1000 : 0);
            }
        }
        else {
            getSupportActionBar().show();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
