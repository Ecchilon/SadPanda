package com.ecchilon.sadpanda.overview;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import com.ecchilon.sadpanda.PagedScrollAdapter;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.api.DataLoader;
import com.google.inject.Inject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;

import java.util.List;

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
            entryView = new EntryView(parent.getContext());
        }

        GalleryEntry entry = getItem(position);

        entryView.getTitleView().setText(entry.getTitle());

        Picasso.with(parent.getContext())
                .load(entry.getThumb())
                .resizeDimen(R.dimen.thumb_size, R.dimen.thumb_size)
                .centerCrop()
                .placeholder(R.drawable.question)
                .into(entryView.getThumbView());

        return entryView;
    }
}
