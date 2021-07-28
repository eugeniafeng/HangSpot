package com.example.hangspot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hangspot.databinding.ItemCandidateBinding;
import com.example.hangspot.fragments.DetailsVotingFragment;
import com.example.hangspot.models.Location;

import java.util.List;

public class CandidatesAdapter extends RecyclerView.Adapter<CandidatesAdapter.ViewHolder> {

    private Context context;
    private List<Location> candidates;
    private Fragment fragment;

    public CandidatesAdapter(Context context, List<Location> candidates, Fragment fragment) {
        this.context = context;
        this.candidates = candidates;
        this.fragment = fragment;
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
            binding.tvDescription.setVisibility(
                    candidate.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            if (fragment instanceof DetailsVotingFragment) {
                binding.ivReorder.setVisibility(View.VISIBLE);
                binding.ivReorder.setOnTouchListener((v, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        ((DetailsVotingFragment) fragment).startDragging(ViewHolder.this);
                        return true;
                    }
                    return false;
                });
            } else {
                binding.ivReorder.setVisibility(View.GONE);
            }
        }
    }
}
