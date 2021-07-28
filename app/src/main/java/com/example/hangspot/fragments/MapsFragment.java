package com.example.hangspot.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsFragment";
    private static final Handler handler = new android.os.Handler();
    private Group group;
    private FragmentMapsBinding binding;
    private SupportMapFragment mapFragment;
    private Fragment previousFragment;
    private GoogleMap map;

    public MapsFragment (Group group, Fragment previousFragment) {
        this.group = group;
        this.previousFragment = previousFragment;
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
                                displayHomeAndCandidateLocations();
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
        if (previousFragment instanceof DetailsCandidatesFragment) {
            View messageView = LayoutInflater.from(getContext()).inflate(R.layout.item_map_alert, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setView(messageView);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> {
                BitmapDescriptor candidateMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                String name = ((EditText) alertDialog.findViewById(R.id.etName)).getText().toString();
                String description = ((EditText) alertDialog
                        .findViewById(R.id.etDescription)).getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                } else {
                    Geocoder geocoder = new Geocoder(getContext());
                    List<Address> addresses = null;
                    String address = null;
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addresses != null && addresses.size() > 0) {
                        address = addresses.get(0).getAddressLine(0);
                    }

                    String snippet = description.isEmpty() ? address : address + "\n" + description;
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .snippet(snippet)
                            .icon(candidateMarker));
                    dropPinEffect(marker);
                    saveLocationCandidate(marker, description, address);
                }
            });

            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    (dialog, id) -> dialog.cancel());
            alertDialog.show();
        }
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

    private void saveLocationCandidate(Marker marker, String description, String address) {
        Location candidate = new Location();
        candidate.setName(marker.getTitle());
        candidate.setDescription(description);
        candidate.setCoordinates(new ParseGeoPoint(
                marker.getPosition().latitude, marker.getPosition().longitude));
        candidate.setType(Constants.TYPE_CANDIDATE);
        candidate.setGroup(group);
        candidate.setAddedBy(ParseUser.getCurrentUser());
        candidate.setAddress(address);

        candidate.saveInBackground(e -> {
            if (e == null) {
                Log.i(TAG, "Successfully saved location candidate");

                List<Location> candidates = group.getLocationCandidates();
                candidates.add(candidate);
                group.setLocationCandidates(candidates);
                group.saveInBackground(e1 -> {
                    if (e1 == null) {
                        Log.i(TAG, "Successfully saved location candidate in group");
                    } else {
                        e1.printStackTrace();
                    }
                });
            } else {
                e.printStackTrace();
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
                    .snippet(centralLocation.getAddress() + "\n" +
                            "The central point of all members of " + group.getName()
                            + " with a 1 km radius around it.")
                    .icon(centerMarker));

            // TODO: Determine radius of circle to display
            CircleOptions circleOptions = new CircleOptions().center(latLng).radius(1000);
            map.addCircle(circleOptions);
        }
    }

    private void displayHomeAndCandidateLocations() {
        ParseQuery<Location> homeQuery = ParseQuery.getQuery("Location");
        homeQuery.whereEqualTo(Location.KEY_GROUP, group);
        homeQuery.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_HOME);

        ParseQuery<Location> candidateQuery = ParseQuery.getQuery("Location");
        candidateQuery.whereEqualTo(Location.KEY_GROUP, group);
        candidateQuery.whereEqualTo(Location.KEY_TYPE, Constants.TYPE_CANDIDATE);

        List<ParseQuery<Location>> queries = new ArrayList<>();
        queries.add(homeQuery);
        queries.add(candidateQuery);

        ParseQuery<Location> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground((objects, e) -> {
            if (e == null) {
                BitmapDescriptor homeMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
                BitmapDescriptor candidateMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                BitmapDescriptor finalMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                LatLngBounds.Builder latLngBounds = LatLngBounds.builder();
                for (Location location : objects) {
                    LatLng latLng = new LatLng(location.getCoordinates().getLatitude(),
                            location.getCoordinates().getLongitude());
                    latLngBounds.include(latLng);
                    if (Constants.TYPE_HOME.equals(location.getType())) {
                        map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(location.getDescription())
                                .snippet(location.getAddress())
                                .icon(homeMarker));
                    } else {
                        String snippet = location.getDescription().isEmpty() ? location.getAddress()
                                : location.getAddress() + "\n" + location.getDescription();
                        if (previousFragment instanceof DetailsCompleteFragment &&
                                location.getObjectId().equals(group.getFinalLocation().getObjectId())) {
                            map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Final Location: " + location.getName())
                                    .snippet(snippet)
                                    .icon(finalMarker));
                        } else {
                            map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(location.getName())
                                    .snippet(snippet)
                                    .icon(candidateMarker));
                        }
                    }
                }
                CameraUpdate cameraUpdate =
                        CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 60);
                map.animateCamera(cameraUpdate);
            } else {
                e.printStackTrace();
            }
        });
    }
}