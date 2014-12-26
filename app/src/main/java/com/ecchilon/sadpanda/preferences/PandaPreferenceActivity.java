package com.ecchilon.sadpanda.preferences;

import android.os.Bundle;
import com.ecchilon.sadpanda.R;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_panda_preference)
public class PandaPreferenceActivity extends RoboActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PandaPreferenceFragment())
					.commit();
		}
	}

}
