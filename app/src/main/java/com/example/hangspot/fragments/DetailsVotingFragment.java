package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsEnterLocationsBinding;

public class DetailsVotingFragment extends Fragment {

    private FragmentDetailsEnterLocationsBinding binding;

    public DetailsVotingFragment() {
        // Required empty public constructor
    }

    public static DetailsVotingFragment newInstance() {
        DetailsVotingFragment fragment = new DetailsVotingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsEnterLocationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}