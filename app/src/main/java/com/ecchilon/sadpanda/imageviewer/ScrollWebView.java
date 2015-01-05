package com.ecchilon.sadpanda.imageviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ScrollWebView extends WebView {
	public ScrollWebView(Context context) {
		super(context);
	}

	public ScrollWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public boolean canScrollHor(int direction) {
		final int offset = computeHorizontalScrollOffset();
		final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
		if (range == 0) return false;
		if (direction < 0) {
			return offset > 0;
		} else {
			return offset < range - 1;
		}
	}
}