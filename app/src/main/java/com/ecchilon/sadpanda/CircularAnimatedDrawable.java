package com.ecchilon.sadpanda;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
/**
 * Source: http://evel.io/2013/07/21/rounded-avatars-in-android/
 *
 * A Drawable that draws an oval with given {@link Bitmap}
 */
public class CircularAnimatedDrawable extends Drawable implements Runnable {
    private static final float FADE_DURATION = 200f; //ms
    private static final int STEPS = 10;

    private final Paint mPaint;
    private final RectF mRectF;
    private final int mBitmapWidth;
    private final int mBitmapHeight;

    private Drawable mPlaceholder;

    private boolean mAnimating;
    private long startTimeMillis;
    int alpha = 0xFF;

    public CircularAnimatedDrawable(Bitmap bitmap, Drawable mPlaceholder, boolean fade) {
        mRectF = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mBitmapHeight = bitmap.getHeight();
        mBitmapWidth = bitmap.getWidth();

        final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);

        if(fade) {
            this.mPlaceholder = mPlaceholder;
            startTimeMillis = SystemClock.uptimeMillis();
            mAnimating = true;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mAnimating) {
            canvas.drawOval(mRectF, mPaint);
        } else {
            float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                mAnimating = false;
                mPlaceholder = null;
                canvas.drawOval(mRectF, mPaint);
            } else {
                if (mPlaceholder != null) {
                    mPlaceholder.draw(canvas);
                }

                int partialAlpha = (int) (alpha * normalized);
                mPaint.setAlpha(partialAlpha);
                canvas.drawOval(mRectF, mPaint);
                mPaint.setAlpha(alpha);

                scheduleSelf(this, (long)(FADE_DURATION / (float)STEPS));
            }
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        mRectF.set(bounds);
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
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
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
    public void run() {
        invalidateSelf();
    }
}
