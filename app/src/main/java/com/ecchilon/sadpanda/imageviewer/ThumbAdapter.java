package com.ecchilon.sadpanda.imageviewer;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.data.GalleryPageCache;
import com.ecchilon.sadpanda.imageviewer.data.ImageEntry;
import com.ecchilon.sadpanda.imageviewer.data.ThumbEntry;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ThumbAdapter extends BaseAdapter {

	private final List<ImageEntry> items;
	private final GalleryPageCache galleryPageCache;
	private final GalleryEntry galleryEntry;
	private Subscription currentSubscription;

	private int mCurrentPage = 0;

	public ThumbAdapter(GalleryPageCache galleryPageCache, GalleryEntry entry) {
		this.galleryPageCache = galleryPageCache;
		items = Lists.newArrayListWithCapacity(entry.getFileCount());
		this.galleryEntry = entry;
	}

	@Override
	public int getCount() {
		return galleryEntry.getFileCount();
	}

	@Override
	public ImageEntry getItem(int position) {
		if (position >= items.size()) {
			return null;
		}

		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView view = (ImageView) convertView;
		if (view == null) {
			view = new ImageView(parent.getContext());

			int height = parent.getContext().getResources().getDimensionPixelSize(R.dimen.thumb_height);
			view.setScaleType(ImageView.ScaleType.FIT_CENTER);
			view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
		}

		ImageEntry item = getItem(position);
		if (item == null) {
			loadNewPage();
		}
		else {
			initThumb(view, items.get(position).getThumbEntry());
		}

		return view;
	}

	private void loadNewPage() {
		if (currentSubscription != null) {
			return;
		}

		currentSubscription = galleryPageCache.getGalleryPage(galleryEntry, mCurrentPage)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(imageEntries -> {
					mCurrentPage++;
					currentSubscription = null;
					items.addAll(imageEntries);
					notifyDataSetChanged();
				}, throwable -> {
					Log.e(ThumbAdapter.class.getSimpleName(), "Couldn't load image entries", throwable);
				});
	}

	private void initThumb(ImageView thumbnail, ThumbEntry item) {
		int height = item.getHeight(), width = item.getWidth();

		if (height == -1 && width == -1) {
			height = 144;
			width = 100;
		}

		CroppedTarget target =
				new CroppedTarget(thumbnail, new Rect(item.getOffset(), 0, item.getOffset() + width, height));

		Picasso.with(thumbnail.getContext())
				.load(item.getUrl())
				.tag(ThumbFragment.getPicassoTag())
				.into(target);

		thumbnail.setTag(target);
	}

	private static class CroppedTarget implements Target {
		private final ImageView target;
		private final Rect bounds;

		private CroppedTarget(ImageView target, Rect bounds) {
			this.target = target;
			this.bounds = bounds;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			target.setImageDrawable(new CroppedBitmapDrawable(bitmap, bounds));
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			target.setImageDrawable(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			target.setImageDrawable(placeHolderDrawable);
		}
	}
}
