package com.cpen321.quizzical.mainmenufragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cpen321.quizzical.R;
import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.data.QuizModules;
import com.cpen321.quizzical.data.questions.IQuestion;
import com.cpen321.quizzical.quizactivities.CreateQuizActivity;
import com.cpen321.quizzical.quizactivities.QuizActivity;
import com.cpen321.quizzical.utils.OtherUtils;
import com.cpen321.quizzical.utils.QuizPackage;
import com.cpen321.quizzical.utils.TestQuestionPackage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import katex.hourglass.in.mathlib.MathView;

public class QuizFragment extends Fragment {

    public static SharedPreferences.OnSharedPreferenceChangeListener quizFragmentSPChangeListener;
    private SharedPreferences sp;

    private boolean is_Instructor;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private FloatingActionButton fab;
    private FloatingActionButton edit_fab;
    private FloatingActionButton module_fab;
    private boolean clicked = false;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout quizLinearLayout;

    private String default_url = "http://quizzical.canadacentral.cloudapp.azure.com/quiz?class_code=0&quiz_code=0";
    private Classes currClass;
    private ArrayList<QuizModules> modulesList;
    private ArrayList<View> moduleViewList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupQuiz(String quizUrl, String localCacheModuleName) {
        Context thisContext = this.getContext();
        assert thisContext != null;

        String localCache = sp.getString(localCacheModuleName, "");

        if (OtherUtils.stringIsNullOrEmpty(quizUrl) && OtherUtils.stringIsNullOrEmpty(localCache)) {
            new AlertDialog.Builder(thisContext).setMessage("Not available.")
                    .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        } else {
            Intent quizIntent = new Intent(getActivity(), QuizActivity.class);
            quizIntent.putExtra(getString(R.string.QUIZ_URL), quizUrl);
            quizIntent.putExtra(getString(R.string.LOCAL_CACHE), localCache);
            startActivity(quizIntent);
        }
    }

    private void setupCreateQuiz() {
        if (modulesList.size() > 0) {
            Intent createQuizIntent = new Intent(getActivity(), CreateQuizActivity.class);
            startActivity(createQuizIntent);
        } else {
            Toast.makeText(this.getContext(), R.string.UI_create_module_notification, Toast.LENGTH_LONG).show();
            promptForAddingModule();
        }
    }

    /*private void setupCreateNote() {
        Intent createNoteIntent = new Intent(getActivity(), CreateNoteActivity.class);
        startActivity(createNoteIntent);
    }*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sp = Objects.requireNonNull(getContext()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);

        is_Instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        if (is_Instructor) {
            return inflater.inflate(R.layout.fragment_quiz_teacher, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_quiz, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        Button quizStartButton = view.findViewById(R.id.quiz_fragment_go_to_quiz_button);
        quizStartButton.setOnClickListener(v -> setupQuiz(default_url, ""));

        currClass = new Classes(sp.getString(getString(R.string.CURR_CLASS), ""));
        TextView textView = view.findViewById(R.id.quiz_page_class_info_text);
        textView.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()));

        quizLinearLayout = view.findViewById(R.id.quiz_list_layout);

        parseQuizModulesFromString();
        moduleViewList = new ArrayList<>();
        setupQuizModule();

        swipeRefreshLayout = view.findViewById(R.id.quiz_page_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::updateQuizList).start());

        quizFragmentSPChangeListener = (sp, key) -> onQuizFragmentSPChanged(key);
        sp.registerOnSharedPreferenceChangeListener(quizFragmentSPChangeListener);



        if (is_Instructor) {
            onTeacherViewCreated();
        }
    }

    private void onTeacherViewCreated() {
        rotateOpen = AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this.getContext(), R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this.getContext(), R.anim.to_bottom_anim);

        fab = Objects.requireNonNull(getView()).findViewById(R.id.fab);
        edit_fab = Objects.requireNonNull(getView()).findViewById(R.id.edit_fab);
        module_fab = Objects.requireNonNull(getView()).findViewById(R.id.module_fab);

        assert fab != null;
        fab.setOnClickListener(v -> onAddButtonClicked());

        assert edit_fab != null;
        assert module_fab != null;
        edit_fab.hide();
        edit_fab.setOnClickListener(v -> setupCreateQuiz());
        module_fab.hide();
        module_fab.setOnClickListener(v -> promptForAddingModule());

    }

    private void onAddButtonClicked() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            edit_fab.show();
            module_fab.show();
            edit_fab.setClickable(true);
            module_fab.setClickable(true);
        } else {
            edit_fab.hide();
            module_fab.hide();
            edit_fab.setClickable(false);
            module_fab.setClickable(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {
            edit_fab.startAnimation(fromBottom);
            module_fab.startAnimation(fromBottom);
            fab.startAnimation(rotateOpen);
        } else {
            edit_fab.startAnimation(toBottom);
            module_fab.startAnimation(toBottom);
            fab.startAnimation(rotateClose);
        }
    }

    private void setupNotes(String notesLink) {
        Context thisContext = this.getContext();
        assert thisContext != null;

        if (OtherUtils.stringIsNullOrEmpty(notesLink)) {
            new AlertDialog.Builder(thisContext).setMessage("Not available.")
                    .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        } else {
            //actually should be download a file
            OtherUtils.readFromURL(notesLink);
        }
    }

    private void setupStats(String statsLink) {
        Context thisContext = this.getContext();
        assert thisContext != null;

        AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(thisContext);
        alertDialogBuilder.setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()));

        if (OtherUtils.stringIsNullOrEmpty(statsLink)) {
            alertDialogBuilder.setMessage(String.format(getString(R.string.UI_stats_string), 80.0, 100.0, 90.0));
        } else {
            String val = OtherUtils.readFromURL(statsLink);
            try {
                JSONObject jsonObject = new JSONObject(val);
                double avg = jsonObject.getDouble(getString(R.string.AVERAGE));
                double highest = jsonObject.getDouble(getString(R.string.HIGHEST));
                double your_score = jsonObject.getDouble(getString(R.string.SCORE));
                alertDialogBuilder.setMessage(String.format(getString(R.string.UI_stats_string), avg, highest, your_score));
            } catch (JSONException e) {
                Log.d("parse_json", "failed. " + val);
            }
        }

        alertDialogBuilder.show();
    }

    private View buildViewForEachWrongQuestion(IQuestion question) {
        Context thisContext = this.getContext();
        assert thisContext != null;

        LinearLayout layout = new LinearLayout(thisContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);

        TextView questionNum = new TextView(thisContext);
        String questionNumText = getString(R.string.QUESTION) + " " + question.getID();
        questionNum.setText(questionNumText);
        layout.addView(questionNum);

        if (question.getCategory() == CourseCategory.Math) {
            MathView mathView = new MathView(thisContext);
            mathView.setDisplayText(question.getQuestion());
            mathView.setLayoutParams(layoutParams);
            layout.addView(mathView);
        } else {
            TextView textView = new TextView(thisContext);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            textView.setText(question.getQuestion());
            layout.addView(textView);
        }

        if (question.hasPic()) {
            ImageView imageView = new ImageView(thisContext);
            Bitmap bitmap = OtherUtils.getBitmapFromUrl(question.getPicSrc());
            assert bitmap != null;
            bitmap = Bitmap.createScaledBitmap(bitmap, 350, 200, true);
            imageView.setImageBitmap(bitmap);
            layout.addView(imageView);
        }

        return layout;
    }

    private View setUpWrongQuestionView(QuizPackage qp) {
        Context thisContext = this.getContext();
        assert thisContext != null;

        ScrollView scrollView = new ScrollView(thisContext);

        LinearLayout layout = new LinearLayout(thisContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);

        List<IQuestion> questions = qp.getQuestionList();
        for (IQuestion q : questions) {
            View v = buildViewForEachWrongQuestion(q);
            v.setLayoutParams(layoutParams);
            layout.addView(v);
        }

        scrollView.addView(layout);
        return scrollView;
    }

    private void setupWrongQuestions(String wrongQuestionLink) {
        Context thisContext = this.getContext();
        assert thisContext != null;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisContext);
        alertDialogBuilder.setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()));
        if (OtherUtils.stringIsNullOrEmpty(wrongQuestionLink)) {
            alertDialogBuilder.setView(setUpWrongQuestionView(new TestQuestionPackage().getPackage()));
        } else {
            String val = OtherUtils.readFromURL(wrongQuestionLink);
            QuizPackage qp = new QuizPackage(val);
            alertDialogBuilder.setView(setUpWrongQuestionView(qp));
        }
        alertDialogBuilder.show();
    }

    private void setupQuizModule() {

        for (View v : moduleViewList) {
            quizLinearLayout.removeView(v);
        }

        moduleViewList = new ArrayList<>();

        for (QuizModules qm : modulesList) {
            View layout = getLayoutInflater().inflate(R.layout.quiz_module_layout, quizLinearLayout, false);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 5, 10, 5);
            layout.setLayoutParams(layoutParams);

            quizLinearLayout.addView(layout);
            moduleViewList.add(layout);
            TextView moduleName = layout.findViewById(R.id.quiz_module_name);
            moduleName.setText(qm.getModuleName());

            Button quizButton = layout.findViewById(R.id.go_to_quiz);
            String localCacheModuleName = getString(R.string.QUIZ) + "_" + qm.getModuleName();
            quizButton.setOnClickListener(v -> setupQuiz(qm.getQuizLink(), localCacheModuleName));

            Button notesButton = layout.findViewById(R.id.notes_button);
            notesButton.setOnClickListener(v -> setupNotes(qm.getNotesLink()));

            Button statsButton = layout.findViewById(R.id.stats_button);
            statsButton.setOnClickListener(v -> setupStats(qm.getStatsLink()));

            Button wrongQuestionButton = layout.findViewById(R.id.wrong_question_button);
            wrongQuestionButton.setOnClickListener(v -> setupWrongQuestions(qm.getWrongQuestionLink()));
        }
    }

    private void updateQuizList() {
        Objects.requireNonNull(getActivity()).runOnUiThread(this::setupQuizModule);

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onQuizFragmentSPChanged(String key) {
        if (getContext() == null) {
            return;
        }

        if (key.equals(getString(R.string.CURR_CLASS))) {
            updateClassText();
            parseQuizModulesFromString();
            updateQuizList();
        }
    }

    private void updateClassText() {
        String class_info_string = sp.getString(getString(R.string.CURR_CLASS), "");
        currClass = new Classes(class_info_string);
        if (getView() != null) {
            TextView textView = getView().findViewById(R.id.quiz_page_class_info_text);
            textView.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()));
        }
    }

    private void parseQuizModulesFromString() {
        modulesList = new ArrayList<>();
        String moduleId = currClass.getClassCode() + getString(R.string.QUIZ_MODULES);
        String moduleList = sp.getString(moduleId, "");
//        if (OtherUtils.stringIsNullOrEmpty(classListString)) {
//            String url = getString(R.string.GET_URL) + "/classes?"
//                    + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "") + "&"
//                    + getString(R.string.CLASS_CODE) + "=" + currClass.getClassCode()
//                    + "&type=" + getString(R.string.QUIZ_MODULES);
//            classListString = OtherUtils.readFromURL(url);
//        }

        if (OtherUtils.stringIsNullOrEmpty(moduleList)) {
            return;
        }

        try {
            String[] modules = moduleList.split(";");
            for (String m : modules) {
                modulesList.add(new QuizModules(m));
            }
        } catch (Exception e) {
            Log.d("parse", "cannot parse class list");
        }
    }

    private String parseModuleListToString() {
        StringBuilder strb = new StringBuilder();
        for (QuizModules qm : modulesList) {
            strb.append(qm.toJson()).append(";");
        }
        if (strb.length() > 0) {
            strb.deleteCharAt(strb.length() - 1);
        }

        Log.d("parse_quiz_module_list", strb.toString());
        return strb.toString();
    }

    private void promptForAddingModule() {
        Context thisContext = this.getContext();
        assert thisContext != null;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisContext);
        alertDialogBuilder.setTitle(R.string.UI_create_new_module);

        LinearLayout layout = new LinearLayout(thisContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);

        TextView textView = new TextView(thisContext);
        textView.setText(R.string.UI_enter_module_name);
        textView.setTextSize(13);
        textView.setLayoutParams(layoutParams);
        layout.addView(textView);

        EditText editText = new EditText(thisContext);
        editText.setHint(R.string.UI_example_module_name);
        editText.setLayoutParams(layoutParams);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(editText);

        TextView errorText = new TextView(thisContext);
        errorText.setText("");
        errorText.setTextColor(getResources().getColor(R.color.colorCrimson));
        layout.addView(errorText);

        alertDialogBuilder.setView(layout);

        alertDialogBuilder.setPositiveButton(R.string.UI_submit, (dialogInterface, i) -> {});

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            String newModuleName = editText.getText().toString();

            if (OtherUtils.checkUserName(newModuleName)) {
                QuizModules qm = new QuizModules(newModuleName, currClass.getClassCode(), currClass.getCategory());
                modulesList.add(qm);
                int quiz_code = modulesList.size() - 1;
                String quiz_link = getString(R.string.GET_URL) + "quiz?class_code=" + currClass.getClassCode() + "&quiz_code=" + quiz_code;
                qm.setQuizLink(quiz_link);
                String moduleList = parseModuleListToString();
                String moduleId = currClass.getClassCode() + getString(R.string.QUIZ_MODULES);
                sp.edit().putString(moduleId, moduleList).apply();
                setupQuizModule();
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.UI_invalid_module_name);
            }
        });
    }
}
