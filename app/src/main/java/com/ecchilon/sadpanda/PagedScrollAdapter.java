package com.ecchilon.sadpanda;

import java.util.ArrayList;
import java.util.List;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class PagedScrollAdapter<T> extends BaseAdapter implements
		OnScrollListener {

	private boolean mLoadRequested = false;
	private int mPreviousTotal = 0;
	private int mCurrentPage = 0;
    private boolean mAutoLoad = true;

	private List<T> mData;

	@Override
	public long getItemId(int position) {
		return position;
	}

	public PagedScrollAdapter() {
		mData = new ArrayList<T>();
	}

    /**
     *
     * @param autoLoad whether data should be automatically loaded (default = true)
     */
    public PagedScrollAdapter(boolean autoLoad) {
        mData = new ArrayList<T>();
        mAutoLoad = autoLoad;
    }

	@Override
	public T getItem(int position) {
		return mData.get(position);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public final void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (mLoadRequested) {
			if (totalItemCount > mPreviousTotal) {
				mLoadRequested = false;
				mPreviousTotal = totalItemCount;
			}
		}
		if (!mLoadRequested
				&& ((firstVisibleItem + visibleItemCount) > totalItemCount - 1)) {
			mLoadRequested = true;
            if(mAutoLoad) {
                loadNewDataSet();
            }
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	/***
	 * 
	 * @param notify
	 *            true if the adapter should notify its listeners after clearing
	 *            its contents
	 */
	public void clear(boolean notify) {
		if (mData.size() > 0) {
			mData.clear();

			mCurrentPage = 0;
			mPreviousTotal = 0;
			mLoadRequested = false;
			if (notify)
				notifyDataSetChanged();
		}
	}

	/**
	 * implement this function to retrieve any data you want add to the adapter.
	 * Adding the data to the adapter should be done manually.
	 */
	public abstract void loadNewDataSet();

	/**
	 * general method to return information to after loadNewDataSet() has been
	 * called.
	 */
	public void addPage(List<T> dataSet) {
		mData.addAll(dataSet);
		notifyDataSetChanged();
		mCurrentPage++;
	}

	public void goToPage(int page) {
		if (page >= 0) {
			clear(false);
			mCurrentPage = page;
			notifyDataSetChanged();
		}
	}

    public void setAutoLoad(boolean autoLoad) {
        //Set was requested but auto-load disabled
        if(mLoadRequested && !mAutoLoad && autoLoad) {
            loadNewDataSet();
        }

        mAutoLoad = autoLoad;
    }

	public int getCurrentPage() {
		return mCurrentPage;
	}
}
