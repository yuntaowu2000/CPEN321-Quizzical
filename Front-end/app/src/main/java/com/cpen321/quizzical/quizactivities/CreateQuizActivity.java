package com.cpen321.quizzical.quizactivities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cpen321.quizzical.R;
import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.utils.ChoicePair;
import com.cpen321.quizzical.utils.QuestionPackage;
import com.google.gson.JsonObject;

import java.util.List;

public class CreateQuizActivity extends AppCompatActivity {

    private ImageButton addQuestionButton;
    private List<String> questionList;
    private Spinner categoryList;
    private CheckBox questionHasPic;
    private Button questionInputButton;
    private String[] categories;

    private QuestionPackage questionPackage;

    private CourseCategory currCategory;
    private String currQuestion;
    private Boolean currHasPic;
    private String currPicSrc;
    private List<ChoicePair> currChoices;
    private int currCorrectAnsNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        addQuestionButton = findViewById(R.id.add_question_button);
        addQuestionButton.setOnClickListener(v -> {addQuestionToList();});

        categoryList = findViewById(R.id.category_list);
        categories = getResources().getStringArray(R.array.course_category_array);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        categoryList.setAdapter(categoryAdapter);
        categoryList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()        {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int selectedPosition = categoryList.getSelectedItemPosition();
                switch (selectedPosition) {
                    case 0:
                        currCategory = CourseCategory.Math;
                        break;
                    case 1:
                        currCategory = CourseCategory.English;
                        break;
                    case 2:
                        currCategory = CourseCategory.QuantumPhysic;
                        break;
                    default:
                        currCategory = CourseCategory.Math;
                        break;
                }
                Toast.makeText(getBaseContext(), currCategory.toString(), Toast.LENGTH_LONG).show();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        questionHasPic = findViewById(R.id.question_has_pic);
        questionHasPic.setOnClickListener(v -> currHasPic = questionHasPic.isChecked());

        questionInputButton = findViewById(R.id.question_input_button);
        questionInputButton.setOnClickListener(v -> getQuestionInput());
    }

    private void getQuestionInput() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView questionText = new TextView(this);
        questionText.setText("Enter string for question");
        questionText.setLayoutParams(layoutParams);

        EditText questionInput = new EditText(this);
        questionInput.setLayoutParams(layoutParams);
        questionInput.setHint("What is 1+1");
        questionInput.setInputType(InputType.TYPE_CLASS_TEXT);
        questionInput.setText(currQuestion);
        questionInput.setMaxLines(1);

        TextView picText = new TextView(this);
        picText.setText("Enter string for pic");
        picText.setLayoutParams(layoutParams);

        EditText picInput = new EditText(this);
        picInput.setLayoutParams(layoutParams);
        picInput.setHint("https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png");
        picInput.setInputType(InputType.TYPE_CLASS_TEXT);
        picInput.setText(currPicSrc);
        picInput.setMaxLines(1);

        layout.addView(questionText);
        layout.addView(questionInput);
        layout.addView(picText);
        layout.addView(picInput);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle("Question input")
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> dialogInterface.dismiss()));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            currQuestion = questionInput.getText().toString();
            currPicSrc = picInput.getText().toString();
            alertDialog.dismiss();
        });
    }

    private void addQuestionToList() {
        JsonObject jsonObject = new JsonObject();
        try {
            questionPackage.addMCQuestion(currCategory, currQuestion, currHasPic, currPicSrc, currChoices, currCorrectAnsNum);
        } catch (Exception e) {
            Log.d("add question failed", "failed");
        }
    }
/*
    private void createQuiz() {
        QuestionPackage Quiz = new QuestionPackage();
        addMCQuestion(CourseCategory category, String question,
        boolean hasPic, String picSrc,
                List<ChoicePair> choices, int correctAnsNum)
    }*/
}