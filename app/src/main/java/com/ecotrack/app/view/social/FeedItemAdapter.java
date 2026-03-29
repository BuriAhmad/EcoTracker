package com.ecotrack.app.view.social;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecotrack.app.model.FeedItem;
import com.example.saturn.R;
import com.example.saturn.databinding.ItemFeedActivityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for the social feed RecyclerView.
 */
public class FeedItemAdapter extends RecyclerView.Adapter<FeedItemAdapter.FeedViewHolder> {

    private List<FeedItem> items = new ArrayList<>();
    private ReactionListener reactionListener;

    public interface ReactionListener {
        void onReaction(String feedItemId, String emoji);
    }

    public FeedItemAdapter(ReactionListener listener) {
        this.reactionListener = listener;
    }

    public void setItems(List<FeedItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedActivityBinding binding = ItemFeedActivityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {

        private final ItemFeedActivityBinding b;

        FeedViewHolder(ItemFeedActivityBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(FeedItem item) {
            // ── Name + avatar (anonymous handling) ─────────────────────
            b.tvName.setText(item.getDisplayName() != null ? item.getDisplayName() : "User");
            b.tvDepartment.setText(item.getDepartment() != null ? item.getDepartment() : "");

            if (item.isAnonymous() || item.getAvatarUrl() == null || item.getAvatarUrl().isEmpty()) {
                b.ivAvatar.setImageResource(R.drawable.ic_eco_leaf);
            } else {
                Glide.with(b.ivAvatar.getContext())
                        .load(item.getAvatarUrl())
                        .placeholder(R.drawable.ic_person_24)
                        .circleCrop()
                        .into(b.ivAvatar);
            }

            // ── Timestamp ──────────────────────────────────────────────
            if (item.getTimestamp() != null) {
                CharSequence relative = DateUtils.getRelativeTimeSpanString(
                        item.getTimestamp().toDate().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
                b.tvTimestamp.setText(relative);
            } else {
                b.tvTimestamp.setText("just now");
            }

            // ── Description ────────────────────────────────────────────
            b.tvDescription.setText(item.getActivityDescription() != null
                    ? item.getActivityDescription() : "");

            // ── Impact row ─────────────────────────────────────────────
            b.tvCo2.setText(String.format(Locale.US, "🌿 %.1f kg CO₂", item.getCo2Saved()));
            b.tvPoints.setText(String.format(Locale.US, "+%d pts", item.getPointsEarned()));

            // ── Reactions ──────────────────────────────────────────────
            Map<String, Long> reactions = item.getReactions();
            bindReaction(b.btnSeedling, "\uD83C\uDF31", reactions);   // 🌱
            bindReaction(b.btnHeart, "\uD83D\uDC9A", reactions);      // 💚
            bindReaction(b.btnParty, "\uD83C\uDF89", reactions);      // 🎉
            bindReaction(b.btnClap, "\uD83D\uDC4F", reactions);       // 👏

            b.btnSeedling.setOnClickListener(v ->
                    reactionListener.onReaction(item.getFeedItemId(), "\uD83C\uDF31"));
            b.btnHeart.setOnClickListener(v ->
                    reactionListener.onReaction(item.getFeedItemId(), "\uD83D\uDC9A"));
            b.btnParty.setOnClickListener(v ->
                    reactionListener.onReaction(item.getFeedItemId(), "\uD83C\uDF89"));
            b.btnClap.setOnClickListener(v ->
                    reactionListener.onReaction(item.getFeedItemId(), "\uD83D\uDC4F"));
        }

        private void bindReaction(android.widget.TextView tv, String emoji,
                                  Map<String, Long> reactions) {
            long count = 0;
            if (reactions != null && reactions.containsKey(emoji)) {
                Long val = reactions.get(emoji);
                count = val != null ? val : 0;
            }
            tv.setText(emoji + " " + count);
        }
    }
}
