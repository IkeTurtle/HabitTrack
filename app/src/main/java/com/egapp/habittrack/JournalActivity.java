package com.egapp.habittrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class JournalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);

        ImageView backIcon = findViewById(R.id.back_icon);
        ImageView menuIcon = findViewById(R.id.menu_icon);

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