package com.ecchilon.sadpanda.data;

import rx.Observable;

public interface OverviewView extends PresenterView {
	Observable<Void> getPageEvents();

	Observable<?> handleError(Observable<Void> observable);
}
