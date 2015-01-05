package com.ecchilon.sadpanda.imageviewer;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * ImageView to display top-crop scale of an image view.
 *
 * @author Chris Arriola
 */
public class CroppedImageView extends ImageView {

	int mDx, mDy, mWidth, mHeight;
	boolean isInit = false;

	public CroppedImageView(Context context) {
		super(context);
		setScaleType(ScaleType.MATRIX);
	}

	public CroppedImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setScaleType(ScaleType.MATRIX);
	}

	public CroppedImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
	}

	@Override
	protected boolean setFrame(int l, int t, int r, int b) {
		if(!isInit)
			return false;

		Matrix matrix = getImageMatrix();
		matrix.setTranslate(mDx, mDy);
		setImageMatrix(matrix);

		return super.setFrame(l, t, r, b);
	}

	public void setBounds(int dx, int dy, int width, int height) {
		mDx = dx;
		mDy = dy;
		mWidth = width;
		mHeight = height;
		isInit = true;
	}
}