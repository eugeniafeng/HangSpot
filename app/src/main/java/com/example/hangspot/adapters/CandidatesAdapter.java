package com.example.hangspot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hangspot.databinding.ItemCandidateBinding;
import com.example.hangspot.models.Location;

import java.util.List;

public class CandidatesAdapter extends RecyclerView.Adapter<CandidatesAdapter.ViewHolder> {

    private Context context;
    private List<Location> candidates;

    public CandidatesAdapter(Context context, List<Location> candidates) {
        this.context = context;
        this.candidates = candidates;
    }

    @NonNull
    @Override
    public CandidatesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCandidateBinding binding = ItemCandidateBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CandidatesAdapter.ViewHolder holder, int position) {
        Location candidate = candidates.get(position);
        holder.bind(candidate);
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }

    public void addAll(List<Location> list) {
        candidates.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        candidates.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemCandidateBinding binding;

        public ViewHolder(@NonNull ItemCandidateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Location candidate) {
            binding.tvLocationName.setText(candidate.getName());
            binding.tvAddress.setText(candidate.getAddress());
            binding.tvDescription.setText(candidate.getDescription());
            if (candidate.getDescription().isEmpty()) {
                binding.tvDescription.setVisibility(View.GONE);
            } else {
                binding.tvDescription.setVisibility(View.VISIBLE);
            }
        }
    }
}
