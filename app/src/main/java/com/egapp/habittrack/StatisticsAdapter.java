package com.egapp.habittrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.ViewHolder> {

    private Context context;
    private List<Habit> habitList;

    public StatisticsAdapter(Context context, List<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.habitstats, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.habitName.setText(habit.getName());

        // completions = "2025-08-13" -> true
        Map<String, Boolean> completions = habit.getCompletions();

        int totalCompletions = 0;
        int totalMisses = 0;

        if (completions != null) {
            for (Boolean done : completions.values()) {
                if (done) {
                    totalCompletions++;
                } else {
                    totalMisses++;
                }
            }
        }

        holder.completions.setText("Completed: " + totalCompletions);
        holder.misses.setText("Missed: " + totalMisses);
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView habitName, completions, misses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.habitNameStats);
            completions = itemView.findViewById(R.id.habitCompletions);
            misses = itemView.findViewById(R.id.habitMisses);
        }
    }
}
