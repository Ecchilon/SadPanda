package com.ecchilon.sadpanda.search;

/**
 * Created by Alex on 7-10-2014.
 */
public class QueryObject {
    private boolean mState;
    protected final String mKey;
    protected final String mOnValue;
    protected final String mOffValue;
    protected final int mNameId;

    public QueryObject(String key, String onQuery, String offQuery, int nameId) {
        mKey = key;
        mOnValue = onQuery;
        mOffValue = offQuery;
        this.mNameId = nameId;
    }

    public QueryObject set(boolean state) {
        mState = state;
        return this;
    }

    public boolean getState(){
        return mState;
    }

    public String getKey() {
        return mKey;
    }

    public String getValue() {
        return mState? mOnValue : mOffValue;
    }

    public int getNameId() {
        return mNameId;
    }

    public QueryObject copy() {
        return new QueryObject(mKey, mOnValue, mOffValue, mNameId).set(mState);
    }
}
