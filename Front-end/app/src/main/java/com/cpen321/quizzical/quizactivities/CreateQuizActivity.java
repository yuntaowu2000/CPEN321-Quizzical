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
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cpen321.quizzical.utils.OtherUtils.getBitmapFromUrl;

public class CreateQuizActivity extends AppCompatActivity {

    private LinearLayout questionCreateLayout;

    private Spinner categoryList;

    private EditText questionInput;

    private CheckBox questionHasPic;
    private ImageButton takePictureButton;

    private LinearLayout answersLayout;
    private List<LinearLayout> answerRows;

    private Button answerInputButton;

    private ImageButton addQuestionButton;

    private List<String> questionList;
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

        categoryList = findViewById(R.id.category_list);
        String[] categories = getResources().getStringArray(R.array.course_category_array);
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

        questionInput = findViewById(R.id.question_input);
        questionInput.setOnClickListener(v -> currQuestion = questionInput.getText().toString());

        questionHasPic = findViewById(R.id.question_has_pic);
        questionHasPic.setOnClickListener(v -> currHasPic = questionHasPic.isChecked());

        takePictureButton = findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(this, TestPage.class);
            startActivity(takePictureIntent);});

        answersLayout = findViewById(R.id.answers_layout);
        answerRows = new ArrayList<>();

        answerInputButton = findViewById(R.id.answer_input_button);
        answerInputButton.setOnClickListener(v -> addNewAnswer());

        addQuestionButton = findViewById(R.id.add_question_button);
        addQuestionButton.setOnClickListener(v -> {addQuestionToList();});

        currCategory = CourseCategory.Misc;
        currQuestion = "";
        currHasPic = false;
        currPicSrc = "";
        currChoices = new ArrayList<>();
        currCorrectAnsNum = 0;
    }

    private void addNewAnswer() {
        //Todo: use a certain stackoverflow post to fix this layout.
        final String[] answer = {""};
        String pic = "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png"; // some default picture
        final boolean[] isPic = {false};

        final LinearLayout.LayoutParams[] layoutParams = {new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)};
        layoutParams[0].setMargins(10, 5, 10, 5);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        TextView answerText = new TextView(this);
        answerText.setText("Enter answer");
        answerText.setLayoutParams(layoutParams[0]);

        EditText answerInput = new EditText(this);
        answerInput.setLayoutParams(layoutParams[0]);
        answerInput.setHint("What is 1+1");
        answerInput.setInputType(InputType.TYPE_CLASS_TEXT);
        answerInput.setText(answer[0]);
        answerInput.setMaxLines(1);

        ImageButton answerPic = new ImageButton(this);
        answerPic.setImageResource(android.R.drawable.ic_menu_camera);
        answerPic.setVisibility(View.GONE);
        answerPic.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(this, TestPage.class);
            startActivity(takePictureIntent);});

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(false);
        checkBox.setText("Picture ");
        checkBox.setOnClickListener(v -> {
            isPic[0] = checkBox.isChecked();
            answerInput.setVisibility(checkBox.isChecked()?View.GONE:View.VISIBLE);
            answerPic.setVisibility(checkBox.isChecked()?View.VISIBLE:View.GONE);
        });

        layout.addView(answerText);
        layout.addView(checkBox);
        layout.addView(answerInput);
        layout.addView(answerPic);

        //answersLayout.addView(layout);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle("Question input")
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> dialogInterface.dismiss()));
        // add cancel


        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            answer[0] = answerInput.getText().toString();
            ChoicePair newAnswer = new ChoicePair(isPic[0], isPic[0]?pic: answer[0]);

            Log.d("Hi_op", ""+newAnswer.isPic()+" "+ answer[0]);
            currChoices.add(newAnswer);

            layoutParams[0] = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(layoutParams[0]);
            // I want a (index num textview?), checkbox for correct, textview or imageview, edit button, delete
            //edit opens dialog box, everything filled in
            CheckBox isCorrect = new CheckBox(this);
            isCorrect.setLayoutParams(layoutParams[0]);
            isCorrect.setChecked(currChoices.indexOf(newAnswer) == currCorrectAnsNum);
            isCorrect.setEnabled(currChoices.indexOf(newAnswer) != currCorrectAnsNum);
            // if checked, uncheck all other checkbox, else do nothing.
            isCorrect.setOnClickListener(u -> {
                Log.d("Hi_op", "checked");
                if (isCorrect.isChecked()) {
                    for (int i = 0; i < answersLayout.getChildCount(); i++) {
                        View child = answersLayout.getChildAt(i);
                        if (child instanceof LinearLayout) {
                            for (int j = 0; j < ((LinearLayout) child).getChildCount(); j++) {
                                View rowChild = ((LinearLayout) child).getChildAt(j);
                                if (rowChild instanceof CheckBox) {
                                    Log.d("Hi_op", "   yay2");
                                    ((CheckBox) rowChild).setChecked(false);
                                    ((CheckBox) rowChild).setEnabled(true);
                                    // to do:  I need to reason how to change the correct ans num, maybe by check i j values after deletion
                                    // so lets add delete button first
                                    // then after these we can do edit based on same ideas
                                }
                            }
                        }
                    }
                    isCorrect.setEnabled(false);
                    isCorrect.setChecked(true);
                }
            });

            TextView answerContent = new TextView(this);
            answerContent.setText(answer[0]);
            answerContent.setLayoutParams(layoutParams[0]);

            ImageView answerImage = new ImageView(this);
            answerImage.setImageBitmap(getBitmapFromUrl(pic));
            answerImage.setLayoutParams(layoutParams[0]);

            Button editButton = new Button(this);
            editButton.setLayoutParams(layoutParams[0]);
            editButton.setText("Edit");
            editButton.setOnClickListener(w -> {
            });

            row.addView(isCorrect);
            row.addView(isPic[0]?answerImage:answerContent);
            row.addView(editButton);
            answerRows.add(row);

            answersLayout.addView(row);
            alertDialog.dismiss();
        });
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