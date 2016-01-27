package com.ecchilon.sadpanda.overview;

import static android.graphics.PorterDuff.Mode.MULTIPLY;

import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.ecchilon.sadpanda.R;
import com.google.common.base.Joiner;
import com.squareup.picasso.Picasso;

public class EntryViewHolder extends RecyclerView.ViewHolder {

	private final ImageView thumbView;
	private final TextView titleView;
	private final TextView subTitleView;
	private final ImageView category;
	private final ForegroundColorSpan subColorSpan;

	public EntryViewHolder(View itemView) {
		super(itemView);

		thumbView = (ImageView) itemView.findViewById(R.id.thumb);
		titleView = (TextView) itemView.findViewById(R.id.title);
		subTitleView = (TextView) itemView.findViewById(R.id.subtitle);

		category = (ImageView) itemView.findViewById(R.id.content_type);

		subColorSpan = new ForegroundColorSpan(itemView.getResources().getColor(R.color.subtitle_gray));
	}

	public void bindGalleryEntry(GalleryEntry entry) {

		titleView.setText(entry.getTitle());
		category.setColorFilter(itemView.getResources().getColor(entry.getCategory().getColor()), MULTIPLY);
		buildSubtitle(entry);

		Picasso.with(itemView.getContext())
				.load(entry.getThumb())
				.resizeDimen(R.dimen.thumb_size, R.dimen.thumb_size)
				.centerCrop()
				.into(thumbView);
	}

	private void buildSubtitle(GalleryEntry entry) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		String fileCountText = String.format(itemView.getResources().getString(R.string.entries), entry.getFileCount(),
				Joiner.on(',').join(entry.getTags()));
		builder.append(fileCountText);
		builder.setSpan(subColorSpan, fileCountText.length(), builder.length(), Spannable
				.SPAN_INCLUSIVE_INCLUSIVE);
		subTitleView.setText(builder);
	}
}
