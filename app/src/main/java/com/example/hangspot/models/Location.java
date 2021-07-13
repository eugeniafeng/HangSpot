package com.example.hangspot.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Location")
public class Location extends ParseObject {

    private static final String KEY_COORDINATES = "coordinates";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_CITY = "city";
    private static final String KEY_STATE = "state";
    private static final String KEY_POSTAL_CODE = "postalCode";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_GROUP = "group";
    private static final String KEY_ADDED_BY = "addedBy";

    public ParseGeoPoint getCoordinates() {
        return getParseGeoPoint(KEY_COORDINATES);
    }

    public void setCoordinates(ParseGeoPoint coordinates) {
        put(KEY_COORDINATES, coordinates);
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

    public String getCity() {
        return getString(KEY_CITY);
    }

    public void setCity(String city) {
        put(KEY_CITY, city);
    }

    public String getState() {
        return getString(KEY_STATE);
    }

    public void setState(String state) {
        put(KEY_STATE, state);
    }

    public String getPostalCode() {
        return getString(KEY_POSTAL_CODE);
    }

    public void setPostalCode(String postalCode) {
        put(KEY_POSTAL_CODE, postalCode);
    }
    public String getCountry() {
        return getString(KEY_COUNTRY);
    }

    public void setCountry(String country) {
        put(KEY_COUNTRY, country);
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
}
