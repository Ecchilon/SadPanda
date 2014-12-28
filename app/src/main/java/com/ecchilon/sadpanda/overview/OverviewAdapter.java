package com.ecchilon.sadpanda.overview;

import static com.ecchilon.sadpanda.util.ViewConstructor.createOverviewItem;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.util.PagedScrollAdapter;
import com.google.inject.Inject;
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
		return createOverviewItem(getItem(position), convertView, parent);
	}
}
