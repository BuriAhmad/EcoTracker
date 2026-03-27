package com.ecotrack.app.view.dashboard;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.ActivityLog;
import com.ecotrack.app.util.DateUtils;
import com.example.saturn.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for recent activity log items shown on
 * the Dashboard and Home screens.
 */
public class RecentLogAdapter extends RecyclerView.Adapter<RecentLogAdapter.ViewHolder> {

    private final List<ActivityLog> logs = new ArrayList<>();

    public void setLogs(List<ActivityLog> newLogs) {
        logs.clear();
        if (newLogs != null) logs.addAll(newLogs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityLog log = logs.get(position);

        // Icon + color based on activity type
        int iconRes = getIconForType(log.getActivityType());
        int colorRes = getColorForType(log.getActivityType());
        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes)));
        holder.viewIconBg.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes) & 0x33FFFFFF));

        // Title — e.g., "Biked 5.2 km"
        String title = formatTitle(log.getActivityType(), log.getQuantity(), log.getUnit());
        holder.tvTitle.setText(title);

        // Time ago
        String timeAgo = log.getTimestamp() != null
                ? DateUtils.formatRelativeTime(log.getTimestamp())
                : "";
        holder.tvTimeAgo.setText(timeAgo);

        // Points
        holder.tvPoints.setText(String.format(Locale.US, "+%d pts", log.getPointsEarned()));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String formatTitle(String type, double quantity, String unit) {
        String verb;
        switch (type != null ? type : "") {
            case "biking":       verb = "Biked"; break;
            case "walking":      verb = "Walked"; break;
            case "recycling":    verb = "Recycled"; break;
            case "water_save":   verb = "Saved water"; break;
            case "energy_saving":verb = "Saved energy"; break;
            case "plastic_free": verb = "Went plastic-free"; break;
            default:             verb = "Logged"; break;
        }

        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.format(Locale.US, "%s %d %s", verb, (int) quantity, unit != null ? unit : "");
        }
        return String.format(Locale.US, "%s %.1f %s", verb, quantity, unit != null ? unit : "");
    }

    private int getIconForType(String type) {
        switch (type != null ? type : "") {
            case "biking":        return R.drawable.ic_directions_bike_24;
            case "walking":       return R.drawable.ic_directions_walk_24;
            case "recycling":     return R.drawable.ic_recycling_24;
            case "water_save":    return R.drawable.ic_water_drop_24;
            case "energy_saving": return R.drawable.ic_bolt_24;
            case "plastic_free":  return R.drawable.ic_eco_24;
            default:              return R.drawable.ic_eco_leaf;
        }
    }

    private int getColorForType(String type) {
        switch (type != null ? type : "") {
            case "biking":        return R.color.color_biking;
            case "walking":       return R.color.color_walking;
            case "recycling":     return R.color.color_recycling;
            case "water_save":    return R.color.color_water_save;
            case "energy_saving": return R.color.color_energy;
            case "plastic_free":  return R.color.color_plastic_free;
            default:              return R.color.accent_green;
        }
    }

    // ── ViewHolder ───────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View viewIconBg;
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvTimeAgo;
        final TextView tvPoints;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIconBg = itemView.findViewById(R.id.view_icon_bg);
            ivIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
            tvPoints = itemView.findViewById(R.id.tv_points);
        }
    }
}
