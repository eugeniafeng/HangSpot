package com.example.hangspot.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.hangspot.databinding.ActivityMainBinding;
import com.example.hangspot.fragments.ComposeFragment;
import com.example.hangspot.fragments.GroupsFragment;
import com.example.hangspot.fragments.SettingsFragment;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final FragmentManager fragmentManager = getSupportFragmentManager();
        // set default selection
        fragmentManager.beginTransaction().replace(binding.flContainer.getId(), new GroupsFragment()).commit();
        binding.bottomBar.setItemActiveIndex(0);
        getSupportActionBar().setTitle("Groups");

        // leave as anonymous function, not lambda to avoid ambiguity
        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                Fragment fragment;
                String title;
                switch(i) {
                    case 0:
                        fragment = new GroupsFragment();
                        title = "Groups";
                        break;
                    case 1:
                        fragment = new ComposeFragment();
                        title = "Compose";
                        break;
                    case 2:
                    default:
                        fragment = new SettingsFragment();
                        title = "Settings";
                        break;
                }
                getSupportActionBar().setTitle(title);
                fragmentManager.beginTransaction().replace(binding.flContainer.getId(), fragment).commit();
                return true;
            }
        });

    }
}