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
import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.utils.OtherUtils;

import java.util.Objects;

public class StatisticFragment extends Fragment {

    public static SharedPreferences.OnSharedPreferenceChangeListener statisticFragmentOnSPChangeListener;
    private SharedPreferences sp;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView realTimeText;

    private boolean isInstructor;
    private TableLayout boardLayout;
    private Button teachersLeaderboardBtn;
    private boolean teacherInStatistic;
    private Classes currClass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sp = Objects.requireNonNull(getContext()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);
        isInstructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        if (isInstructor) {
            return inflater.inflate(R.layout.fragment_class_statistic, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_leaderboard, container, false);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // may need to separate for teacher and students
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        currClass = new Classes(sp.getString(getString(R.string.CURR_CLASS), ""));

        swipeRefreshLayout = view.findViewById(R.id.statistic_swipe_layout);

        realTimeText = view.findViewById(R.id.statistic_time_text);
        new Thread(this::updateText).start();

        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::updateText).start());

        //TODO: implement the same thing for the quiz fragment
        //TODO: load the info from the server
        if (isInstructor) {
            TextView class_name_text = Objects.requireNonNull(getView()).findViewById(R.id.statistic_class_name_text);

            Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                    class_name_text.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()))
            );
            statisticFragmentOnSPChangeListener = (sp, key) -> onStatsFragmentSPChanged(key, class_name_text);
            sp.registerOnSharedPreferenceChangeListener(statisticFragmentOnSPChangeListener);

            boardLayout = view.findViewById(R.id.class_statistic_board);
            teacherInStatistic = true;

            teachersLeaderboardBtn = view.findViewById(R.id.teacher_leader_board_btn);
            teachersLeaderboardBtn.setOnClickListener(v -> switchTeacherLeaderboard());

        } else {
            TextView class_name_text = Objects.requireNonNull(getView()).findViewById(R.id.leader_board_class_code_text);

            Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                    class_name_text.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()))
            );
            statisticFragmentOnSPChangeListener = (sp, key) -> onStatsFragmentSPChanged(key, class_name_text);
            sp.registerOnSharedPreferenceChangeListener(statisticFragmentOnSPChangeListener);

            boardLayout = view.findViewById(R.id.student_leaderboard_table);
        }
    }

    private void onStatsFragmentSPChanged(String key, TextView class_name_text) {
        if (getContext() == null) {
            return;
        }
        if (key.equals(getContext().getString(R.string.CURR_CLASS))) {

            String class_info = sp.getString(getContext().getString(R.string.CURR_CLASS), "");

            if (!OtherUtils.stringIsNullOrEmpty(class_info)) {

                currClass = new Classes(class_info);
                Log.d("In_statistic", "class code changed to " + currClass.getClassCode());


                Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                        class_name_text.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()))
                );
                updateText();
                //should generate statistic table here as well
                if (isInstructor) {
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
            if (isInstructor && teacherInStatistic) {
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

        if (teacherInStatistic) {
            teacherInStatistic = false;
            teachersLeaderboardBtn.setText(R.string.UI_back_to_statistics);

            newRow.addView(generateTableElement(getString(R.string.UI_leaderboard_number)), 0);
            newRow.addView(generateTableElement(getString(R.string.UI_username)), 1);
            newRow.addView(generateTableElement(getString(R.string.EXP)), 2);

            boardLayout.addView(newRow);
            generateLeaderboardRows();

        } else {
            teacherInStatistic = true;
            teachersLeaderboardBtn.setText(R.string.UI_teachers_leaderboard);

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
