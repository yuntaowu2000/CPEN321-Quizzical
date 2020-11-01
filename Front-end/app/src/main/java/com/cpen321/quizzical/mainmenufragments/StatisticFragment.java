package com.cpen321.quizzical.mainmenufragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cpen321.quizzical.R;
import com.cpen321.quizzical.utils.OtherUtils;

import java.util.Objects;

public class StatisticFragment extends Fragment {

    public static SharedPreferences.OnSharedPreferenceChangeListener classCodeChangeListener;
    private SharedPreferences sp;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView realTimeText;

    private boolean is_Instructor;
    private int curr_class_code;
    private TableLayout boardLayout;
    private Button teachers_leaderboard_btn;
    private boolean teacher_in_statistic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sp = Objects.requireNonNull(getContext()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);
        is_Instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);
        curr_class_code = sp.getInt(getString(R.string.CLASS_CODE), 0);

        if (is_Instructor) {
            return inflater.inflate(R.layout.fragment_class_statistic, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_leaderboard, container, false);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // may need to separate for teacher and students
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        swipeRefreshLayout = view.findViewById(R.id.statistic_swipe_layout);

        realTimeText = view.findViewById(R.id.statistic_time_text);
        new Thread(this::updateText).start();

        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::updateText).start());

        //TODO: implement the same thing for the quiz fragment
        //TODO: load the info from the server
        if (is_Instructor) {
            TextView class_name_text = Objects.requireNonNull(getView()).findViewById(R.id.statistic_class_name_text);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> class_name_text.setText("Current class code " + curr_class_code));
            classCodeChangeListener = (sp, key) -> onClassCodeChanged(key, class_name_text);
            sp.registerOnSharedPreferenceChangeListener(classCodeChangeListener);

            boardLayout = view.findViewById(R.id.class_statistic_board);
            teacher_in_statistic = true;

            teachers_leaderboard_btn = view.findViewById(R.id.teacher_leader_board_btn);
            teachers_leaderboard_btn.setOnClickListener(v -> switchTeacherLeaderboard());

        } else {
            TextView class_name_text = Objects.requireNonNull(getView()).findViewById(R.id.leader_board_class_code_text);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> class_name_text.setText("Current class code " + curr_class_code));
            classCodeChangeListener = (sp, key) -> onClassCodeChanged(key, class_name_text);
            sp.registerOnSharedPreferenceChangeListener(classCodeChangeListener);

            boardLayout = view.findViewById(R.id.student_leaderboard_table);
        }
    }

    private void onClassCodeChanged(String key, TextView debug_text) {
        if (getContext() == null) {
            return;
        }
        String class_code_string = getContext().getString(R.string.CLASS_CODE);
        Log.d("In_statistic", "on class code changed called");
        if (key.equals(class_code_string)) {
            int class_code = sp.getInt(class_code_string, 0);
            if (class_code != 0) {
                Log.d("In_statistic", "class code changed to " + class_code);
                curr_class_code = class_code;
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> debug_text.setText("Current class code " + curr_class_code));
                updateText();
                //should generate statistic table here as well
                if (is_Instructor) {
                    generateClassStatisticTableRows(true);
                } else {
                    generateLeaderboardRows();
                }
            }
        }
    }

    private void updateText() {
        //we need to update UI on UI thread, otherwise it will crash the app
        //however, it adds the load onto the Main thread, causing lags
        //so we need to run a new thread which runs this function
        String timerLink = "http://193.122.108.23:8080/Time";
        String result = OtherUtils.readFromURL(timerLink);
        result = OtherUtils.stringIsNullOrEmpty(result) ? getString(R.string.UI_server_failure_msg) : result;
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        final String text = result;

        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
        {
            realTimeText.setText(text);
            if (is_Instructor && teacher_in_statistic) {
                generateClassStatisticTableRows(false);
            } else {
                generateLeaderboardRows();
            }
        });
    }

    private void generateClassStatisticTableRows(boolean cleanup) {
        int count = boardLayout.getChildCount();
        if (count > 5 || cleanup) {
            boardLayout.removeViews(1, count - 1);
        }
        TableRow newRow = new TableRow(this.getContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(lp);

        newRow.addView(generateTableElement("1"), 0);
        newRow.addView(generateTableElement("test student"), 1);
        newRow.addView(generateTableElement("98"), 2);
        newRow.addView(generateTableElement("100"), 3);

        boardLayout.addView(newRow, 1);
    }

    private TextView generateTableElement(String text) {
        TextView textView = new TextView(this.getContext());

        TableRow.LayoutParams textViewParam = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT, 1.0f);
        textViewParam.setMargins(10, 10, 10, 10);

        textView.setLayoutParams(textViewParam);
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.setTextSize(15);
        return textView;
    }

    private void switchTeacherLeaderboard() {
        boardLayout.removeAllViews();

        TableRow newRow = new TableRow(this.getContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(lp);

        if (teacher_in_statistic) {
            teacher_in_statistic = false;
            teachers_leaderboard_btn.setText(R.string.UI_back_to_statistics);

            newRow.addView(generateTableElement(getString(R.string.UI_leaderboard_number)), 0);
            newRow.addView(generateTableElement(getString(R.string.UI_username)), 1);
            newRow.addView(generateTableElement(getString(R.string.EXP)), 2);

            boardLayout.addView(newRow);
            generateLeaderboardRows();

        } else {
            teacher_in_statistic = true;
            teachers_leaderboard_btn.setText(R.string.UI_teachers_leaderboard);

            newRow.addView(generateTableElement(getString(R.string.UI_leaderboard_number)), 0);
            newRow.addView(generateTableElement(getString(R.string.UI_username)), 1);
            newRow.addView(generateTableElement(getString(R.string.UI_leaderboard_score)), 2);
            newRow.addView(generateTableElement(getString(R.string.EXP)), 3);

            boardLayout.addView(newRow);
            generateClassStatisticTableRows(true);
        }
    }

    private void generateLeaderboardRows() {
        int count = boardLayout.getChildCount();
        if (count > 5) {
            boardLayout.removeViews(1, 5);
        }
        TableRow newRow = new TableRow(this.getContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(lp);

        newRow.addView(generateTableElement("1"), 0);
        newRow.addView(generateTableElement("test student"), 1);
        newRow.addView(generateTableElement("100"), 2);

        boardLayout.addView(newRow, 1);
    }
}
