package com.ecchilon.sadpanda.favorites;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.ecchilon.sadpanda.overview.OverviewFragment;

public class FavoritesPagerAdapter extends FragmentStatePagerAdapter {
	public static final int FAVORITES_TAB_COUNT = 11;
	private static final String BASE_FAVORITES_URL = "http://exhentai.org/favorites.php";
	private static final String FAVORITES_CAT_URL = BASE_FAVORITES_URL + "?favcat=%d";

	private final String mFavoritesTitle;
	private final String mAllFavoritesTitle;

	public FavoritesPagerAdapter(FragmentManager fm, String favoritesCategoryTitle, String allFavoritesTitle) {
		super(fm);
		this.mFavoritesTitle = favoritesCategoryTitle;
		this.mAllFavoritesTitle = allFavoritesTitle;
	}

	@Override
	public Fragment getItem(int i) {
		String url;
		if (i == 0) {
			url = BASE_FAVORITES_URL;
		}
		else {
			url = String.format(FAVORITES_CAT_URL, i - 1);
		}
		return OverviewFragment.newInstance(url, i - 1, null,
				OverviewFragment.SearchType.SIMPLE);
	}

	@Override
	public int getCount() {
		return FAVORITES_TAB_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if(position == 0) {
			return mAllFavoritesTitle;
		}
		else {
			return mFavoritesTitle + " " + (position - 1);
		}
	}
}
