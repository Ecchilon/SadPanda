package com.ecchilon.sadpanda.imageviewer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Created by Alex on 21-9-2014.
 */
public class ImageViewerFragment extends RoboFragment {
    public interface VisibilityToggler {
        void toggleVisibility(boolean delayUI);
    }

    public static final String GALLERY_ITEM_KEY = "ExhentaiGallery";

    public static ImageViewerFragment newInstance(GalleryEntry entry) {
        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle args = new Bundle();
        args.putString(GALLERY_ITEM_KEY, new Gson().toJson(entry));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    @InjectView(R.id.pager)
    private GestureViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private VisibilityToggler mVisibilityToggler;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_viewer, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mVisibilityToggler = (VisibilityToggler) activity;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Activity " + activity.getClass().getSimpleName() +
                    " must implement ImageViewerFragment.VisibilityToggler");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVisibilityToggler.toggleVisibility(false);

        Gson gson = new Gson();

        GalleryEntry entry = gson
                .fromJson(getArguments().getString(GALLERY_ITEM_KEY), GalleryEntry.class);

        ImageLoader loader = new ImageLoader(entry, getSupportActivity());

        mPagerAdapter = new ScreenSlidePagerAdapter(
                getSupportActivity().getSupportFragmentManager(), loader, entry);
        mPager.setAdapter(mPagerAdapter);
        mPager.setGestureDetector(new GestureDetector(getSupportActivity(), new SingleTapListener()));
        mPager.setOffscreenPageLimit(5);
    }

    private ActionBarActivity getSupportActivity() {
        return (ActionBarActivity) getActivity();
    }

    private class SingleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mVisibilityToggler.toggleVisibility(true);
            return super.onSingleTapConfirmed(e);
        }
    }
}
