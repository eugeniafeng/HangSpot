package com.example.hangspot.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hangspot.R;
import com.example.hangspot.fragments.ComposeFragment;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class UsersCompletionView extends TokenCompleteTextView<ParseUser> {

    private ParseUser user;

    public UsersCompletionView(Context context) {
        super(context);
    }

    public UsersCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UsersCompletionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View getViewForObject(ParseUser user) {
        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.user_token, (ViewGroup) getParent(), false);
        view.setText(user.getUsername());

        return view;
    }

    @Override
    protected ParseUser defaultObject(String completionText) {
        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public boolean shouldIgnoreToken(ParseUser token) {
        boolean repeat = getObjects().contains(token) ||
                ParseUser.getCurrentUser().getUsername().equals(token.getUsername());
        if (repeat) {
            Toast.makeText(getContext(), "User already added", Toast.LENGTH_SHORT).show();
        }
        return repeat;
    }
}
