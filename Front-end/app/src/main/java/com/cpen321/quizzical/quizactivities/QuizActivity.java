package com.cpen321.quizzical.quizactivities;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cpen321.quizzical.HomeActivity;
import com.cpen321.quizzical.R;
import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.data.questions.IQuestion;
import com.cpen321.quizzical.data.questions.QuestionType;
import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.cpen321.quizzical.utils.ChoicePair;
import com.cpen321.quizzical.utils.OtherUtils;
import com.cpen321.quizzical.data.QuizPackage;
import com.cpen321.quizzical.utils.TestQuestionPackage;
import com.cpen321.quizzical.utils.buttonwrappers.ButtonTypes;
import com.cpen321.quizzical.utils.buttonwrappers.IButtons;
import com.cpen321.quizzical.utils.buttonwrappers.ImageButtonWrapper;
import com.cpen321.quizzical.utils.buttonwrappers.MathButtonWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import katex.hourglass.in.mathlib.MathView;


public class QuizActivity extends AppCompatActivity {

    private int totalQuestionNum = 3;
    private IButtons selectedChoice;
    private IButtons correctChoice;
    private TextView infoLabel;
    private Button submitButton;
    private LinearLayout centerStack;
    private LinearLayout questionStack;
    private MathView currQuestion;
    private ImageView currQuestionPic;
    private TextView questionInfoText;
    private int questionNumber;
    private List<IQuestion> questions;
    private int correctNumber;
    private ArrayList<Integer> wrongQuestionIds;
    private int quizId;
    private String instructorUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set up page
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        String quizContent = "";
        if (getIntent() != null && getIntent().getExtras() != null) {
            quizContent = Objects.requireNonNull(getIntent().getExtras()).getString(getString(R.string.QUIZ_CONTENT));
        }

        this.questionNumber = 0;

        infoLabel = findViewById(R.id.quiz_page_info_label);
        submitButton = findViewById(R.id.quiz_page_submit_button);
        centerStack = findViewById(R.id.center_stack);
        questionStack = findViewById(R.id.question_stack);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        if (this.questionNumber == 0) {
            setUp(quizContent);
            while (questions == null || questions.size() == 0) {
                setUp(quizContent);
            }

        }
        try {
            generatePage();
        } catch (Exception e) {
            Log.d("Generate page error", "not implemented");
        }
    }

    private void setUp(String quizContent) {
        questions = new ArrayList<>();

        QuizPackage quizPackage = new QuizPackage(quizContent);
        questions = quizPackage.getQuestionList();

        if (questions.size() == 0) {
            TestQuestionPackage testPackage = new TestQuestionPackage();
            quizPackage = testPackage.getPackage();
            questions = quizPackage.getQuestionList();
        }

        quizId = quizPackage.getQuizCode();
        instructorUID = quizPackage.getInstructorUID();

        totalQuestionNum = questions.size();

        correctNumber = 0;
        wrongQuestionIds = new ArrayList<>(0);
    }


    private void generatePage() {
        IQuestion question = questions.get(this.questionNumber);
        switch (question.getQuestionType()) {
            case MC:
                generateMCPage(question);
                break;
            case Text:
                //TODO: may implement this if we have time
                generateBlankPage();
                Log.d("quiz page", "text questions not implemented");
                break;
            default:
                generateBlankPage();
                break;
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
            String formattedString = String.format(getString(R.string.mid_align_format), question.getQuestion());
            view.setDisplayText(formattedString);
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
        correctChoice.setButtonId(-100);

        //set a random wrong answer for testing use
        Random random = new Random();
        int buttonNum = 0;
        while (q.getCorrectAnsNum() - 1 == buttonNum) {
            buttonNum = random.nextInt(choices.size());
        }
        buttonsList.get(buttonNum).setButtonId(-98);

        Collections.shuffle(buttonsList);

        for (IButtons button : buttonsList) {
            if (button.getButtonType().equals(ButtonTypes.Image)) {
                ImageButtonWrapper imageButton = (ImageButtonWrapper) button;
                centerStack.addView(imageButton.getButtonAsImageButton());
            } else {
                MathButtonWrapper mathButton = (MathButtonWrapper) button;
                centerStack.addView(mathButton.getButtonAsMathButton());
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
        if (pic == null) {
            pic = OtherUtils.decodeImage(q.getPicSrc());
        }
        Bitmap finalPic = pic;
        runOnUiThread(() -> {
            questionInfoText = new TextView(this);
            if (finalPic == null) {
                questionInfoText.setText(R.string.UI_error_loading_pic_msg);
                questionStack.addView(questionInfoText);
            } else {
                questionInfoText.setText(R.string.UI_quiz_pic_msg);
                questionStack.addView(questionInfoText);

                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
                layoutParams.setMargins(0, 30, 0, 0);
                imageView.setLayoutParams(layoutParams);

                imageView.setImageBitmap(finalPic);
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
                if (image == null) {
                    image = OtherUtils.decodeImage(choicePair.getStr());
                }
                assert image != null;
                Bitmap finalImage = Bitmap.createScaledBitmap(image, 400, 200, true);
                runOnUiThread(() -> imageButton.setImageBitmap(finalImage));
            }).start();

            button = new ImageButtonWrapper(imageButton);
            imageButton.setOnClickListener(view -> onChoiceClicked(button));

        } else {

            MathView mathButton = new MathView(this);

            String formattedChoice = String.format(getString(R.string.mid_align_format), choicePair.getStr());

            mathButton.setDisplayText(formattedChoice);

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
            selectedChoice.myButtonSetBackGroundColor(Color.WHITE);
        }

        selectedChoice = button;
        selectedChoice.myButtonSetBackGroundColor(getResources().getColor(R.color.colorAqua));
    }

    private void updateSubmitButtonInfo() {
        submitButton.setText(R.string.UI_submit);
        submitButton.setOnClickListener(view -> onSubmitClicked());
    }

    private void onSubmitClicked() {
        if (!checkAnswer())
            return;

        if (this.questionNumber < totalQuestionNum - 1) {
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
        selectedChoice.myButtonSetBackGroundColor(getResources().getColor(R.color.colorLawnGreen));
        return true;
    }

    private boolean responseWrongAnswerEntered() {
        String response = String.format(getString(R.string.UI_in_quiz_wrong_msg), correctNumber, totalQuestionNum);
        infoLabel.setText(response);
        infoLabel.setTextColor(getResources().getColor(R.color.colorCrimson));

        selectedChoice.myButtonSetBackGroundColor(getResources().getColor(R.color.colorCrimson));
        correctChoice.myButtonSetBackGroundColor(getResources().getColor(R.color.colorLawnGreen));

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
            Log.d("Generate_page_error", "not implemented");
        }
    }

    public void onFinishClicked() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        Intent intent = new Intent(QuizActivity.this, QuizFinishedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(getString(R.string.correct_num), correctNumber);
        intent.putExtra(getString(R.string.total_num), totalQuestionNum);

        Classes mClass = new Classes(sp.getString(getString(R.string.CURR_CLASS), ""));

        if (!sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false)) {
            String uid = sp.getString(getString(R.string.UID), "");
            String type = String.format(getString(R.string.quiz_result), quizId);

            int prev_EXP = sp.getInt(getString(R.string.EXP), 0);
            int prev_quiz_num = sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0);

            int[] quizNumAndExp = updateQuizNumAndExp(sp, prev_quiz_num, prev_EXP);

            intent.putExtra(getString(R.string.EXP_earned_for_quiz), quizNumAndExp[1] - prev_EXP);

            String parsedResult = parseQuizResults(quizNumAndExp[0], quizNumAndExp[1], mClass.getClassCode());
            new Thread(() -> OtherUtils.uploadToServer(
                    getString(R.string.STUDENT_STATS_ENDPOINT),
                    uid,
                    type,
                    parsedResult)).start();
        }

        if (instructorUID != null && !sp.getString(getString(R.string.UID), "").equals(instructorUID)) {
            //null guarding
            // we can pass the parameters to the next page, asking whether the student like the quiz
            intent.putExtra(getString(R.string.INSTRUCTOR_UID), instructorUID);
            intent.putExtra(getString(R.string.VOTE_FOR_LIKE), true);
            intent.putExtra(getString(R.string.QUIZ_CODE), quizId);
            intent.putExtra(getString(R.string.CLASS_CODE), mClass.getClassCode());
        }

        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    private String parseQuizResults(int quizNum, int exp, int classCode) {
        Collections.sort(wrongQuestionIds);
        Gson gson = new Gson();
        String jsonForList = gson.toJson(wrongQuestionIds);
        JsonObject jsonObject = new JsonObject();
        try {
            double score = ((double)correctNumber / (double)totalQuestionNum) * 100.0;
            score = Math.round(score * 100.0) / 100.0;
            jsonObject.addProperty(getString(R.string.USER_QUIZ_COUNT), quizNum);
            jsonObject.addProperty(getString(R.string.EXP), exp);
            jsonObject.addProperty(getString(R.string.SCORE), score);
            jsonObject.addProperty(getString(R.string.CLASS_CODE), classCode);
            jsonObject.addProperty(getString(R.string.INSTRUCTOR_UID), instructorUID);
            jsonObject.addProperty(getString(R.string.QUIZ_CODE), quizId);
            if (correctNumber != totalQuestionNum)
                jsonObject.addProperty(getString(R.string.wrong_question_ids), jsonForList);

        } catch (Exception e) {
            Log.d("parse_result_failed", e.getMessage() + "");
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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.UI_warning)
                .setMessage(R.string.UI_quit_quiz_warning)
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> goBackToHome())
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    public void goBackToHome() {
        Intent intent = new Intent(QuizActivity.this, HomeActivity.class);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }
}
