package com.ecchilon.sadpanda.overview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.ecchilon.sadpanda.ErrorFragment;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.SwipeBackRoboActivity;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.Inject;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_search)
public class SearchActivity extends SwipeBackRoboActivity {

    @Inject
    private ExhentaiAuth mExhentaiAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Fragment fragment;

            String query = getIntent().getStringExtra(OverviewFragment.QUERY_KEY);
            getActionBar().setTitle(query);

            if(mExhentaiAuth.isLoggedIn()) {
                fragment = OverviewFragment.newInstance(query);
            }
            else {
                fragment = ErrorFragment.newInstance(R.string.login_request);
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.search_fragment, fragment)
                    .commit();
        }
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
}
