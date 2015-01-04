package com.ecchilon.sadpanda.favorites;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.ecchilon.sadpanda.overview.OverviewFragment;

public class FavoritesPagerAdapter extends FragmentStatePagerAdapter {
	public static final int FAVORITES_TAB_COUNT = 10;
	private static final String FAVORITES_URL = "http://exhentai.org/favorites.php?favcat=%d";

	private final String mFavoritesTitle;

	public FavoritesPagerAdapter(FragmentManager fm, String favoritesTitle) {
		super(fm);
		this.mFavoritesTitle = favoritesTitle;
	}

	@Override
	public Fragment getItem(int i) {
		return OverviewFragment.newInstance(String.format(FAVORITES_URL, i), true, null,
				OverviewFragment.SearchType.SIMPLE);
	}

	@Override
	public int getCount() {
		return FAVORITES_TAB_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mFavoritesTitle + " " + position;
	}
}
