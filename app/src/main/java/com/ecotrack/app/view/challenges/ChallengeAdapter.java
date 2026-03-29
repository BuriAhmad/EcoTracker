package com.ecotrack.app.view.challenges;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.util.Constants;
import com.example.saturn.R;
import com.example.saturn.databinding.ItemChallengeBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Adapter for the challenges list RecyclerView.
 */
public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private List<Challenge> challenges = new ArrayList<>();
    private Set<String> joinedChallengeIds = new HashSet<>();
    private ChallengeClickListener listener;

    public interface ChallengeClickListener {
        void onChallengeClick(String challengeId);
        void onJoinClick(String challengeId);
    }

    public ChallengeAdapter(ChallengeClickListener listener) {
        this.listener = listener;
    }

    public void setChallenges(List<Challenge> newChallenges) {
        this.challenges = newChallenges != null ? newChallenges : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setJoinedChallengeIds(Set<String> ids) {
        this.joinedChallengeIds = ids != null ? ids : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChallengeBinding binding = ItemChallengeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChallengeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        holder.bind(challenges.get(position));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder {

        private final ItemChallengeBinding b;

        ChallengeViewHolder(ItemChallengeBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(Challenge challenge) {
            b.tvTitle.setText(challenge.getTitle() != null ? challenge.getTitle() : "Challenge");
            b.tvDescription.setText(challenge.getDescription() != null
                    ? challenge.getDescription() : "");

            // Activity-type icon + tint
            int iconRes = getActivityIcon(challenge.getActivityType());
            int tintColor = getActivityColor(challenge.getActivityType());
            b.ivIcon.setImageResource(iconRes);
            b.ivIcon.setColorFilter(b.ivIcon.getContext().getColor(tintColor));

            // Participants
            b.tvParticipants.setText(String.valueOf(challenge.getParticipantCount()));

            // Points
            b.tvPoints.setText(String.format(Locale.US, "🏆 %d pts", challenge.getPointsReward()));

            // Days left
            long days = challenge.getDaysRemaining();
            b.tvDaysLeft.setText(days > 0 ? days + "d left" : "Ended");

            // Join / Joined state
            boolean joined = joinedChallengeIds.contains(challenge.getChallengeId());
            if (joined) {
                b.btnJoin.setText("Joined");
                b.btnJoin.setEnabled(false);
                b.btnJoin.setAlpha(0.6f);
            } else {
                b.btnJoin.setText("Join");
                b.btnJoin.setEnabled(true);
                b.btnJoin.setAlpha(1f);
            }

            // Active state glow border
            if (challenge.isActive()) {
                b.cardChallenge.setStrokeColor(
                        b.cardChallenge.getContext().getColor(R.color.accent_green_dim));
            } else {
                b.cardChallenge.setStrokeColor(
                        b.cardChallenge.getContext().getColor(R.color.border_card));
            }

            // Clicks
            b.getRoot().setOnClickListener(v ->
                    listener.onChallengeClick(challenge.getChallengeId()));
            b.btnJoin.setOnClickListener(v -> {
                if (!joined) {
                    listener.onJoinClick(challenge.getChallengeId());
                }
            });
        }

        private int getActivityIcon(String type) {
            if (type == null) return R.drawable.ic_eco_24;
            switch (type) {
                case Constants.ACTIVITY_BIKING:
                    return R.drawable.ic_directions_bike_24;
                case Constants.ACTIVITY_WALKING:
                    return R.drawable.ic_directions_walk_24;
                case Constants.ACTIVITY_RECYCLING:
                    return R.drawable.ic_recycling_24;
                case Constants.ACTIVITY_WATER_SAVE:
                    return R.drawable.ic_water_drop_24;
                case Constants.ACTIVITY_ENERGY_SAVING:
                    return R.drawable.ic_bolt_24;
                case Constants.ACTIVITY_PUBLIC_TRANSIT:
                    return R.drawable.ic_directions_walk_24;
                default:
                    return R.drawable.ic_eco_24;
            }
        }

        private int getActivityColor(String type) {
            if (type == null) return R.color.accent_green;
            switch (type) {
                case Constants.ACTIVITY_BIKING:
                    return R.color.color_biking;
                case Constants.ACTIVITY_WALKING:
                    return R.color.color_walking;
                case Constants.ACTIVITY_RECYCLING:
                    return R.color.color_recycling;
                case Constants.ACTIVITY_WATER_SAVE:
                    return R.color.color_water_save;
                case Constants.ACTIVITY_ENERGY_SAVING:
                    return R.color.color_energy;
                default:
                    return R.color.accent_green;
            }
        }
    }
}
