package com.ecchilon.sadpanda.search;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by Alex on 7-10-2014.
 */
@Accessors(chain = true)
public class QueryObject {
    protected final String mOnValue;
    protected final String mOffValue;

    @Getter
    @Setter
    private boolean active;
    @Getter
    protected final String key;
    @Getter
    protected final int nameId;

    public QueryObject(String key, String onQuery, String offQuery, int nameId) {
        this.key = key;
        mOnValue = onQuery;
        mOffValue = offQuery;
        this.nameId = nameId;
    }

    public String getValue() {
        return active ? mOnValue : mOffValue;
    }

    public String getOnValue() {
        return mOnValue;
    }

    public String getOffValue() {
        return mOffValue;
    }

    public QueryObject copy() {
        return new QueryObject(key, mOnValue, mOffValue, nameId).setActive(active);
    }
}
