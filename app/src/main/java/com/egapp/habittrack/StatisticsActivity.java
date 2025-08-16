package com.egapp.habittrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class StatisticsActivity extends AppCompatActivity {

    TextView statisticsTitle, streakTitle;
    RecyclerView statisticsRecyclerView;
    DatabaseReference statisticsDbReference;
    List<Habit> statisticsHabitList;
    String selectedDate;
    StatisticsAdapter statisticsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        ImageView backIcon = findViewById(R.id.back_icon);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        statisticsTitle =findViewById(R.id.statisticsTitle);
        streakTitle = findViewById(R.id.streakTextView);
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView);
        statisticsDbReference = FirebaseDatabase.getInstance().getReference();
        statisticsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        statisticsHabitList = new ArrayList<>();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


        statisticsAdapter = new StatisticsAdapter(this, statisticsHabitList);
        statisticsRecyclerView.setAdapter(statisticsAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            statisticsDbReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("habits");
        }  // Wenn der Nutzer nicht leer ist (eingeloggt) werden hier die Daten zur aktuellen User-ID geladen


        loadHabits(); //Hier werden die Habits aus der Datenbank geladen und im Recyclerview angezeigt


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });  //Back Icon Navigation

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

    //Hier folgt die Toolbar navigation zwischen den Screens, welche auf allen Screens außer Main angezeigt wird
    private void loadHabits() {
        if (statisticsDbReference != null) {
            statisticsDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    statisticsHabitList.clear();
                    for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                        Habit habit = habitSnapshot.getValue(Habit.class);
                        if (habit != null) {
                            // ensure id is set (used for updates)
                            habit.setId(habitSnapshot.getKey());

                            // ensure completions map is initialized
                            if (habit.getCompletions() == null) {
                                habit.setCompletions(new HashMap<>());
                            }

                            // read completion state for selectedDate
                            Boolean completedForDate = habit.getCompletions().get(selectedDate);
                            if (completedForDate == null) {
                                // If no entry exists, treat as missed (false) and store it
                                completedForDate = false;
                                habit.getCompletions().put(selectedDate, false);
                            }

                            habit.setCompleted(completedForDate);

                            statisticsHabitList.add(habit);
                        }
                    }

                    Collections.sort(statisticsHabitList, (h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));
                    statisticsAdapter.notifyDataSetChanged();

                    updateStreak();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(StatisticsActivity.this, "Failed to load habits", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateStreak() {
        if (statisticsHabitList.isEmpty()) {
            streakTitle.setText("Streak: 0 days");
            return;
        }

        List<String> completedDates = new ArrayList<>();
        for (Habit habit : statisticsHabitList) {
            if (habit.getCompletions() != null) {
                for (String date : habit.getCompletions().keySet()) {
                    if (habit.getCompletions().get(date)) {
                        if (!completedDates.contains(date)) {
                            completedDates.add(date);
                        }
                    }
                }
            }
        } //Hier wird die Anzahl aller Tage gesammelt in denen der Nutzer alle Habits als completed gesetzt hat

        Collections.sort(completedDates, Collections.reverseOrder()); //Hier werden die Tage absteigend sortiert


        int streak = 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Date currentDate = null;
        //Hier wird die Anzahl der nachfolgenden Tage gezählt

        try {
            currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today);
        } catch (Exception e) {
            e.printStackTrace();
            streakTitle.setText("Streak: 0 days");
            return;
        }

        while (true) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate);

            boolean allCompleted = true;
            for (Habit habit : statisticsHabitList) {
                if (habit.getCompletions() == null || habit.getCompletions().get(dateStr) == null || !habit.getCompletions().get(dateStr)) {
                    allCompleted = false;
                    break;
                }
            } //Für jeden Durchgang der Schleife erhöht sich der Counter der streak bis der allCompleted boolean nicht mehr war ist.

            if (!allCompleted) break;

            streak++;

            long millis = currentDate.getTime();
            currentDate = new Date(millis - 24 * 60 * 60 * 1000); //Durdch diese beiden Zeiten wird der vorherige Tag geladen und zur Prüfung bereitgestellt
        }

        streakTitle.setText("Streak: " + streak + " days");
    }

    //Hier folgt die Toolbar navigation zwischen den Screens, welche auf allen Screens außer Main angezeigt wird
    private void showMenu(View popUp){
        PopupMenu popupMenu = new PopupMenu(StatisticsActivity.this, popUp);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.popUp_home){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(menuItem.getItemId() == R.id.popUp_dailyHabits){
                    Intent intent = new Intent(getApplicationContext(), HabitOverviewActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(menuItem.getItemId() == R.id.popUp_Journal){
                    Intent intent = new Intent(getApplicationContext(), JournalActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(menuItem.getItemId() == R.id.popUp_habitManagement){
                    Intent intent = new Intent(getApplicationContext(), HabitManagementActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(menuItem.getItemId() == R.id.popUp_statistics){
                    Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(menuItem.getItemId() == R.id.popUp_settings){
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        popupMenu.show();
    }
}