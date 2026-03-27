package com.ecotrack.app.view.logging;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saturn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the activity type selection grid.
 * Supports single selection with visual feedback.
 */
public class ActivityCategoryAdapter
        extends RecyclerView.Adapter<ActivityCategoryAdapter.ViewHolder> {

    private final List<ActivityCategory> categories = new ArrayList<>();
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnCategorySelectedListener listener;

    // ── Data Model ───────────────────────────────────────────────────────

    public static class ActivityCategory {
        public final String type;
        public final String label;
        public final int iconRes;
        public final int colorRes;

        public ActivityCategory(String type, String label, int iconRes, int colorRes) {
            this.type = type;
            this.label = label;
            this.iconRes = iconRes;
            this.colorRes = colorRes;
        }
    }

    // ── Callback ─────────────────────────────────────────────────────────

    public interface OnCategorySelectedListener {
        void onCategorySelected(ActivityCategory category);
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    // ── Data ─────────────────────────────────────────────────────────────

    public void setCategories(List<ActivityCategory> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        int old = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (old != RecyclerView.NO_POSITION) {
            notifyItemChanged(old);
        }
    }

    // ── RecyclerView.Adapter ─────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityCategory category = categories.get(position);
        boolean isSelected = position == selectedPosition;

        holder.tvLabel.setText(category.label);
        holder.ivIcon.setImageResource(category.iconRes);

        int iconColor = ContextCompat.getColor(holder.itemView.getContext(), category.colorRes);
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(iconColor));

        // Selection state
        if (isSelected) {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_green_dark));
            holder.card.setStrokeColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_green));
            holder.card.setStrokeWidth(
                    (int) holder.itemView.getContext().getResources().getDimension(R.dimen.card_border_width) * 2);
            holder.tvLabel.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        } else {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.bg_card));
            holder.card.setStrokeColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.border_card));
            holder.card.setStrokeWidth(
                    (int) holder.itemView.getContext().getResources().getDimension(R.dimen.card_border_width));
            holder.tvLabel.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        holder.card.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            int previousSelected = selectedPosition;
            selectedPosition = adapterPosition;

            if (previousSelected != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategorySelected(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // ── ViewHolder ───────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView ivIcon;
        final TextView tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvLabel = itemView.findViewById(R.id.tv_label);
        }
    }
}
