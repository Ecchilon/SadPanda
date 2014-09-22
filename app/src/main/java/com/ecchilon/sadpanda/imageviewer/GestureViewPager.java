package com.ecchilon.sadpanda.imageviewer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Alex on 1/24/14.
 */
public class GestureViewPager extends ViewPager {
	private GestureDetector mDetector;

    public void setGestureDetector(GestureDetector detector) { mDetector = detector; }

    public GestureViewPager(Context context) {
        super(context);
    }

    public GestureViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mDetector != null)
            mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
