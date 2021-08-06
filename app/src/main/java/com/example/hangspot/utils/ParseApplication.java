package com.example.hangspot.utils;

import android.app.Application;

import com.example.hangspot.R;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.models.UserGroups;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse models
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(UserGroups.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("U4HqvFZ94vC0faH2WziK6EnuLAh2tUqnNTBayVWM")
                .clientKey("H9TollicjQrGes74uXeNvg9Zy9SCm7KRKAb0zPM6")
                .server("https://parseapi.back4app.com")
                .build()
        );

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", R.string.gcm_sender_id);
        installation.saveInBackground();
    }
}
