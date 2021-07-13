package com.example.hangspot.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.hangspot.R;
import com.example.hangspot.databinding.ActivityMapBinding;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}