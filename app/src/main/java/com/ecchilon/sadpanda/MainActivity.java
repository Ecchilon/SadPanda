package com.ecchilon.sadpanda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.auth.LoginFragment;
import com.ecchilon.sadpanda.favorites.FavoritesFragment;
import com.ecchilon.sadpanda.overview.OverviewFragment;
import com.ecchilon.sadpanda.overview.SearchActivity;
import com.ecchilon.sadpanda.preferences.PandaPreferenceActivity;
import com.ecchilon.sadpanda.search.OnSearchSubmittedListener;
import com.google.inject.Inject;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActionBarActivity implements LoginFragment.LoginListener,
		NavigationDrawerFragment.NavigationDrawerCallbacks, OnSearchSubmittedListener {

	public static final String DEFAULT_QUERY_KEY = "defaultQueryKey";
	private static final String DEFAULT_QUERY_URL = "http://exhentai.org";

	private static final String OVERVIEW_TAG = "PandaOverviewTag";
	private static final String FAVORITES_TAG = "PandaFavoritesTag";

	@Inject
	private ExhentaiAuth mAuth;

	@Inject
	private SharedPreferences mPreferences;

	private LoginFragment mLoginFragment;

	private NavigationDrawerFragment mNavigationDrawerFragment;

	private CharSequence mTitle;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.front_page);

		if (savedInstanceState == null) {
			if (mAuth.isLoggedIn()) {
				showOverviewFragment();
			}
			else {
				showErrorFragment();
				showLoginFragment();
			}
		}

		mNavigationDrawerFragment = (NavigationDrawerFragment)
				getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return super.onCreateOptionsMenu(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem login = menu.findItem(R.id.login_menu);

		if (login == null) {
			return true;
		}

		if (mAuth.isLoggedIn()) {
			login.setTitle(R.string.logout_menu);
		}
		else {
			login.setTitle(R.string.login_menu);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.login_menu:
				showLoginFragment();
				return true;
			case R.id.preferences:
				openPreferences();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void openPreferences() {
		Intent preferences = new Intent(this, PandaPreferenceActivity.class);
		startActivity(preferences);
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	private void showLoginFragment() {
		if (mLoginFragment != null) {
			return;
		}

		mLoginFragment = new LoginFragment();
		mLoginFragment.show(getSupportFragmentManager(), "LOGIN");
	}

	private void closeLoginFragment() {
		mLoginFragment.dismiss();
		mLoginFragment = null;
	}

	private void showOverviewFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();

		OverviewFragment fragment = (OverviewFragment) fragmentManager.findFragmentByTag(OVERVIEW_TAG);
		if(fragment == null) {
			fragment = OverviewFragment.newInstance(getDefaultQuery(), false, null, OverviewFragment.SearchType.ADVANCED);
		}

		fragmentManager.beginTransaction()
				.replace(R.id.container, fragment, OVERVIEW_TAG)
				.commit();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		switch (position) {
			case 0:
				showOverviewFragment();
				break;
			case 1:
				FragmentManager fragmentManager = getSupportFragmentManager();

				FavoritesFragment fragment = (FavoritesFragment) fragmentManager.findFragmentByTag(FAVORITES_TAG);
				if(fragment == null) {
					fragment = new FavoritesFragment();
				}

				fragmentManager.beginTransaction()
						.replace(R.id.container, fragment, FAVORITES_TAG)
						.commit();
				break;
		}
	}

	private String getDefaultQuery() {
		return mPreferences.getString(DEFAULT_QUERY_KEY, DEFAULT_QUERY_URL);
	}

	private void showErrorFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, ErrorFragment.newInstance(R.string.login_request))
				.commit();
	}

	@Override
	public void onSuccess() {
		closeLoginFragment();
		showOverviewFragment();
	}

	@Override
	public void onSearchSubmitted(String url, String query) {
		Intent searchIntent = new Intent(this, SearchActivity.class);
		searchIntent.putExtra(OverviewFragment.URL_KEY, url);
		searchIntent.putExtra(SearchActivity.QUERY_KEY, query);

		startActivity(searchIntent);
	}
}
