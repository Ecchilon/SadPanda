package com.ecchilon.sadpanda.imageviewer;

import java.io.IOException;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.SadPandaApp;
import com.ecchilon.sadpanda.imageviewer.data.ImageEntry;
import com.google.inject.Inject;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import org.codehaus.jackson.map.ObjectMapper;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Alex on 1/24/14.
 */
public class ScreenSlidePageFragment extends RoboFragment implements Callback {

	private static final String TAG = ScreenSlidePageFragment.class.getSimpleName();

	private static final String CENTER_HTML =
			"<html>"
					+ "<head>"
					+ "<style type='text/css'>"
					+ "#im {"
					+ "position: absolute;"
					+ "top: 0;"
					+ "left: 0;"
					+ "right: 0;"
					+ "bottom: 0;"
					+ "background-image: url(\"%s\");"
					+ "background-repeat: no-repeat;"
					+ "background-size: contain;"
					+ "background-position: center;"
					+ "}"
					+ "</style>"
					+ "</head>"
					+ "<body>"
					+ "<div id=\"im\">"
					+ "</div>"
					+ "</body>"
					+ "</html>";

	private static final String CONTENT_TYPE = "text/html";
	private static final String CONTENT_ENCODING = "UTF-8";
	private static final String ENTRY_KEY ="entryKey";

	public static final String IMAGE_SCALE_KEY = "imageScaleKey";

	public static final String MAX_ZOOM_KEY = "ExhentaiMaxScale";

	public static final float MAX_SCALE = 2.0f;

	@InjectView(R.id.image_view)
	private PhotoView mImageView;
	@InjectView(R.id.loading_view)
	private ProgressBar mLoadingBar;
	@InjectView(R.id.failure_text)
	private TextView mFailureText;
	@InjectView(R.id.animated_view)
	private WebView mAnimatedView;
	@InjectView(R.id.container)
	private FrameLayout mContainer;

	@Inject
	private ObjectMapper objectMapper;

	private ImageScale mImageScale = ImageScale.FIT_TO_SCREEN;

	private ImageEntry mImageEntry;
	private float mMaxZoom = MAX_SCALE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			if (getArguments().containsKey(MAX_ZOOM_KEY)) {
				mMaxZoom = getArguments().getFloat(MAX_ZOOM_KEY);
			}

			if (getArguments().containsKey(IMAGE_SCALE_KEY)) {
				mImageScale = (ImageScale) getArguments().getSerializable(IMAGE_SCALE_KEY);
			}
		}

		if(savedInstanceState != null && savedInstanceState.containsKey(ENTRY_KEY)) {
			try {
				mImageEntry = objectMapper.readValue(savedInstanceState.getString(ENTRY_KEY), ImageEntry.class);
			}
			catch (IOException e) {
				Log.e(TAG, "Couldn't load entry from saved state", e);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		try {
			outState.putString(ENTRY_KEY, objectMapper.writeValueAsString(mImageEntry));
		}
		catch (IOException e) {
			Log.e(TAG, "Couldn't write entry to string", e);
		}
	}

	@Override
	public void onDestroy() {
		//Because GC bug in WebView
		mContainer.removeAllViews();
		mAnimatedView.destroy();
		super.onDestroy();
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

		mAnimatedView.getSettings().setSupportZoom(true);
		mAnimatedView.setBackgroundColor(getResources().getColor(R.color.black));
		mAnimatedView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				mLoadingBar.setVisibility(View.GONE);
			}
		});

		if (mImageEntry != null && mImageEntry.getSrc() != null) {
			loadImage();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				loadImage();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void loadImage() {
		mLoadingBar.setVisibility(View.VISIBLE);
		mFailureText.setVisibility(View.GONE);

		if (mImageEntry == null || mImageEntry.getSrc() == null) {
			return;
		}

		if (mImageEntry.getSrc().endsWith(".gif")) {
			loadAnimatedView();
		}
		else {
			loadSimpleView();
		}
	}

	private void loadAnimatedView() {
		mImageView.setVisibility(View.GONE);
		mAnimatedView.setVisibility(View.VISIBLE);
		mAnimatedView.loadData(String.format(CENTER_HTML, mImageEntry.getSrc()), CONTENT_TYPE, CONTENT_ENCODING);
	}

	private void loadSimpleView() {
		RequestCreator requestCreator = Picasso.with(getActivity()).load(mImageEntry.getSrc());

		switch (mImageScale) {
			case DOUBLE:
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x * 2;
				int height = size.y * 2;
				requestCreator.resize(width, height);
				break;
			case FIT_TO_SCREEN:
				requestCreator.fit().centerInside();
				break;
		}

		requestCreator.into(mImageView, this);
	}

	public void onImageRetrieved(ImageEntry entry) {
		mImageEntry = entry;

		if (mImageView != null && mImageEntry != null && mImageEntry.getSrc() != null) {
			loadImage();
		}
	}

	public void onImageError(Throwable e) {
		mFailureText.setVisibility(View.VISIBLE);
		Log.e("ScreenSlidePageFragment", "Error loading image", e);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		Picasso.with(getActivity()).cancelRequest(mImageView);
	}

	@Override
	public void onSuccess() {
		mLoadingBar.setVisibility(View.GONE);
	}

	@Override
	public void onError() {
		if (SadPandaApp.getLastException() instanceof Downloader.ResponseException) {
			mFailureText.setText(R.string.image_not_found);
		}
		else {
			mFailureText.setText(R.string.image_loading_failed);
		}

		mFailureText.setVisibility(View.VISIBLE);

		mLoadingBar.setVisibility(View.GONE);
	}
}