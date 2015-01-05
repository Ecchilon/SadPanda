package com.paging.listview;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;

public class PagingGridView extends GridView implements PagingView {

	private boolean isLoading;
	private boolean hasMoreItems;
	private Pagingable pagingableListener;

	private OnScrollListener onScrollListener;

	public PagingGridView(Context context) {
		super(context);
		init();
	}

	public PagingGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PagingGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@Override
	public boolean isLoading() {
		return isLoading;
	}

	@Override
	public void setIsLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

	@Override
	public void setPagingableListener(Pagingable pagingableListener) {
		this.pagingableListener = pagingableListener;
	}

	@Override
	public void setHasMoreItems(boolean hasMoreItems) {
		this.hasMoreItems = hasMoreItems;
	}

	@Override
	public boolean hasMoreItems() {
		return this.hasMoreItems;
	}

	@Override
	public void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems) {
		setHasMoreItems(hasMoreItems);
		setIsLoading(false);
		if(newItems != null && newItems.size() > 0) {
			ListAdapter adapter = getAdapter();
			if(adapter instanceof PagingBaseAdapter ) {
				((PagingBaseAdapter)adapter).addMoreItems(newItems);
			}
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		this.onScrollListener = l;
	}

	private void init() {
		isLoading = false;
		super.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//Dispatch to child OnScrollListener
				if (onScrollListener != null) {
					onScrollListener.onScrollStateChanged(view, scrollState);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				//Dispatch to child OnScrollListener
				if (onScrollListener != null) {
					onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				}

				int lastVisibleItem = firstVisibleItem + visibleItemCount;
				if (!isLoading && hasMoreItems && (lastVisibleItem == totalItemCount)) {
					if (pagingableListener != null) {
						isLoading = true;
						pagingableListener.onLoadMoreItems();
					}

				}
			}
		});
	}
}
