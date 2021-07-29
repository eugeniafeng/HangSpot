package com.example.hangspot.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.hangspot.models.Group;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.List;

public class SaveVotesWorker extends Worker {

    private static final String TAG = "SaveVotesWorker";
    private Context context;

    public SaveVotesWorker(@NonNull @NotNull Context context,
                           @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        String groupObjectId = null;
        String username = null;
        List<String> readRankings = null;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    context.openFileInput("group.txt")));
            String line;
            int count = 0;
            while ((line = input.readLine()) != null) {
                if (count == 0) {
                    count ++;
                    groupObjectId = line;
                } else {
                    username = line;
                }
            }
            Log.i(TAG, groupObjectId + " " + username);

            ObjectInputStream in = new ObjectInputStream(context.openFileInput("rankings.txt"));
            readRankings = (List<String>) in.readObject();
            Log.i(TAG, readRankings.toString());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (groupObjectId != null && username != null && readRankings != null) {
            ParseQuery<Group> query = ParseQuery.getQuery("Group");
            List<String> finalReadRankings = readRankings;
            String finalUsername = username;
            query.getInBackground(groupObjectId, (object, e) -> {
                if (e == null) {
                    JSONObject rankings = object.getRankings();
                    for (int i = 0; i < finalReadRankings.size(); i++) {
                        try {
                            String objectId = finalReadRankings.get(i);
                            rankings.put(objectId, rankings.getInt(objectId) + i);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                    object.setRankings(rankings);

                    JSONObject userStatuses = object.getUserStatuses();
                    try {
                        userStatuses.put(finalUsername, true);
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                    object.setUserStatuses(userStatuses);

                    object.saveInBackground(e3 -> {
                        if (e3 == null) {
                            Log.i(TAG, "Group status saved successfully");
                            try {
                                object.checkStatus(context);
                            } catch (JSONException jsonException) {
                                jsonException.printStackTrace();
                            }
                        } else {
                            e3.printStackTrace();
                        }
                    });
                } else {
                    e.printStackTrace();
                }
            });
        } else {
            return Result.failure();
        }

        return Result.success();
    }
}
