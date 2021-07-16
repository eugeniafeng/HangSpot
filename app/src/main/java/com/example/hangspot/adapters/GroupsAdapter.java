package com.example.hangspot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hangspot.databinding.ItemGroupBinding;
import com.example.hangspot.models.Group;
import com.parse.ParseQuery;

import java.util.Collections;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {
    private Context context;
    private List<Group> groups;

    public GroupsAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.ViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void clear() {
        groups.clear();
        notifyDataSetChanged();
    }

    public void addAllReverse(List<Group> list) {
        groups.addAll(list);
        Collections.reverse(groups);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemGroupBinding binding;

        public ViewHolder(@NonNull ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(Group group) {
            ParseQuery<Group> query = ParseQuery.getQuery("Group");
            query.getInBackground(group.getObjectId(), (object, e) -> {
                binding.tvGroupName.setText(object.getName());
                binding.tvUsers.setText(object.getUsersString());
                binding.tvStatus.setText(object.getStatusString());
            });
        }
    }
}
