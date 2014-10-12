package com.ecchilon.sadpanda.util;

import java.util.ArrayList;
import java.util.List;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

import lombok.Getter;

public abstract class PagedScrollAdapter<T> extends BaseAdapter implements
		OnScrollListener {

    public interface PageLoadListener{
        public void onPageLoadStart(int page);
        public void onPageLoadEnd(int page);
    }

    public static final int DEFAULT_MAX_RELOADS = 3;

	private boolean mLoadRequested = false;
	private int mPreviousTotal = 0;
	private int mCurrentPage = 0;
    private boolean mAutoLoad = true;

    private int mMaxReloads = DEFAULT_MAX_RELOADS;
    private int mReloadCount = 0;

    private PageLoadListener mListener;

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
                loadNewData();
            }
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

    public void setPageLoadListener(PageLoadListener listener) {
        mListener = listener;
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
	 * Adding the data to the adapter should be done manually by calling {@link #addPage}.
	 */
	protected abstract void loadNewDataSet();

    /**
     * Triggers the load of new data based on the current page. Notifies any attached listeners.
     */
    public void loadNewData() {
        if(mListener != null) {
            mListener.onPageLoadStart(mCurrentPage);
        }

        loadNewDataSet();
    }

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

        if(mListener != null) {
            mListener.onPageLoadEnd(mCurrentPage);
        }

		mCurrentPage++;
	}

    /**
     * resets the adapter to page 0
     */
    public void reload() {
        startFromPage(0);
    }

    /**
     * Reloads the data and starts loading from the page indicated
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
            loadNewData();
        }

        mAutoLoad = autoLoad;
    }

	public int getCurrentPage() {
		return mCurrentPage;
	}
}
