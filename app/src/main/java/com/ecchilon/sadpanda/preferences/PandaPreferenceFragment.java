package com.ecchilon.sadpanda.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.ecchilon.sadpanda.R;

public class PandaPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
}
