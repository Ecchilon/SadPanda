package com.ecchilon.sadpanda.search;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.overview.OverviewFragment;

import roboguice.activity.RoboFragmentActivity;

/**
 * Created by Alex on 11-10-2014.
 */
public abstract class AbstractSearchActivity extends RoboFragmentActivity implements SearchDrawer.OnSearchSubmittedListener {

    private SearchDrawer mSearchDrawer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View mSearchView = findViewById(R.id.search_layout);
        View contentView = findViewById(android.R.id.content);

        mSearchDrawer = new SearchDrawer(mSearchView, false);
        contentView.post(new Runnable()
        {
            public void run()
            {
                mSearchDrawer.setupViews();
                mSearchDrawer.setSearchListener(AbstractSearchActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                mSearchDrawer.showSearch(!mSearchDrawer.isShowing());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected String getQuery() {
        return mSearchDrawer.getQuery();
    }
}
