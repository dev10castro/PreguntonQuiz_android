package com.example.preguntonquiz.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.preguntonquiz.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Constructor vacío obligatorio
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        Button playButton = view.findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AnswersScreen.class);
            startActivity(intent);
        });

        return view;
    }
}
