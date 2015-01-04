package com.ecchilon.sadpanda.util;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.EntryView;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.squareup.picasso.Picasso;

public class ViewConstructor {
	public static final int TAG_GROUP_ID = 584029843;
	public static final String MENU_INFO_POS_KEY = "menuInfoPosKey";

	public static View createOverviewItem(GalleryEntry entry, View convertView, ViewGroup parent) {
		EntryView entryView = (EntryView) convertView;
		if (entryView == null) {
			entryView = new EntryView(parent.getContext(), R.layout.gallery_entry);
		}

		entryView.getTitleView().setText(entry.getTitle());
		entryView.setCategory(entry.getCategory());
		entryView.setTags(entry.getTags());
		entryView.setFileCount(entry.getFileCount());

		if (entryView.getTag() != null && entryView.getTag() instanceof AnimatedTarget) {
			AnimatedTarget oldTarget = (AnimatedTarget) entryView.getTag();

			if (oldTarget.getUrl().equals(entry.getThumb())) {
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

	public static void createGalleryMenu(GalleryEntry entry, Menu menu, AdapterView.AdapterContextMenuInfo info,
			Context context) {
		String uploader = context.getString(R.string.uploader);
		String tag = context.getString(R.string.tag);

		Intent posIntent = getPositionIntent(info);

		if (entry.getUploader() != null) {
			menu.add(TAG_GROUP_ID, 0, 0, uploader + " - " + entry.getUploader()).setIntent(posIntent);
		}
		String[] tags = entry.getTags();
		for (int i = 0; i < tags.length; i++) {
			menu.add(TAG_GROUP_ID, i + 1, 0, tag + " - " + tags[i]).setIntent(posIntent);
		}
	}


	public static Intent getPositionIntent(AdapterView.AdapterContextMenuInfo menuInfo) {
		Intent posIntent = new Intent();
		posIntent.putExtra(MENU_INFO_POS_KEY, menuInfo.position);
		return posIntent;
	}
}
