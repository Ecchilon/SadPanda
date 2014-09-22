package com.ecchilon.sadpanda;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import roboguice.fragment.RoboFragment;

/**
 * Created by Alex on 21-9-2014.
 */
public class ErrorFragment extends RoboFragment {

    public static final String ERROR_KEY = "ExhentaiError";

    private static final int DEFAULT_ERROR = R.string.login_request;

    public static ErrorFragment newInstance(int errorId) {
        ErrorFragment fragment = new ErrorFragment();
        if(errorId != 0) {
            Bundle args = new Bundle();
            args.putInt(ERROR_KEY, errorId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.error_view, container, false);

        int error = DEFAULT_ERROR;
        if(getArguments().containsKey(ERROR_KEY)) {
            error = getArguments().getInt(ERROR_KEY);
        }

        ((TextView)view.findViewById(R.id.error_view)).setText(error);

        return view;
    }
}
