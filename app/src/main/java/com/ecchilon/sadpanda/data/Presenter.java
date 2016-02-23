package com.ecchilon.sadpanda.data;

import lombok.AccessLevel;
import lombok.Getter;
import rx.Observable;

public abstract class Presenter<T extends PresenterView> {

	@Getter(AccessLevel.PROTECTED)
	private T view;

	public void attach(T view) {
		this.view = view;
	}

	public void detach(T view) {
		if (this.view == view) {
			this.view = null;
		}
	}

	protected <X> Observable.Transformer<X, X> bindToView() {
		return xObservable -> xObservable.filter(x -> view != null);
	}
}
