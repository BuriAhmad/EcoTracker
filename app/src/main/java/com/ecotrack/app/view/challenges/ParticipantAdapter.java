package com.ecotrack.app.view.challenges;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecotrack.app.model.ChallengeParticipant;
import com.example.saturn.R;
import com.example.saturn.databinding.ItemChallengeParticipantBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the participant list on the challenge detail screen.
 */
public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    private List<ChallengeParticipant> participants = new ArrayList<>();
    private String unit = "";

    public void setParticipants(List<ChallengeParticipant> list) {
        this.participants = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit : "";
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChallengeParticipantBinding binding = ItemChallengeParticipantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ParticipantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        holder.bind(participants.get(position));
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    class ParticipantViewHolder extends RecyclerView.ViewHolder {

        private final ItemChallengeParticipantBinding b;

        ParticipantViewHolder(ItemChallengeParticipantBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(ChallengeParticipant participant) {
            b.tvName.setText(participant.getDisplayName() != null
                    ? participant.getDisplayName() : "User");

            // Avatar
            if (participant.getAvatarUrl() != null && !participant.getAvatarUrl().isEmpty()) {
                Glide.with(b.ivAvatar.getContext())
                        .load(participant.getAvatarUrl())
                        .placeholder(R.drawable.ic_person_24)
                        .circleCrop()
                        .into(b.ivAvatar);
            } else {
                b.ivAvatar.setImageResource(R.drawable.ic_person_24);
            }

            // Progress bar
            int progressPercent = (int) (participant.getProgressFraction() * 100);
            b.progressBar.setProgress(progressPercent);

            // Progress text
            b.tvProgress.setText(String.format(Locale.US, "%.1f/%.0f %s",
                    participant.getCurrentProgress(),
                    participant.getGoalQuantity(),
                    unit));
        }
    }
}
