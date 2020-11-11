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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;

public class StatisticFragment extends Fragment {

    public static SharedPreferences.OnSharedPreferenceChangeListener statisticFragmentOnSPChangeListener;
    private SharedPreferences sp;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        
        //TODO: load the info from the server
        if (isInstructor) {
            TextView classNameText = Objects.requireNonNull(getView()).findViewById(R.id.statistic_class_name_text);

            Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                    classNameText.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()))
            );
            statisticFragmentOnSPChangeListener = (sp, key) -> onStatsFragmentSPChanged(key, classNameText);
            sp.registerOnSharedPreferenceChangeListener(statisticFragmentOnSPChangeListener);

            boardLayout = view.findViewById(R.id.class_statistic_board);
            teacherInStatistic = true;

            teachersLeaderboardBtn = view.findViewById(R.id.teacher_leader_board_btn);
            teachersLeaderboardBtn.setOnClickListener(v -> switchTeacherLeaderboard());

        } else {
            TextView classNameText = Objects.requireNonNull(getView()).findViewById(R.id.leader_board_class_code_text);

            Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                    classNameText.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()))
            );
            statisticFragmentOnSPChangeListener = (sp, key) -> onStatsFragmentSPChanged(key, classNameText);
            sp.registerOnSharedPreferenceChangeListener(statisticFragmentOnSPChangeListener);

            boardLayout = view.findViewById(R.id.student_leaderboard_table);
        }

        new Thread(this::updateLeaderboard).start();

        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::updateLeaderboard).start());
    }

    private void onStatsFragmentSPChanged(String key, TextView class_name_text) {
        if (getContext() == null) {
            return;
        }
        if (key.equals(getContext().getString(R.string.CURR_CLASS))) {

            String classInfoString = sp.getString(getContext().getString(R.string.CURR_CLASS), "");

            if (!OtherUtils.stringIsNullOrEmpty(classInfoString)) {

                currClass = new Classes(classInfoString);
                Log.d("In_statistic", "class code changed to " + currClass.getClassCode());


                Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                        class_name_text.setText(String.format(getString(R.string.UI_current_class_name), currClass.getClassName()))
                );
                updateLeaderboard();
            }
        }
    }

    private void updateLeaderboard() {
        //we need to update UI on UI thread, otherwise it will crash the app
        //however, it adds the load onto the Main thread, causing lags
        //so we need to run a new thread which runs this function
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
        {
            if (isInstructor && teacherInStatistic) {
                generateClassStatisticTableRows("");
            } else if (isInstructor){
                String urlToFetch = getString(R.string.GET_URL) + getString(R.string.INSTRUCTOR_LEADERBOARD_ENDPOINT)
                        + "?" + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "");
                generateLeaderboardRows(urlToFetch);
            } else {
                generateLeaderboardRows("");
            }
        });
    }

    private void generateClassStatisticTableRows(String urlToFetch) {
        int count = boardLayout.getChildCount();

        boardLayout.removeViews(1, count - 1);

        for (int i = 1; i <= 20; i++) {

            TableRow newRow = new TableRow(this.getContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            newRow.setLayoutParams(lp);
            newRow.addView(generateTableElement(String.valueOf(i)), 0);
            newRow.addView(generateTableElement("test student " + i), 1);
            newRow.addView(generateTableElement("98"), 2);
            newRow.addView(generateTableElement("100"), 3);
            boardLayout.addView(newRow, i);
        }


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
            String urlToFetch = getString(R.string.GET_URL) + getString(R.string.INSTRUCTOR_LEADERBOARD_ENDPOINT)
                    + "?" + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "");
            generateLeaderboardRows(urlToFetch);

        } else {
            teacherInStatistic = true;
            teachersLeaderboardBtn.setText(R.string.UI_teachers_leaderboard);

            newRow.addView(generateTableElement(getString(R.string.UI_leaderboard_number)), 0);
            newRow.addView(generateTableElement(getString(R.string.UI_username)), 1);
            newRow.addView(generateTableElement(getString(R.string.UI_leaderboard_score)), 2);
            newRow.addView(generateTableElement(getString(R.string.EXP)), 3);

            boardLayout.addView(newRow);
            generateClassStatisticTableRows("");
        }
    }

    private void generateLeaderboardRows(String url) {
        int count = boardLayout.getChildCount();
        boardLayout.removeViews(1, count - 1);

        if (!OtherUtils.stringIsNullOrEmpty(url)) {
            String leaderboardContent = OtherUtils.readFromURL(url);
            try {
                //shows top 10 and the current user if not in
                JSONArray jsonArray = new JSONArray(leaderboardContent);
                for (int i = 0; i < jsonArray.length() - 2; i++) {
                    TableRow newRow = getTableRow(jsonArray, i + 1, i);

                    boardLayout.addView(newRow, i + 1);
                }
                int userPosition = jsonArray.getInt(jsonArray.length() - 2);
                if (userPosition > 10) {
                    TableRow newRow = getTableRow(jsonArray, userPosition, jsonArray.length() - 1);

                    boardLayout.addView(newRow, 11);
                }
            } catch (JSONException e) {
                Log.d("parse", "parse leaderboard failed");
            }
        } else {
            for (int i = 1; i <= 10; i++) {
                TableRow newRow = new TableRow(this.getContext());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                newRow.setLayoutParams(lp);

                newRow.addView(generateTableElement(String.valueOf(i)), 0);
                newRow.addView(generateTableElement("test student" + i), 1);
                newRow.addView(generateTableElement("100"), 2);

                boardLayout.addView(newRow, i);
            }
        }
    }

    private TableRow getTableRow(JSONArray jsonArray, int userPosition, int objectPosition) throws JSONException {
        TableRow newRow = new TableRow(this.getContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(lp);

        newRow.addView(generateTableElement(String.valueOf(userPosition)), 0);
        newRow.addView(generateTableElement(jsonArray.getJSONObject(objectPosition).getString(getString(R.string.USERNAME))), 1);
        newRow.addView(generateTableElement(jsonArray.getJSONObject(objectPosition).getString(getString(R.string.EXP))), 2);
        return newRow;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLeaderboard();
    }

}
