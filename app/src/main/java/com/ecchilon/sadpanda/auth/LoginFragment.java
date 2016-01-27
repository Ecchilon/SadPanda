package com.ecchilon.sadpanda.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import com.ecchilon.sadpanda.R;
import com.ecchilon.sadpanda.auth.ExhentaiAuth.AuthException;
import com.google.inject.Inject;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class LoginFragment extends RxDialogFragment {

	private static final int ANIMATION_DURATION = 200;

	public interface LoginListener {
		void onSuccess();

		void onDismiss();
	}

	@Inject
	private ExhentaiAuth auth;

	private LoginListener authListener;

	private View mLoginFormView;
	private View mProgressView;

	private EditText username;
	private EditText password;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		RoboGuice.getInjector(activity).injectMembersWithoutViews(this);

		try {
			authListener = (LoginListener) activity;
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"Activity " + activity.getLocalClassName() + " should implement LoginFragment.LoginListener", e);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		authListener = null;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View loginView = LayoutInflater.from(getActivity()).inflate(R.layout.exhentai_login, null);

		mLoginFormView = loginView.findViewById(R.id.login_form);
		mProgressView = loginView.findViewById(R.id.login_progress);

		username = (EditText) loginView.findViewById(R.id.username);
		password = (EditText) loginView.findViewById(R.id.password);

		Observable.combineLatest(RxTextView.textChanges(password),
				RxTextView.textChanges(username), Pair::create)
				.compose(bindToLifecycle())
				.filter(chars -> getDialog() != null)
				.subscribe(textPair ->
					((AlertDialog) getDialog())
							.getButton(DialogInterface.BUTTON_POSITIVE)
							.setEnabled(textPair.first.length() > 0 && textPair.second.length() > 0)
				);

		RxTextView.editorActionEvents(password)
				.compose(bindToLifecycle())
				.filter(event -> event.actionId() == EditorInfo.IME_ACTION_SEND)
				.subscribe(event -> performLogin());

		builder.setView(loginView);
		builder.setTitle(R.string.login_title);
		builder.setPositiveButton(R.string.action_sign_in, null);

		final AlertDialog alertDialog = builder.create();

		alertDialog.setOnShowListener(dialog -> {
			Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			b.setOnClickListener(view -> performLogin());
		});

		return alertDialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		authListener.onDismiss();
	}

	private void performLogin() {
		showProgress(true);

		auth.login(username.getText().toString(), password.getText().toString())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(exhentaiResult -> {
					dismiss();
					authListener.onSuccess();
				}, throwable -> {
					showProgress(false);
					Log.e(LoginFragment.class.getSimpleName(), "Couldn't log in", throwable);
					if(throwable.getCause() instanceof AuthException) {
						username.setError(((AuthException) throwable.getCause()).getError().getErrorMessage());
						username.requestFocus();
					}
				});
	}

	public void showProgress(final boolean show) {
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
}
