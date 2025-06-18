package com.example.preguntonquiz.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.preguntonquiz.R;

public class MainActivity extends AppCompatActivity {

    private Button googleButton, facebookButton;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        googleButton = findViewById(R.id.googleSignInButton);
        facebookButton = findViewById(R.id.facebookSignInButton);


        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NickNameScreen.class);
                startActivity(intent);
            }
        });

    }
}