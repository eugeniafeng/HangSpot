package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsCompleteBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class DetailsCompleteFragment extends Fragment {

    private FragmentDetailsCompleteBinding binding;
    private Group group;

    public DetailsCompleteFragment() {}

    public DetailsCompleteFragment(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsCompleteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Location finalLocation = group.getFinalLocation();
        finalLocation.fetchIfNeededInBackground((GetCallback<Location>) (location, e) -> {
            if (e == null) {
                binding.tvName.setText(location.getName());
                binding.tvAddress.setText(location.getAddress());

                if (location.getDescription().isEmpty()) {
                    binding.tvDescription.setVisibility(View.GONE);
                } else {
                    binding.tvDescription.setVisibility(View.VISIBLE);
                    binding.tvDescription.setText(location.getDescription());
                }

                location.getAddedBy().fetchIfNeededInBackground((GetCallback<ParseUser>) (user, e1) -> {
                    if (e1 == null) {
                        String addedBy = "Added by: " + user.getUsername();
                        binding.tvAddedBy.setText(addedBy);
                    } else {
                        e1.printStackTrace();
                    }
                });
            } else {
                e.printStackTrace();
            }
        });

        binding.btnMap.setOnClickListener(v -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flDetailsContainer, new MapsFragment(group, this))
                .addToBackStack("DetailsCompleteFragment")
                .commit());
    }
}