package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.adapters.CandidatesAdapter;
import com.example.hangspot.databinding.FragmentDetailsCandidatesBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailsCandidatesFragment extends Fragment {

    private static final String TAG = "DetailsCandidatesFragment";
    private FragmentDetailsCandidatesBinding binding;
    private Group group;
    private CandidatesAdapter adapter;
    private List<Location> allCandidates;

    public DetailsCandidatesFragment() {}

    public DetailsCandidatesFragment(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsCandidatesBinding.inflate(inflater, container, false);
        allCandidates = new ArrayList<>();
        adapter = new CandidatesAdapter(getContext(), allCandidates);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvCandidates.setAdapter(adapter);
        binding.rvCandidates.setLayoutManager(new LinearLayoutManager(getContext()));
        queryCandidates();

        binding.swipeContainer.setOnRefreshListener(this::queryCandidates);

        try {
            String waiting = group.getRemainingUsersString() + " to enter a candidate.";
            binding.tvWaiting.setText(waiting);
            binding.tvWaiting.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            binding.tvWaiting.setVisibility(View.GONE);
            e.printStackTrace();
        }

        binding.btnAdd.setOnClickListener(v -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flDetailsContainer, new MapsFragment(group))
                .addToBackStack("DetailsCandidatesFragment")
                .commit());

        binding.btnDone.setOnClickListener(v -> {
            try {
                updateUserStatuses();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void queryCandidates() {
        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.include("*");
        query.whereEqualTo(Location.KEY_GROUP, group);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_CANDIDATE);
        query.addDescendingOrder("createdAt");
        query.findInBackground((objects, e) -> {
            adapter.clear();
            adapter.addAll(objects);
            binding.swipeContainer.setRefreshing(false);
        });
    }

    private void updateUserStatuses() throws JSONException {
        JSONObject userStatuses = group.getUserStatuses();
        userStatuses.put(ParseUser.getCurrentUser().getUsername(), true);
        group.setUserStatuses(userStatuses);
        group.saveInBackground(e -> {
            if (e == null) {
                Log.i(TAG, "Group status saved successfully");
                binding.btnDone.setEnabled(false);
                binding.btnAdd.setEnabled(false);
                try {
                    String waiting = group.getRemainingUsersString() + " to enter a candidate.";
                    binding.tvWaiting.setText(waiting);
                    binding.tvWaiting.setVisibility(View.VISIBLE);
                    if (group.getRemainingUsersString().isEmpty()) {
                        getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flDetailsContainer, new DetailsVotingFragment(group))
                                .commit();
                    }
                    group.checkStatus(getContext());
                } catch (JSONException jsonException) {
                    binding.tvWaiting.setVisibility(View.GONE);
                    jsonException.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        });
    }
}