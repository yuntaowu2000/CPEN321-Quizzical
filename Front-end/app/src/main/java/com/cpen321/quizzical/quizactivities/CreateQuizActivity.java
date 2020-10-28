package com.cpen321.quizzical.quizactivities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.cpen321.quizzical.TestPage;
import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.utils.ChoicePair;
import com.cpen321.quizzical.utils.QuestionPackage;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateQuizActivity extends AppCompatActivity {

    private LinearLayout questionCreateLayout;
    private ImageButton addQuestionButton;
    private List<String> questionList;
    private Spinner categoryList;
    private CheckBox questionHasPic;
    private Button answerInputButton;
    private String[] categories;

    private ImageButton takePictureButton;
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

        questionCreateLayout = findViewById(R.id.question_create_layout);

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
                        currCategory = CourseCategory.Misc;
                        break;
                }
                Toast.makeText(getBaseContext(), currCategory.toString(), Toast.LENGTH_LONG).show();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        questionHasPic = findViewById(R.id.question_has_pic);
        questionHasPic.setOnClickListener(v -> currHasPic = questionHasPic.isChecked());

        answerInputButton = findViewById(R.id.answer_input_button);
        answerInputButton.setOnClickListener(v -> addNewAnswer());

        takePictureButton = findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(this, TestPage.class);
            startActivity(takePictureIntent);});



    }

    private void addNewAnswer() {
        //Todo: use a certain stackoverflow post to fix this layout.
        String answer = "";
        String pic = "";
        final boolean[] isPic = {false};

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        TextView answerText = new TextView(this);
        answerText.setText("Enter answer");
        answerText.setLayoutParams(layoutParams);

        EditText answerInput = new EditText(this);
        answerInput.setLayoutParams(layoutParams);
        answerInput.setHint("What is 1+1");
        answerInput.setInputType(InputType.TYPE_CLASS_TEXT);
        answerInput.setText(answer);
        answerInput.setMaxLines(1);

        ImageButton answerPic = new ImageButton(this);
        answerPic.setImageResource(android.R.drawable.ic_menu_camera);
        answerPic.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(this, TestPage.class);
            startActivity(takePictureIntent);});

        CheckBox checkBox = findViewById(R.id.question_has_pic);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPic[0] = checkBox.isChecked();
                answerPic.setVisibility(checkBox.isChecked()?View.GONE:View.VISIBLE);
                answerInput.setVisibility(checkBox.isChecked()?View.VISIBLE:View.GONE);
            }
        });

        layout.addView(answerText);
        layout.addView(checkBox);
        layout.addView(answerInput);
        layout.addView(answerPic);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle("Question input")
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> dialogInterface.dismiss()));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            currQuestion = answerInput.getText().toString();
            alertDialog.dismiss();
        });
        // create new row here
    }

    private void addQuestionToList() {
        JsonObject jsonObject = new JsonObject();
        try {
            currChoices= Arrays.asList(new ChoicePair(false, "$$2$$"), new ChoicePair(false, "$$3$$"));
            currCorrectAnsNum = 1;
            questionPackage.addMCQuestion(currCategory, currQuestion, currHasPic, currPicSrc, currChoices, currCorrectAnsNum);
        } catch (Exception e) {
            Log.d("add question failed", "failed");
            Log.d(e.getMessage(),"except");
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