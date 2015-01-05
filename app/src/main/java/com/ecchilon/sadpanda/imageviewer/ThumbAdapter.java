package com.ecchilon.sadpanda.imageviewer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.paging.listview.PagingBaseAdapter;
import com.squareup.picasso.Picasso;

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
		thumbnail.setLayoutParams(new AbsListView.LayoutParams(width, height));

		Picasso.with(thumbnail.getContext())
				.load(item.getUrl())
				.into(thumbnail);
	}
}
