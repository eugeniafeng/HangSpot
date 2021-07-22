package com.example.hangspot.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hangspot.R;
import com.example.hangspot.adapters.CustomWindowAdapter;
import com.example.hangspot.databinding.FragmentMapsBinding;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.example.hangspot.utils.Constants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseQuery;
import org.jetbrains.annotations.NotNull;

public class MapsFragment extends Fragment implements GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsFragment";
    private static final Handler handler = new android.os.Handler();
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
                map.setOnMapLongClickListener(this);
                map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));
                group.fetchIfNeededInBackground((object, e) -> {
                    if (e == null) {
                        group.getCentralLocation().fetchIfNeededInBackground((object1, e1) -> {
                            if (e1 == null) {
                                displayCentralLocation();
                                displayHomeLocations();
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

    @Override
    public void onMapLongClick(@NonNull @NotNull LatLng latLng) {
        View messageView = LayoutInflater.from(getContext()).inflate(R.layout.item_map_alert, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(messageView);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            BitmapDescriptor defaultMarker =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
            String name = ((EditText) alertDialog.findViewById(R.id.etName)).getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            } else {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .snippet(((EditText) alertDialog.findViewById(R.id.etDescription)).getText().toString())
                        .icon(defaultMarker));
                dropPinEffect(marker);
                // TODO: save location in database as candidate
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                (dialog, id) -> dialog.cancel());
        alertDialog.show();
    }

    private void dropPinEffect(final Marker marker) {
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;
        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                // Set the anchor within the marker image
                marker.setAnchor(0.5f, 1.0f + 10 * t);

                if (t > 0.0) {
                    handler.postDelayed(this, 15);
                } else {
                    marker.showInfoWindow();
                }
            }
        });
    }

    private void displayCentralLocation() {
        Location centralLocation = group.getCentralLocation();
        if (group.getCentralLocation() != null) {
            LatLng latLng = new LatLng(centralLocation.getCoordinates().getLatitude(),
                    centralLocation.getCoordinates().getLongitude());
            BitmapDescriptor centerMarker =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Center Point")
                    .snippet("The central point of all members of " + group.getName()
                            + " with a 1 km radius around it.")
                    .icon(centerMarker));

            // TODO: Determine radius of circle to display
            CircleOptions circleOptions = new CircleOptions().center(latLng).radius(1000);
            map.addCircle(circleOptions);
        }
    }

    private void displayHomeLocations() {
        ParseQuery<Location> query = ParseQuery.getQuery("Location");
        query.include("*");
        query.whereEqualTo(Location.KEY_GROUP, group);
        query.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_HOME);
        query.findInBackground((objects, e) -> {
            if (e == null) {
                BitmapDescriptor homeMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
                LatLngBounds.Builder latLngBounds = LatLngBounds.builder();
                for (Location location : objects) {
                    LatLng latLng = new LatLng(location.getCoordinates().getLatitude(),
                            location.getCoordinates().getLongitude());
                    latLngBounds.include(latLng);
                    map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(location.getDescription())
                            .snippet(location.getAddress())
                            .icon(homeMarker));
                }
                CameraUpdate cameraUpdate =
                        CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 40);
                map.animateCamera(cameraUpdate);
            } else {
                e.printStackTrace();
            }
        });
    }
}