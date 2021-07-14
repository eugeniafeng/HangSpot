package com.example.hangspot.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public List<Location> getLocationCandidates() {
        return getList(KEY_LOCATION_CANDIDATES);
    }

    public void setLocationCandidates(List<Location> locationCandidates) {
        put(KEY_LOCATION_CANDIDATES, locationCandidates);
    }

    // TODO: try getMap? instead of getJSONObject
    public JSONObject getRankings() {
        return getJSONObject(KEY_RANKINGS);
    }

    public void setRankings(JSONObject rankings) {
        put(KEY_RANKINGS, rankings);
    }

    public int getStatus() {
        return getInt(KEY_STATUS);
    }

    public void setStatus(int status) {
        put(KEY_STATUS, status);
    }

    public JSONObject getUserStatuses() {
        return getJSONObject(KEY_USER_STATUSES);
    }

    public void setUserStatuses(JSONObject userStatuses) {
        put(KEY_USER_STATUSES, userStatuses);
    }

    // TODO: make sure this works
    public Location getCentralLocation() {
        return (Location) get(KEY_CENTRAL_LOCATION);
    }

    public void setCentralLocation(Location location) {
        put(KEY_CENTRAL_LOCATION, location);
    }

    public Location getFinalLocation(Location location) {
        return (Location) get(KEY_FINAL_LOCATION);
    }

    public void setFinalLocation(Location location) {
        put(KEY_FINAL_LOCATION, location);
    }

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        long DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
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
