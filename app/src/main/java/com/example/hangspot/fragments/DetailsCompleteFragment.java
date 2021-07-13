package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsCompleteBinding;

public class DetailsCompleteFragment extends Fragment {

    private FragmentDetailsCompleteBinding binding;

    public DetailsCompleteFragment() {
        // Required empty public constructor
    }

    public static DetailsCompleteFragment newInstance() {
        DetailsCompleteFragment fragment = new DetailsCompleteFragment();
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
        binding = FragmentDetailsCompleteBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}