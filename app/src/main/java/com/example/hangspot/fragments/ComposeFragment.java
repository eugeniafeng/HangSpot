package com.example.hangspot.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hangspot.R;
import com.example.hangspot.activities.GroupDetailActivity;
import com.example.hangspot.activities.MainActivity;
import com.example.hangspot.adapters.UserAdapter;
import com.example.hangspot.databinding.FragmentComposeBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.models.UserGroups;
import com.example.hangspot.utils.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseACL;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tokenautocomplete.CharacterTokenizer;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ibrahimsn.lib.SmoothBottomBar;

public class ComposeFragment extends Fragment {
    private static final String TAG = "ComposeFragment";
    private FragmentComposeBinding binding;
    private List<ParseUser> allUsers;
    private UserAdapter adapter;

    public ComposeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentComposeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            binding.completionView.setPrefix("To: ");
        }
        allUsers = new ArrayList<>();
        adapter = new UserAdapter(getContext(), R.layout.item_suggestion, allUsers);
        binding.completionView.setAdapter(adapter);
        binding.completionView.setTokenizer(
                new CharacterTokenizer(Arrays.asList('.', ',', ' '), ","));

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ParseUser> selectedUsers = binding.completionView.getObjects();
                selectedUsers.add(ParseUser.getCurrentUser());
                if (selectedUsers.size() < 2){
                    Toast.makeText(getContext(), "Please enter a recipient", Toast.LENGTH_SHORT).show();
                } else if (binding.etName.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "Please enter a group name", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        createNewGroup(selectedUsers);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        queryUsers();
    }

    private void createNewGroup(List<ParseUser> selectedUsers) throws JSONException {
        Group group = new Group();
        group.setName(binding.etName.getText().toString());
        group.setUsers(selectedUsers);
        group.initializeUserStatuses(selectedUsers);
        saveGroup(group, selectedUsers);
    }

    public void saveGroup(Group group, List<ParseUser> selectedUsers) {
        group.saveInBackground(inner_error -> {
            if (inner_error != null) {
                Log.e(TAG, "Error while saving group", inner_error);
                Snackbar.make(binding.getRoot(), "Error while saving!", Snackbar.LENGTH_SHORT)
                        .setAction("Retry", v -> saveGroup(group, selectedUsers)).show();
            } else {
                Log.i(TAG, "Group save was successful!");
                for (ParseUser user : selectedUsers) {
                    ParseQuery<UserGroups> query = ParseQuery.getQuery("UserGroups");
                    // get the user's usergroups
                    query.getInBackground(((UserGroups)user.get(Constants.KEY_USER_GROUPS)).getObjectId(),
                            (object, e) -> {
                        if (e == null) {
                            // edit and save the usergroup
                            object.addGroup(group);
                            object.saveInBackground(error -> {
                                if (error != null) {
                                    Log.e(TAG,
                                            "Error while saving userGroup for "
                                                    + user.getUsername(),
                                            error);
                                    Snackbar.make(binding.getRoot(),
                                            "Error while saving!",
                                            Snackbar.LENGTH_SHORT)
                                            .setAction("Retry",
                                                    v -> saveGroup(group, selectedUsers)).show();
                                } else {
                                    Log.i(TAG, "userGroups save was successful!");
                                    if (user.equals(ParseUser.getCurrentUser())){
                                        showDetail(group);
                                    }
                                }
                            });
                        } else {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    // First switch to groups fragment so back button will lead to correct place
    public void showDetail(Group group) {
        ((MainActivity) getActivity()).composeToGroupsFragment();
        Intent intent = new Intent(getContext(), GroupDetailActivity.class);
        intent.putExtra(Constants.KEY_GROUP, Parcels.wrap(group));
        getContext().startActivity(intent);
    }

    public void queryUsers() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.findInBackground((users, e) -> {
            if (e == null) {
                allUsers.addAll(users);
                adapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, e.getMessage());
            }
        });
    }
}