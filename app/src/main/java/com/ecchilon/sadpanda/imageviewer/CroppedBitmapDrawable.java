package com.ecchilon.sadpanda.imageviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class CroppedBitmapDrawable extends Drawable {

	private RectF mDestRect = new RectF();
	private Rect mSrcRect = new Rect();
	private Bitmap mBitmap;
	private Paint mPaint;

	public CroppedBitmapDrawable(Bitmap bitmap, Rect bounds) {
		mBitmap = bitmap;
		mSrcRect.set(bounds);

		initShader();
	}

	public CroppedBitmapDrawable(Bitmap bitmap, int l, int t, int r, int b) {
		mBitmap = bitmap;
		mSrcRect.set(l, t, r, b);

		initShader();
	}

	public void setCroppedBounds(int l, int t, int r, int b) {
		mSrcRect.set(l, t, r, b);
		invalidateSelf();
	}

	public void setCroppedBounds(Rect bounds) {
		mSrcRect.set(bounds);
		invalidateSelf();
	}

	private void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
		initShader();
	}

	private void initShader() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);

		final BitmapShader shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);
	}

	@Override
	public void setAlpha(int alpha) {
		if (mPaint.getAlpha() != alpha) {
			mPaint.setAlpha(alpha);
			invalidateSelf();
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getIntrinsicWidth() {
		return mSrcRect.width();
	}

	@Override
	public int getIntrinsicHeight() {
		return mSrcRect.height();
	}

	@Override
	public int getOpacity() {
		Bitmap bm = mBitmap;
		return (bm == null || bm.hasAlpha() || mPaint.getAlpha() < 255) ?
			PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
	}

	@Override
	public void draw(Canvas canvas) {
		if(mBitmap != null) {
			canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, mPaint);
		}
	}

	public void setAntiAlias(boolean aa) {
		mPaint.setAntiAlias(aa);
		invalidateSelf();
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		mPaint.setFilterBitmap(filter);
		invalidateSelf();
	}

	@Override
	public void setDither(boolean dither) {
		mPaint.setDither(dither);
		invalidateSelf();
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		mDestRect.set(bounds);
	}
}
