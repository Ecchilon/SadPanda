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
		Bundle args = new Bundle();
		args.putString(OverviewFragment.URL_KEY, String.format(FAVORITES_URL, i));
		args.putSerializable(OverviewFragment.SEARCH_TYPE_KEY, OverviewFragment.SearchType.SIMPLE);

		OverviewFragment fragment = new OverviewFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public int getCount() {
		return FAVORITES_TAB_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mFavoritesTitle  + " " + position;
	}
}
