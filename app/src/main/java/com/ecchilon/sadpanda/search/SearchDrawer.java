package com.ecchilon.sadpanda.search;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;

/**
 * Created by Alex on 5-10-2014.
 */
public class SearchDrawer {

    public interface OnSearchSubmittedListener {
        void onSearchSubmitted(String url, String query);
    }

    private static final float ANIMATION_SPEED = 1f;

    private final SearchController mSearchController;
    private final View mSearchView;
    private final EditText mQueryText;
    private final InputMethodManager mInputManager;

    private int mFullSize;
    private int mHalfSize;
    private final boolean mAlwaysShowAdvanced;

    private OnSearchSubmittedListener mListener;

    public SearchDrawer(View mSearchView, boolean alwaysShowAdvanced) {
        this.mSearchView = mSearchView;
        this.mAlwaysShowAdvanced = alwaysShowAdvanced;

        mInputManager = (InputMethodManager) mSearchView.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        mSearchController = new SearchController(mSearchView);

        if(alwaysShowAdvanced) {
            mSearchView.findViewById(R.id.show_advanced).setEnabled(false);
        }
        else {
            mSearchView.findViewById(R.id.show_advanced).setOnClickListener(new AdvancedListener());
        }

        mSearchView.findViewById(R.id.search_action).setOnClickListener(new SearchListener());
        mQueryText = (EditText) mSearchView.findViewById(R.id.search_query);
        mQueryText.setOnEditorActionListener(new ActionSubmitListener());
    }

    public void setupViews() {
        mFullSize = mSearchView.getHeight();
        mHalfSize = mSearchView.findViewById(R.id.small_header).getHeight();

        showSearch(false, false);
        mSearchView.setVisibility(View.VISIBLE);
    }

    public void setSearchListener(OnSearchSubmittedListener listener) {
        mListener = listener;
    }

    public void showSearch(boolean show) {
        showSearch(show, true);
    }

    public boolean isShowing() {
        return mSearchView.getTranslationY() != -mFullSize;
    }

    public String getQuery() {
        return mSearchController.getQuery();
    }

    private void showSearch(boolean show, boolean animate) {
        if(show == isShowing()) {
            return;
        }

        if(show) {
            int newTranslation = (mAlwaysShowAdvanced? 0 : -mFullSize + mHalfSize);

            if(animate) {
                DecelerateInterpolator interpolator = new DecelerateInterpolator();
                animate(newTranslation, interpolator);
            }
            else {
                mSearchView.setTranslationY(newTranslation);
            }

            mQueryText.requestFocus();
            mInputManager.showSoftInput(mQueryText, 0);
        }
        else {
            if(animate) {
                AccelerateInterpolator interpolator = new AccelerateInterpolator();
                animate(-mFullSize, interpolator);
            }
            else {
                mSearchView.setTranslationY(-mFullSize);
            }

            mInputManager.hideSoftInputFromWindow(mQueryText.getWindowToken(), 0);
        }
    }

    private void submitQuery() {
        if(mListener != null) {
            mListener.onSearchSubmitted(mSearchController.getQuery(), mQueryText.getText().toString());
        }

        showSearch(false);
    }

    private void toggleAdvanced(boolean state) {
        if(state) {
            DecelerateInterpolator interpolator = new DecelerateInterpolator();
            animate(0, interpolator);
        }
        else {
            AccelerateInterpolator interpolator = new AccelerateInterpolator();
            animate(-mFullSize + mHalfSize, interpolator);
        }
    }

    private void animate(int height, Interpolator interpolator) {
        mSearchView
                .animate()
                .translationY(height)
                .setDuration((long)  Math.abs((height - mSearchView.getTranslationY())* ANIMATION_SPEED))
                .setInterpolator(interpolator)
                .start();
    }

    private class SearchListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            submitQuery();
        }
    }

    private class AdvancedListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            toggleAdvanced(mSearchView.getTranslationY() != 0);
        }
    }

    private class ActionSubmitListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            submitQuery();
            return true;
        }
    }
}
