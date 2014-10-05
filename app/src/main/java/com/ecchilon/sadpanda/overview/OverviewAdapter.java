package com.ecchilon.sadpanda.overview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ecchilon.sadpanda.CircularAnimatedDrawable;
import com.ecchilon.sadpanda.util.PagedScrollAdapter;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.DataLoader;
import com.google.inject.Inject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import lombok.Getter;
import roboguice.RoboGuice;

/**
 * Created by Alex on 21-9-2014.
 */
public class OverviewAdapter extends PagedScrollAdapter<GalleryEntry> {

    @Inject
    private DataLoader mDataLoader;

    private final String mBaseUrl;

    public OverviewAdapter(String url, Context context) {
        this.mBaseUrl = url;

        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void loadNewDataSet() {
        final int currentPage = getCurrentPage();
        new AsyncTask<String, Void, List<GalleryEntry>>() {

            @Override
            protected List<GalleryEntry> doInBackground(String... params) {
                return mDataLoader.getGalleryIndex(params[0], currentPage);
            }

            @Override
            protected void onPostExecute(List<GalleryEntry> result) {
                super.onPostExecute(result);

                addPage(result);
            }
        }.execute(mBaseUrl);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EntryView entryView = (EntryView) convertView;
        if (entryView == null) {
            entryView = new EntryView(parent.getContext(), R.layout.gallery_entry);
        }

        GalleryEntry entry = getItem(position);

        entryView.getTitleView().setText(entry.getTitle());
        entryView.setCategory(entry.getCategory());
        entryView.setTags(entry.getTags());
        entryView.setFileCount(entry.getFileCount());


        if(entryView.getTag() != null && entryView.getTag() instanceof AnimatedTarget) {
            AnimatedTarget oldTarget = (AnimatedTarget) entryView.getTag();

            if(oldTarget.getUrl().equals(entry.getThumb())) {
                return entryView;
            }
        }

        AnimatedTarget target = new AnimatedTarget(entryView.getThumbView(), entry.getThumb());
        entryView.setTag(target);

        Picasso.with(parent.getContext())
                .load(entry.getThumb())
                .resizeDimen(R.dimen.thumb_size, R.dimen.thumb_size)
                .centerCrop()
                .placeholder(R.drawable.question)
                .into(target);

        return entryView;
    }

    private class AnimatedTarget implements Target {
        private final ImageView imageView;
        @Getter
        private final String url;
        private Drawable placeHolder;

        private AnimatedTarget(@NonNull ImageView imageView, String url) {
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
}
