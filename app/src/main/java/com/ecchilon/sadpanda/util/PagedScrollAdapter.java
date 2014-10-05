package com.ecchilon.sadpanda.util;

import java.util.ArrayList;
import java.util.List;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

import lombok.Getter;

public abstract class PagedScrollAdapter<T> extends BaseAdapter implements
		OnScrollListener {

    private static final int DEFAULT_MAX_RELOADS = 3;

	private boolean mLoadRequested = false;
	private int mPreviousTotal = 0;
	private int mCurrentPage = 0;
    private boolean mAutoLoad = true;

    private int mMaxReloads = DEFAULT_MAX_RELOADS;
    private int mReloadCount = 0;

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

    public void setMaxReloadAttempts(int attempts) {
        if(attempts < 0) {
            throw new IllegalArgumentException("Number of reload attempts can't be smaller than 0!");
        }

        mMaxReloads = attempts;
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

        if(mReloadCount > mMaxReloads) {
            return;
        }

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
            mReloadCount = 0;
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
        if(dataSet.size() == 0) {
            mReloadCount++;
        }
        else {
            mReloadCount = 0;
        }

		mData.addAll(dataSet);
		notifyDataSetChanged();
		mCurrentPage++;
	}

    /**
     * @param page The page to start loading new data from
     */
	public void startFromPage(int page) {
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
