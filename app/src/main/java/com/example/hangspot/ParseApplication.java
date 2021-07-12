package com.example.hangspot;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("U4HqvFZ94vC0faH2WziK6EnuLAh2tUqnNTBayVWM")
                .clientKey("H9TollicjQrGes74uXeNvg9Zy9SCm7KRKAb0zPM6")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
