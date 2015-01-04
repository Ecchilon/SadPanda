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
public class SearchView implements View.OnClickListener {

	private final SearchController mSearchController = new SearchController();
	private final EditText mQueryView;

	public SearchView(View searchView) {
		attachQueryViews(searchView);

		mQueryView = (EditText) searchView.findViewById(R.id.search_query);
	}

	public String getUrl() {
		return mSearchController.getUrl(mQueryView.getText().toString());
	}

	public String getQuery() {
		return mQueryView.getText().toString();
	}

	private void attachQueryViews(View searchView) {

		ViewGroup advancedSearch = (ViewGroup) searchView.findViewById(R.id.advanced_view);

		for(QueryObject entry : mSearchController.getQueryParameters()) {
			SearchEntry entryView = new SearchEntry(advancedSearch.getContext(), entry);
			if(entry instanceof CategoryObject) {
				entryView.setColor(((CategoryObject)entry).getColor());
			}
			entryView.setText(entry.getNameId());
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
		mSearchController.reset();
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
			mSearchController.setStars(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}
}
