package com.example.hangspot.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.adapters.CandidatesAdapter;
import com.example.hangspot.databinding.FragmentDetailsVotingBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.utils.Constants;
import com.example.hangspot.utils.SaveVotesWorker;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailsVotingFragment extends Fragment {

    public static final String TAG = "DetailsVotingFragment";
    private FragmentDetailsVotingBinding binding;
    private Group group;
    private CandidatesAdapter adapter;
    private List<Location> allCandidates;
    private ItemTouchHelper itemTouchHelper;
    private ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END,
            0) {
        @Override
        public boolean onMove(@NonNull @NotNull RecyclerView recyclerView,
                              @NonNull @NotNull RecyclerView.ViewHolder viewHolder,
                              @NonNull @NotNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(allCandidates, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {}

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.setBackgroundColor(getResources().getColor(R.color.light_blue_gray));
            }
        }

        @Override
        public void clearView(@NonNull @NotNull RecyclerView recyclerView,
                              @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundColor(Color.WHITE);
        }
    };

    public DetailsVotingFragment() {}

    public DetailsVotingFragment(Group group) {
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsVotingBinding.inflate(inflater, container, false);
        allCandidates = new ArrayList<>();
        adapter = new CandidatesAdapter(getContext(), allCandidates, this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.rvVoting.setAdapter(adapter);
        binding.rvVoting.setLayoutManager(new LinearLayoutManager(getContext()));
        queryCandidates();

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvVoting);
        
        try {
            if (!group.getRemainingUsersString().isEmpty()) {
                String waiting = group.getRemainingUsersString() + " to finish voting.";
                binding.tvWaiting.setText(waiting);
                binding.tvWaiting.setVisibility(View.VISIBLE);
            } else {
                binding.tvWaiting.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            binding.tvWaiting.setVisibility(View.GONE);
            e.printStackTrace();
        }

        binding.btnMap.setOnClickListener(v -> getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flDetailsContainer, new MapsFragment(group, this))
                .addToBackStack("DetailsVotingFragment")
                .commit());
        
        binding.btnSubmit.setOnClickListener(v -> {
            try {
                saveRanking();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        
        boolean userStatus = false;
        try {
            userStatus = group.getUserStatuses().getBoolean(ParseUser.getCurrentUser().getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (userStatus) {
            binding.btnMap.setEnabled(false);
            binding.btnSubmit.setEnabled(false);
            binding.ivOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void saveRanking() throws JSONException {
        binding.btnSubmit.setEnabled(false);
        binding.btnMap.setEnabled(false);
        binding.ivOverlay.setVisibility(View.VISIBLE);
        binding.ivOverlay.setElevation(1);

        JSONObject rankings = group.getRankings();
        for (int i = 0; i < allCandidates.size(); i++) {
            String objectId = allCandidates.get(i).getObjectId();
            rankings.put(objectId, rankings.getInt(objectId) + i);
        }
        group.setRankings(rankings);

        JSONObject userStatuses = group.getUserStatuses();
        userStatuses.put(ParseUser.getCurrentUser().getUsername(), true);
        group.setUserStatuses(userStatuses);

        if (!group.getRemainingUsersString().isEmpty()) {
            String waiting = group.getRemainingUsersString() + " to finish voting.";
            binding.tvWaiting.setText(waiting);
            binding.tvWaiting.setVisibility(View.VISIBLE);
        } else {
            binding.tvWaiting.setVisibility(View.GONE);
        }

        group.saveInBackground(e -> {
            if (e == null) {
                try {
                    if (group.checkStatus()) {
                        String objectId = group.findFinalLocation();
                        ParseQuery<Location> query = ParseQuery.getQuery("Location");
                        query.include("*");
                        query.getInBackground(objectId, (object, e1) -> {
                            if (e1 == null) {
                                group.setFinalLocation(object);
                                getActivity()
                                        .getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.flDetailsContainer, new DetailsCompleteFragment(group))
                                        .commit();
                                group.saveInBackground(e2 -> {
                                    if (e2 == null) {
                                        Log.i(TAG, "Successfully saved final location");
                                    } else {
                                        e2.printStackTrace();
                                    }
                                });
                            } else {
                                e1.printStackTrace();
                            }
                        });
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            } else {
                e.printStackTrace();
                try {
                    if (group.checkStatus()) {
                        getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flDetailsContainer, new DetailsCompleteFragment(group))
                                .commit();
                    }
                    saveWhenNetworkConnected();
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        });
    }

    private void saveWhenNetworkConnected() {
        try {
            FileOutputStream fos = getContext().openFileOutput(
                    "group.txt", Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(group.getObjectId() + "\n" +
                    ParseUser.getCurrentUser().getUsername());
            writer.close();

            fos = getContext().openFileOutput("rankings.txt", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            List<String> individualRankings = new ArrayList<>();
            for (Location candidate : allCandidates) {
                individualRankings.add(candidate.getObjectId());
            }
            out.writeObject(individualRankings);
            out.close();
            fos.close();
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest saveVotesWorkRequest = new OneTimeWorkRequest
                .Builder(SaveVotesWorker.class).setConstraints(constraints).build();
        WorkManager.getInstance().enqueue(saveVotesWorkRequest);
    }

    private void queryCandidates() {
        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.include("*");
        query.whereEqualTo(Location.KEY_GROUP, group);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_CANDIDATE);
        query.addDescendingOrder("createdAt");
        query.findInBackground((objects, e) -> adapter.addAll(objects));
    }

    public void startDragging(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}