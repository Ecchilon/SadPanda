package com.ecchilon.sadpanda;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.auth.LoginFragment;
import com.ecchilon.sadpanda.favorites.FavoritesFragment;
import com.ecchilon.sadpanda.overview.OverviewFragment;
import com.ecchilon.sadpanda.overview.OverviewFragment.PageContainer;
import com.ecchilon.sadpanda.overview.SearchActivity;
import com.ecchilon.sadpanda.preferences.PandaPreferenceActivity;
import com.ecchilon.sadpanda.search.OnSearchSubmittedListener;
import com.ecchilon.sadpanda.util.Nullable;
import com.google.inject.Inject;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import roboguice.inject.InjectView;

public class MainActivity extends RoboAppCompatActivity implements LoginFragment.LoginListener,
		OnSearchSubmittedListener, TabContainer, PageContainer {

	private static final String DEFAULT_QUERY_KEY = "defaultQueryKey";
	private static final String DEFAULT_QUERY_URL = "http://exhentai.org";

	private static final long TAB_SHOW_DELAY = 150;

	@Inject
	private ExhentaiAuth mAuth;

	@Inject
	private SharedPreferences mPreferences;

	private LoginFragment mLoginFragment;

	private final Handler tabHandler = new Handler();

	@Nullable
	@InjectView(R.id.tabs)
	private TabLayout tabs;

	private BottomBar bottomBar;
	private boolean initialLoad = true;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!mAuth.isLoggedIn()) {
			showLoginFragment();
		}
		else {
			showMainContent(savedInstanceState);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.logout:
				setContentView(new View(this)); //because inserting null produces an NPE
				mAuth.logout();
				showLoginFragment();
				return true;
			case R.id.settings:
				openPreferences();
				return true;
		}
		return super.onOptionsItemSelected(item);
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

	private void showFavorites(boolean animateTabs) {
		if (animateTabs) {
			tabHandler.postDelayed(() -> tabs.setVisibility(VISIBLE), TAB_SHOW_DELAY);
		}
		else {
			tabs.setVisibility(VISIBLE);
		}

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.container, new FavoritesFragment())
				.commit();
	}

	private void showFrontPage(boolean animateTabs) {
		if (animateTabs) {
			tabHandler.postDelayed(() -> tabs.setVisibility(GONE), TAB_SHOW_DELAY);
		}
		else {
			tabs.setVisibility(GONE);
		}
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.container, OverviewFragment.newInstance(getDefaultQuery(), null, null,
						OverviewFragment.SearchType.ADVANCED))
				.commit();
	}

	private String getDefaultQuery() {
		return mPreferences.getString(DEFAULT_QUERY_KEY, DEFAULT_QUERY_URL);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (bottomBar != null) {
			bottomBar.onSaveInstanceState(outState);
		}
	}

	@Override
	public void onSuccess() {
		closeLoginFragment();
		showMainContent(null);
	}

	@Override
	public void onDismiss() {
		finish();
	}

	private void showMainContent(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		bottomBar =
				BottomBar.attachShy((CoordinatorLayout) findViewById(R.id.main_content), findViewById(R.id.container),
						savedInstanceState);

		bottomBar.useDarkTheme();
		bottomBar.noTabletGoodness();
		bottomBar.setActiveTabColor(getResources().getColor(R.color.white));

		bottomBar.setItemsFromMenu(R.menu.bottom_nav, new OnMenuTabClickListener() {
			@Override
			public void onMenuTabSelected(@IdRes int menuItemId) {
				showItem(menuItemId, !initialLoad);
				initialLoad = false;
			}

			@Override
			public void onMenuTabReSelected(@IdRes int menuItemId) {
				Fragment active = getSupportFragmentManager().findFragmentById(R.id.container);
				if (active instanceof Refreshable) {
					((Refreshable) active).refresh();
				}
			}
		});
	}

	private void showItem(@IdRes int menuItemId, boolean animate) {
		switch (menuItemId) {
			case R.id.front_page:
				setTitle(R.string.front_page);
				showFrontPage(animate);
				break;
			case R.id.favorites:
				setTitle(R.string.favorites);
				getSupportActionBar().setSubtitle(null);
				showFavorites(animate);
				break;
		}
	}

	@Override
	public void onSearchSubmitted(String url, String query) {
		startActivity(SearchActivity.newInstance(this, query, url));
	}

	@Override
	public TabLayout getTabs() {
		return tabs;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onPage(int page) {
		getSupportActionBar().setSubtitle(String.format(getString(R.string.current_page), page));
	}
}
