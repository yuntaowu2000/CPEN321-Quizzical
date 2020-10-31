package com.cpen321.quizzical.mainmenufragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cpen321.quizzical.quizactivities.CreateQuizActivity;
import com.cpen321.quizzical.quizactivities.QuizActivity;
import com.cpen321.quizzical.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class QuizFragment extends Fragment {

    private boolean is_Instructor;
    private int class_code;

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private FloatingActionButton fab;
    private FloatingActionButton edit_fab;
    private FloatingActionButton note_fab;
    private boolean clicked = false;

    private Button quizStartButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout quizLinearLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupQuiz() {
        Intent quizIntent = new Intent(getActivity(), QuizActivity.class);
        startActivity(quizIntent);
    }

    private void setupCreateQuiz() {
        Intent createQuizIntent = new Intent(getActivity(), CreateQuizActivity.class);
        startActivity(createQuizIntent);
    }

    /*private void setupCreateNote() {
        Intent createNoteIntent = new Intent(getActivity(), CreateNoteActivity.class);
        startActivity(createNoteIntent);
    }*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sp = Objects.requireNonNull(getContext()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);

        is_Instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);
        class_code = sp.getInt(getString(R.string.CLASS_CODE), 0);

        if (is_Instructor) {
            return inflater.inflate(R.layout.fragment_quiz_teacher, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_quiz, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //TODO: may need to separate for teacher and students
        //TODO: follow what is done in statistic fragment and get values from the server to setup the fragment
        quizStartButton = view.findViewById(R.id.quiz_fragment_go_to_quiz_button);
        quizStartButton.setOnClickListener(v -> setupQuiz());

        swipeRefreshLayout = view.findViewById(R.id.quiz_page_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::updateQuizList).start());

        quizLinearLayout = view.findViewById(R.id.quiz_list_layout);

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
        note_fab = Objects.requireNonNull(getView()).findViewById(R.id.note_fab);

        assert fab != null;
        fab.setOnClickListener(v -> onAddButtonClicked());

        //TODO: implement the actual function for these two buttons
        assert edit_fab != null;
        assert note_fab != null;
        edit_fab.hide();
        edit_fab.setOnClickListener(v -> setupCreateQuiz());
        note_fab.hide();
        note_fab.setOnClickListener(v -> Toast.makeText(this.getContext(), "Note button clicked", Toast.LENGTH_SHORT).show());

    }

    private void onAddButtonClicked() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            edit_fab.show();
            note_fab.show();
            edit_fab.setClickable(true);
            note_fab.setClickable(true);
        } else {
            edit_fab.hide();
            note_fab.hide();
            edit_fab.setClickable(false);
            note_fab.setClickable(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {
            edit_fab.startAnimation(fromBottom);
            note_fab.startAnimation(fromBottom);
            fab.startAnimation(rotateOpen);
        } else {
            edit_fab.startAnimation(toBottom);
            note_fab.startAnimation(toBottom);
            fab.startAnimation(rotateClose);
        }
    }

    private void updateQuizList() {
        Objects.requireNonNull(getActivity()).runOnUiThread(()->{
            View layout = getLayoutInflater().inflate(R.layout.quiz_module_layout, quizLinearLayout, false);
            quizLinearLayout.addView(layout);
            Button quizButton = layout.findViewById(R.id.go_to_quiz);
            quizButton.setOnClickListener(v->setupQuiz());
        });
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        int childCount = quizLinearLayout.getChildCount();

        Log.d("update_quiz_list", "child count: " + childCount);
    }
}
