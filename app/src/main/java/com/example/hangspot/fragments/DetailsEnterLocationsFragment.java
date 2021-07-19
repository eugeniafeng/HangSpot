package com.example.hangspot.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class DetailsEnterLocationsFragment extends Fragment {

    private static final String TAG = "DetailsEnterLocationsFragment";
    private FragmentDetailsEnterLocationsBinding binding;
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
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpPlacesAutocomplete();
    }

    public void setUpPlacesAutocomplete() {
        Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_maps_api_key));
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
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
        location.saveInBackground(e -> {
            if (e == null) {
                Log.i(TAG, "Location saved successfully");
            } else {
                e.printStackTrace();
            }
        });
    }
}