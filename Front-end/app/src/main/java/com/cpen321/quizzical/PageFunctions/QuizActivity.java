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

import java.util.*;

import katex.hourglass.in.mathlib.MathView;


public class QuizActivity extends AppCompatActivity {

    private int questionNumber;
    private int totalPageNum;
    private static final int totalQuestionNum = 3;
    private List<IQuestion> questions;
    private int correctNumber;

    IButtons selectedChoice;
    IButtons correctChoice;

    TextView infoLabel;
    Button submitButton;
    LinearLayout centerStack;
    LinearLayout questionStack;

    MathView currQuestion;
    ImageView currQuestionPic;
    TextView questionInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set up page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        this.questionNumber = getIntent().getIntExtra(getString(R.string.Question_Num), 0);

        infoLabel = (TextView)findViewById(R.id.quiz_page_info_label);
        submitButton = (Button)findViewById(R.id.quiz_page_submit_button);
        centerStack = (LinearLayout)findViewById(R.id.center_stack);
        questionStack = (LinearLayout)findViewById(R.id.question_stack);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        if (this.questionNumber == 0)
        {
            SetUp();
            while (questions == null || questions.size() == 0)
            {
                SetUp();
            }

        }
        try {
            GeneratePage();
        } catch (Exception e) {
            Log.d("Generate page error", "not implemented");
        }
    }

    private void SetUp()
    {
        questions = new ArrayList<>();
        TestQuestionPackage testPackage = new TestQuestionPackage();

        questions = testPackage.GetPackage().GetQuestionsByCategory(CourseCategory.Math, totalQuestionNum);
        totalPageNum = questions.size();
        correctNumber = 0;
    }


    private void GeneratePage() throws Exception
    {
        IQuestion question = questions.get(this.questionNumber);
        switch (question.getQuestionType()) {
            case MC:
                GenerateMCPage(question);
                break;
            case Text:
                throw new Exception("Not implemented");
            default:
                GenerateBlankPage();
        }
    }

    private void GenerateBlankPage()
    {
        infoLabel.setText(R.string.error_generating_quiz_page);
        submitButton.setText(R.string.Next);
        submitButton.setOnClickListener(view->OnNextClicked());
    }

    private void GenerateMCPage(IQuestion question)
    {
        QuestionsMC q = (QuestionsMC)question;
        UpdateSubmitButtonInfo();

        CleanUpQuestionStack();
        selectedChoice = null;

        if (!OtherUtils.StringIsNullOrEmpty(q.getQuestion())){

            MathView view = new MathView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
            view.setDisplayText(question.getQuestion());
            questionStack.addView(view);
            currQuestion = view;
        }

        if (q.hasPic())
        {
            SetUpQuestionPicture(q);
        }

        List<IButtons> buttonsList = new ArrayList<>();

        List<ChoicePair> choices = q.getChoices();
        for (ChoicePair choice : choices)
        {
            buttonsList.add(SetUpButtons(choice));
        }

        correctChoice = buttonsList.get(q.getCorrectAnsNum() - 1);

        Collections.shuffle(buttonsList);

        for (IButtons button : buttonsList)
        {
            if (button.GetButtonType().equals(ButtonTypes.Image))
            {
                ImageButtonWrapper imageButton = (ImageButtonWrapper)button;
                centerStack.addView(imageButton.GetButtonAsImageButton());
            }
            else
            {
                MathButtonWrapper mathButton = (MathButtonWrapper)button;
                centerStack.addView(mathButton.GetButtonAsMathButton());
            }
        }

    }

    private void CleanUpQuestionStack() {
        if (currQuestion != null)
            questionStack.removeView(currQuestion);
        if (currQuestionPic != null)
            questionStack.removeView(currQuestionPic);
        if (questionInfoText != null)
            questionStack.removeView(questionInfoText);
    }

    private void SetUpQuestionPicture(QuestionsMC q) {

        Bitmap pic = OtherUtils.getBitmapFromUrl(q.getPicSrc());
        questionInfoText = new TextView(this);
        if (pic == null)
        {
            questionInfoText.setText(R.string.error_loading_pic);
            questionStack.addView(questionInfoText);
        } else {
            questionInfoText.setText(R.string.quiz_pic_hint);
            questionStack.addView(questionInfoText);

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
            layoutParams.setMargins(0,30,0,0);
            imageView.setLayoutParams(layoutParams);

            imageView.setImageBitmap(pic);
            questionStack.addView(imageView);
            currQuestionPic = imageView;
        }
    }

    private IButtons SetUpButtons(ChoicePair choicePair)
    {
        LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        layoutParams.setMargins(30,15,30,15);

        final IButtons button;
        if (choicePair.isPic())
        {
            String str= "<p align=\"middle\"><img src=\""+choicePair.getStr()+"\" height=\"100%\"/></p>";
            MathView mathButton = new MathView(this);
            mathButton.setDisplayText(str);
            mathButton.setViewBackgroundColor(Color.WHITE);
            mathButton.setLayoutParams(layoutParams);
            mathButton.setClickable(true);

            button = new MathButtonWrapper(mathButton);
            mathButton.setOnClickListener(view->OnChioceClicked(button));

        } else {

            MathView mathButton = new MathView(this);
            mathButton.setDisplayText(choicePair.getStr());
            mathButton.setViewBackgroundColor(Color.WHITE);
            mathButton.setLayoutParams(layoutParams);
            mathButton.setClickable(true);

            button = new MathButtonWrapper(mathButton);
            mathButton.setOnClickListener(view->OnChioceClicked(button));

        }
        return button;
    }

    private void OnChioceClicked(IButtons button)
    {
        if (selectedChoice != null)
        {
            selectedChoice.SetBackGroundColor(Color.WHITE);
        }

        selectedChoice = button;
        selectedChoice.SetBackGroundColor(Color.GREEN);
    }

    private void UpdateSubmitButtonInfo()
    {
        submitButton.setText(R.string.submit);
        submitButton.setOnClickListener(view->OnSubmitClicked());
    }

    private void OnSubmitClicked()
    {
        if (!CheckAnswer())
            return;

        if (this.questionNumber < totalPageNum - 1)
        {
            submitButton.setText(R.string.Next);
            submitButton.setOnClickListener(view->OnNextClicked());
        }
        else {
            submitButton.setText(R.string.Finish);
            submitButton.setOnClickListener(view->OnFinishClicked());
        }

    }

    private boolean CheckAnswer()
    {
        if (questions.get(this.questionNumber).getQuestionType().equals(QuestionType.MC))
        {
            return CheckMC();
        }

        return false;
    }

    private boolean CheckMC()
    {

        if (selectedChoice == null)
        {
            return ResponseNoAnswerEntered();
        }
        else if(selectedChoice.equals(correctChoice))
        {
            return ResponseCorrectAnswerEntered();
        }
        else
        {
            return ResponseWrongAnswerEntered();
        }
    }

    private boolean ResponseNoAnswerEntered()
    {
        infoLabel.setText(R.string.No_answer_response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));
        return false;
    }

    private boolean ResponseCorrectAnswerEntered()
    {
        correctNumber++;
        @SuppressLint("DefaultLocale") String response = String.format("You are correct. You got %d/%d correct.", correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorLawnGreen));
        return true;
    }

    private boolean ResponseWrongAnswerEntered()
    {
        @SuppressLint("DefaultLocale") String response = String.format("You are wrong. You got %d/%d correct.", correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));
        return true;
    }

    public void OnNextClicked()
    {
        centerStack.removeAllViews();
        this.questionNumber += 1;
        infoLabel.setText("");

        try {
            GeneratePage();
        } catch (Exception e) {
            Log.d("Generate page error", "not implemented");
        }
    }

    public void OnFinishClicked()
    {
        Intent intent = new Intent(QuizActivity.this, QuizFinishedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.correct_num), correctNumber);
        intent.putExtra(getString(R.string.total_num), totalQuestionNum);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }
}
