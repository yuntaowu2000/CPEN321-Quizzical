package com.cpen321.quizzical.quizactivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cpen321.quizzical.HomeActivity;
import com.cpen321.quizzical.R;
import com.cpen321.quizzical.utils.OtherUtils;
import com.google.gson.JsonObject;

public class QuizFinishedActivity extends AppCompatActivity {

    private ImageButton likeQuizButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_finished);

        TextView response = findViewById(R.id.quiz_finished_page_response);
        TextView expMsg = findViewById(R.id.quiz_finished_page_exp_earned);
        Button button = findViewById(R.id.quiz_finished_page_button);

        TextView likeQuizText = findViewById(R.id.like_quiz_text);
        likeQuizButton = findViewById(R.id.like_quiz_vote_button);

        int totalNum = getIntent().getIntExtra(getString(R.string.total_num), 0);
        int correctNum = getIntent().getIntExtra(getString(R.string.correct_num), 0);
        int exp_gotten = getIntent().getIntExtra(getString(R.string.EXP_earned_for_quiz), 0);
        boolean canVote = getIntent().getBooleanExtra(getString(R.string.VOTE_FOR_LIKE), false);
        if (!canVote) {
            likeQuizText.setVisibility(View.INVISIBLE);
            likeQuizButton.setClickable(false);
            likeQuizButton.setVisibility(View.INVISIBLE);
        } else {
            String instructorUID = getIntent().getStringExtra(getString(R.string.INSTRUCTOR_UID));
            int classCode = getIntent().getIntExtra(getString(R.string.CLASS_CODE), 0);
            int quizCode= getIntent().getIntExtra(getString(R.string.QUIZ_CODE), 0);
            String jsonToSend = parseContent(classCode, quizCode, instructorUID);
            likeQuizButton.setOnClickListener(v -> sendLikeToServer(jsonToSend));
        }

        if (correctNum < (double) totalNum / 2) {
            response.setText(String.format(getString(R.string.UI_quiz_finished_low_score_msg), correctNum, totalNum));
            response.setTextColor(Color.BLACK);
        } else if (correctNum == totalNum) {
            response.setText(String.format(getString(R.string.UI_quiz_finished_full_score_msg), correctNum, totalNum));
            response.setTextColor(getResources().getColor(R.color.colorOrangeRed));
            response.setTextSize(16);
        } else {
            response.setText(String.format(getString(R.string.UI_quiz_finished_normal_score_msg), correctNum, totalNum));
            response.setTextColor(getResources().getColor(R.color.colorLawnGreen));
            response.setTextSize(14);
        }

        if (exp_gotten == 0) {
            expMsg.setText("");
        } else {
            expMsg.setText(String.format(getString(R.string.UI_quiz_finished_earned_exp_msg), exp_gotten));
        }

        button.setOnClickListener(view -> onBackButtonClicked());
    }

    private void onBackButtonClicked() {
        Intent intent = new Intent(QuizFinishedActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    private String parseContent(int classCode, int quizCode, String instructorUID) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty(getString(R.string.CLASS_CODE), classCode);
            jsonObject.addProperty(getString(R.string.QUIZ_CODE), quizCode);
            jsonObject.addProperty(getString(R.string.INSTRUCTOR_UID), instructorUID);
        } catch (Exception e) {
            Log.d("parse_result_failed", e.getMessage() + "");
        }
        return jsonObject.toString();
    }

    private void sendLikeToServer(String parsedData) {
        SharedPreferences sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        String uid = sp.getString(getString(R.string.UID), "");
        likeQuizButton.setOnClickListener(null);
        new Thread(() -> OtherUtils.uploadToServer(
                getString(R.string.LIKE_ENDPOINT),
                uid,
                getString(R.string.VOTE_FOR_LIKE),
                parsedData
                )).start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(QuizFinishedActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

}
