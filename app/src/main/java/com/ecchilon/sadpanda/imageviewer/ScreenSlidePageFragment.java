package com.ecchilon.sadpanda.imageviewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.ApiErrorCode;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Alex on 1/24/14.
 */
public class ScreenSlidePageFragment extends RoboFragment implements ImageLoader.ImageListener, Callback {

    public static final String MAX_SCALE_KEY = "ExhentaiMaxScale";

    public static final float MAX_SCALE = 2.0f;

    @InjectView(R.id.image_view)
	private PhotoView mImageView;
    @InjectView(R.id.loading_view)
	private ProgressBar loadingBar;
    @InjectView(R.id.failure_text)
	private TextView failureText;

	private ImageEntry mImageEntry;
    private float mMaxZoom = MAX_SCALE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(getArguments() != null && getArguments().containsKey(MAX_SCALE_KEY)) {
            mMaxZoom = getArguments().getFloat(MAX_SCALE_KEY);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_single_image, container, false);
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.image, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView.setMaximumScale(mMaxZoom);

        if (mImageEntry != null && mImageEntry.getSrc() != null) {
            loadImage(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                loadImage(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadImage(boolean cache) {
        loadingBar.setVisibility(View.VISIBLE);
        failureText.setVisibility(View.GONE);

        if(mImageEntry == null || mImageEntry.getSrc() == null) {
            return;
        }

        RequestCreator requestCreator =Picasso.with(getActivity()).load(mImageEntry.getSrc());
        if(!cache) {
            Log.d("ScreenSlidePageFragment", "Skipping cache for " + mImageEntry.getSrc());
            requestCreator.skipMemoryCache();
        }
        requestCreator.into(mImageView, this);
    }

    @Override
    public void onLoad(ImageEntry entry) {
        mImageEntry = entry;

        if (mImageView != null && mImageEntry != null && mImageEntry.getSrc() != null) {
            loadImage(true);
        }
    }

    @Override
    public void onSuccess() {
        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        failureText.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void onError(ApiErrorCode code) {
        failureText.setVisibility(View.VISIBLE);
        Log.e("ScreenSlidePageFragment", code.name());
    }
}