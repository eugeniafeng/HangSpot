package com.example.hangspot.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.Nullable;

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

    public String getStatusString() {
        String status;
        switch (getStatus()) {
            case 0:
                status = "Entering locations";
                break;
            case 1:
                status = "Selecting location candidates";
                break;
            case 2:
                status = "Voting on locations";
                break;
            case 3:
            default:
                status = "Complete";
                break;
        }
        return status;
    }

    public void setStatus(int status) {
        put(KEY_STATUS, status);
    }

    public void checkStatus(Context context) throws JSONException {
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
            if (getStatus() == 1) {
                calculateCentralLocation(context);
            }
            saveInBackground(e -> {
                if (e == null) {
                    Log.i("Group", "Successfully updated status");
                } else {
                    e.printStackTrace();
                }
            });
        }
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

    public void calculateCentralLocation(Context context) {
        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.include("*");
        query.whereEqualTo(Location.KEY_GROUP, this);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_HOME);
        query.findInBackground((objects, e) -> {
            if (e == null) {
                LatLngBounds.Builder latLngBounds = LatLngBounds.builder();
                for (Location location : objects) {
                    latLngBounds.include(new LatLng(location.getCoordinates().getLatitude(),
                            location.getCoordinates().getLongitude()));
                }
                LatLng centerCoords = latLngBounds.build().getCenter();
                Location center = new Location();
                center.setType(Constants.TYPE_CENTER);
                center.setName(getName() + " Center Point");
                center.setCoordinates(new ParseGeoPoint(centerCoords.latitude, centerCoords.longitude));
                center.setGroup(Group.this);

                Geocoder geocoder = new Geocoder(context);
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(
                            centerCoords.latitude, centerCoords.longitude, 1);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (addresses != null && addresses.size() > 0) {
                    center.setAddress(addresses.get(0).getAddressLine(0));
                }

                center.saveInBackground(e1 -> {
                    if (e1 == null) {
                        setCentralLocation(center);
                        saveInBackground(e2 -> {
                            if (e2 == null) {
                                Log.i("Group", "Successfully saved central location");
                            } else {
                                e2.printStackTrace();
                            }
                        });
                    } else {
                        e1.printStackTrace();
                    }
                });
            } else {
                e.printStackTrace();
            }
        });
    }

    public Location getFinalLocation(Location location) {
        return (Location) get(KEY_FINAL_LOCATION);
    }

    public void setFinalLocation(Location location) {
        put(KEY_FINAL_LOCATION, location);
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
