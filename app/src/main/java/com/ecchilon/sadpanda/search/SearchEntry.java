package com.ecchilon.sadpanda.search;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;

import lombok.Getter;
import lombok.NonNull;


/**
 * Created by Alex on 7-10-2014.
 */
public class SearchEntry extends RelativeLayout {

    @Getter
    private final QueryObject queryObject;

    private CheckBox queryBox;
    private ImageView queryColor;

    public SearchEntry(Context context, @NonNull QueryObject queryObject) {
        super(context);

        this.queryObject = queryObject;
        inflate(context, R.layout.checkbox_entry, this);
        setupViews();
    }

    public SearchEntry(Context context, AttributeSet attrs) {
        super(context, attrs);

        throw new IllegalStateException("This view can't be created by inflation!");
    }

    public SearchEntry(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        throw new IllegalStateException("This view can't be created by inflation!");
    }

    private void setupViews() {
        queryBox = (CheckBox) findViewById(R.id.query_button);
        queryColor = (ImageView) findViewById(R.id.query_color);

        queryBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                queryObject.setActive(isChecked);
            }
        });

        queryBox.setChecked(queryObject.isActive());
    }

    public void setColor(int colorId) {
        queryColor.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY);
    }

    public void setText(String text) {
        queryBox.setText(text);
    }

    public void setText(int resId) {
        queryBox.setText(resId);
    }
}
