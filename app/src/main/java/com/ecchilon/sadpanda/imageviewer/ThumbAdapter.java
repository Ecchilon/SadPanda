package com.ecchilon.sadpanda.imageviewer;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.ecchilon.sadpanda.PageLoadTaskFactory;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.imageviewer.data.ImageEntry;
import com.ecchilon.sadpanda.imageviewer.data.ThumbEntry;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ThumbAdapter extends BaseAdapter {

	private final List<ImageEntry> items;
	private final PageLoadTaskFactory mPageTaskFactory;
	private final GalleryEntry mGalleryEntry;

	private PageLoadTaskFactory.PageLoadTask mCurrentTask;
	private int mCurrentPage = 0;

	public ThumbAdapter(PageLoadTaskFactory taskFactory, GalleryEntry entry) {
		items = Lists.newArrayListWithCapacity(entry.getFileCount());
		mPageTaskFactory = taskFactory;
		mGalleryEntry = entry;
	}

	@Override
	public int getCount() {
		return mGalleryEntry.getFileCount();
	}

	@Override
	public ImageEntry getItem(int position) {
		if(position >= items.size()) {
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
		if(item == null) {
			loadNewPage();
		}
		else {
			initThumb(view, items.get(position).getThumbEntry());
		}

		return view;
	}

	private void loadNewPage() {
		if(mCurrentTask != null) {
			 return;
		}

		mCurrentTask = mPageTaskFactory.create(mGalleryEntry, mCurrentPage);
		mCurrentTask.setListener(new AsyncResultTask.Callback<List<ImageEntry>>() {
			@Override
			public void onSuccess(List<ImageEntry> result) {
				mCurrentPage++;
				mCurrentTask = null;
				items.addAll(result);
				notifyDataSetChanged();
			}

			@Override
			public void onError(Exception e) {

			}
		}).execute();
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
