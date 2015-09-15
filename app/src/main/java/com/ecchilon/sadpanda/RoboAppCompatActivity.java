package com.ecchilon.sadpanda;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.inject.Inject;
import com.google.inject.Key;
import roboguice.RoboGuice;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.context.event.OnConfigurationChangedEvent;
import roboguice.context.event.OnCreateEvent;
import roboguice.context.event.OnDestroyEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContentViewListener;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

public class RoboAppCompatActivity extends AppCompatActivity implements RoboContext {
	protected EventManager eventManager;
	protected HashMap<Key<?>, Object> scopedObjects = new HashMap<>();

	@Inject
	ContentViewListener ignored; // BUG find a better place to put this

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final RoboInjector injector = RoboGuice.getInjector(this);
		eventManager = injector.getInstance(EventManager.class);
		injector.injectMembersWithoutViews(this);
		super.onCreate(savedInstanceState);
		eventManager.fire(new OnCreateEvent<Activity>(this, savedInstanceState));
	}

	@Override
	protected void onResume() {
		super.onResume();
		eventManager.fire(new OnResumeEvent(this));
	}

	@Override
	protected void onPause() {
		super.onPause();
		eventManager.fire(new OnPauseEvent(this));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		eventManager.fire(new OnNewIntentEvent(this));
	}

	@Override
	protected void onStop() {
		try {
			eventManager.fire(new OnStopEvent(this));
		}
		finally {
			super.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		try {
			eventManager.fire(new OnDestroyEvent<Activity>(this));
		}
		finally {
			try {
				RoboGuice.destroyInjector(this);
			}
			finally {
				super.onDestroy();
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		RoboGuice.getInjector(this).injectViewMembers(this);
		eventManager.fire(new OnContentChangedEvent(this));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		final Configuration currentConfig = getResources().getConfiguration();
		super.onConfigurationChanged(newConfig);
		eventManager.fire(new OnConfigurationChangedEvent<Activity>(this, currentConfig, newConfig));
	}

	@Override
	public Map<Key<?>, Object> getScopedObjectMap() {
		return scopedObjects;
	}
}
