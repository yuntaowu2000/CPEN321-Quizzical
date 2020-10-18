package com.cpen321.quizzical;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.cpen321.quizzical.Utils.OtherUtils;
import com.cpen321.quizzical.ui.main.MyHomePagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Animation rotateOpen, rotateClose, fromBottom, toBottom;
    HorizontalScrollView class_scroll_view;
    List<Button> class_list;
    List<Integer> class_code_list;
    LinearLayout class_scroll_content_layout;
    ImageButton add_class_button;
    FloatingActionButton class_switch_button;
    TabLayout tabLayout;
    boolean is_Instructor;
    int curr_class_code;
    SharedPreferences sp;


    /**
     * This is the class for the main home screen of the APP
     * We use a tab layout for quiz, class statistics/leader board, and profile
     * Instructor and students will have different layout and functionalities
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        //check if the user is instructor from shared preferences
        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        is_Instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        class_scroll_view = findViewById(R.id.class_scroll_view);
        class_scroll_content_layout = findViewById(R.id.class_scroll_linear_layout);
        add_class_button = findViewById(R.id.add_class_button);

        class_switch_button = findViewById(R.id.class_switch_fab);
        class_switch_button.setOnClickListener(v -> onClassSwitchButtonClicked());

        if (is_Instructor) {
            add_class_button.setOnClickListener(v -> createClass());
        } else {
            add_class_button.setOnClickListener(v -> joinClass());
        }

        //we need to initialize the list based on server info
        class_list = new ArrayList<>(0);
        class_code_list = new ArrayList<>(0);

        //if class code is 0, we need to prompt the user to
        // create a class if instructor
        // join a class if student
        // this will be default class code
        // we can have multiple class code stored on server and use a floating action button to switch class
        // for each class code, we need a teacher, a class stats, a set of notes/quizzes
        curr_class_code = sp.getInt(getString(R.string.class_code), 0);

        if (curr_class_code == 0) {
            promptForClassCode();
        } else {
            appendNewClassToList(curr_class_code);
            //set up the default class
            switchClass(curr_class_code);
        }

        ViewPager viewPager = findViewById(R.id.view_pager);

        tabLayout = findViewById(R.id.tabs);

        setupAllTabs();

        //The following is used for tab behavior set up (from template)
        MyHomePagerAdapter adapter = new MyHomePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (class_scroll_view.getVisibility() == View.VISIBLE) {
                    class_switch_button.setAnimation(rotateClose);
                    class_scroll_view.setVisibility(View.INVISIBLE);
                    class_scroll_view.setAnimation(toBottom);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void promptForClassCode() {

        //this will be used for a new user initial setup
        //and for future enrollment by clicking on some button
        if (is_Instructor) {
            createClass();
        } else {
            joinClass();
        }

    }

    private void joinClass() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText editText = new EditText(this);
        editText.setHint(R.string.example_course_code);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        TextView errorText = new TextView(this);
        errorText.setText("");
        errorText.setTextColor(getResources().getColor(R.color.colorCrimson));

        layout.addView(editText);
        layout.addView(errorText);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle(R.string.enter_class_code_hint)
                .setView(layout)
                .setPositiveButton(R.string.SUBMIT, ((dialogInterface, i) -> {
                }));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            int class_code = Integer.parseInt(editText.getText().toString());
            if (OtherUtils.checkClassCode(class_code)) {
                appendNewClassToList(class_code);
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.invalid_class_code);
            }
        });

    }

    private void createClass() {
        //use spinner to select a course category
        Spinner drop_down_list = new Spinner(this);
        String[] items = new String[]{getString(R.string.MATH), getString(R.string.ENGLISH), getString(R.string.QUANTUM_PHYSICS)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items);
        drop_down_list.setAdapter(adapter);

        new AlertDialog.Builder(this).setTitle(R.string.select_course_category_hint)
                .setView(drop_down_list)
                .setPositiveButton(R.string.SUBMIT, ((dialogInterface, i) ->
                {
                    setupNewClassCode(drop_down_list.getSelectedItemPosition());
                    dialogInterface.dismiss();
                }))
                .show();
    }

    private void setupNewClassCode(int category) {
        //TODO: this value should be generated by the server and sent to user's email as well.
        //TODO: we should first send a POST request to the server
        //content should be something like: username=xxx;course_category=xxx
        //and wait for the server to respond with a class code
        //then cache them in the shared preferences
        curr_class_code = category + 1 + 2 + 3;
        new AlertDialog.Builder(this).setTitle(R.string.create_class_success_msg)
                .setMessage(String.format(getString(R.string.create_class_success_msg_details), curr_class_code))
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();

        appendNewClassToList(curr_class_code);
        sp.edit().putInt(getString(R.string.course_category), category).apply();
    }

    private void appendNewClassToList(int class_code) {

        if (class_code_list.contains(class_code)) {
            Toast.makeText(this, R.string.You_have_joined_the_class, Toast.LENGTH_SHORT).show();
            return;
        }
        class_code_list.add(class_code);
        Button newClassButton = new Button(this);

        //probably can have teachers to name their class
        String class_name = String.valueOf(class_code);
        newClassButton.setText(class_name);
        newClassButton.setOnClickListener(v -> switchClass(class_code));

        class_list.add(newClassButton);

        class_scroll_content_layout.removeAllViews();

        for (Button b : class_list) {
            class_scroll_content_layout.addView(b);
        }

        class_scroll_content_layout.addView(add_class_button);

        switchClass(class_code);
    }

    private void switchClass(int class_code) {
        sp.edit().putInt(getString(R.string.class_code), class_code).apply();
        Log.d("home activity", "curr selected class code " + class_code);
    }

    private void setupTab(String tabText) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(tabText);
        tabLayout.addTab(tab);
    }

    private void setupAllTabs() {
        setupTab(getString(R.string.Quizzes));
        if (is_Instructor) {
            setupTab(getString(R.string.Class_Statistics));
        } else {
            setupTab(getString(R.string.LeaderBoard));
        }
        setupTab(getString(R.string.Profile));
    }

    private void onClassSwitchButtonClicked() {
        if (class_scroll_view.getVisibility() == View.INVISIBLE) {
            class_switch_button.setAnimation(rotateOpen);
            class_scroll_view.setVisibility(View.VISIBLE);
            class_scroll_view.setAnimation(fromBottom);
        } else {
            class_switch_button.setAnimation(rotateClose);
            class_scroll_view.setVisibility(View.INVISIBLE);
            class_scroll_view.setAnimation(toBottom);
        }
    }

}