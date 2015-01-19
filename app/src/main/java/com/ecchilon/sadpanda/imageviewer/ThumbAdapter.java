package com.ecchilon.sadpanda.imageviewer;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.paging.listview.PagingBaseAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ThumbAdapter extends PagingBaseAdapter<ImageEntry> {

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public ImageEntry getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CroppedImageView view = (CroppedImageView) convertView;
		if (view == null) {
			view = new CroppedImageView(parent.getContext());
		}

		initThumb(view, items.get(position).getThumbEntry());

		return view;
	}

	private void initThumb(CroppedImageView thumbnail, ThumbEntry item) {
		int height = item.getHeight(), width = item.getWidth();

		if (height == -1 && width == -1) {
			height = 144;
			width = 100;
		}

		thumbnail.setBounds(item.getOffset(), 0, width, height);
		thumbnail.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		CroppedTarget target = new CroppedTarget(thumbnail);

		Picasso.with(thumbnail.getContext())
				.load(item.getUrl())
				.into(target);

		thumbnail.setTag(target);
	}

	private static class CroppedTarget implements Target {
		private final CroppedImageView target;

		private CroppedTarget(CroppedImageView target) {
			this.target = target;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			target.setImageBitmap(bitmap);
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
