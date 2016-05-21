package com.ecchilon.sadpanda;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;
import roboguice.fragment.RoboFragment;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class RxRoboFragment extends RoboFragment {

	private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

	public final Observable<ActivityEvent> lifecycle() {
		return this.lifecycleSubject.asObservable();
	}

	public final <T> Observable.Transformer<T, T> bindUntilEvent(ActivityEvent event) {
		return RxLifecycle.bindUntilActivityEvent(this.lifecycleSubject, event);
	}

	public final <T> Observable.Transformer<T, T> bindToLifecycle() {
		return RxLifecycle.bindActivity(this.lifecycleSubject);
	}

	@CallSuper
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.lifecycleSubject.onNext(ActivityEvent.CREATE);
	}

	@CallSuper
	public void onStart() {
		super.onStart();
		this.lifecycleSubject.onNext(ActivityEvent.START);
	}

	@CallSuper
	public void onResume() {
		super.onResume();
		this.lifecycleSubject.onNext(ActivityEvent.RESUME);
	}

	@CallSuper
	public void onPause() {
		this.lifecycleSubject.onNext(ActivityEvent.PAUSE);
		super.onPause();
	}

	@CallSuper
	public void onStop() {
		this.lifecycleSubject.onNext(ActivityEvent.STOP);
		super.onStop();
	}

	@CallSuper
	public void onDestroy() {
		this.lifecycleSubject.onNext(ActivityEvent.DESTROY);
		super.onDestroy();
	}
}

