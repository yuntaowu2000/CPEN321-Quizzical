package com.cpen321.quizzical.QuizActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cpen321.quizzical.Data.CourseCategory;
import com.cpen321.quizzical.Data.Questions.IQuestion;
import com.cpen321.quizzical.Data.Questions.QuestionType;
import com.cpen321.quizzical.Data.Questions.QuestionsMC;
import com.cpen321.quizzical.R;
import com.cpen321.quizzical.Utils.ButtonWrappers.ButtonTypes;
import com.cpen321.quizzical.Utils.ButtonWrappers.IButtons;
import com.cpen321.quizzical.Utils.ButtonWrappers.ImageButtonWrapper;
import com.cpen321.quizzical.Utils.ButtonWrappers.MathButtonWrapper;
import com.cpen321.quizzical.Utils.ChoicePair;
import com.cpen321.quizzical.Utils.OtherUtils;
import com.cpen321.quizzical.Utils.TestQuestionPackage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import katex.hourglass.in.mathlib.MathView;


public class QuizActivity extends AppCompatActivity {

    private static final int totalQuestionNum = 3;
    IButtons selectedChoice;
    IButtons correctChoice;
    TextView infoLabel;
    Button submitButton;
    LinearLayout centerStack;
    LinearLayout questionStack;
    MathView currQuestion;
    ImageView currQuestionPic;
    TextView questionInfoText;
    private int questionNumber;
    private int totalPageNum;
    private List<IQuestion> questions;
    private int correctNumber;
    private ArrayList<Integer> wrongQuestionIds;
    private int quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set up page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        this.questionNumber = 0;

        infoLabel = findViewById(R.id.quiz_page_info_label);
        submitButton = findViewById(R.id.quiz_page_submit_button);
        centerStack = findViewById(R.id.center_stack);
        questionStack = findViewById(R.id.question_stack);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        if (this.questionNumber == 0) {
            setUp();
            while (questions == null || questions.size() == 0) {
                setUp();
            }

        }
        try {
            generatePage();
        } catch (Exception e) {
            Log.d("Generate page error", "not implemented");
        }
    }

    private void setUp() {
        questions = new ArrayList<>();
        TestQuestionPackage testPackage = new TestQuestionPackage();

        quizId = testPackage.GetPackage().getId();

        questions = testPackage.GetPackage().getQuestionsByCategory(CourseCategory.Math, totalQuestionNum);
        totalPageNum = questions.size();

        correctNumber = 0;
        wrongQuestionIds = new ArrayList<>(0);
    }


    private void generatePage() throws Exception {
        IQuestion question = questions.get(this.questionNumber);
        switch (question.getQuestionType()) {
            case MC:
                generateMCPage(question);
                break;
            case Text:
                //TODO: may implement this if we have time
                throw new Exception("Not implemented");
            default:
                generateBlankPage();
        }
    }

    private void generateBlankPage() {
        infoLabel.setText(R.string.UI_error_generating_quiz_page_msg);
        submitButton.setText(R.string.UI_next);
        submitButton.setOnClickListener(view -> onNextClicked());
    }

    private void generateMCPage(IQuestion question) {
        QuestionsMC q = (QuestionsMC) question;
        updateSubmitButtonInfo();

        cleanUpQuestionStack();
        selectedChoice = null;

        if (!OtherUtils.stringIsNullOrEmpty(q.getQuestion())) {

            MathView view = new MathView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
            view.setDisplayText(question.getQuestion());
            questionStack.addView(view);
            currQuestion = view;
        }

        if (q.hasPic()) {
            new Thread(() -> setUpQuestionPicture(q)).start();
        }

        List<IButtons> buttonsList = new ArrayList<>();

        List<ChoicePair> choices = q.getChoices();
        for (ChoicePair choice : choices) {
            buttonsList.add(setUpButtons(choice));
        }

        correctChoice = buttonsList.get(q.getCorrectAnsNum() - 1);

        Collections.shuffle(buttonsList);

        for (IButtons button : buttonsList) {
            if (button.GetButtonType().equals(ButtonTypes.Image)) {
                ImageButtonWrapper imageButton = (ImageButtonWrapper) button;
                centerStack.addView(imageButton.GetButtonAsImageButton());
            } else {
                MathButtonWrapper mathButton = (MathButtonWrapper) button;
                centerStack.addView(mathButton.GetButtonAsMathButton());
            }
        }

    }

    private void cleanUpQuestionStack() {
        if (currQuestion != null)
            questionStack.removeView(currQuestion);
        if (currQuestionPic != null)
            questionStack.removeView(currQuestionPic);
        if (questionInfoText != null)
            questionStack.removeView(questionInfoText);
    }

    private void setUpQuestionPicture(QuestionsMC q) {

        Bitmap pic = OtherUtils.getBitmapFromUrl(q.getPicSrc());
        runOnUiThread(() -> {
            questionInfoText = new TextView(this);
            if (pic == null) {
                questionInfoText.setText(R.string.UI_error_loading_pic_msg);
                questionStack.addView(questionInfoText);
            } else {
                questionInfoText.setText(R.string.UI_quiz_pic_msg);
                questionStack.addView(questionInfoText);

                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
                layoutParams.setMargins(0, 30, 0, 0);
                imageView.setLayoutParams(layoutParams);

                imageView.setImageBitmap(pic);
                questionStack.addView(imageView);
                currQuestionPic = imageView;
            }
        });
    }

    private IButtons setUpButtons(ChoicePair choicePair) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
        layoutParams.setMargins(30, 15, 30, 15);

        final IButtons button;
        if (choicePair.isPic()) {
            ImageButton imageButton = new ImageButton(this);

            imageButton.setBackgroundColor(Color.WHITE);
            imageButton.setLayoutParams(layoutParams);

            new Thread(() ->
            {
                Bitmap image = OtherUtils.getBitmapFromUrl(choicePair.getStr());
                assert image != null;
                Bitmap finalImage = Bitmap.createScaledBitmap(image, 400, 200, true);
                runOnUiThread(() -> imageButton.setImageBitmap(finalImage));
            }).start();

            button = new ImageButtonWrapper(imageButton);
            imageButton.setOnClickListener(view -> onChoiceClicked(button));

        } else {

            MathView mathButton = new MathView(this);

            mathButton.setDisplayText(choicePair.getStr());

            mathButton.setViewBackgroundColor(Color.WHITE);
            mathButton.setLayoutParams(layoutParams);
            mathButton.setClickable(true);

            button = new MathButtonWrapper(mathButton);
            mathButton.setOnClickListener(view -> onChoiceClicked(button));

        }
        return button;
    }

    private void onChoiceClicked(IButtons button) {
        if (selectedChoice != null) {
            selectedChoice.SetBackGroundColor(Color.WHITE);
        }

        selectedChoice = button;
        selectedChoice.SetBackGroundColor(getResources().getColor(R.color.colorAqua));
    }

    private void updateSubmitButtonInfo() {
        submitButton.setText(R.string.UI_submit);
        submitButton.setOnClickListener(view -> onSubmitClicked());
    }

    private void onSubmitClicked() {
        if (!checkAnswer())
            return;

        if (this.questionNumber < totalPageNum - 1) {
            submitButton.setText(R.string.UI_next);
            submitButton.setOnClickListener(view -> onNextClicked());
        } else {
            submitButton.setText(R.string.UI_finish);
            submitButton.setOnClickListener(view -> onFinishClicked());
        }

    }

    private boolean checkAnswer() {
        if (questions.get(this.questionNumber).getQuestionType().equals(QuestionType.MC)) {
            return checkMC();
        }

        return false;
    }

    private boolean checkMC() {

        if (selectedChoice == null) {
            return responseNoAnswerEntered();
        } else if (selectedChoice.equals(correctChoice)) {
            return responseCorrectAnswerEntered();
        } else {
            return responseWrongAnswerEntered();
        }
    }

    private boolean responseNoAnswerEntered() {
        infoLabel.setText(R.string.UI_no_answer_response_msg);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));
        return false;
    }

    private boolean responseCorrectAnswerEntered() {
        correctNumber++;
        String response = String.format(getString(R.string.UI_in_quiz_correct_msg), correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorLawnGreen));
        selectedChoice.SetBackGroundColor(getResources().getColor(R.color.colorLawnGreen));
        return true;
    }

    private boolean responseWrongAnswerEntered() {
        String response = String.format(getString(R.string.UI_in_quiz_wrong_msg), correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));

        selectedChoice.SetBackGroundColor(getResources().getColor(R.color.colorCrimson));
        correctChoice.SetBackGroundColor(getResources().getColor(R.color.colorLawnGreen));

        wrongQuestionIds.add(questions.get(this.questionNumber).getID());
        return true;
    }

    public void onNextClicked() {
        centerStack.removeAllViews();
        this.questionNumber += 1;
        infoLabel.setText("");

        try {
            generatePage();
        } catch (Exception e) {
            Log.d("Generate page error", "not implemented");
        }
    }

    public void onFinishClicked() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        Intent intent = new Intent(QuizActivity.this, QuizFinishedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.correct_num), correctNumber);
        intent.putExtra(getString(R.string.total_num), totalQuestionNum);

        if (!sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false)) {
            String uid = sp.getString(getString(R.string.UID), "");
            String type = String.format(getString(R.string.quiz_result), quizId);

            int prev_EXP = sp.getInt(getString(R.string.EXP), 0);
            int prev_quiz_num = sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0);

            int[] quizNumAndExp = updateQuizNumAndExp(sp, prev_quiz_num, prev_EXP);

            intent.putExtra(getString(R.string.EXP_earned_for_quiz), quizNumAndExp[1] - prev_EXP);

            String parsedResult = parseQuizResults(quizNumAndExp[0], quizNumAndExp[1]);
            new Thread(() -> OtherUtils.uploadToServer(uid, type, parsedResult)).start();
        }


        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    private String parseQuizResults(int quizNum, int exp) {
        Collections.sort(wrongQuestionIds);
        Gson gson = new Gson();
        String jsonForList = gson.toJson(wrongQuestionIds);
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty(getString(R.string.USER_QUIZ_COUNT), quizNum);
            jsonObject.addProperty(getString(R.string.EXP), exp);
            jsonObject.addProperty(getString(R.string.correct_num), correctNumber);
            if (correctNumber != totalQuestionNum)
                jsonObject.addProperty(getString(R.string.wrong_question_ids), jsonForList);

        } catch (Exception e) {
            Log.d("parse result failed", e.getMessage() + "");
        }
        return jsonObject.toString();
    }

    private int[] updateQuizNumAndExp(SharedPreferences sp, int prev_quiz_num, int prev_EXP) {
        int new_quiz_num = prev_quiz_num + 1;
        sp.edit().putInt(getString(R.string.USER_QUIZ_COUNT), new_quiz_num).apply();

        //calculate the new EXP based on the correct number of questions
        //all student will earn a BASIC_EXP for finishing the quiz, regardless of the score
        //using the formula 3/(1+exp(50 - score)) + 1/(1+exp(67-score)) + 1/(1+exp(90-score) to calculate the bonus exp
        //where score is correctNum/totalNum * 100
        double score = (double) correctNumber / (double) totalQuestionNum * 100.0;
        int additional_exp = (int) Math.round(3 / (1 + Math.exp(50.0 - score)) + 1 / (1 + Math.exp(67.0 - score)) + 1 / (1 + Math.exp(90.0 - score)));

        int BASIC_EXP = 10;
        int new_exp = prev_EXP + BASIC_EXP + additional_exp;

        sp.edit().putInt(getString(R.string.EXP), new_exp).apply();

        return new int[]{new_quiz_num, new_exp};
    }
}
