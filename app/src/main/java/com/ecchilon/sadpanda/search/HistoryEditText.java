package com.ecchilon.sadpanda.search;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class HistoryEditText extends AutoCompleteTextView implements View.OnClickListener {
	public HistoryEditText(Context context) {
		super(context);
		init();
	}

	public HistoryEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HistoryEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HistoryEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		setOnClickListener(this);
	}

	@Override
	public boolean enoughToFilter() {
		return true;
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);

		if(focused && !isPopupShowing()) {
			showDropDown();
		}
	}

	@Override
	public void onClick(View v) {
		if(v == this && !isPopupShowing()) {
			showDropDown();
		}
	}
}
