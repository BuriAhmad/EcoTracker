package com.ecotrack.app.view.teams;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.Team;
import com.example.saturn.R;
import com.example.saturn.databinding.ItemTeamBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the team list.
 */
public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(String teamId);
    }

    public TeamAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void setTeams(List<Team> newTeams) {
        this.teams = newTeams != null ? newTeams : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamBinding binding = ItemTeamBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TeamViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        holder.bind(teams.get(position));
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {

        private final ItemTeamBinding b;

        TeamViewHolder(ItemTeamBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Team team) {
            b.tvTeamName.setText(team.getName() != null ? team.getName() : "Team");
            b.tvInitial.setText(team.getInitial());

            // Type label
            String type = team.getType();
            if (type != null) {
                b.tvTeamType.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
            } else {
                b.tvTeamType.setText("Team");
            }

            // Member count
            b.tvMemberCount.setText(String.format(Locale.US, "%d members", team.getMemberCount()));

            // Points
            b.tvTeamPoints.setText(String.format(Locale.US, "%,d pts", team.getTotalPoints()));

            // Color the initial circle based on type
            int bgColor = getTypeColor(team.getType());
            if (b.tvInitial.getBackground() instanceof GradientDrawable) {
                GradientDrawable bg = (GradientDrawable) b.tvInitial.getBackground().mutate();
                bg.setColor(b.tvInitial.getContext().getColor(bgColor));
            }

            // Click
            b.getRoot().setOnClickListener(v -> {
                if (listener != null && team.getTeamId() != null) {
                    listener.onTeamClick(team.getTeamId());
                }
            });
        }

        private int getTypeColor(String type) {
            if (type == null) return R.color.accent_green;
            switch (type) {
                case "club":
                    return R.color.accent_cyan;
                case "department":
                    return R.color.accent_violet;
                default:
                    return R.color.accent_green;
            }
        }
    }
}
