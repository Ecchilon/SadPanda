package com.ecchilon.sadpanda.overview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;

import lombok.Getter;

/**
 * Created by Alex on 21-9-2014.
 */
public class EntryView extends RelativeLayout {

    @Getter
    private ImageView thumbView;
    @Getter
    private TextView titleView;

    public EntryView(Context context) {
        super(context);
        inflate(context, R.layout.gallery_entry, this);

        thumbView = (ImageView) findViewById(R.id.thumb);
        titleView = (TextView) findViewById(R.id.title);

        onFinishInflate();
    }

    public EntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
