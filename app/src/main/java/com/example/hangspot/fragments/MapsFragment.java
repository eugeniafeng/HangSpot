package com.example.hangspot.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.hangspot.R;
import com.example.hangspot.databinding.FragmentMapsBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.jetbrains.annotations.NotNull;

import permissions.dispatcher.RuntimePermissions;

public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";
    private Group group;
    private FragmentMapsBinding binding;
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    public MapsFragment (Group group) {
        this.group = group;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                group.fetchIfNeededInBackground((object, e) -> {
                    if (e == null) {
                        group.getCentralLocation().fetchIfNeededInBackground((object1, e1) -> {
                            if (e1 == null) {
                                displayLocation();
                            } else {
                                e1.printStackTrace();
                            }
                        });
                    } else {
                        e.printStackTrace();
                    }
                });
            });
        }
    }

    private void displayLocation() {
        Location centralLocation = group.getCentralLocation();
        if (group.getCentralLocation() != null) {
            LatLng latLng = new LatLng(centralLocation.getCoordinates().getLatitude(),
                    centralLocation.getCoordinates().getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            map.animateCamera(cameraUpdate);
        }
    }
}