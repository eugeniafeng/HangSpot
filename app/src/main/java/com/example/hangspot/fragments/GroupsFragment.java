package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.adapters.GroupsAdapter;
import com.example.hangspot.databinding.FragmentGroupsBinding;
import com.example.hangspot.models.Group;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private static final String TAG = "GroupsFragment";

    private FragmentGroupsBinding binding;
    protected GroupsAdapter adapter;
    protected List<Group> allGroups;

    public GroupsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        allGroups = new ArrayList<>();
        adapter = new GroupsAdapter(getContext(), allGroups);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvGroups.setAdapter(adapter);
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        queryGroups();
    }

    private void queryGroups() {
        // TODO: fix query to only get Groups user is in
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        // include all nested Parse objects
        query.include("*");
        query.include("users");
        query.setLimit(50);
        query.addDescendingOrder("updatedAt");

        query.findInBackground((groups, e) -> {
            if (e == null) {
                allGroups.addAll(groups);
                adapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, e.getMessage());
            }
        });
    }
}