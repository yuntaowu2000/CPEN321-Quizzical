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
import androidx.core.app.ActivityCompat;

import com.cpen321.quizzical.HomeActivity;
import com.cpen321.quizzical.R;
import com.cpen321.quizzical.PictureActivity;
import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.data.QuizModules;
import com.cpen321.quizzical.data.questions.IQuestion;
import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.cpen321.quizzical.utils.ChoicePair;
import com.cpen321.quizzical.utils.OtherUtils;
import com.cpen321.quizzical.data.QuizPackage;

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
    private int quiz_code;
    private Classes currClass;
    private List<IQuestion> questionList;
    private List<Bitmap> prevUsedImageList;
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
                quiz_code = quizModuleList.get(selectedPosition).getId();
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
        prevUsedImageList = new ArrayList<>();

        Button finishButton = findViewById(R.id.quiz_create_finish);
        finishButton.setOnClickListener(v -> onFinishClicked());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || data.getExtras() == null) {
            return;
        }
        if(requestCode == QUESTION_PICTURE_CAPTURE_CODE)
        {
            Bitmap orignalPic = (Bitmap) Objects.requireNonNull(data.getExtras()).get(getString(R.string.ORIGINAL_IMG));
            Bitmap pic = (Bitmap) data.getExtras().get(getString(R.string.MODIFIED_IMG));
            int questionNum = data.getIntExtra(getString(R.string.QUESTION_NUM), 0);

            prevUsedImageList.set(questionNum, orignalPic);
            imageList.set(questionNum, pic);
            picPreview.get(questionNum).setImageBitmap(pic);
        } else if (requestCode == CHOICE_PICTURE_CAPTURE_CODE) {
            Bitmap originalPic = (Bitmap) Objects.requireNonNull(data.getExtras()).get(getString(R.string.ORIGINAL_IMG));
            Bitmap pic = (Bitmap) Objects.requireNonNull(data.getExtras()).get(getString(R.string.MODIFIED_IMG));
            int questionNum = data.getIntExtra(getString(R.string.QUESTION_NUM), 0);
            int choiceNum = data.getIntExtra(getString(R.string.CHOICE_NUM), 0);

            prevUsedImageList.set(questionNum, originalPic);
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
        q.setID(questionList.size() + 1);
        q.setCategory(currClass.getCategory());
        q.setCorrectAnsNum(-1);

        questionList.add(q);
        imageList.add(null); //make sure all the lists are aligned
        prevUsedImageList.add(null);
        choicesImageList.add(new ArrayList<>());
        choicesPicPreview.add(new ArrayList<>());
        correctAnsCheckBoxes.add(new ArrayList<>());

        TextView questionNumberText = newQuestionView.findViewById(R.id.question_number);
        String questionNumber = getString(R.string.QUESTION) + " " + questionList.size();
        questionNumberText.setText(questionNumber);

        ImageView preview = newQuestionView.findViewById(R.id.pic_preview);
        picPreview.add(preview);

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
            int updated_curr_question_num = findQuestionPosition(q);
            Intent takePictureIntent = new Intent(this, PictureActivity.class);
            takePictureIntent.putExtra(getString(R.string.QUESTION_NUM), updated_curr_question_num);
            takePictureIntent.putExtra(getString(R.string.ORIGINAL_IMG), prevUsedImageList.get(updated_curr_question_num));
            takePictureIntent.putExtra(getString(R.string.REQUEST_CODE), QUESTION_PICTURE_CAPTURE_CODE);
            startActivityForResult(takePictureIntent, QUESTION_PICTURE_CAPTURE_CODE);
        });

        LinearLayout answersLayout = newQuestionView.findViewById(R.id.answers_layout);

        Button answerInputButton = newQuestionView.findViewById(R.id.answer_input_button);
        answerInputButton.setOnClickListener(v -> addNewAnswer(answersLayout, q));

        ImageButton deleteButton = newQuestionView.findViewById(R.id.delete_question_button);
        deleteButton.setOnClickListener(v -> deleteQuestion(q));

    }

    private int findQuestionPosition (IQuestion q) {
        int pos;
        for (pos = 0; pos < questionList.size(); pos++) {
            if (q.equals(questionList.get(pos))) {
                break;
            }
        }
        return pos;
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
            } else {
                questionList.get(i).setHasPic(false);
            }
        }
    }

    private boolean checkQuestionsValid() {

        if (questionList.size() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.UI_warning))
                    .setMessage(R.string.UI_invalid_quiz)
                    .setPositiveButton(R.string.OK, ((dialogInterface, j) -> dialogInterface.dismiss()))
                    .show();
            return false;
        }

        for (int i = 0; i < questionList.size(); i++) {
            QuestionsMC mc = (QuestionsMC)questionList.get(i);

            //check if the question field is valid
            if (OtherUtils.stringIsNullOrEmpty(mc.getQuestion()) && OtherUtils.stringIsNullOrEmpty(mc.getPicSrc())) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.UI_warning))
                        .setMessage(String.format(getString(R.string.UI_no_question), i + 1))
                        .setPositiveButton(R.string.OK, ((dialogInterface, j) -> dialogInterface.dismiss()))
                        .show();
                return false;
            }

            //check if the choice field is valid
            if (mc.getChoices().size() < 2) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.UI_warning))
                        .setMessage(String.format(getString(R.string.UI_too_few_choices), i))
                        .setPositiveButton(R.string.OK, ((dialogInterface, j) -> dialogInterface.dismiss()))
                        .show();
                return false;
            }

            for (ChoicePair cp : mc.getChoices()) {
                if (OtherUtils.stringIsNullOrEmpty(cp.getStr())) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.UI_warning))
                            .setMessage(String.format(getString(R.string.UI_empty_choice), i))
                            .setPositiveButton(R.string.OK, ((dialogInterface, j) -> dialogInterface.dismiss()))
                            .show();
                    return false;
                }
            }

            //check if the correct answer is given
            if (mc.getCorrectAnsNum() == -1) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.UI_warning))
                        .setMessage(String.format(getString(R.string.UI_no_correct_answer), i))
                        .setPositiveButton(R.string.OK, ((dialogInterface, j) -> dialogInterface.dismiss()))
                        .show();
                return false;
            }
        }

        //every question is valid
        return true;
    }

    private void confirmedFinish() {
        int classCode = currClass.getClassCode();
        CourseCategory category = currClass.getCategory();
        String instructorUID = sp.getString(getString(R.string.UID), "");

        QuizPackage quizPackage = new QuizPackage(classCode, category, instructorUID, currModule, questionList);
        quizPackage.setQuizCode(quiz_code);
        String quizPackJson = quizPackage.toJson();

        String localCacheName = getString(R.string.QUIZ) + "_" + currModule;
        sp.edit().putString(localCacheName, quizPackJson).apply();

        //update EXP point
        int currEXP = sp.getInt(getString(R.string.EXP), 0);
        currEXP += 10;
        sp.edit().putInt(getString(R.string.EXP), currEXP).apply();
        String currEXPVal = String.valueOf(currEXP);

        //update quiz count
        int userQuizCount = sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0);
        userQuizCount += 1;
        sp.edit().putInt(getString(R.string.USER_QUIZ_COUNT), userQuizCount).apply();
        String currQuizCountVal = String.valueOf(userQuizCount);

        new Thread(() -> {
            OtherUtils.uploadToServer(
                    getString(R.string.QUIZ_ENDPOINT),
                    instructorUID,
                    getString(R.string.CREATE_QUIZ),
                    quizPackJson);
            OtherUtils.uploadToServer(
                    getString(R.string.INSTRUCTOR_STATS_ENDPOINT),
                    instructorUID,
                    getString(R.string.EXP),
                    currEXPVal);
            OtherUtils.uploadToServer(
                    getString(R.string.INSTRUCTOR_STATS_ENDPOINT),
                    instructorUID,
                    getString(R.string.USER_QUIZ_COUNT),
                    currQuizCountVal);
        }).start();
        Log.d("Quiz_create", quizPackJson);

        new AlertDialog.Builder(this).setMessage(R.string.UI_create_quiz_success_msg)
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }))
                .show();
    }

    private void onFinishClicked() {
        formatImages();

        if (!checkQuestionsValid()) {
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_finish_create_quiz)
                .setPositiveButton(R.string.YES, ((dialogInterface, i) -> {dialogInterface.dismiss(); confirmedFinish();}))
                .setNegativeButton(R.string.NO, (((dialogInterface, i) -> dialogInterface.dismiss())))
                .show();
    }

    private void addNewAnswer(LinearLayout answersLayout, IQuestion q) {

        int questionNum = findQuestionPosition(q);

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

        ImageButton answerPic = new ImageButton(this);
        answerPic.setImageResource(android.R.drawable.ic_menu_camera);
        answerPic.setVisibility(View.GONE);
        answerPic.setOnClickListener(v -> {
            int updated_question_num = findQuestionPosition(q);
            Intent takePictureIntent = new Intent(this, PictureActivity.class);
            takePictureIntent.putExtra(getString(R.string.QUESTION_NUM), updated_question_num);
            takePictureIntent.putExtra(getString(R.string.CHOICE_NUM), choiceNum);
            takePictureIntent.putExtra(getString(R.string.ORIGINAL_IMG), prevUsedImageList.get(updated_question_num));
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
                int updated_question_num = findQuestionPosition(q);
                generateAnswerRow(answersLayout, q, isPic[0], ans[0], choicesImageList.get(updated_question_num).get(choiceNum));
                alertDialog.dismiss();
        });
    }

    private void generateAnswerRow(LinearLayout answersLayout, IQuestion q, boolean isPic, String ans, Bitmap ansBitmap) {

        QuestionsMC currQuestion = (QuestionsMC)q;
        int questionNum = findQuestionPosition(q);

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
        List<CheckBox> currQuestionCheckBoxs = correctAnsCheckBoxes.get(questionNum);
        currQuestionCheckBoxs.add(isCorrectAnsCheck);

        isCorrectAnsCheck.setOnClickListener(v -> {
            for (CheckBox checkBox : currQuestionCheckBoxs) {
                if (!checkBox.equals(isCorrectAnsCheck))
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
        deleteButton.setOnClickListener(v -> {
            int curr_question_num = findQuestionPosition(q);
            deleteChoice(answersLayout, curr_question_num, choicePair);
        });
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

    private void deleteQuestion(IQuestion q) {
        int questionPos = findQuestionPosition(q);
        questionList.remove(q);
        questionCreateLayout.removeViewAt(questionPos + 1);
        picPreview.remove(questionPos);
        prevUsedImageList.remove(questionPos);
        imageList.remove(questionPos);
        choicesImageList.remove(questionPos);
        choicesPicPreview.remove(questionPos);
        correctAnsCheckBoxes.remove(questionPos);

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_quit_quiz_editing_warning)
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> goBackToHome())
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    public void goBackToHome() {
        Intent intent = new Intent(CreateQuizActivity.this, HomeActivity.class);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }
}