package com.cpen321.quizzical.MainMenuFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cpen321.quizzical.R;
import com.cpen321.quizzical.Utils.OtherUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StatisticFragment extends Fragment {

    SharedPreferences sp;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView realTimeText;
    String serverLink = "http://193.122.108.23:8080/Time";

    Animation rotateOpen, rotateClose, fromBottom, toBottom;
    FloatingActionButton fab;

    boolean is_Instructor;
    int class_code;
    int course_category = -1;

    public StatisticFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        rotateOpen = AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this.getContext(), R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this.getContext(), R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this.getContext(), R.anim.to_bottom_anim);

        sp = getContext().getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);


        //TODO: refactor this part of code, and do the same for quiz fragment
        //there is some problem with extracting the code to a new method, I am not sure why
        if (is_Instructor) {
            fab = getView().findViewById(R.id.class_switch_fab);
            TextView debug_text = getView().findViewById(R.id.class_statistic_debug_text);

            new Thread(() -> {
                while (course_category == -1 || class_code == 0) {
                    class_code = sp.getInt(getString(R.string.class_code), 0);
                    course_category = sp.getInt(getString(R.string.course_category), -1);
                }

                getActivity().runOnUiThread(() -> debug_text.setText("Current class code " + class_code + ", current category " + course_category));
            }).start();


        } else {
            fab = getView().findViewById(R.id.class_switch_fab);

            TextView debug_text = getView().findViewById(R.id.leader_board_debug_text);

            new Thread(() -> {
                while (class_code == 0) {
                    class_code = sp.getInt(getString(R.string.class_code), 0);
                }
                getActivity().runOnUiThread(() -> debug_text.setText("Current class code " + class_code));
            }).start();
        }

        fab.setOnClickListener(v -> onSwitchClassButtonClicked());

    }

    private void onSwitchClassButtonClicked() {
        Toast.makeText(getContext(), "change class clicked", Toast.LENGTH_SHORT).show();
    }

    private void updateText() {
        //we need to update UI on UI thread, otherwise it will crash the app
        //however, it adds the load onto the Main thread, causing lags
        //so we need to run a new thread which runs this function
        String result = OtherUtils.readFromURL(serverLink);
        result = OtherUtils.stringIsNullOrEmpty(result) ? getString(R.string.server_get_failed) : result;
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        final String text = result;

        getActivity().runOnUiThread(() -> realTimeText.setText(text));
    }

}
