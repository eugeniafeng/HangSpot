package com.example.hangspot.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Location")
public class Location extends ParseObject {

    private static final String KEY_COORDINATES = "coordinates";
    private static final String KEY_TYPE = "type";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_GROUP = "group";
    private static final String KEY_ADDED_BY = "addedBy";
    private static final String KEY_PLACES_ID = "placesId";

    public ParseGeoPoint getCoordinates() {
        return getParseGeoPoint(KEY_COORDINATES);
    }

    public void setCoordinates(ParseGeoPoint coordinates) {
        put(KEY_COORDINATES, coordinates);
    }

    public String getType() {
        return getString(KEY_TYPE);
    }

    public void setType(String type) {
        put(KEY_TYPE, type);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getAddress() {
        return getString(KEY_ADDRESS);
    }

    public void setAddress(String address) {
        put(KEY_ADDRESS, address);
    }

    public Group getGroup() {
        return (Group) get(KEY_GROUP);
    }

    public void setGroup(Group group) {
        put(KEY_GROUP, group);
    }

    public ParseUser getAddedBy() {
        return getParseUser(KEY_ADDED_BY);
    }

    public void setAddedBy(ParseUser user) {
        put(KEY_ADDED_BY, user);
    }

    public String getPlacesId() {
        return getString(KEY_PLACES_ID);
    }

    public void setPlacesId(String placesId) {
        put(KEY_PLACES_ID, placesId);
    }
}
