package com.example.hangspot.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("UserGroups")
public class UserGroups extends ParseObject {
    private static final String KEY_GROUPS = "groups";

    public List<Group> getGroups() {
        return getList(KEY_GROUPS);
    }

    public void setGroups(List<Group> groups) {
        put(KEY_GROUPS, groups);
    }

    public void addGroup(Group group) {
        if (!getGroups().contains(group)) {
            add(KEY_GROUPS, group);
        }
    }
}
