package com.ecchilon.sadpanda.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.GalleryEntry;

public class AddFavoriteCallback implements AsyncResultTask.Callback<GalleryEntry> {

	private final Context mContext;

	public AddFavoriteCallback(Context context) {
		this.mContext = context.getApplicationContext();
	}

	@Override
	public void onSuccess(GalleryEntry result) {
		Toast.makeText(mContext, R.string.favorite_added, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onError(Exception e) {
		Toast.makeText(mContext, R.string.favorite_added_failed, Toast.LENGTH_SHORT).show();
		Log.e("AddFavoriteCallback", "Failed to add favorite", e);
	}
}
