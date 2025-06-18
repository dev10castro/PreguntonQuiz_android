package com.example.preguntonquiz.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preguntonquiz.R;
import com.example.preguntonquiz.model.User;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private GoogleSignInClient mGoogleSignInClient;
    private Button googleLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        googleLoginBtn = findViewById(R.id.googleSignInButton); // Asegúrate de tener este botón en el layout

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Verifica si ya hay sesión iniciada
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserNickname(currentUser.getUid());
        } else {
            googleLoginBtn.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GOOGLE", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        // 👤 Crear nuevo usuario solo si no existe
                                        Map<String, Boolean> answered = new HashMap<>();
                                        User newUser = new User(uid, null, 0, answered);
                                        usersRef.child(uid).setValue(newUser)
                                                .addOnCompleteListener(task -> checkUserNickname(uid));
                                    } else {
                                        // 🧠 Ya existe → solo comprobamos nickname
                                        checkUserNickname(uid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("FIREBASE", "Error al comprobar existencia de usuario", error.toException());
                                }
                            });
                        }
                    } else {
                        Log.w("AUTH", "signInWithCredential:failure", task.getException());
                    }
                });
    }


    private void checkUserNickname(String uid) {
        usersRef.child(uid).child("nickname").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || snapshot.getValue() == null || snapshot.getValue(String.class).trim().isEmpty()) {
                            // No tiene nickname → pedirlo
                            startActivity(new Intent(MainActivity.this, NickNameScreen.class));
                        } else {
                            // Tiene nickname → ir a Home
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FIREBASE", "Error al comprobar nickname", error.toException());
                    }
                }
        );
    }
}
