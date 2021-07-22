package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsCandidatesBinding;
import com.example.hangspot.models.Group;

import org.jetbrains.annotations.NotNull;

public class DetailsCandidatesFragment extends Fragment {

    private FragmentDetailsCandidatesBinding binding;
    private Group group;

    public DetailsCandidatesFragment() {}

    public DetailsCandidatesFragment(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsCandidatesBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnMap.setOnClickListener(v -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flDetailsContainer, new MapsFragment(group))
                .addToBackStack("DetailsCandidatesFragment")
                .commit());
    }
}