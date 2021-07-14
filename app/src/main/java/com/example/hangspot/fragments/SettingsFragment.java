package com.example.hangspot.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.activities.AuthenticationActivity;
import com.example.hangspot.databinding.FragmentSettingsBinding;
import com.parse.ParseUser;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnLogout.setOnClickListener(v -> {
            ParseUser.logOut();
            // Return to sign up/log in and dismiss the main activity so user cannot use back button
            Intent i = new Intent(getContext(), AuthenticationActivity.class);
            startActivity(i);
            getActivity().finish();
        });
    }
}