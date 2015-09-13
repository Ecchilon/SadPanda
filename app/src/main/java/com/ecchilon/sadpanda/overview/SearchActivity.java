package com.ecchilon.sadpanda.overview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.ecchilon.sadpanda.ErrorFragment;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.RoboAppCompatActivity;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.search.OnSearchSubmittedListener;
import com.google.inject.Inject;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_overview)
public class SearchActivity extends RoboAppCompatActivity implements SwipeBackActivityBase, OnSearchSubmittedListener {

	public static final String QUERY_KEY = "ExhentaiQuery";

	private SwipeBackActivityHelper mHelper;

	@Inject
	private ExhentaiAuth mExhentaiAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mHelper = new SwipeBackActivityHelper(this);
		mHelper.onActivityCreate();

		//if the image viewer activity crashes the fragment gets restored correctly,
		// but the actionbar (and thus its title) doesn't. Hence we always set it
		String query = getIntent().getStringExtra(QUERY_KEY);
		getSupportActionBar().setTitle(query);

		if (savedInstanceState == null) {
			String url = getIntent().getStringExtra(OverviewFragment.URL_KEY);
			onSearchSubmitted(url, query);
		}
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v == null && mHelper != null) {
			return mHelper.findViewById(id);
		}
		return v;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				scrollToFinishActivity();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public SwipeBackLayout getSwipeBackLayout() {
		return mHelper.getSwipeBackLayout();
	}

	@Override
	public void setSwipeBackEnable(boolean enable) {
		getSwipeBackLayout().setEnableGesture(enable);
	}

	@Override
	public void scrollToFinishActivity() {
		Utils.convertActivityToTranslucent(this);
		getSwipeBackLayout().scrollToFinishActivity();
	}

	@Override
	public void onSearchSubmitted(String url, String query) {
		Fragment fragment;

		getSupportActionBar().setTitle(query);

		if (mExhentaiAuth.isLoggedIn()) {
			fragment = OverviewFragment.newInstance(url, false, query, OverviewFragment.SearchType.ADVANCED);
		}
		else {
			fragment = ErrorFragment.newInstance(R.string.login_request);
		}

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, fragment)
				.commit();
	}
}
