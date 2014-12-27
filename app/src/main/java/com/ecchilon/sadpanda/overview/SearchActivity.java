package com.ecchilon.sadpanda.overview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;

import com.ecchilon.sadpanda.ErrorFragment;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.ecchilon.sadpanda.search.AbstractSearchActivity;
import com.google.inject.Inject;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_main)
public class SearchActivity extends AbstractSearchActivity implements SwipeBackActivityBase {

    private SwipeBackActivityHelper mHelper;

    @Inject
    private ExhentaiAuth mExhentaiAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();

        if (savedInstanceState == null) {
            String url = getIntent().getStringExtra(OverviewFragment.URL_KEY);
            String query = getIntent().getStringExtra(OverviewFragment.QUERY_KEY);
            onSearchSubmitted(url, query);
        }
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
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

        if(mExhentaiAuth.isLoggedIn()) {
            fragment = OverviewFragment.newInstance(url);
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
