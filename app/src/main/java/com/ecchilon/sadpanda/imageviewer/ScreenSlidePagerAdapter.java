package com.ecchilon.sadpanda.imageviewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.ecchilon.sadpanda.overview.GalleryEntry;

/**
 * Created by Alex on 1/22/14.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

	private final ImageLoader loader;
	private final GalleryEntry entry;
	private final Bundle arguments;

	public ScreenSlidePagerAdapter(FragmentManager fm, ImageLoader loader, GalleryEntry entry, Bundle arguments) {
		super(fm);

		this.loader = loader;
		this.entry = entry;
		this.arguments = arguments;
	}

	@Override
	public Fragment getItem(int position) {
		ScreenSlidePageFragment frag = new ScreenSlidePageFragment();
		frag.setArguments(arguments);

		loader.getImage(position + 1, frag);
		return frag;
	}

	@Override
	public int getCount() {
		return entry.getFileCount();
	}
}
