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

    private static final String GROUPS_TITLE = "My Groups";
    private static final String COMPOSE_TITLE = "Compose";
    private static final String SETTINGS_TITLE = "Settings";
    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private GroupsFragment groupsFragment;
    private ComposeFragment composeFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupsFragment = new GroupsFragment();
        composeFragment = new ComposeFragment();
        settingsFragment = new SettingsFragment();
        activeFragment = groupsFragment;

        fragmentManager = getSupportFragmentManager();
        // set default selection
        fragmentManager.beginTransaction()
                .add(binding.flContainer.getId(), groupsFragment, GROUPS_TITLE).commit();
        fragmentManager.beginTransaction()
                .add(binding.flContainer.getId(), composeFragment, COMPOSE_TITLE)
                .hide(composeFragment).commit();
        fragmentManager.beginTransaction()
                .add(binding.flContainer.getId(), settingsFragment, SETTINGS_TITLE)
                .hide(settingsFragment).commit();
        binding.bottomBar.setItemActiveIndex(0);
        getSupportActionBar().setTitle(GROUPS_TITLE);

        // leave as anonymous function, not lambda to avoid ambiguity
        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                Fragment fragment;
                String title;
                switch(i) {
                    case 0:
                        fragment = groupsFragment;
                        title = GROUPS_TITLE;
                        break;
                    case 1:
                        fragment = composeFragment;
                        title = COMPOSE_TITLE;
                        break;
                    case 2:
                    default:
                        fragment = settingsFragment;
                        title = SETTINGS_TITLE;
                        break;
                }
                getSupportActionBar().setTitle(title);
                fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
                activeFragment = fragment;
                return true;
            }
        });
    }

    public void composeToGroupsFragment() {
        GroupsFragment newGroupsFragment = new GroupsFragment();
        ComposeFragment newComposeFragment = new ComposeFragment();
        fragmentManager.beginTransaction().remove(groupsFragment)
                .add(binding.flContainer.getId(), newGroupsFragment, GROUPS_TITLE).commit();
        fragmentManager.beginTransaction().remove(composeFragment)
                .add(binding.flContainer.getId(), newComposeFragment, COMPOSE_TITLE)
                .hide(newComposeFragment).commit();
        groupsFragment = newGroupsFragment;
        composeFragment = newComposeFragment;
        activeFragment = groupsFragment;
        binding.bottomBar.setItemActiveIndex(0);
        getSupportActionBar().setTitle(GROUPS_TITLE);
    }
}