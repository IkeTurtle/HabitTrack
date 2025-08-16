package com.egapp.habittrack;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class HabitOverviewAdapter extends RecyclerView.Adapter<HabitOverviewAdapter.HabitOverviewViewHolder> {

    private Context context;
    private List<Habit> habitList;
    private String date; // selected date

    public HabitOverviewAdapter(Context context, List<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public HabitOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.overviewrecycler, parent, false);
        return new HabitOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitOverviewViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.nameText.setText(habit.getName());
        holder.checkBox.setOnCheckedChangeListener(null); // reset listener before reusing

        holder.checkBox.setChecked(habit.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            habit.setCompleted(isChecked);

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("habits")
                    .child(habit.getId())
                    .child("completions")
                    .child(date);

            dbRef.setValue(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitOverviewViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        CheckBox checkBox;

        public HabitOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.habit_name);
            checkBox = itemView.findViewById(R.id.habit_checkbox);
        }
    }
}
