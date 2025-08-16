package com.egapp.habittrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {


    TextInputEditText editTextEmail, editTextPassword;
    Button buttonRegister;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textViewReminder;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } //Siehe: https://firebase.google.com/docs/auth/android/password-auth?hl=de#java_2
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


            mAuth= FirebaseAuth.getInstance();
            editTextEmail =findViewById(R.id.email);
            editTextPassword= findViewById(R.id.password);
            buttonRegister = findViewById(R.id.registerButton);
            progressBar = findViewById(R.id.progressBar);
            textViewReminder = findViewById(R.id.registerNow);

            textViewReminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email, password;
                    email = String.valueOf(editTextEmail.getText());                //Registrierung Emaileingabe
                    password = String.valueOf(editTextPassword.getText());          //Registrierung Passworteingabe
                    progressBar.setVisibility(View.VISIBLE);                        //Progressbar für die Regstrierung ist bei Erstanmeldung sichtbar

                    if(TextUtils.isEmpty(email)){
                        Toast.makeText(RegisterActivity.this, "Enter your E-Mail", Toast.LENGTH_SHORT).show();
                        return;
                    }  //Schleife prüft ob Email leer ist. Wenn ja erscheint ein kleines Pop-Up

                    if(TextUtils.isEmpty(password)){
                        Toast.makeText(RegisterActivity.this, "Enter your Password", Toast.LENGTH_SHORT).show();
                        return;
                    }   //Schleife prüft ob Passwort leer ist. Wenn ja erscheint ein kleines Pop-Up

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE); //Nach Registrierung verschwindet Progressbar
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Account created.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });  //Siehe https://firebase.google.com/docs/auth/android/password-auth?hl=de#java_2
                }


            }


            );

    }
}