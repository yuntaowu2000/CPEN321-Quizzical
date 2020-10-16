package com.cpen321.quizzical.PageFunctions;

import android.annotation.SuppressLint;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set up page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        this.questionNumber = getIntent().getIntExtra(getString(R.string.Question_Num), 0);

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

        questions = testPackage.GetPackage().getQuestionsByCategory(CourseCategory.Math, totalQuestionNum);
        totalPageNum = questions.size();
        correctNumber = 0;
    }


    private void generatePage() throws Exception {
        IQuestion question = questions.get(this.questionNumber);
        switch (question.getQuestionType()) {
            case MC:
                generateMCPage(question);
                break;
            case Text:
                throw new Exception("Not implemented");
            default:
                generateBlankPage();
        }
    }

    private void generateBlankPage() {
        infoLabel.setText(R.string.error_generating_quiz_page);
        submitButton.setText(R.string.Next);
        submitButton.setOnClickListener(view -> onNextClicked());
    }

    private void generateMCPage(IQuestion question) {
        QuestionsMC q = (QuestionsMC) question;
        updateSubmitButtonInfo();

        cleanUpQuestionStack();
        selectedChoice = null;

        if (!OtherUtils.StringIsNullOrEmpty(q.getQuestion())) {

            MathView view = new MathView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
            view.setDisplayText(question.getQuestion());
            questionStack.addView(view);
            currQuestion = view;
        }

        if (q.hasPic()) {
            setUpQuestionPicture(q);
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
        questionInfoText = new TextView(this);
        if (pic == null) {
            questionInfoText.setText(R.string.error_loading_pic);
            questionStack.addView(questionInfoText);
        } else {
            questionInfoText.setText(R.string.quiz_pic_hint);
            questionStack.addView(questionInfoText);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
            layoutParams.setMargins(0, 30, 0, 0);
            imageView.setLayoutParams(layoutParams);

            imageView.setImageBitmap(pic);
            questionStack.addView(imageView);
            currQuestionPic = imageView;
        }
    }

    private IButtons setUpButtons(ChoicePair choicePair) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
        layoutParams.setMargins(30, 15, 30, 15);

        final IButtons button;
        if (choicePair.isPic()) {
            Bitmap image = OtherUtils.getBitmapFromUrl(choicePair.getStr());
            image = Bitmap.createScaledBitmap(image, 400, 200, true);

            ImageButton imageButton = new ImageButton(this);
            imageButton.setImageBitmap(image);
            imageButton.setBackgroundColor(Color.WHITE);
            imageButton.setLayoutParams(layoutParams);

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
        submitButton.setText(R.string.SUBMIT);
        submitButton.setOnClickListener(view -> onSubmitClicked());
    }

    private void onSubmitClicked() {
        if (!checkAnswer())
            return;

        if (this.questionNumber < totalPageNum - 1) {
            submitButton.setText(R.string.Next);
            submitButton.setOnClickListener(view -> onNextClicked());
        } else {
            submitButton.setText(R.string.FINISH);
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
        infoLabel.setText(R.string.No_answer_response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));
        return false;
    }

    private boolean responseCorrectAnswerEntered() {
        correctNumber++;
        @SuppressLint("DefaultLocale") String response = String.format("You are correct. You got %d/%d correct.", correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorLawnGreen));
        selectedChoice.SetBackGroundColor(getResources().getColor(R.color.colorLawnGreen));
        return true;
    }

    private boolean responseWrongAnswerEntered() {
        @SuppressLint("DefaultLocale") String response = String.format("You are wrong. You got %d/%d correct.", correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));

        selectedChoice.SetBackGroundColor(getResources().getColor(R.color.colorCrimson));
        correctChoice.SetBackGroundColor(getResources().getColor(R.color.colorLawnGreen));
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
        Intent intent = new Intent(QuizActivity.this, QuizFinishedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.correct_num), correctNumber);
        intent.putExtra(getString(R.string.total_num), totalQuestionNum);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }
}
