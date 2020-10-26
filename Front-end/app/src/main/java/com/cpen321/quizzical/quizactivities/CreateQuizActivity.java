package com.cpen321.quizzical.quizactivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.cpen321.quizzical.R;

import java.util.List;

public class CreateQuizActivity extends AppCompatActivity {

    private ImageButton addQuestionButton;
    private List<String> questionList;
    private Spinner categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        addQuestionButton = findViewById(R.id.add_question_button);
        categoryList = findViewById(R.id.category_list);
        String[] categories = getResources().getStringArray(R.array.course_category_array);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        categoryList.setAdapter(categoryAdapter);
    }




/*
    private void addQuestion() {
        course_categories[course_category_list.getSelectedItemPosition()]
        String jsonString = "{\"category\":\""+  +"\", \"age\":21}";
        questionList.add(jsonString);
    }

    private void createQuiz() {
        QuestionPackage Quiz = new QuestionPackage();
        addMCQuestion(CourseCategory category, String question,
        boolean hasPic, String picSrc,
                List<ChoicePair> choices, int correctAnsNum)
    }*/
}