package com.ecchilon.sadpanda.favorites;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.TabContainer;
import org.apache.commons.lang.IllegalClassException;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class FavoritesFragment extends RoboFragment {

	@InjectView(R.id.pager)
	private ViewPager mFavoritesPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_favorites, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		FavoritesPagerAdapter adapter =
				new FavoritesPagerAdapter(getChildFragmentManager(), getString(R.string.favorites),
						getString(R.string.favorites_all));

		mFavoritesPager.setAdapter(adapter);

		try {
			((TabContainer) getActivity()).getTabs().setupWithViewPager(mFavoritesPager);
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("parent activity should implement " + TabContainer.class.getSimpleName());
		}
	}
}
