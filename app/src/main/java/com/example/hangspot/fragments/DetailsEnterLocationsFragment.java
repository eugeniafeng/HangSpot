package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsEnterLocationsBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.models.UserGroups;
import com.example.hangspot.utils.Constants;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class DetailsEnterLocationsFragment extends Fragment {

    private static final String TAG = "DetailsEnterLocationsFragment";
    private FragmentDetailsEnterLocationsBinding binding;
    private AutocompleteSupportFragment autocompleteFragment;
    private Group group;
    private Location location;

    public DetailsEnterLocationsFragment() {}

    public DetailsEnterLocationsFragment(Group group) {
        this.group = group;
        location = new Location();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsEnterLocationsBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpPlacesAutocomplete();

        try {
            String waiting = group.getRemainingUsersString() + " to enter their location.";
            binding.tvWaiting.setText(waiting);
            binding.tvWaiting.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            binding.tvWaiting.setVisibility(View.GONE);
            e.printStackTrace();
        }

        binding.btnSubmit.setOnClickListener(v -> {
            try {
                saveLocation();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean userStatus = false;
        try {
            userStatus = group.getUserStatuses().getBoolean(ParseUser.getCurrentUser().getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (userStatus) {
            disableViews();
            findLocation();
        }
    }

    public void setUpPlacesAutocomplete() {
        Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_maps_api_key));
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull @NotNull Place place) {
                location.setType(Constants.TYPE_HOME);
                location.setCoordinates(new ParseGeoPoint(
                        place.getLatLng().latitude, place.getLatLng().longitude));
                location.setAddress(place.getAddress());
                location.setName(place.getName());
                location.setDescription(ParseUser.getCurrentUser().getUsername() + "'s location");
                location.setGroup(group);
                location.setAddedBy(ParseUser.getCurrentUser());
                location.setPlacesId(place.getId());
                Log.i(TAG, place.getName() + " " + place.getAddress());
            }

            @Override
            public void onError(@NonNull @NotNull Status status) {
                Log.e(TAG, "Error: " + status);
            }
        });
    }

    public void saveLocation() throws JSONException {
        location.saveInBackground(e -> {
            if (e == null) {
                Log.i(TAG, "Location saved successfully");
                disableViews();
            } else {
                e.printStackTrace();
            }
        });
        JSONObject userStatuses = group.getUserStatuses();
        userStatuses.put(ParseUser.getCurrentUser().getUsername(), true);
        group.setUserStatuses(userStatuses);
        group.saveInBackground(e -> {
            if (e == null) {
                Log.i(TAG, "Group status saved successfully");
                try {
                    if (!group.getRemainingUsersString().isEmpty()) {
                        String waiting = group.getRemainingUsersString() + " to enter their location.";
                        binding.tvWaiting.setText(waiting);
                        binding.tvWaiting.setVisibility(View.VISIBLE);
                    } else {
                        getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flDetailsContainer, new DetailsCandidatesFragment(group))
                                .commit();
                    }
                    group.checkStatus(getContext());
                } catch (JSONException jsonException) {
                    binding.tvWaiting.setVisibility(View.GONE);
                    jsonException.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        });
        // TODO: how to progress if status complete? next button? refresh?
    }

    public void disableViews() {
        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText(R.string.submitted);
        autocompleteFragment.getView()
                .findViewById(R.id.places_autocomplete_search_input).setEnabled(false);
        autocompleteFragment.getView()
                .findViewById(R.id.places_autocomplete_clear_button).setEnabled(false);
        autocompleteFragment.getView()
                .findViewById(R.id.places_autocomplete_search_button).setEnabled(false);
    }

    public void findLocation() {
        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.include("*");
        query.whereEqualTo(Location.KEY_ADDED_BY, ParseUser.getCurrentUser());
        query.whereEqualTo(Location.KEY_GROUP, group);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_HOME);
        query.findInBackground((objects, e) -> {
            if (e == null) {
                location = objects.get(0);
                autocompleteFragment.setText(location.getName());
            } else {
                e.printStackTrace();
            }
        });
    }
}