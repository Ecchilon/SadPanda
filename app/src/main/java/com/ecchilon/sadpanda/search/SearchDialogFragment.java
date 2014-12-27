package com.ecchilon.sadpanda.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.ecchilon.sadpanda.R;
import roboguice.inject.InjectView;

public class SearchDialogFragment extends DialogFragment {

	private OnSearchSubmittedListener mListener;

	private SearchController mSearchController;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.action_search);

		final View searchView = LayoutInflater.from(getActivity()).inflate(R.layout.search_view, null);

		mSearchController = new SearchController(searchView);

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
}
