package com.example.hangspot.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.hangspot.fragments.MapsFragment;
import com.example.hangspot.utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

@ParseClassName("Group")
public class Group extends ParseObject {

    private static final String KEY_NAME = "name";
    private static final String KEY_USERS = "users";
    private static final String KEY_LOCATION_CANDIDATES = "locationCandidates";
    private static final String KEY_RANKINGS = "rankings";
    private static final String KEY_STATUS = "status";
    private static final String KEY_USER_STATUSES = "userStatuses";
    private static final String KEY_CENTRAL_LOCATION = "centralLocation";
    private static final String KEY_FINAL_LOCATION = "finalLocation";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int STATUS_ENTER_LOCATIONS = 0;
    private static final int STATUS_CANDIDATES = 1;
    private static final int STATUS_VOTING = 2;
    private static final int STATUS_COMPLETE = 3;

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public List<ParseUser> getUsers() {
        return getList(KEY_USERS);
    }

    public void setUsers(List<ParseUser> users) {
        put(KEY_USERS, users);
    }

    public String getUsersString() {
        String userList = "";
        for (Iterator<String> it = getUserStatuses().keys(); it.hasNext();) {
            String key = it.next();
            userList += key + ", ";
        }
        if (userList.isEmpty()) {
            return userList;
        }
        return userList.substring(0, userList.length()-2);
    }

    public JSONObject getRankings() {
        return getJSONObject(KEY_RANKINGS);
    }

    public void setRankings(JSONObject rankings) {
        put(KEY_RANKINGS, rankings);
    }

    public void initializeRankings(List<Location> candidates) throws JSONException {
        JSONObject rankings = new JSONObject();
        for (Location candidate : candidates) {
            rankings.put(candidate.getObjectId(), 0);
        }
        setRankings(rankings);
    }

    public int getStatus() {
        return getInt(KEY_STATUS);
    }

    public String getStatusString() {
        String status;
        switch (getStatus()) {
            case STATUS_ENTER_LOCATIONS:
                status = "Entering locations";
                break;
            case STATUS_CANDIDATES:
                status = "Selecting location candidates";
                break;
            case STATUS_VOTING:
                status = "Voting on locations";
                break;
            case STATUS_COMPLETE:
            default:
                status = "Complete";
                break;
        }
        return status;
    }

    public void setStatus(int status) {
        put(KEY_STATUS, status);
    }

    public boolean checkStatus() throws JSONException {
        boolean hasCompleted = true;
        JSONObject userStatuses = getUserStatuses();
        for (Iterator<String> it = userStatuses.keys(); it.hasNext();) {
            String key = it.next();
            if (!userStatuses.getBoolean(key)) {
                hasCompleted = false;
            }
        }
        if (hasCompleted) {
            resetUserStatuses();
            setStatus(getStatus() + 1);
        }
        return hasCompleted;
    }

    public JSONObject getUserStatuses() {
        return getJSONObject(KEY_USER_STATUSES);
    }

    public void setUserStatuses(JSONObject userStatuses) {
        put(KEY_USER_STATUSES, userStatuses);
    }

    public void initializeUserStatuses(List<ParseUser> users) throws JSONException {
        JSONObject statuses = new JSONObject();
        for (ParseUser user : users) {
            statuses.put(user.getUsername(), false);
        }
        this.setUserStatuses(statuses);
    }

    public void resetUserStatuses() throws JSONException {
        JSONObject statuses = getUserStatuses();
        for (Iterator<String> it = statuses.keys(); it.hasNext();) {
            String key = it.next();
            statuses.put(key, false);
        }
        this.setUserStatuses(statuses);
    }

    public String getRemainingUsersString() throws JSONException {
        JSONObject statuses = getUserStatuses();
        String remainingUsers = "";
        for (Iterator<String> it = statuses.keys(); it.hasNext();) {
            String key = it.next();
            if (!statuses.getBoolean(key)) {
                remainingUsers += key + ", ";
            }
        }
        if (remainingUsers.isEmpty()) {
            return remainingUsers;
        }
        return "Waiting on " + remainingUsers.substring(0, remainingUsers.length()-2);
    }

    public Location getCentralLocation() {
        return (Location)get(KEY_CENTRAL_LOCATION);
    }

    public void setCentralLocation(Location location) {
        put(KEY_CENTRAL_LOCATION, location);
    }

    public Location getFinalLocation() {
        return (Location) get(KEY_FINAL_LOCATION);
    }

    public void setFinalLocation(Location location) {
        put(KEY_FINAL_LOCATION, location);
    }

    public String findFinalLocation() throws JSONException {
        JSONObject rankings = getRankings();
        List<String> minObjectIds = new ArrayList<>();
        int min = rankings.getInt(rankings.names().getString(0));
        for (int i = 0; i < rankings.names().length(); i++) {
            String key = rankings.names().getString(i);
            if (rankings.getInt(key) < min) {
                min = rankings.getInt(key);
                minObjectIds.clear();
                minObjectIds.add(key);
            } else if (rankings.getInt(key) == min) {
                minObjectIds.add(key);
            }
        }

        String finalObjectId;
        if (minObjectIds.size() == 1) {
            finalObjectId = minObjectIds.get(0);
        } else {
            Random random = new Random();
            finalObjectId = minObjectIds.get(random.nextInt(minObjectIds.size()));
        }
        return finalObjectId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Group && ((Group)obj).getObjectId() != null) {
            return ((Group)obj).getObjectId().equals(this.getObjectId());
        }
        return false;
    }

    public static String calculateTimeAgo(Date createdAt) {
        try {
            final long diff = createdAt.getTime() - System.currentTimeMillis();
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minute ago";
            } else if (diff < 60 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 2 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 2 * DAY_MILLIS) {
                return diff / DAY_MILLIS + " day ago";
            } else if (diff < 30 * DAY_MILLIS) {
                return diff / DAY_MILLIS + " days ago";
            } else {
                Calendar current = Calendar.getInstance();
                Calendar then = Calendar.getInstance();
                then.setTime(createdAt);
                if (current.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
                    return then.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " "
                            + then.get(Calendar.DAY_OF_MONTH);
                } else {
                    return then.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " "
                            + then.get(Calendar.DAY_OF_MONTH)
                            + ", " + (then.get(Calendar.YEAR));
                }
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }
}
