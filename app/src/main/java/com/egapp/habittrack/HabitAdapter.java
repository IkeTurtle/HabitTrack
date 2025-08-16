package com.egapp.habittrack;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    Context context;
    List<Habit> habitList;

    public HabitAdapter(Context context, List<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
    }  //Konstruktor

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new HabitViewHolder(view);
    } //Sorgt dafür, dass die Items im RecycleView expandiert werden und richtig angezeigt werden können

    @Override  //Diese Methode befüllt die einzelnen Habits mit Werten und Attributen wie Position
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.textView.setText(habit.getName());

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit or Delete Habit");
            String[] options = {"Edit", "Delete"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) { // Edit
                    AlertDialog.Builder editDialog = new AlertDialog.Builder(context);
                    editDialog.setTitle("Edit Habit");

                    final EditText input = new EditText(context);
                    input.setText(habit.getName());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    editDialog.setView(input);

                    editDialog.setPositiveButton("Save", (dialogInterface, i) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            habit.setName(newName);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("habits")
                                    .child(habit.getId())
                                    .setValue(habit)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Habit updated", Toast.LENGTH_SHORT).show());
                            notifyItemChanged(position);
                        }
                    });
                    editDialog.setNegativeButton("Cancel", null);
                    editDialog.show();

                } else if (which == 1) { // Delete
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("habits")
                            .child(habit.getId())
                            .removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show());
                    habitList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, habitList.size());
                }
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
