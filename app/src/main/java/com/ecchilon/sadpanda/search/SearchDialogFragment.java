package com.ecchilon.sadpanda.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import com.ecchilon.sadpanda.R;
import com.google.common.collect.Lists;
import roboguice.util.Strings;

public class SearchDialogFragment extends DialogFragment {

	static final String TAG = "SearchDialogFragment";

	private static final String HISTORY_FILE = "history.txt";

	private static final int MAX_HISTORY_SIZE = 3;

	private OnSearchSubmittedListener mListener;

	private SearchController mSearchController;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.action_search);

		final View searchView = LayoutInflater.from(getActivity()).inflate(R.layout.search_view, null);

		mSearchController = new SearchController(searchView);

		HistoryEditText query = (HistoryEditText) searchView.findViewById(R.id.search_query);

		final List<String> history = getHistory();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_dropdown_item_1line, Lists.newArrayList(history));

		query.setAdapter(adapter);

		searchView.findViewById(R.id.show_advanced).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View advancedView = getDialog().findViewById(R.id.advanced_view_container);
				advancedView.setVisibility(advancedView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			}
		});

		builder.setView(searchView);

		builder.setPositiveButton(R.string.action_search, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addToHistory(history, mSearchController.getQuery());

				mListener.onSearchSubmitted(mSearchController.getUrl(), mSearchController.getQuery());
				dismiss();
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});

		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (OnSearchSubmittedListener) activity;
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Activity " + activity.getLocalClassName()
					+ " should implement SearchDrawer.OnSearchSubmittedListener", e);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null;
	}

	private void addToHistory(List<String> history, String query) {
		if (Strings.isEmpty(query)) {
			return;
		}

		if (history.size() >= MAX_HISTORY_SIZE) {
			history = history.subList(0, MAX_HISTORY_SIZE - 1);

		}

		history.add(0, query);
		writeHistory(history);
	}

	private void writeHistory(@NonNull List<String> historyLines) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(getActivity().openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE)));

			for (String line : historyLines) {
				writer.write(line);
				writer.newLine();
			}
		}
		catch (IOException e) {
			Log.e(TAG, "Failed to write history file", e);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
					Log.e(TAG, "Failed to close history file", e);
				}
			}
		}
	}

	private List<String> getHistory() {
		BufferedReader reader = null;
		List<String> historyLines = Lists.newArrayListWithCapacity(MAX_HISTORY_SIZE);
		try {
			reader =
					new BufferedReader(new InputStreamReader(getActivity().openFileInput(HISTORY_FILE)));

			String line = null;
			while ((line = reader.readLine()) != null && historyLines.size() < MAX_HISTORY_SIZE) {
				historyLines.add(line);
			}
		}
		catch (IOException ignored) {
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					Log.e(TAG, "Failed to close history file", e);
				}
			}
		}

		return historyLines;
	}
}
