package com.example.preguntonquiz.views;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.preguntonquiz.R;
import com.example.preguntonquiz.data.Question;
import com.example.preguntonquiz.data.QuestionApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnswersScreen extends AppCompatActivity {

    private TextView questionText, timerText;
    private RadioGroup answersGroup;

    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 10000; // 10 segundos

    private int currentScore = 0;
    private DatabaseReference usersRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers_screen);

        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        answersGroup = findViewById(R.id.answersGroup);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Cargar puntuación actual
        loadCurrentScore();

        // Cargar preguntas y mostrarlas
        QuestionApiService apiService = new QuestionApiService();
        apiService.fetchAndTranslateQuestions(new QuestionApiService.QuestionCallback() {
            @Override
            public void onQuestionsReady(List<Question> fetchedQuestions) {
                runOnUiThread(() -> {
                    questions.clear();
                    questions.addAll(fetchedQuestions);
                    showNextQuestion();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AnswersScreen.this, "Error cargando preguntas", Toast.LENGTH_LONG).show();
                    Log.e("QUESTION_API", "Error al cargar preguntas", e);
                });
            }
        });
    }

    private void loadCurrentScore() {
        usersRef.child(uid).child("score").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentScore = snapshot.getValue(Integer.class);
                        } else {
                            currentScore = 0;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("FIREBASE", "Error leyendo puntuación", error.toException());
                    }
                }
        );
    }

    private void showNextQuestion() {
        if (currentIndex >= questions.size()) {
            // Fin de las preguntas
            Toast.makeText(this, "¡Has terminado! Puntuación final: " + currentScore, Toast.LENGTH_LONG).show();
            // Aquí podrías ir a un ranking o pantalla final
            finish();
            return;
        }

        Question q = questions.get(currentIndex);
        questionText.setText(q.getQuestion());

        // Preparar respuestas mezcladas
        answersGroup.removeAllViews();

        List<String> answers = new ArrayList<>(q.getIncorrectAnswers());
        answers.add(q.getCorrectAnswer());
        Collections.shuffle(answers);

        for (String answer : answers) {
            RadioButton rb = new RadioButton(this);
            rb.setText(answer);
            rb.setTextSize(18f);
            answersGroup.addView(rb);
        }

        // Iniciar el temporizador
        startTimer();

        // Listener para respuestas
        answersGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == -1) return; // si no hay selección

            countDownTimer.cancel();

            RadioButton selected = findViewById(checkedId);
            String selectedAnswer = selected.getText().toString();

            if (selectedAnswer.equals(q.getCorrectAnswer())) {
                // Correcto: sumar 5 puntos
                currentScore += 5;
                updateScoreInFirebase(currentScore);
                Toast.makeText(this, "Correcto! +5 puntos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Incorrecto", Toast.LENGTH_SHORT).show();
            }

            currentIndex++;
            showNextQuestion();
        });
    }

    private void startTimer() {
        timerText.setText("10");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                Toast.makeText(AnswersScreen.this, "Tiempo agotado!", Toast.LENGTH_SHORT).show();
                currentIndex++;
                showNextQuestion();
            }
        }.start();
    }

    private void updateScoreInFirebase(int newScore) {
        usersRef.child(uid).child("score").setValue(newScore)
                .addOnFailureListener(e -> Log.e("FIREBASE", "Error actualizando puntuación", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
