package com.cpen321.quizzical.qa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cpen321.quizzical.HomeActivity;
import com.cpen321.quizzical.R;

public class QuizFinishedActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_finished);

        TextView response = findViewById(R.id.quiz_finished_page_response);
        TextView exp_msg = findViewById(R.id.quiz_finished_page_exp_earned);
        Button button = findViewById(R.id.quiz_finished_page_button);

        int totalNum = getIntent().getIntExtra(getString(R.string.total_num), 0);
        int correctNum = getIntent().getIntExtra(getString(R.string.correct_num), 0);
        int exp_gotten = getIntent().getIntExtra(getString(R.string.EXP_earned_for_quiz), 0);

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
            exp_msg.setText("");
        } else {
            exp_msg.setText(String.format(getString(R.string.UI_quiz_finished_earned_exp_msg), exp_gotten));
        }

        button.setOnClickListener(view -> onBackClicked());
    }


    private void onBackClicked() {
        Intent intent = new Intent(QuizFinishedActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

}
