package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.adapters.GroupsAdapter;
import com.example.hangspot.databinding.FragmentGroupsBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.UserGroups;
import com.example.hangspot.utils.Constants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupsFragment extends Fragment {

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

        binding.swipeContainer.setOnRefreshListener(() -> queryGroups(true));

        queryGroups(false);
    }

    private void queryGroups(boolean swipe) {
        adapter.clear();
        ParseQuery<UserGroups> query = ParseQuery.getQuery("UserGroups");
        query.getInBackground(((UserGroups)ParseUser
                .getCurrentUser()
                .get(Constants.KEY_USER_GROUPS))
                .getObjectId(),
                (object, e) -> {
            adapter.addAllReverse(object.getGroups());

            if (allGroups.size() == 0) {
                binding.tvNoGroups.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoGroups.setVisibility(View.GONE);
            }

            if (swipe) {
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }
}