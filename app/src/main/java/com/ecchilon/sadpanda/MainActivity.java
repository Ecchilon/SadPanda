package com.ecchilon.sadpanda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.auth.LoginFragment;
import com.ecchilon.sadpanda.favorites.FavoritesFragment;
import com.ecchilon.sadpanda.overview.OverviewFragment;
import com.ecchilon.sadpanda.overview.SearchActivity;
import com.ecchilon.sadpanda.preferences.PandaPreferenceActivity;
import com.ecchilon.sadpanda.search.OnSearchSubmittedListener;
import com.ecchilon.sadpanda.util.Nullable;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

public class MainActivity extends RoboAppCompatActivity implements LoginFragment.LoginListener,
		OnSearchSubmittedListener, NavigationView.OnNavigationItemSelectedListener, TabContainer {

	public static final String DEFAULT_QUERY_KEY = "defaultQueryKey";
	private static final String DEFAULT_QUERY_URL = "http://exhentai.org";

	private static final String OVERVIEW_TAG = "PandaOverviewTag";
	private static final String FAVORITES_TAG = "PandaFavoritesTag";

	private static final long DRAWER_CLOSE_DELAY_MS = 250;
	private static final String NAV_ITEM_ID = "navItemId";

	@Inject
	private ExhentaiAuth mAuth;

	@Inject
	private SharedPreferences mPreferences;

	private LoginFragment mLoginFragment;

	private final Handler mDrawerActionHandler = new Handler();

	@Nullable
	@InjectView(R.id.drawer_layout)
	private DrawerLayout mDrawerLayout;

	@Nullable
	@InjectView(R.id.tabs)
	private TabLayout tabs;

	private ActionBarDrawerToggle mDrawerToggle;
	private int mNavItemId;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (null == savedInstanceState) {
			mNavItemId = R.id.front_page;
		}
		else {
			mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
		}

		if (!mAuth.isLoggedIn()) {
			showLoginFragment();
		}
		else {
			showMainContent();
		}
	}

	private void navigate(int navItemId) {
		switch (navItemId) {
			case R.id.front_page:
				tabs.setVisibility(View.GONE);
				showOverviewFragment();
				break;
			case R.id.favorites:
				tabs.setVisibility(View.VISIBLE);
				FragmentManager fragmentManager = getSupportFragmentManager();

				FavoritesFragment fragment = (FavoritesFragment) fragmentManager.findFragmentByTag(FAVORITES_TAG);
				if (fragment == null) {
					fragment = new FavoritesFragment();
				}

				fragmentManager.beginTransaction()
						.replace(R.id.container, fragment, FAVORITES_TAG)
						.commit();
				break;
		}
	}

	private void openPreferences() {
		Intent preferences = new Intent(this, PandaPreferenceActivity.class);
		startActivity(preferences);
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
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, OverviewFragment.newInstance(getDefaultQuery(), null, null,
						OverviewFragment.SearchType.ADVANCED))
				.commit();
	}

	private String getDefaultQuery() {
		return mPreferences.getString(DEFAULT_QUERY_KEY, DEFAULT_QUERY_URL);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		int id = menuItem.getItemId();

		if (id == mNavItemId) {
			return true;
		}

		if (id == R.id.settings) {
			openPreferences();
		}
		else if (id == R.id.logout) {
			setContentView(new View(this)); //because inserting null produces an NPE
			mAuth.logout();
			showLoginFragment();
		}
		else {
			menuItem.setChecked(true);
			mNavItemId = menuItem.getItemId();

			mDrawerLayout.closeDrawer(GravityCompat.START);
			mDrawerActionHandler.postDelayed(() -> navigate(mNavItemId), DRAWER_CLOSE_DELAY_MS);
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
			return mDrawerToggle.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(NAV_ITEM_ID, mNavItemId);
	}

	@Override
	public void onSuccess() {
		closeLoginFragment();
		showMainContent();
	}

	@Override
	public void onDismiss() {
		finish();
	}

	private void showMainContent() {
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle(R.string.front_page);

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_drawer);
		navigationView.setNavigationItemSelectedListener(this);

		navigationView.getMenu().findItem(mNavItemId).setChecked(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open,
				R.string.close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		navigate(mNavItemId);
	}

	@Override
	public void onSearchSubmitted(String url, String query) {
		startActivity(SearchActivity.newInstance(this, query, url));
	}
	@Override
	public TabLayout getTabs() {
		if(tabs.getVisibility() == View.GONE) {
			throw new IllegalStateException("Tabs are not available right now!");
		}

		return tabs;
	}
}
