package com.egapp.habittrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button buttonLogout;
    Button buttonSettings, buttonDailyHabits, buttonManagement, buttonJournal, buttonStatistics;
    TextView textViewUserName;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);


        auth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.logout);
        buttonSettings = findViewById(R.id.home_settingsButton);
        buttonDailyHabits = findViewById(R.id.home_dailyHabitsButton);
        buttonManagement = findViewById(R.id.home_habitManagementButton);
        buttonJournal = findViewById(R.id.home_journalButton);
        buttonStatistics = findViewById(R.id.home_statisticsButton);
        textViewUserName = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            textViewUserName.setText(user.getEmail());
        }

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HabitManagementActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonDailyHabits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HabitOverviewActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), JournalActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}