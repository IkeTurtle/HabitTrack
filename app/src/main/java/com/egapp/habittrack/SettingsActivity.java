package com.egapp.habittrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {


    Button buttonPasswordChange;
    FirebaseAuth sAuth;
    ProgressBar progressBarChange;
    TextView textViewUserNameSettings;
    FirebaseUser user;
    String userString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        sAuth = FirebaseAuth.getInstance();
        ImageView backIcon = findViewById(R.id.back_icon);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        buttonPasswordChange = findViewById(R.id.settings_changePwButton);
        textViewUserNameSettings = findViewById(R.id.user_details_settings);
        progressBarChange = findViewById(R.id.progressBarChange);
        user = sAuth.getCurrentUser();


        if(user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            textViewUserNameSettings.setText(user.getEmail());
        }

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

        buttonPasswordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userString = textViewUserNameSettings.getText().toString();
                if(!TextUtils.isEmpty(userString)){
                    ResetPassword();
                }else{
                    Toast.makeText(SettingsActivity.this, "Error when trying to find E-Mail", Toast.LENGTH_SHORT).show();
                }
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;



        });
    }

    private void ResetPassword() {
        progressBarChange.setVisibility(View.VISIBLE);

        sAuth.sendPasswordResetEmail(userString)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SettingsActivity.this, "Reset Password Link has been Send to your E-Mail, you will be logged out now.", Toast.LENGTH_SHORT).show();
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(SettingsActivity.this, "Make sure to check your Junk Folder!", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Error. Password can't be changed at the current moment.", Toast.LENGTH_SHORT).show();
                        progressBarChange.setVisibility(View.INVISIBLE);
                    }
                });

    }
    private void showMenu(View popUp){
        PopupMenu popupMenu = new PopupMenu(SettingsActivity.this, popUp);
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