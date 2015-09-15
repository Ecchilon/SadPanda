package com.ecchilon.sadpanda.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.RoboAppCompatActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_overview)
public class PandaPreferenceActivity extends RoboAppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		setTitle(R.string.settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PandaPreferenceFragment())
					.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// We're not part of the app's task, so we create a new one
				TaskStackBuilder.create(this)
						.addNextIntentWithParentStack(upIntent)
						.startActivities();
			}
			else {
				// We're part of the app's task, so we navigate back (create up intent destroys existing parent
				// intent forcing a reload of the page)
				onBackPressed();
			}

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
