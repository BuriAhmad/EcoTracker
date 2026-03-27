package com.ecotrack.app.view.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.Badge;
import com.ecotrack.app.model.BadgeDefinition;

import com.example.saturn.databinding.ItemBadgeBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for the badge grid on the Profile screen.
 * Earned badges show full colour; locked badges show 40 % opacity + lock icon.
 */
public class BadgeGridAdapter extends RecyclerView.Adapter<BadgeGridAdapter.BadgeViewHolder> {

    public interface OnBadgeClickListener {
        void onBadgeClicked(BadgeDefinition definition, boolean isEarned);
    }

    private final List<BadgeDefinition> definitions = new ArrayList<>();
    private final Set<String> earnedTypes = new HashSet<>();
    private OnBadgeClickListener listener;

    public void setOnBadgeClickListener(OnBadgeClickListener listener) {
        this.listener = listener;
    }

    public void submitData(List<BadgeDefinition> defs, List<Badge> earned) {
        definitions.clear();
        earnedTypes.clear();
        if (defs != null) definitions.addAll(defs);
        if (earned != null) {
            for (Badge b : earned) earnedTypes.add(b.getBadgeType());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBadgeBinding binding = ItemBadgeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BadgeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeDefinition def = definitions.get(position);
        boolean isEarned = earnedTypes.contains(def.getBadgeType());
        holder.bind(def, isEarned, listener);
    }

    @Override
    public int getItemCount() {
        return definitions.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private final ItemBadgeBinding binding;

        BadgeViewHolder(@NonNull ItemBadgeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BadgeDefinition def, boolean isEarned, OnBadgeClickListener listener) {
            binding.tvBadgeName.setText(def.getName() != null ? def.getName() : def.getBadgeType());

            if (isEarned) {
                binding.badgeFrame.setAlpha(1.0f);
                binding.ivLockOverlay.setVisibility(android.view.View.GONE);
                binding.ivBadgeIcon.setAlpha(1.0f);
            } else {
                binding.badgeFrame.setAlpha(0.4f);
                binding.ivLockOverlay.setVisibility(android.view.View.VISIBLE);
                binding.ivBadgeIcon.setAlpha(0.3f);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onBadgeClicked(def, isEarned);
            });
        }
    }
}
