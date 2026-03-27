package com.ecotrack.app.view.leaderboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.User;

import com.example.saturn.R;
import com.example.saturn.databinding.ItemLeaderboardEntryBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the ranked list below the podium.
 * Highlights the current user row with accent_green_dim background.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.EntryViewHolder> {

    private final List<User> entries = new ArrayList<>();
    private String currentUserId;

    public void setCurrentUserId(String id) {
        this.currentUserId = id;
    }

    public void submitList(List<User> users) {
        entries.clear();
        entries.addAll(users);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardEntryBinding binding = ItemLeaderboardEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        // Ranks after podium start at 4 (positions 0-2 are podium, list starts from index 3)
        holder.bind(entries.get(position), position + 4, currentUserId);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {

        private final ItemLeaderboardEntryBinding binding;
        private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

        EntryViewHolder(@NonNull ItemLeaderboardEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user, int rank, String currentUserId) {
            binding.tvRank.setText("#" + rank);
            binding.tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            binding.tvDepartment.setText(user.getDepartment() != null ? user.getDepartment() : "");
            binding.tvPoints.setText(nf.format(user.getTotalPoints()));
            binding.tvActivities.setText(user.getTotalActivitiesLogged() + " activities");

            // Highlight current user
            if (user.getUserId() != null && user.getUserId().equals(currentUserId)) {
                binding.rowContainer.setBackgroundColor(
                        binding.getRoot().getContext().getColor(R.color.accent_green_dim));
            } else {
                binding.rowContainer.setBackgroundColor(0x00000000);
            }
        }
    }
}
