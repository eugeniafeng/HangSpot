package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsEnterLocationsBinding;
import com.example.hangspot.databinding.FragmentDetailsVotingBinding;
import com.example.hangspot.models.Group;

import org.jetbrains.annotations.NotNull;

public class DetailsVotingFragment extends Fragment {

    private FragmentDetailsVotingBinding binding;
    private Group group;

    public DetailsVotingFragment() {}

    public DetailsVotingFragment(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsVotingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnMap.setOnClickListener(v -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                // TODO: modify map for voting so it is not editable
                .replace(R.id.flDetailsContainer, new MapsFragment(group))
                .addToBackStack("DetailsVotingFragment")
                .commit());
    }
}