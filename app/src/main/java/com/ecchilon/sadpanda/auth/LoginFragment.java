package com.ecchilon.sadpanda.auth;

import static com.ecchilon.sadpanda.auth.ExhentaiAuth.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ecchilon.sadpanda.R;
import com.google.inject.Inject;

import roboguice.fragment.RoboDialogFragment;
import roboguice.util.Strings;

/**
 * Created by Alex on 21-9-2014.
 */
public class LoginFragment extends RoboDialogFragment {

	private static final int ANIMATION_DURATION = 200;

	public interface LoginListener {
		void onSuccess();
	}

	@Inject
	private ExhentaiAuth mExhentaiAuth;

	private LoginListener mAuthListener;

	private View mLoginFormView;
	private View mProgressView;

	private EditText mUsername;
	private EditText mPassword;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mAuthListener = (LoginListener) activity;
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"Activity " + activity.getLocalClassName() + " should implement LoginFragment.LoginListener", e);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mAuthListener = null;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View loginView = LayoutInflater.from(getActivity()).inflate(R.layout.exhentai_login, null);

		mLoginFormView = loginView.findViewById(R.id.login_form);
		mProgressView = loginView.findViewById(R.id.login_progress);

		mUsername = (EditText) loginView.findViewById(R.id.username);
		mPassword = (EditText) loginView.findViewById(R.id.password);

		FormWatcher watcher = new FormWatcher();
		mUsername.addTextChangedListener(watcher);
		mPassword.addTextChangedListener(watcher);

		mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					performLogin();
					return true;
				}

				return false;
			}
		});

		builder.setView(loginView);
		builder.setTitle(R.string.login_title);
		builder.setPositiveButton(R.string.action_sign_in, null);

		final AlertDialog alertDialog = builder.create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						performLogin();
					}
				});
			}
		});

		return alertDialog;
	}

	private void performLogin() {
		showProgress(true);

		new PandaLoginTask().execute(mUsername.getText().toString(), mPassword.getText().toString());
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(ANIMATION_DURATION).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(ANIMATION_DURATION).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		}
		else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private class FormWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			if (Strings.isEmpty(s)) {
				((AlertDialog) getDialog())
						.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
			}
			else {
				((AlertDialog) getDialog())
						.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

	}

	private class PandaLoginTask extends AsyncTask<String, Void, ExhentaiResult> {

		@Override
		protected ExhentaiResult doInBackground(String... params) {
			return mExhentaiAuth.login(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(ExhentaiResult result) {
			if(result == ExhentaiResult.SUCCESS) {
				dismiss();
				mAuthListener.onSuccess();
			}
			else {
				showProgress(false);
				mUsername.setError(result.getErrorMessage());
				mUsername.requestFocus();
			}
		}
	}
}
