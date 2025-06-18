package com.example.preguntonquiz.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preguntonquiz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class NickNameScreen extends AppCompatActivity {

    private EditText nicknameEditText;
    private Button saveButton;
    private DatabaseReference usersRef;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick_name_screen);

        nicknameEditText = findViewById(R.id.nicknameInput);
        saveButton = findViewById(R.id.playButton);

        // Limita el campo a 25 caracteres desde código
        nicknameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "Error: no hay sesión activa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        saveButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();

            if (nickname.isEmpty()) {
                Toast.makeText(this, "El nickname no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nickname.length() > 25) {
                Toast.makeText(this, "Máximo 25 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            usersRef.child(firebaseUser.getUid()).child("nickname").setValue(nickname)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Nickname guardado", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(NickNameScreen.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIREBASE", "Error al guardar nickname", e);
                        Toast.makeText(this, "Error al guardar nickname", Toast.LENGTH_SHORT).show();
                    });
        });

    }
}
