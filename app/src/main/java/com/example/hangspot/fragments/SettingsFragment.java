package com.example.hangspot.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.hangspot.R;
import com.example.hangspot.activities.AuthenticationActivity;
import com.parse.ParseUser;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference logoutPreference = findPreference("logout");
        logoutPreference.setOnPreferenceClickListener(preference -> {
            ParseUser.logOutInBackground();
            Intent i = new Intent(getContext(), AuthenticationActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        });
    }
}