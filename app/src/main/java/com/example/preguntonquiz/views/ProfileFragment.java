package com.example.preguntonquiz.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.preguntonquiz.views.MainActivity;
import com.example.preguntonquiz.R;

import java.io.IOException;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private TextView nicknameText;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        nicknameText = view.findViewById(R.id.nicknameText);
        Button changePhotoButton = view.findViewById(R.id.changePhotoButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        // Mostrar nickname desde SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("PREGUNTON", Context.MODE_PRIVATE);
        String nickname = prefs.getString("nickname", "Tu nickname");
        nicknameText.setText(nickname);

        // Botón para cambiar la foto
        changePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        logoutButton.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();

        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
