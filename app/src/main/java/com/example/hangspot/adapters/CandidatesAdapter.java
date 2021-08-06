package com.example.hangspot.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hangspot.R;
import com.example.hangspot.databinding.ItemCandidateBinding;
import com.example.hangspot.fragments.DetailsVotingFragment;
import com.example.hangspot.models.Group;
import com.example.hangspot.models.Location;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class CandidatesAdapter extends RecyclerView.Adapter<CandidatesAdapter.ViewHolder> {

    private Context context;
    private List<Location> candidates;
    private Fragment fragment;
    private Group group;

    public CandidatesAdapter(Context context, List<Location> candidates, Fragment fragment, Group group) {
        this.context = context;
        this.candidates = candidates;
        this.fragment = fragment;
        this.group = group;
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
                binding.ivAction.setImageResource(R.drawable.ic_baseline_reorder_24);
                binding.ivAction.setVisibility(View.VISIBLE);
                binding.ivAction.setOnTouchListener((v, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        ((DetailsVotingFragment) fragment).startDragging(ViewHolder.this);
                        return true;
                    }
                    return false;
                });
            } else {
                if (ParseUser.getCurrentUser().getObjectId().equals(candidate.getAddedBy().getObjectId())) {
                    binding.ivAction.setImageResource(R.drawable.ic_baseline_close_24);
                    binding.ivAction.setOnClickListener(v -> {
                        int ix = candidates.indexOf(candidate);
                        candidates.remove(candidate);
                        notifyItemRemoved(ix);

                        List<Location> locations = group.getLocationCandidates();
                        locations.remove(candidate);
                        group.setLocationCandidates(locations);
                        group.saveInBackground(e -> {
                            if (e == null) {
                                candidate.deleteInBackground(e1 -> {
                                    if (e1 == null) {
                                        Log.i("CandidatesAdapter", "Successfully deleted candidate");
                                    } else {
                                        e1.printStackTrace();
                                    }
                                });
                            } else {
                                e.printStackTrace();
                            }
                        });
                    });
                } else {
                    binding.ivAction.setVisibility(View.GONE);
                }
            }
        }
    }
}
