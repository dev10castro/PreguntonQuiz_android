package com.example.preguntonquiz.data;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.*;

public class QuestionApiService {

    private final OkHttpClient client = new OkHttpClient();

    public interface QuestionCallback {
        void onQuestionsReady(List<Question> questions);
        void onError(Exception e);
    }

    public void fetchAndTranslateQuestions(QuestionCallback callback) {
        Request request = new Request.Builder()
                .url("https://opentdb.com/api.php?amount=5")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }

                try {
                    String json = response.body().string();
                    JSONObject root = new JSONObject(json);
                    JSONArray results = root.getJSONArray("results");

                    List<Question> questions = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.getJSONObject(i);

                        String questionText = item.getString("question");
                        String correctAnswer = item.getString("correct_answer");

                        JSONArray incorrectArray = item.getJSONArray("incorrect_answers");
                        List<String> incorrectAnswers = new ArrayList<>();
                        for (int j = 0; j < incorrectArray.length(); j++) {
                            incorrectAnswers.add(incorrectArray.getString(j));
                        }

                        questions.add(new Question(questionText, correctAnswer, incorrectAnswers));
                    }

                    // Ahora traducimos todo en paralelo de forma asíncrona
                    translateAllQuestions(questions, callback);

                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    private void translateAllQuestions(List<Question> questions, QuestionCallback callback) {
        AtomicInteger pending = new AtomicInteger(0);
        List<Question> translatedQuestions = Collections.synchronizedList(new ArrayList<>());

        for (Question q : questions) {
            pending.incrementAndGet();

            translateTextAsync(q.getQuestion(), translatedQuestion -> {
                q.setQuestion(translatedQuestion);

                translateTextAsync(q.getCorrectAnswer(), translatedCorrect -> {
                    q.setCorrectAnswer(translatedCorrect);

                    List<String> incorrectAnswers = q.getIncorrectAnswers();
                    List<String> translatedIncorrects = Collections.synchronizedList(new ArrayList<>());
                    AtomicInteger pendingIncorrects = new AtomicInteger(incorrectAnswers.size());

                    if (incorrectAnswers.size() == 0) {
                        finishTranslation(q, translatedQuestions, pending, callback);
                    } else {
                        for (String inc : incorrectAnswers) {
                            translateTextAsync(inc, translatedInc -> {
                                translatedIncorrects.add(translatedInc);
                                if (pendingIncorrects.decrementAndGet() == 0) {
                                    q.setIncorrectAnswers(translatedIncorrects);
                                    finishTranslation(q, translatedQuestions, pending, callback);
                                }
                            });
                        }
                    }
                });
            });
        }
    }

    private void finishTranslation(Question q, List<Question> translatedQuestions, AtomicInteger pending, QuestionCallback callback) {
        translatedQuestions.add(q);
        if (pending.decrementAndGet() == 0) {
            callback.onQuestionsReady(translatedQuestions);
        }
    }

    private void translateTextAsync(String text, TranslationCallback callback) {
        RequestBody body = new FormBody.Builder()
                .add("q", text)
                .add("source", "en")
                .add("target", "es")
                .add("format", "text")
                .build();

        Request request = new Request.Builder()
                .url("https://translate.argosopentech.com/translate")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TRANSLATE", "Error traduciendo: ", e);
                callback.onTranslated(text); // fallback: texto original
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("TRANSLATE", "Error traduciendo: " + response.message());
                    callback.onTranslated(text); // fallback
                    return;
                }
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String translated = obj.getString("translatedText");
                    callback.onTranslated(translated);
                } catch (Exception e) {
                    Log.e("TRANSLATE", "Error parseando traducción", e);
                    callback.onTranslated(text);
                }
            }
        });
    }

    private interface TranslationCallback {
        void onTranslated(String translatedText);
    }
}
