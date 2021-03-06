package com.example.hangspot.fragments;

import android.app.ProgressDialog;
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
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hangspot.R;
import com.example.hangspot.activities.GroupDetailActivity;
import com.example.hangspot.adapters.CandidatesAdapter;
import com.example.hangspot.databinding.FragmentDetailsVotingBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.utils.Constants;
import com.example.hangspot.utils.SaveVotesWorker;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
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
                ((GroupDetailActivity) getActivity()).disableSwipeRefresh();
            }
        }

        @Override
        public void clearView(@NonNull @NotNull RecyclerView recyclerView,
                              @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundColor(Color.WHITE);
            ((GroupDetailActivity) getActivity()).enableSwipeRefresh();
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
        adapter = new CandidatesAdapter(getContext(), allCandidates, this, group);
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
        
        binding.btnSubmit.setOnClickListener(v -> saveRanking());
        
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

    private void saveRanking() {
        binding.btnSubmit.setEnabled(false);
        binding.btnMap.setEnabled(false);
        binding.ivOverlay.setVisibility(View.VISIBLE);
        binding.ivOverlay.setElevation(1);

        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Saving...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.show();

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = () -> {
            Toast.makeText(getContext(), "No internet connection. Will retry later.", Toast.LENGTH_LONG).show();
            saveWhenNetworkConnected();
            pd.dismiss();
        };
        handler.postDelayed(runnable,5000);

        try {
            updateUserStatuses();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.getInBackground(group.getObjectId(), (object, e) -> {
            handler.removeCallbacks(runnable);
            if (e == null) {
                group = object;
                try {
                    JSONObject rankings = group.getRankings();
                    for (int i = 0; i < allCandidates.size(); i++) {
                        String objectId = allCandidates.get(i).getObjectId();
                        rankings.put(objectId, rankings.getInt(objectId) + i);
                    }
                    group.setRankings(rankings);
                    updateUserStatuses();
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

                group.saveInBackground(e1 -> {
                    if (e1 == null) {
                        try {
                            if (group.checkStatus()) {
                                String objectId = group.findFinalLocation();
                                ParseQuery<Location> locationQuery = ParseQuery.getQuery("Location");
                                locationQuery.include("*");
                                locationQuery.getInBackground(objectId, (location, e2) -> {
                                    if (e2 == null) {
                                        group.setFinalLocation(location);
                                        getActivity()
                                                .getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.flDetailsContainer,
                                                        new DetailsCompleteFragment(group))
                                                .commit();
                                        group.saveInBackground(e3 -> {
                                            if (e3 == null) {
                                                Log.i(TAG, "Successfully saved final location");
                                                pd.dismiss();
                                            } else {
                                                e3.printStackTrace();
                                            }
                                        });
                                    } else {
                                        e2.printStackTrace();
                                    }
                                });
                            } else {
                                pd.dismiss();
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                    } else {
                        e1.printStackTrace();
                    }
                });
            } else {
                e.printStackTrace();
            }
        });
    }

    private void updateUserStatuses() throws JSONException {
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
    }

    private void saveWhenNetworkConnected() {
        try {
            if (group.checkStatus()) {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flDetailsContainer, new DetailsCompleteFragment(group))
                        .commit();
            }

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }

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