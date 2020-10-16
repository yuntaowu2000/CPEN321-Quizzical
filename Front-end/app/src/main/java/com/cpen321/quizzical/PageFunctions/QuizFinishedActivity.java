package com.cpen321.quizzical.PageFunctions;

import android.annotation.SuppressLint;
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

    private TextView response;
    private Button button;

    private int totalNum;
    private int correctNum;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_finished);

        response = findViewById(R.id.quiz_finished_page_response);
        button = findViewById(R.id.quiz_finished_page_button);

        totalNum = getIntent().getIntExtra(getString(R.string.total_num), 0);
        correctNum = getIntent().getIntExtra(getString(R.string.correct_num), 0);

        if (correctNum < totalNum / 2) {
            response.setText(String.format("You got %d/%d correct. Keep learning!", correctNum, totalNum));
            response.setTextColor(Color.BLACK);
        } else if (correctNum == totalNum) {
            response.setText(String.format("You got %d/%d correct. Congratulations!", correctNum, totalNum));
            response.setTextColor(getResources().getColor(R.color.colorOrangeRed));
            response.setTextSize(16);
        } else {
            response.setText(String.format("You got %d/%d correct. Keep learning!", correctNum, totalNum));
            response.setTextColor(getResources().getColor(R.color.colorLawnGreen));
            response.setTextSize(14);
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
