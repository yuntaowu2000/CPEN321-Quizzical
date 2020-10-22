package com.cpen321.quizzical.mainMenuFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private String serverLink = "http://193.122.108.23:8080/Time";

    private boolean is_Instructor;
    private int curr_class_code;
    
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
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        swipeRefreshLayout = view.findViewById(R.id.statistic_swipe_layout);

        realTimeText = view.findViewById(R.id.statistic_test_text);
        new Thread(this::updateText).start();

        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(this::updateText).start());

        //TODO: implement the same thing for the quiz fragment
        //TODO: load the info from the server
        if (is_Instructor) {
            TextView debug_text = Objects.requireNonNull(getView()).findViewById(R.id.class_statistic_debug_text);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> debug_text.setText("Current class code " + curr_class_code));
            classCodeChangeListener = (sp, key) -> onClassCodeChanged(key, debug_text);
            sp.registerOnSharedPreferenceChangeListener(classCodeChangeListener);
        } else {
            TextView debug_text = Objects.requireNonNull(getView()).findViewById(R.id.leader_board_debug_text);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> debug_text.setText("Current class code " + curr_class_code));
            classCodeChangeListener = (sp, key) -> onClassCodeChanged(key, debug_text);
            sp.registerOnSharedPreferenceChangeListener(classCodeChangeListener);
        }
    }

    private void onClassCodeChanged(String key, TextView debug_text) {
        if (getContext() == null) {
            return;
        }
        String class_code_string = getContext().getString(R.string.CLASS_CODE);
        Log.d("In statistic", "on class code changed called");
        if (key.equals(class_code_string)) {
            int class_code = sp.getInt(class_code_string, 0);
            if (class_code != 0) {
                Log.d("In statistic", "class code changed to " + class_code);
                curr_class_code = class_code;
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> debug_text.setText("Current class code " + curr_class_code));
            }
        }
    }

    private void updateText() {
        //we need to update UI on UI thread, otherwise it will crash the app
        //however, it adds the load onto the Main thread, causing lags
        //so we need to run a new thread which runs this function
        String result = OtherUtils.readFromURL(serverLink);
        result = OtherUtils.stringIsNullOrEmpty(result) ? getString(R.string.UI_server_failure_msg) : result;
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        final String text = result;

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> realTimeText.setText(text));
    }

}
