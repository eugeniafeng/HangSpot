package com.example.hangspot.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;

import com.example.hangspot.databinding.ActivityGroupDetailBinding;
import com.example.hangspot.fragments.DetailsCandidatesFragment;
import com.example.hangspot.fragments.DetailsCompleteFragment;
import com.example.hangspot.fragments.DetailsEnterLocationsFragment;
import com.example.hangspot.fragments.DetailsVotingFragment;
import com.example.hangspot.models.Group;
import com.example.hangspot.utils.Constants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

public class GroupDetailActivity extends AppCompatActivity {

    private ActivityGroupDetailBinding binding;
    private Group group;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        group = Parcels.unwrap(getIntent().getParcelableExtra(Constants.KEY_GROUP));
        fragmentManager = getSupportFragmentManager();
        setDetailsFragment();

        binding.swipeContainer.setOnRefreshListener(() -> setDetailsFragment());
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            Log.i("GroupDetailActivity", getSupportFragmentManager().getFragments().toString());
        } else {
            super.onBackPressed();
        }
    }

    private void setDetailsFragment() {
        ParseQuery<Group> query = ParseQuery.getQuery("Group");
        query.getInBackground(group.getObjectId(), (object, e) -> {
            if (e == null) {
                group = object;
                Fragment fragment;
                switch (object.getStatus()) {
                    case 0:
                        fragment = new DetailsEnterLocationsFragment(group);
                        break;
                    case 1:
                        fragment = new DetailsCandidatesFragment(group);
                        break;
                    case 2:
                        fragment = new DetailsVotingFragment(group);
                        break;
                    case 3:
                    default:
                        fragment = new DetailsCompleteFragment(group);
                        break;
                }
                getSupportActionBar().setTitle(group.getName());
                binding.swipeContainer.setRefreshing(false);
                fragmentManager.beginTransaction().replace(binding.flDetailsContainer.getId(), fragment).commit();
            } else {
                e.printStackTrace();
            }
        });
    }
}