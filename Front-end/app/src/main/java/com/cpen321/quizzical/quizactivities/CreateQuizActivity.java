package com.cpen321.quizzical.quizactivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cpen321.quizzical.R;
import com.cpen321.quizzical.PictureActivity;
import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.data.QuizModules;
import com.cpen321.quizzical.data.questions.IQuestion;
import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.cpen321.quizzical.utils.ChoicePair;
import com.cpen321.quizzical.utils.OtherUtils;
import com.cpen321.quizzical.utils.QuizPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateQuizActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private static int QUESTION_PICTURE_CAPTURE_CODE = 2;
    private static int CHOICE_PICTURE_CAPTURE_CODE = 3;

    private LinearLayout questionCreateLayout;

    private Spinner moduleList;

    private List<ImageView> picPreview;
    private List<List<ImageView>> choicesPicPreview;

    private String currModule;
    private Classes currClass;
    private List<IQuestion> questionList;
    private List<Bitmap> imageList;
    private List<List<Bitmap>> choicesImageList;
    private List<List<CheckBox>> correctAnsCheckBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);

        currClass = new Classes(sp.getString(getString(R.string.CURR_CLASS), ""));

        ArrayList<QuizModules> quizModuleList = new ArrayList<>();
        String moduleId = currClass.getClassCode() + getString(R.string.QUIZ_MODULES);
        String moduleListString = sp.getString(moduleId, "");

        if (OtherUtils.stringIsNullOrEmpty(moduleListString)) {
            //this should never happen
            Log.d("failed", "module list is none");
        }

        try {
            String[] modules = moduleListString.split(";");
            for (String m : modules) {
                quizModuleList.add(new QuizModules(m));
            }
        } catch (Exception e) {
            Log.d("parse", "cannot parse class list");
        }

        questionCreateLayout = findViewById(R.id.question_create_layout);

        moduleList = findViewById(R.id.module_list);

        String[] moduleNames = new String[quizModuleList.size()];

        for (int i = 0; i < quizModuleList.size(); i++) {
            moduleNames[i] = quizModuleList.get(i).getModuleName();
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, moduleNames);
        moduleList.setAdapter(categoryAdapter);
        moduleList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int selectedPosition = moduleList.getSelectedItemPosition();
                currModule = moduleNames[selectedPosition];
                Toast.makeText(getBaseContext(), currModule, Toast.LENGTH_LONG).show();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                /*not used*/
            }
        });

        Button addQuestionButton = findViewById(R.id.add_question_button);
        addQuestionButton.setOnClickListener(v -> addNewQuestion());

        questionList = new ArrayList<>();
        imageList = new ArrayList<>();
        picPreview = new ArrayList<>();
        choicesPicPreview = new ArrayList<>();
        choicesImageList = new ArrayList<>();
        correctAnsCheckBoxes = new ArrayList<>();

        Button finishButton = findViewById(R.id.quiz_create_finish);
        finishButton.setOnClickListener(v -> onFinishClicked());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == QUESTION_PICTURE_CAPTURE_CODE)
        {
            Bitmap orignalPic = (Bitmap) Objects.requireNonNull(data.getExtras()).get(getString(R.string.ORIGINAL_IMG));
            Bitmap pic = (Bitmap) data.getExtras().get(getString(R.string.MODIFIED_IMG));
            int questionNum = data.getIntExtra(getString(R.string.QUESTION_NUM), 0);

            imageList.set(questionNum, orignalPic);
            picPreview.get(questionNum).setImageBitmap(pic);
        } else if (requestCode == CHOICE_PICTURE_CAPTURE_CODE) {
            Bitmap pic = (Bitmap) Objects.requireNonNull(data.getExtras()).get(getString(R.string.MODIFIED_IMG));
            int questionNum = data.getIntExtra(getString(R.string.QUESTION_NUM), 0);
            int choiceNum = data.getIntExtra(getString(R.string.CHOICE_NUM), 0);

            choicesImageList.get(questionNum).set(choiceNum, pic);
            choicesPicPreview.get(questionNum).get(choiceNum).setImageBitmap(pic);
        }
    }

    private void addNewQuestion() {

        int MAX_QUESTION_NUM = 20;
        if (questionList.size() >= MAX_QUESTION_NUM) {
            new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                    .setMessage(String.format(getString(R.string.UI_too_many_questions), MAX_QUESTION_NUM))
                    .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
            return;
        }

        View newQuestionView = getLayoutInflater().inflate(R.layout.create_quiz_question_layout, questionCreateLayout, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);

        QuestionsMC q = new QuestionsMC();
        q.setCategory(currClass.getCategory());
        q.setCorrectAnsNum(-1);

        questionList.add(q);
        imageList.add(null); //make sure all the lists are aligned
        choicesImageList.add(new ArrayList<>());
        choicesPicPreview.add(new ArrayList<>());
        correctAnsCheckBoxes.add(new ArrayList<>());

        TextView questionNumberText = newQuestionView.findViewById(R.id.question_number);
        String questionNumber = getString(R.string.QUESTION) + " " + questionList.size();
        questionNumberText.setText(questionNumber);

        ImageView preview = newQuestionView.findViewById(R.id.pic_preview);
        picPreview.add(preview);

        int curr_question_num = questionList.size() - 1;

        int childCount = questionCreateLayout.getChildCount();
        questionCreateLayout.addView(newQuestionView, childCount - 1, params);

        EditText questionInput = newQuestionView.findViewById(R.id.question_input);
        questionInput.setInputType(InputType.TYPE_CLASS_TEXT);
        questionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*not used*/
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*not used*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                q.setQuestion(editable.toString());
            }
        });

        CheckBox questionHasPic = newQuestionView.findViewById(R.id.question_has_pic);
        questionHasPic.setOnClickListener(v -> q.setHasPic(questionHasPic.isChecked()));

        ImageButton takePictureButton = newQuestionView.findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(this, PictureActivity.class);
            takePictureIntent.putExtra(getString(R.string.QUESTION_NUM), curr_question_num);
            takePictureIntent.putExtra(getString(R.string.ORIGINAL_IMG), imageList.get(curr_question_num));
            takePictureIntent.putExtra(getString(R.string.REQUEST_CODE), QUESTION_PICTURE_CAPTURE_CODE);
            startActivityForResult(takePictureIntent, QUESTION_PICTURE_CAPTURE_CODE);
        });

        LinearLayout answersLayout = newQuestionView.findViewById(R.id.answers_layout);

        Button answerInputButton = newQuestionView.findViewById(R.id.answer_input_button);
        answerInputButton.setOnClickListener(v -> addNewAnswer(answersLayout, curr_question_num));
    }

    private void formatImages() {
        if (questionList.size() != imageList.size()) {
            Log.d("create_quiz", "bug in list length alignment!");
            return;
        }
        for (int i = 0 ; i < questionList.size(); i++) {
            if (imageList.get(i) != null) {
                String encodedImage = OtherUtils.encodeImage(imageList.get(i));
                questionList.get(i).setHasPic(true);
                questionList.get(i).setPicSrc(encodedImage);
            }
        }
    }

    private void onFinishClicked() {
        int classCode = currClass.getClassCode();
        CourseCategory category = currClass.getCategory();
        String instructorUID = sp.getString(getString(R.string.UID), "");

        for (int i = 0; i < questionList.size(); i++) {
            QuestionsMC mc = (QuestionsMC)questionList.get(i);

            if (mc.getCorrectAnsNum() == -1) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.UI_warning))
                        .setMessage(String.format(getString(R.string.UI_no_correct_answer), i))
                        .setPositiveButton(R.string.OK, ((dialogInterface, j) -> dialogInterface.dismiss()))
                        .show();
                return;
            }
        }

        formatImages();

        QuizPackage quizPackage = new QuizPackage(classCode, category, instructorUID, currModule, questionList);
        String quizPackJson = quizPackage.toJson();

        new Thread(() -> {
            //TODO: probably needs to get all quiz related info here
            OtherUtils.uploadToServer(instructorUID, getString(R.string.CREATE_QUIZ), quizPackJson);
        }).start();
        Log.d("Quiz_create", quizPackJson);

        new AlertDialog.Builder(this).setMessage(R.string.UI_create_quiz_success_msg)
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }))
                .show();

    }

    private void addNewAnswer(LinearLayout answersLayout, int questionNum) {

        int choiceNum = choicesImageList.get(questionNum).size();

        int MAX_CHOICE_NUM = 6;
        if (choiceNum >= MAX_CHOICE_NUM) {
            new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                    .setMessage(String.format(getString(R.string.UI_too_many_choices), MAX_CHOICE_NUM))
                    .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
            return;
        }

        ImageView choicePicPreview = new ImageView(this);
        choicesPicPreview.get(questionNum).add(choicePicPreview);

        choicesImageList.get(questionNum).add(null);

        boolean[] isPic = {false};
        String[] ans = {""};

        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView answerText = new TextView(this);
        answerText.setText(R.string.UI_enter_answer);
        answerText.setLayoutParams(layoutParams);

        EditText answerInput = new EditText(this);
        answerInput.setLayoutParams(layoutParams);
        answerInput.setHint(R.string.UI_example_answer);
        answerInput.setInputType(InputType.TYPE_CLASS_TEXT);
        answerInput.setMaxLines(1);
        answerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*not used*/
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*not used*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ans[0] = editable.toString();
            }
        });

        Bitmap imageToPassIn = imageList.get(questionNum);
        if (imageToPassIn == null) {
            List<Bitmap> choicesImages = choicesImageList.get(questionNum);
            for (Bitmap b : choicesImages) {
                if (b != null) {
                    imageToPassIn = b;
                    break;
                }
            }
        }

        final Bitmap finalImageToPassIn = imageToPassIn;

        ImageButton answerPic = new ImageButton(this);
        answerPic.setImageResource(android.R.drawable.ic_menu_camera);
        answerPic.setVisibility(View.GONE);
        answerPic.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(this, PictureActivity.class);
            takePictureIntent.putExtra(getString(R.string.QUESTION_NUM), questionNum);
            takePictureIntent.putExtra(getString(R.string.CHOICE_NUM), choiceNum);
            takePictureIntent.putExtra(getString(R.string.ORIGINAL_IMG), finalImageToPassIn);
            takePictureIntent.putExtra(getString(R.string.REQUEST_CODE), CHOICE_PICTURE_CAPTURE_CODE);
            startActivityForResult(takePictureIntent, CHOICE_PICTURE_CAPTURE_CODE);
        });

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(false);
        checkBox.setText(R.string.UI_picture);
        checkBox.setOnClickListener(v -> {
            isPic[0] = checkBox.isChecked();
            answerInput.setVisibility(checkBox.isChecked() ? View.GONE : View.VISIBLE);
            answerPic.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.GONE);
        });

        layout.addView(answerText);
        layout.addView(checkBox);
        layout.addView(answerInput);
        layout.addView(answerPic);
        layout.addView(choicePicPreview);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle("Answer Input")
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> {}));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                generateAnswerRow(answersLayout, questionNum, isPic[0], ans[0], choicesImageList.get(questionNum).get(choiceNum));
                alertDialog.dismiss();
        });
    }

    private void generateAnswerRow(LinearLayout answersLayout, int curr_question_num, boolean isPic, String ans, Bitmap ansBitmap) {

        QuestionsMC currQuestion = (QuestionsMC)questionList.get(curr_question_num);

        String choiceString = ans;
        if (isPic) {
            choiceString = OtherUtils.encodeImage(ansBitmap);
        }

        ChoicePair choicePair = new ChoicePair(isPic, choiceString);

        currQuestion.addChoice(choicePair);

        int curr_choice_num = answersLayout.getChildCount();

        HorizontalScrollView scrollView = new HorizontalScrollView(this);

        LinearLayout layout = new LinearLayout(this);

        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 5, 20, 5);
        layout.setLayoutParams(layoutParams);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        CheckBox isCorrectAnsCheck = new CheckBox(this);
        isCorrectAnsCheck.setText(R.string.UI_correct_answer);
        isCorrectAnsCheck.setLayoutParams(layoutParams);
        List<CheckBox> currQuestionCheckBoxs = correctAnsCheckBoxes.get(curr_question_num);
        currQuestionCheckBoxs.add(isCorrectAnsCheck);

        isCorrectAnsCheck.setOnClickListener(v -> {
            for (CheckBox checkBox : currQuestionCheckBoxs) {
                if (checkBox != isCorrectAnsCheck)
                    checkBox.setChecked(false);
            }
            currQuestion.setCorrectAnsNum(curr_choice_num + 1);
        });

        layout.addView(isCorrectAnsCheck);

        if (isPic) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageBitmap(ansBitmap);
            layout.addView(imageView);
        } else {
            TextView textView = new TextView(this);
            textView.setLayoutParams(layoutParams);
            textView.setText(ans);
            layout.addView(textView);
        }

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setBackgroundResource(R.drawable.ic_baseline_delete_24);
        deleteButton.setLayoutParams(layoutParams);
        deleteButton.setOnClickListener(v -> deleteChoice(answersLayout, curr_question_num, choicePair));
        layout.addView(deleteButton);

        scrollView.addView(layout);
        answersLayout.addView(scrollView);
    }

    private void deleteChoice(LinearLayout answersLayout, int questionNum, ChoicePair choicePair) {
        QuestionsMC q = (QuestionsMC) questionList.get(questionNum);
        int choiceNum = q.getChoiceIndex(choicePair);

        choicesImageList.get(questionNum).remove(choiceNum);
        choicesPicPreview.get(questionNum).remove(choiceNum);

        q.deleteChoice(choiceNum);

        answersLayout.removeViewAt(choiceNum);

        if (q.getCorrectAnsNum() == choiceNum) {
            q.setCorrectAnsNum(0);
        }
    }


}