package com.ecchilon.sadpanda.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import com.ecchilon.sadpanda.CircularAnimatedDrawable;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import lombok.Getter;

public class AnimatedTarget implements Target {
    private final ImageView imageView;
    @Getter
    private final String url;
    private Drawable placeHolder;

    public AnimatedTarget(@NonNull ImageView imageView, String url) {
        this.imageView = imageView;
        this.url = url;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        imageView.setImageDrawable(new CircularAnimatedDrawable(bitmap, placeHolder, from != Picasso.LoadedFrom.MEMORY));
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        imageView.setImageDrawable(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        placeHolder = placeHolderDrawable;
        imageView.setImageDrawable(placeHolder);
    }
}
