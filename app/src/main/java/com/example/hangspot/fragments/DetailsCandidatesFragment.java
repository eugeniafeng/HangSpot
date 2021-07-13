package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.databinding.FragmentDetailsCandidatesBinding;

public class DetailsCandidatesFragment extends Fragment {

    private FragmentDetailsCandidatesBinding binding;

    public DetailsCandidatesFragment() {
        // Required empty public constructor
    }

    public static DetailsCandidatesFragment newInstance() {
        DetailsCandidatesFragment fragment = new DetailsCandidatesFragment();
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
        binding = FragmentDetailsCandidatesBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}