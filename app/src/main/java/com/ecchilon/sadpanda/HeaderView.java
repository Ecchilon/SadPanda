package com.ecchilon.sadpanda;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ecchilon.sadpanda.auth.ExhentaiAuth;
import com.google.inject.Inject;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import roboguice.inject.RoboInjector;

public class HeaderView extends RelativeLayout {

	@Inject
	private ExhentaiAuth auth;

	@InjectView(R.id.username)
	private TextView userName;

	public HeaderView(Context context) {
		super(context);
		init();
	}

	public HeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		if(!isInEditMode()) {
			RoboInjector injector = RoboGuice.getInjector(getContext());
			injector.injectMembersWithoutViews(this);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		if(!isInEditMode()) {
			RoboInjector injector = RoboGuice.getInjector(getContext());
			injector.injectViewMembers(this);
			userName.setText(auth.getUserName());
		}
		else {
			userName = (TextView) findViewById(R.id.username);
			userName.setText("SadPanda user");
		}
	}
}
