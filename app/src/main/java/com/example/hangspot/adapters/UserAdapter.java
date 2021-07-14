package com.example.hangspot.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.example.hangspot.R;
import com.parse.ParseUser;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.util.List;

public class UserAdapter extends FilteredArrayAdapter<ParseUser> {
    @LayoutRes private int layoutId;

    public UserAdapter(Context context, @LayoutRes int layoutId, List<ParseUser> users) {
        super(context, layoutId, users);
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = l.inflate(layoutId, parent, false);
        }

        ParseUser p = getItem(position);
        ((TextView)convertView.findViewById(R.id.tvUsername)).setText(p.getUsername());

        return convertView;
    }

    @Override
    protected boolean keepObject(ParseUser user, String mask) {
        mask = mask.toLowerCase();
        return user.getUsername().toLowerCase().startsWith(mask);
    }
}
