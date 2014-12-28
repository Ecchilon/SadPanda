package com.ecchilon.sadpanda.util;

import android.view.View;
import android.view.ViewGroup;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.EntryView;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.squareup.picasso.Picasso;

public class ViewConstructor {
	public static View createOverviewItem(GalleryEntry entry, View convertView, ViewGroup parent) {
		EntryView entryView = (EntryView) convertView;
		if (entryView == null) {
			entryView = new EntryView(parent.getContext(), R.layout.gallery_entry);
		}

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
}
