package com.egapp.habittrack;
import com.egapp.habittrack.Habit;
import com.egapp.habittrack.HabitAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;


public class HabitManagementActivity extends AppCompatActivity {

    Button addHabit;
    EditText newHabitText;
    RecyclerView habitOverviewList;
    DatabaseReference habitManagementDbReference;
    List<Habit> habitList;
    HabitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_habit_management);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        habitManagementDbReference = FirebaseDatabase.getInstance().getReference();
        ImageView backIcon = findViewById(R.id.back_icon);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        addHabit = findViewById(R.id.addHabitButton);
        newHabitText = findViewById(R.id.habitNameEditText);
        habitOverviewList = findViewById(R.id.habitsRecyclerView);
        habitOverviewList.setLayoutManager(new LinearLayoutManager(this));
        habitList = new ArrayList<>();
        adapter = new HabitAdapter(this, habitList);
        habitOverviewList.setAdapter(adapter);

        if (user != null) {
            String uid = user.getUid();

            habitManagementDbReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("habits");}
        // Wenn der Nutzer nicht leer ist (eingeloggt) werden hier die Daten zur aktuellen User-ID geladen


        loadHabits();   //Hier werden die Habits aus der Datenbank geladen und im Recyclerview angezeigt

        addHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewHabit();
            }
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
        if (habitManagementDbReference != null) {
            habitManagementDbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    habitList.clear(); //Liste wird beim Laden geleert, sodass die Liste keine Dopplungen hat
                    for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                        Habit habit = habitSnapshot.getValue(Habit.class);
                        if (habit != null) {
                            habit.setId(habitSnapshot.getKey());
                            habitList.add(habit);
                        }
                    }
                    Collections.sort(habitList, (h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName())); //Sortierung der Liste
                    adapter.notifyDataSetChanged(); //Der Adapter kümmert sich darum, dass die Daten aus der DB in der Liste angezeigt werden können
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HabitManagementActivity.this, "Failed to load habits", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addNewHabit() {
        String habitName = newHabitText.getText().toString().trim();

         if (habitManagementDbReference != null) {
                String id = UUID.randomUUID().toString(); //Jeder Habit bekommt eine eigene ID, um später besser Attribute für individuelle Habits auswerten zu können
                Habit habit = new Habit(id, habitName, false, "");
                habitManagementDbReference.child(id).setValue(habit)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Habit added", Toast.LENGTH_SHORT).show();
                            newHabitText.setText("");
                        }) //Hinzufügen des neuen Habits zur DB
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show());
            }
        }

    //Hier folgt die Toolbar navigation zwischen den Screens, welche auf allen Screens außer Main angezeigt wird
    private void showMenu(View popUp){
        PopupMenu popupMenu = new PopupMenu(HabitManagementActivity.this, popUp);
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