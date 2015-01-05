package com.paging.listview;

import java.util.List;

import android.widget.AbsListView;

public interface PagingView {

	public interface Pagingable {
		void onLoadMoreItems();
	}

	boolean isLoading();

	void setIsLoading(boolean isLoading);

	void setPagingableListener(Pagingable pagingableListener);

	void setHasMoreItems(boolean hasMoreItems);

	boolean hasMoreItems();

	void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems);

	void setOnScrollListener(AbsListView.OnScrollListener listener);
}
