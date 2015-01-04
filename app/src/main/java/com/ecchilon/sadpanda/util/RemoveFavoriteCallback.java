package com.ecchilon.sadpanda.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.GalleryEntry;

public class RemoveFavoriteCallback implements AsyncResultTask.Callback<GalleryEntry> {
	private final Context mContext;

	public RemoveFavoriteCallback(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public void onSuccess(GalleryEntry result) {
		Toast.makeText(mContext, R.string.favorite_removed, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onError(Exception e) {
		Toast.makeText(mContext, R.string.favorite_removed_failed, Toast.LENGTH_SHORT).show();
		Log.e("RemoveFavoriteCallback", "Failed to remove favorite", e);
	}
}
