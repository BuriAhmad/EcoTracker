package com.ecotrack.app.view.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.model.Challenge;
import com.ecotrack.app.model.Team;
import com.example.saturn.R;
import com.example.saturn.databinding.ItemSearchResultBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Heterogeneous adapter for search results — displays both Challenge and Team items.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    private static final int TYPE_CHALLENGE = 0;
    private static final int TYPE_TEAM = 1;

    private final List<Object> items = new ArrayList<>();
    private OnSearchItemClickListener listener;

    public interface OnSearchItemClickListener {
        void onChallengeClick(String challengeId);
        void onTeamClick(String teamId);
    }

    public SearchResultAdapter(OnSearchItemClickListener listener) {
        this.listener = listener;
    }

    public void setResults(List<Challenge> challenges, List<Team> teams) {
        items.clear();
        if (challenges != null) items.addAll(challenges);
        if (teams != null) items.addAll(teams);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Challenge ? TYPE_CHALLENGE : TYPE_TEAM;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchResultBinding binding = ItemSearchResultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SearchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Object item = items.get(position);
        if (item instanceof Challenge) {
            holder.bindChallenge((Challenge) item);
        } else if (item instanceof Team) {
            holder.bindTeam((Team) item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        private final ItemSearchResultBinding b;

        SearchViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bindChallenge(Challenge challenge) {
            b.tvSearchTitle.setText(challenge.getTitle() != null ? challenge.getTitle() : "Challenge");
            long days = challenge.getDaysRemaining();
            b.tvSearchSubtitle.setText(String.format(Locale.US, "Challenge · %s",
                    days > 0 ? days + "d left" : "Ended"));
            b.ivSearchIcon.setImageResource(R.drawable.ic_flag_24);

            b.getRoot().setOnClickListener(v -> {
                if (listener != null && challenge.getChallengeId() != null) {
                    listener.onChallengeClick(challenge.getChallengeId());
                }
            });
        }

        void bindTeam(Team team) {
            b.tvSearchTitle.setText(team.getName() != null ? team.getName() : "Team");
            b.tvSearchSubtitle.setText(String.format(Locale.US, "Team · %d members",
                    team.getMemberCount()));
            b.ivSearchIcon.setImageResource(R.drawable.ic_group_24);

            b.getRoot().setOnClickListener(v -> {
                if (listener != null && team.getTeamId() != null) {
                    listener.onTeamClick(team.getTeamId());
                }
            });
        }
    }
}
