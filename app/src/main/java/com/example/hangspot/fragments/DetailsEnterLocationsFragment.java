package com.example.hangspot.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentDetailsEnterLocationsBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.utils.Constants;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class DetailsEnterLocationsFragment extends Fragment {

    private static final String TAG = "DetailsEnterLocationsFragment";
    private FragmentDetailsEnterLocationsBinding binding;
    private AutocompleteSupportFragment autocompleteFragment;
    private Group group;
    private Location location;
    private ProgressDialog pd;

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
            if (!group.getRemainingUsersString().isEmpty()) {
                String waiting = group.getRemainingUsersString() + " to enter their location.";
                binding.tvWaiting.setText(waiting);
                binding.tvWaiting.setVisibility(View.VISIBLE);
            } else {
                binding.tvWaiting.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            binding.tvWaiting.setVisibility(View.GONE);
            e.printStackTrace();
        }

        binding.btnSubmit.setOnClickListener(v -> saveLocation());
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

        // only to set location for emulator
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(39.944412, -75.327577),
                new LatLng(40.048167, -75.146331)));

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

    public void saveLocation() {
        disableViews();

        pd = new ProgressDialog(getContext());
        pd.setTitle("Saving...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.show();

        location.saveInBackground(e -> {
            if (e == null) {
                Log.i(TAG, "Location saved successfully");
                ParseQuery<Group> query = ParseQuery.getQuery("Group");
                query.getInBackground(group.getObjectId(), (object, e1) -> {
                    if (e1 == null) {
                        group = object;
                        JSONObject userStatuses = group.getUserStatuses();
                        try {
                            userStatuses.put(ParseUser.getCurrentUser().getUsername(), true);
                            group.setUserStatuses(userStatuses);

                            if (!group.getRemainingUsersString().isEmpty()) {
                                String waiting = group.getRemainingUsersString() + " to enter their location.";
                                binding.tvWaiting.setText(waiting);
                                binding.tvWaiting.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvWaiting.setVisibility(View.GONE);
                            }

                            if (group.checkStatus()) {
                                calculateCentralLocation();
                            } else {
                                group.saveInBackground(e2 -> {
                                    if (e2 == null) {
                                        Log.i(TAG, "Group status saved successfully");
                                        pd.dismiss();
                                    } else {
                                        e2.printStackTrace();
                                    }
                                });
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                    } else {
                        e1.printStackTrace();
                    }
                });
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

    public void calculateCentralLocation() {
        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.include("*");
        query.whereEqualTo(Location.KEY_GROUP, group);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_HOME);
        query.findInBackground((objects, e) -> {
            if (e == null) {
                LatLngBounds.Builder latLngBounds = LatLngBounds.builder();
                for (Location location : objects) {
                    latLngBounds.include(new LatLng(location.getCoordinates().getLatitude(),
                            location.getCoordinates().getLongitude()));
                }
                LatLng centerCoords = latLngBounds.build().getCenter();
                Location center = new Location();
                center.setType(Constants.TYPE_CENTER);
                center.setName(group.getName() + " Center Point");
                center.setCoordinates(new ParseGeoPoint(centerCoords.latitude, centerCoords.longitude));
                center.setGroup(group);
                center.setAddress(MapsFragment.findAddress(centerCoords, getContext()));
                center.saveInBackground(e1 -> {
                    if (e1 == null) {
                        group.setCentralLocation(center);
                        group.saveInBackground(e2 -> {
                            if (e2 == null) {
                                Log.i("Group", "Successfully saved central location");
                                pd.dismiss();
                            } else {
                                e2.printStackTrace();
                            }
                        });
                        getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flDetailsContainer, new DetailsCandidatesFragment(group))
                                .commit();
                    } else {
                        e1.printStackTrace();
                    }
                });
            } else {
                e.printStackTrace();
            }
        });
    }
}