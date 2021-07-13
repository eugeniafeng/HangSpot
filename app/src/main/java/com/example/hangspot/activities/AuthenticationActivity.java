package com.example.hangspot.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.hangspot.databinding.ActivityAuthenticationBinding;
import com.parse.ParseUser;

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
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            loginUser(username, password);
        });

        binding.btnSignup.setOnClickListener(v -> {
            Log.i(TAG, "onClick signup button");
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            signupUser(username, password);
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
                        "Issue with login!",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            goMainActivity();
            Toast.makeText(
                    AuthenticationActivity.this,
                    "Success!",
                    Toast.LENGTH_SHORT)
                    .show();
        });
    }

    private void signupUser(String username, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Issue with signup", e);
                Toast.makeText(
                        AuthenticationActivity.this,
                        "Issue with sign up!",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            goMainActivity();
            Toast.makeText(
                    AuthenticationActivity.this,
                    "Success!",
                    Toast.LENGTH_SHORT)
                    .show();
            Log.i(TAG, "Current user: " + ParseUser.getCurrentUser().getUsername());
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        // Dismiss this activity so back button won't return to login/signup
        finish();
    }

}