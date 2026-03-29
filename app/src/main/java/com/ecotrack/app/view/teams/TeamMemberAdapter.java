package com.ecotrack.app.view.teams;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.User;
import com.example.saturn.databinding.ItemTeamMemberBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for team member list.
 */
public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.MemberViewHolder> {

    private List<User> members = new ArrayList<>();
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

    public void setMembers(List<User> newMembers) {
        this.members = newMembers != null ? newMembers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamMemberBinding binding = ItemTeamMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        private final ItemTeamMemberBinding b;

        MemberViewHolder(ItemTeamMemberBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(User user, int rank) {
            b.tvRank.setText("#" + rank);
            b.tvMemberName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            b.tvMemberPoints.setText(nf.format(user.getTotalPoints()) + " pts");
        }
    }
}
