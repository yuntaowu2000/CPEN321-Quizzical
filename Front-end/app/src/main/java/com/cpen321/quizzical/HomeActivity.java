package com.cpen321.quizzical;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.cpen321.quizzical.Utils.OtherUtils;
import com.cpen321.quizzical.ui.main.MyHomePagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity {

    TabLayout tabLayout;
    boolean is_Instructor;
    int class_code;
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

        //check if the user is instructor from shared preferences
        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        is_Instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        //if class code is 0, we need to prompt the user to
        // create a class if instructor
        // join a class if student
        // this will be default class code
        // we can have multiple class code stored on server and use a floating action button to switch class
        // for each class code, we need a teacher, a class stats, a set of notes/quizzes
        class_code = getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE).
                getInt(getString(R.string.class_code), 0);

        if (class_code == 0) {
            promptForClassCode();
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

        AlertDialog.Builder alertBuilder= new AlertDialog.Builder(this).setTitle(R.string.enter_class_code_hint)
                .setView(layout)
                .setPositiveButton(R.string.SUBMIT, ((dialogInterface, i) -> {}));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->
        {
            int class_code = Integer.parseInt(editText.getText().toString());
            if (OtherUtils.checkClassCode(class_code)) {
                sp.edit().putInt(getString(R.string.class_code), class_code).apply();
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
        class_code = category + 1 + 2 + 3;
        new AlertDialog.Builder(this).setTitle("You just created a new class")
                .setMessage("Your class code is " + class_code + ". " +
                        "You will also get an email containing the code. " +
                        "Please share it with your students.")
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();

        sp.edit().putInt(getString(R.string.course_category), category).apply();
        sp.edit().putInt(getString(R.string.class_code), class_code).apply();
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

}