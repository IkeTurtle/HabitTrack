package com.egapp.habittrack;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JournalActivity extends AppCompatActivity {

    EditText journalEditText;
    Button saveButton,viewAllButton, deleteButton;
    DatabaseReference journalDbReference;
    TextView currentDateTitle;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);

        ImageView backIcon = findViewById(R.id.back_icon);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        journalDbReference = FirebaseDatabase.getInstance().getReference();
        saveButton = findViewById(R.id.saveJournalButton);
        viewAllButton = findViewById(R.id.viewAllJournalsButton);
        journalEditText = findViewById(R.id.journalText);
        currentDateTitle = findViewById(R.id.journalCurrentDate);
        deleteButton = findViewById((R.id.deleteJourneyEntry));




        if (user != null) {
            String uid = user.getUid();

            journalDbReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("journalEntries");}
        // Wenn der Nutzer nicht leer ist (eingeloggt) werden hier die Daten zur aktuellen User-ID geladen



            loadTodaysJournal();    //Hier wird das Datum für das aktuelle Datum geladen

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  saveJournal();
                  Toast.makeText(JournalActivity.this, "Journal entry saved", Toast.LENGTH_SHORT).show();
                }
        }); //Speicherfunktion für Einträge

        viewAllButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(
                        JournalActivity.this,
                        (dateView, year, month, dayOfMonth) -> {
                            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            loadJournalForDate(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePicker.show();
            }
        })); //Hier wird mit Hilfe der DatePicker packages ein Pop-Up geöffnet mit dem der User andere Journaleinträge anzeigen kann

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEntry();
            }
        }); //Löschen des aktuell geöffneten Eintrags


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


    private void loadTodaysJournal() {
        String dateKey = java.time.LocalDate.now().toString(); //Laden der Systemzeit
        currentDateTitle.setText(dateKey);
        journalDbReference.child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String text = snapshot.child("text").getValue(String.class);
                    journalEditText.setText(text);
                }
            } //Wenn sich das Datum durch das System ändert wird der angezeigt Eintrag angepasst

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("JournalActivity", "Failed to load today's journal", error.toException());
            }   //onCancelled wird per default vorrausgesetzt für onDataChange
        });
    }
        private void saveJournal() {
            String journalText = journalEditText.getText().toString().trim();
            if (journalText.isEmpty()) {
                Toast.makeText(this, "Please write something first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateKey = (selectedDate != null) ? selectedDate : java.time.LocalDate.now().toString();
            Map<String, String> journalEntry = new HashMap<>(); //Die HashMap wird benötigt um den Journal-Eintrag zum dazugehörigen Datum zu speichern
            journalEntry.put("text", journalText);

            journalDbReference.child(dateKey).setValue(journalEntry)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(JournalActivity.this, "Journal saved!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(JournalActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );  //Eintrag wird zum ausgewählten Datum in DB gespeichert
        }

        private void loadJournalForDate(String dateKey){
            currentDateTitle.setText(dateKey);
            journalDbReference.child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String text = snapshot.child("text").getValue(String.class);
                        journalEditText.setText(text);
                    } else {
                        journalEditText.setText(""); // No entry yet, allow creating a new one
                        Toast.makeText(JournalActivity.this, "No entry for this date. You can create one now.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("JournalActivity", "Failed to load journal for date: " + dateKey, error.toException());
                }
            });
        }   //Ähnliche methode zu loadTodaysJournal(), nur für den aus dem Datepicker ausgewählten Datum

        private void deleteEntry()
        {
            if(selectedDate == null) {
                selectedDate = java.time.LocalDate.now().toString();
            }   //Wenn kein Datum ausgewählt wird wird das aktuelle Datum geladen

            String dateKey = selectedDate;

            journalDbReference.child(dateKey).removeValue()
                    .addOnSuccessListener(unused -> {
                        journalEditText.setText("");
                        Toast.makeText(JournalActivity.this, "Journal entry deleted", Toast.LENGTH_SHORT).show();
                    }) //Löschen des Text für das ausgewählte Datum
                    .addOnFailureListener(e ->
                            Toast.makeText(JournalActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }


        //Hier folgt die Toolbar navigation zwischen den Screens, welche auf allen Screens außer Main angezeigt wird
    private void showMenu(View popUp){
        PopupMenu popupMenu = new PopupMenu(JournalActivity.this, popUp);
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
