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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HabitOverviewActivity extends AppCompatActivity {

    TextView overviewDate;
    RecyclerView overviewRecycler;
    Button overviewSwapDateButton;
    DatabaseReference habitOverviewDbReference;
    HabitOverviewAdapter adapterOverview;
    List<Habit> overviewHabitList;
    String selectedDate;

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
        overviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        overviewHabitList = new ArrayList<>();
        adapterOverview = new HabitOverviewAdapter(this, overviewHabitList);
        overviewRecycler.setAdapter(adapterOverview);
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        overviewDate.setText(selectedDate);
        adapterOverview.setDate(selectedDate);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            habitOverviewDbReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("habits");
        } // Wenn der Nutzer nicht leer ist (eingeloggt) werden hier die Daten zur aktuellen User-ID geladen

        loadHabits();   //Hier werden die Habits aus der Datenbank geladen und im Recyclerview angezeigt

        overviewSwapDateButton.setOnClickListener(v -> {
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
        }); //Datepicker bei Klick auf Button um Tasks für andere Daten als erfüllt setzen zu können

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
                            habit.setId(habitSnapshot.getKey());

                            if (habit.getCompletions() == null) {
                                habit.setCompletions(new HashMap<>());
                            }

                            Boolean completedForDate = habit.getCompletions().get(selectedDate);
                            if (completedForDate == null) {
                                completedForDate = false;
                                habit.getCompletions().put(selectedDate, false);
                                habitOverviewDbReference.child(habit.getId()).child("completions")
                                        .child(selectedDate).setValue(false);
                            } //Hier wird eine leere Checkbox als false gesetzt um dafür zu sorgen, dass wenn ein Tag aktiv ist später in der StatisticActivity die verfehlten Habits gezählt werden können

                            habit.setCompleted(completedForDate);
                            overviewHabitList.add(habit);
                        }
                    }
                    Collections.sort(overviewHabitList, (h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName())); //listensortierung

                    adapterOverview.notifyDataSetChanged(); //Adapter der sich um die Anzeige der Daten in der Liste kümmert
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HabitOverviewActivity.this, "Failed to load habits", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //Hier folgt die Toolbar navigation zwischen den Screens, welche auf allen Screens außer Main angezeigt wird
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
