package com.ecchilon.sadpanda.data;

import java.util.List;

import android.util.Log;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.api.GalleryScroller;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class OverviewPresenter extends Presenter<OverviewView> {

	public enum State {
		LOADED,
		EMPTY,
		ERROR,
		LOADING,
		END
	}

	private final GalleryScroller galleryScroller;
	private final BehaviorSubject<State> stateSubject;

	@Getter
	@Setter
	private String query;

	@Inject
	OverviewPresenter(GalleryScroller galleryScroller) {
		this.galleryScroller = galleryScroller;
		this.stateSubject = BehaviorSubject.create();
	}

	public Observable<List<GalleryEntry>> getGalleryPages() {
		return getView().getPageEvents()
				.filter(aVoid -> canLoadMoreItems())
				.compose(bindToView())
				.scan(0, (page, event) -> page + 1)
				.flatMap(page -> {
							stateSubject.onNext(State.LOADING);
							return galleryScroller.loadPage(query, page)
									.doOnError(throwable -> stateSubject.onNext(State.ERROR))
									.retryWhen(this::onError)
									.doOnNext(galleryEntries -> updateState(page, galleryEntries));
						}
				);
	}

	public int getPageForPosition(int position) {
		return (position / DataLoader.GALLERIES_PER_PAGE) + 1;
	}

	private Observable<?> onError(Observable<? extends Throwable> observable) {
		return getView().handleError(observable.map(o -> {
			Log.e(OverviewPresenter.class.getSimpleName(), "Error loading page", o);
			return null;
		}));
	}

	private void updateState(int page, List<GalleryEntry> galleryEntries) {
		if (page == 0 && galleryEntries.isEmpty()) {
			stateSubject.onNext(State.EMPTY);
		}
		else if (!galleryScroller.isHasMoreItems()) {
			stateSubject.onNext(State.END);
		}
		else {
			stateSubject.onNext(State.LOADED);
		}
	}

	public Observable<State> getOverviewStateEvents() {
		return stateSubject.asObservable();
	}

	private boolean canLoadMoreItems() {
		return galleryScroller.isHasMoreItems() && !galleryScroller.isLoading();
	}
}
