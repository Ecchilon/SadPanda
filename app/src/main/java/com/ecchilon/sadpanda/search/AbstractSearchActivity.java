package com.ecchilon.sadpanda.search;

import android.view.Menu;
import android.view.MenuItem;

import com.ecchilon.sadpanda.R;

import com.ecchilon.sadpanda.RoboActionBarActivity;
import roboguice.activity.RoboFragmentActivity;

/**
 * Created by Alex on 11-10-2014.
 */
public abstract class AbstractSearchActivity extends RoboActionBarActivity implements OnSearchSubmittedListener {

    private SearchDialogFragment fragment = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                showSearchFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSearchFragment() {
        SearchDialogFragment fragment = new SearchDialogFragment();
        fragment.show(getSupportFragmentManager(), "Search");
    }
}
