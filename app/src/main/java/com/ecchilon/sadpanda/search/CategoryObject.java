package com.ecchilon.sadpanda.search;

import lombok.Getter;

/**
 * Created by Alex on 8-10-2014.
 */
public class CategoryObject extends QueryObject {

    @Getter
    private final int color;

    public CategoryObject(String key, String onQuery, String offQuery, int color, int nameId) {
        super(key, onQuery, offQuery, nameId);
        this.color = color;
    }

    @Override
    public CategoryObject copy() {
        return (CategoryObject) new CategoryObject(mKey, mOnValue, mOffValue, color, mNameId)
                .set(getState());
    }
}
