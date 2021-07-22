package com.example.hangspot.adapters;

import android.view.LayoutInflater;
import android.view.View;

import com.example.hangspot.databinding.ItemCustomInfoWindowBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater inflater;
    private ItemCustomInfoWindowBinding binding;

    public CustomWindowAdapter(LayoutInflater i){
        inflater = i;
    }

    @Override
    public View getInfoContents(Marker marker) {
        binding = ItemCustomInfoWindowBinding.inflate(inflater);
        binding.tvInfoName.setText(marker.getTitle());
        binding.tvInfoDescription.setText(marker.getSnippet());
        return binding.getRoot();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO: Change shape of window
        //  This changes the frame of the info window; returning null uses the default frame.
        //  This is just the border and arrow surrounding the contents specified above
        return null;
    }
}
