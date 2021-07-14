package com.example.hangspot.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.hangspot.R;
import com.example.hangspot.databinding.ActivityGroupDetailBinding;
import com.example.hangspot.fragments.ComposeFragment;
import com.example.hangspot.fragments.DetailsCandidatesFragment;
import com.example.hangspot.fragments.DetailsCompleteFragment;
import com.example.hangspot.fragments.DetailsEnterLocationsFragment;
import com.example.hangspot.fragments.DetailsVotingFragment;
import com.example.hangspot.fragments.GroupsFragment;
import com.example.hangspot.fragments.SettingsFragment;
import com.example.hangspot.models.Group;
import com.example.hangspot.utils.Constants;

import org.parceler.Parcels;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        group = Parcels.unwrap(getIntent().getParcelableExtra(Constants.KEY_GROUP));
        switch(group.getStatus()) {
            case 0:
                fragment = new DetailsEnterLocationsFragment();
                break;
            case 1:
                fragment = new DetailsCandidatesFragment();
                break;
            case 2:
                fragment = new DetailsVotingFragment();
                break;
            case 3:
            default:
                fragment = new DetailsCompleteFragment();
                break;
        }
        fragmentManager.beginTransaction().replace(binding.flContainer.getId(), fragment).commit();
    }
}