package com.ecchilon.sadpanda.search;

import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import roboguice.util.Strings;

/**
 * Created by Alex on 5-10-2014.
 */
public class SearchController implements View.OnClickListener {

	private static final String STAR_PARAM = "f_srdd";
	private static final String SEARCH_PARAM = "f_search";
	private static final String AUTHORITY = "exhentai.org";
	private static final String SCHEME = "http";

	private static final ImmutableList<CategoryObject> CATEGORY_PARAMS =
			new ImmutableList.Builder<CategoryObject>().add(
					(CategoryObject) new CategoryObject("f_doujinshi", "1", "0", R.color.doujinshi,
							R.string.doujinshi).setActive(true),
					(CategoryObject) new CategoryObject("f_manga", "1", "0", R.color.manga, R.string.manga).setActive(
							true),
					(CategoryObject) new CategoryObject("f_artistcg", "1", "0", R.color.artist_cg,
							R.string.artist_cg).setActive(true),
					(CategoryObject) new CategoryObject("f_gamecg", "1", "0", R.color.game_cg,
							R.string.game_cg).setActive(true),
					(CategoryObject) new CategoryObject("f_western", "1", "0", R.color.western,
							R.string.western).setActive(true),
					(CategoryObject) new CategoryObject("f_non-h", "1", "0", R.color.non_h, R.string.nonh).setActive(
							true),
					(CategoryObject) new CategoryObject("f_imageset", "1", "0", R.color.image_set,
							R.string.imageset).setActive(true),
					(CategoryObject) new CategoryObject("f_cosplay", "1", "0", R.color.cosplay,
							R.string.cosplay).setActive(true),
					(CategoryObject) new CategoryObject("f_asianporn", "1", "0", R.color.asian_porn, R.string
							.asianporn)
							.setActive(
									true),
					(CategoryObject) new CategoryObject("f_misc", "1", "0", R.color.misc, R.string.misc).setActive(
							true)).build();

	private static final ImmutableList<QueryObject> QUERY_PARAMS = new ImmutableList.Builder<QueryObject>().add(
			new QueryObject("f_sname", "on", "", R.string.by_name).setActive(true),
			new QueryObject("f_stags", "on", "", R.string.by_tag).setActive(false),
			new QueryObject("f_sh", "on", "", R.string.by_expunged).setActive(false),
			new QueryObject("f_sdesc", "on", "", R.string.by_desc).setActive(false),
			new QueryObject("f_sr", "on", "", R.string.by_star).setActive(false)).build();

	private final List<QueryObject> mQueryParameters =
			Lists.newArrayListWithCapacity(QUERY_PARAMS.size() + CATEGORY_PARAMS.size());
	private int mStars;
	private final EditText mQueryView;

	public SearchController(View searchView) {
		attachQueryViews(searchView);

		mQueryView = (EditText) searchView.findViewById(R.id.search_query);
	}

	public String getUrl() {
		Uri.Builder builder = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path("/");
		for (QueryObject query : mQueryParameters) {
			if (!Strings.isEmpty(query.getValue())) {
				builder.appendQueryParameter(query.getKey(), query.getValue());
			}
		}

		String query = mQueryView.getText().toString();
		if (query != null) {
			builder.appendQueryParameter(SEARCH_PARAM, query);
		}

		builder.appendQueryParameter(STAR_PARAM, Integer.toString(mStars));

		return builder.build().toString();
	}

	public String getQuery() {
		return mQueryView.getText().toString();
	}

	private void attachQueryViews(View searchView) {

		ViewGroup advancedSearch = (ViewGroup) searchView.findViewById(R.id.advanced_view);

		for (CategoryObject entry : CATEGORY_PARAMS) {
			CategoryObject copy = entry.copy();
			mQueryParameters.add(copy);
			SearchEntry entryView = new SearchEntry(advancedSearch.getContext(), copy);
			entryView.setColor(copy.getColor());
			entryView.setText(copy.getNameId());
			advancedSearch.addView(entryView);
		}

		for (QueryObject entry : QUERY_PARAMS) {
			QueryObject copy = entry.copy();
			mQueryParameters.add(copy);
			SearchEntry entryView = new SearchEntry(advancedSearch.getContext(), copy);
			entryView.setText(copy.getNameId());
			advancedSearch.addView(entryView);
		}

		View star = View.inflate(advancedSearch.getContext(), R.layout.star_entry, advancedSearch);

		Spinner spinner = (Spinner) star.findViewById(R.id.star_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(advancedSearch.getContext(),
				R.array.star_count, android.R.layout.simple_spinner_item);

		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerClickListener());
	}

	public void reset() {
		int catSize = CATEGORY_PARAMS.size(), querySize = QUERY_PARAMS.size();
		for (int i = 0; i < catSize; i++) {
			mQueryParameters.get(i).setActive(CATEGORY_PARAMS.get(i).isActive());
		}

		for (int i = 0; i < querySize; i++) {
			mQueryParameters.get(catSize + i).setActive(QUERY_PARAMS.get(i).isActive());
		}

		mStars = 0;
		mQueryView.setText(null);
	}

	public void resetQuery() {
		mQueryView.setText(null);
	}

	@Override
	public void onClick(View v) {
		if (v instanceof CheckBox) {
			QueryObject object = (QueryObject) v.getTag();
			object.setActive(((CheckBox) v).isChecked());
		}
	}

	private class SpinnerClickListener implements AdapterView.OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mStars = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}
}
