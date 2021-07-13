package com.example.hangspot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.widget.Toast;

import com.example.hangspot.databinding.ActivityMainBinding;
import com.example.hangspot.fragments.ComposeFragment;
import com.example.hangspot.fragments.GroupsFragment;
import com.example.hangspot.fragments.ProfileFragment;

import java.security.acl.Group;

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

        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                Fragment fragment;
                switch(i) {
                    case 0:
                        fragment = new GroupsFragment();
                        break;
                    case 1:
                        fragment = new ComposeFragment();
                        break;
                    case 2:
                    default:
                        fragment = new ProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(binding.flContainer.getId(), fragment).commit();
                return true;
            }
        });

    }
}