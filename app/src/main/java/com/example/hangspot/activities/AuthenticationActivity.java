package com.example.hangspot.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.hangspot.databinding.ActivityAuthenticationBinding;
import com.example.hangspot.models.UserGroups;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class AuthenticationActivity extends AppCompatActivity {

    private static final String TAG = "AuthenticationActivity";

    private ActivityAuthenticationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if already logged in, skip this activity
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        binding.btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick login button");
            loginUser(binding.etUsername.getText().toString(), binding.etPassword.getText().toString());
        });

        binding.btnSignup.setOnClickListener(v -> {
            Log.i(TAG, "onClick signup button");
            signupUser(binding.etUsername.getText().toString(), binding.etPassword.getText().toString());
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with login", e);
                Toast.makeText(
                        AuthenticationActivity.this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            goMainActivity();
        });
    }

    private void signupUser(String username, String password) {
        ParseUser user = new ParseUser();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                    this,
                    "Username/password cannot be blank",
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        user.setUsername(username);
        user.setPassword(password);

        UserGroups userGroups = new UserGroups();
        userGroups.setGroups(new ArrayList<>());
        userGroups.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Issue with userGroups", e);
            } else {
                Log.i(TAG, "Successfully saved userGroup");

                user.put("userGroups", userGroups);
                user.signUpInBackground(error -> {
                    if (error != null) {
                        Log.e(TAG, "Issue with signup", error);
                        Toast.makeText(
                                AuthenticationActivity.this,
                                error.getMessage(),
                                Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    goMainActivity();
                    Log.i(TAG, "Current user: " + ParseUser.getCurrentUser().getUsername());
                });
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        // Dismiss this activity so back button won't return to login/signup
        finish();
    }

}