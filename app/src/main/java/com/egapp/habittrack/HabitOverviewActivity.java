package com.egapp.habittrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitOverviewActivity extends AppCompatActivity {

    TextView overviewDate;
    RecyclerView overviewRecycler;
    Button overviewSwapDateButton;
    DatabaseReference habitOverviewDbReference;
    HabitOverviewAdapter adapterOverview;
    List<Habit> overviewHabitList;
    String selectedDate; // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_overview);

        ImageView backIcon = findViewById(R.id.back_icon);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        overviewDate = findViewById(R.id.overviewCurrentDate);
        overviewRecycler = findViewById(R.id.overviewRecyclerView);
        overviewSwapDateButton = findViewById(R.id.overviewDifferentDateButton);

        // set layout manager (this was missing)
        overviewRecycler.setLayoutManager(new LinearLayoutManager(this));

        // create adapter + list
        overviewHabitList = new ArrayList<>();
        adapterOverview = new HabitOverviewAdapter(this, overviewHabitList);

        // set adapter to recycler
        overviewRecycler.setAdapter(adapterOverview);

        // selected date defaults to today in yyyy-MM-dd
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        overviewDate.setText(selectedDate);

        // give the adapter the date so its checkbox updates will write to completions/{date}
        adapterOverview.setDate(selectedDate);

        // set DB reference for current user (guard for null user)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            habitOverviewDbReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("habits");
        } else {
            Toast.makeText(this, "No authenticated user", Toast.LENGTH_SHORT).show();
            return;
        }

        // load habits (also sets habit.completed by reading completions/{selectedDate})
        loadHabits();

        // optional: show date picker to change selectedDate
        overviewSwapDateButton.setOnClickListener(v -> {
            // you can reuse your existing DatePicker implementation here
            // quick example: open date picker and when date chosen set selectedDate, overviewDate, adapter.setDate(...) and call loadHabits()
            new android.app.DatePickerDialog(
                    HabitOverviewActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        overviewDate.setText(selectedDate);
                        adapterOverview.setDate(selectedDate);
                        loadHabits();
                    },
                    Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date())),
                    Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(new Date())) - 1,
                    Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()))
            ).show();
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }); //Back Icon Navigation

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View popUp) {
                showMenu(popUp);
            }
        });  //Menu Icon Popup

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadHabits() {
        if (habitOverviewDbReference != null) {
            habitOverviewDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    overviewHabitList.clear();
                    for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                        Habit habit = habitSnapshot.getValue(Habit.class);
                        if (habit != null) {
                            // ensure id is set (used for updates)
                            habit.setId(habitSnapshot.getKey());

                            // read completion state for selectedDate (may be null)
                            Boolean completedForDate = null;
                            if (habitSnapshot.child("completions").child(selectedDate).exists()) {
                                completedForDate = habitSnapshot.child("completions").child(selectedDate).getValue(Boolean.class);
                            }
                            habit.setCompleted(completedForDate != null && completedForDate);

                            overviewHabitList.add(habit);
                        }
                    }
                    // optional sorting
                    Collections.sort(overviewHabitList, (h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));

                    // notify adapter
                    adapterOverview.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HabitOverviewActivity.this, "Failed to load habits", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showMenu(View popUp){
        PopupMenu popupMenu = new PopupMenu(HabitOverviewActivity.this, popUp);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.popUp_home){
                    startActivity(new Intent(getApplicationContext(), MainActivity.class)); finish();
                }
                if(menuItem.getItemId() == R.id.popUp_dailyHabits){
                    startActivity(new Intent(getApplicationContext(), HabitOverviewActivity.class)); finish();
                }
                if(menuItem.getItemId() == R.id.popUp_Journal){
                    startActivity(new Intent(getApplicationContext(), JournalActivity.class)); finish();
                }
                if(menuItem.getItemId() == R.id.popUp_habitManagement){
                    startActivity(new Intent(getApplicationContext(), HabitManagementActivity.class)); finish();
                }
                if(menuItem.getItemId() == R.id.popUp_statistics){
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class)); finish();
                }
                if(menuItem.getItemId() == R.id.popUp_settings){
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class)); finish();
                }
                return false;
            }
        });

        popupMenu.show();
    }
}
