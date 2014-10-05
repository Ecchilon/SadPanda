package com.ecchilon.sadpanda.overview;

import static android.graphics.PorterDuff.Mode.MULTIPLY;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;
import com.google.common.base.Joiner;

import lombok.Getter;

/**
 * Created by Alex on 21-9-2014.
 */
public class EntryView extends RelativeLayout {

    @Getter
    private ImageView thumbView;
    @Getter
    private TextView titleView;

    private TextView subTitleView;
    private ImageView category;

    final ForegroundColorSpan subColorSpan;

    private String fileCountText = "";
    private String tagText = "";

    public EntryView(Context context, int layout) {
        super(context);
        inflate(context, layout, this);

        thumbView = (ImageView) findViewById(R.id.thumb);
        titleView = (TextView) findViewById(R.id.title);
        subTitleView = (TextView) findViewById(R.id.subtitle);

        category = (ImageView) findViewById(R.id.content_type);

        subColorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.subtitle_gray));

        onFinishInflate();
    }

    public EntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        subColorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.subtitle_gray));
    }

    public EntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        subColorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.subtitle_gray));
    }

    public void setCategory(Category type) {
        category.setColorFilter(getResources().getColor(type.getColor()), MULTIPLY);
    }

    public void setFileCount(int fileCount) {
        fileCountText = fileCount + " " + getContext().getResources().getString(R.string.entries) + " - ";
        buildSubTitle();
    }

    public void setTags(String[] tags) {
        if(tags == null) return;
        tagText = Joiner.on(", ").join(tags);
        buildSubTitle();
    }

    private void buildSubTitle() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(fileCountText);
        builder.append(tagText);
        builder.setSpan(subColorSpan, fileCountText.length(), builder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        subTitleView.setText(builder);
    }
}
