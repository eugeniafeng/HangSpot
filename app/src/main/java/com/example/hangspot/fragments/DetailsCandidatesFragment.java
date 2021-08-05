package com.example.hangspot.fragments;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.example.hangspot.R;
import com.example.hangspot.adapters.CandidatesAdapter;
import com.example.hangspot.databinding.FragmentDetailsCandidatesBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.utils.Constants;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailsCandidatesFragment extends Fragment {

    public static final String TAG = "DetailsCandidatesFragment";
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
        adapter = new CandidatesAdapter(getContext(), allCandidates, this);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvCandidates.setAdapter(adapter);
        binding.rvCandidates.setLayoutManager(new LinearLayoutManager(getContext()));
        queryCandidates();

        try {
            if (!group.getRemainingUsersString().isEmpty()) {
                String waiting = group.getRemainingUsersString() + " to finish entering candidates.";
                binding.tvWaiting.setText(waiting);
                binding.tvWaiting.setVisibility(View.VISIBLE);
            } else {
                binding.tvWaiting.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            binding.tvWaiting.setVisibility(View.GONE);
            e.printStackTrace();
        }

        binding.btnAdd.setOnClickListener(v -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flDetailsContainer, new MapsFragment(group, this))
                .addToBackStack("DetailsCandidatesFragment")
                .commit());

        binding.btnDone.setOnClickListener(v -> updateUserStatuses());

        boolean userStatus = false;
        try {
            userStatus = group.getUserStatuses().getBoolean(ParseUser.getCurrentUser().getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (userStatus) {
            binding.btnDone.setEnabled(false);
            binding.btnAdd.setEnabled(false);
        }
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
        });
    }

    private void updateUserStatuses() {
        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Saving...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.show();

        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.whereEqualTo(Location.KEY_GROUP, group);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_CANDIDATE);
        query.whereEqualTo(Location.KEY_ADDED_BY, ParseUser.getCurrentUser());
        query.findInBackground((objects, e) -> {
            if (e == null && objects != null && objects.size() > 0) {
                ParseQuery<Group> groupQuery = ParseQuery.getQuery("Group");
                groupQuery.getInBackground(group.getObjectId(), (object, e1) -> {
                    if (e1 == null) {
                        group = object;
                        try {
                            JSONObject userStatuses = group.getUserStatuses();
                            userStatuses.put(ParseUser.getCurrentUser().getUsername(), true);
                            group.setUserStatuses(userStatuses);
                            if (group.checkStatus()) {
                                group.initializeRankings();
                                getActivity()
                                        .getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.flDetailsContainer, new DetailsVotingFragment(group))
                                        .commit();
                            } else {
                                binding.btnDone.setEnabled(false);
                                binding.btnAdd.setEnabled(false);
                                if (!group.getRemainingUsersString().isEmpty()) {
                                    String waiting = group.getRemainingUsersString()
                                            + " to finish entering candidates.";
                                    binding.tvWaiting.setText(waiting);
                                    binding.tvWaiting.setVisibility(View.VISIBLE);
                                } else {
                                    binding.tvWaiting.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                            binding.tvWaiting.setVisibility(View.GONE);
                        }

                        group.saveInBackground(e2 -> {
                            if (e2 == null) {
                                Log.i(TAG, "Group status saved successfully");
                                pd.dismiss();
                            } else {
                                e2.printStackTrace();
                            }
                        });
                    } else {
                        e1.printStackTrace();
                    }
                });
            } else if (e != null) {
                e.printStackTrace();
            } else {
                pd.dismiss();
                Toast.makeText(getContext(),
                        "Please add at least one candidate",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}