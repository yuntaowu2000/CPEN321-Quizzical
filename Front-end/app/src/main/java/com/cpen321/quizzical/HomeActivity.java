package com.cpen321.quizzical;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.mainmenufragments.ProfileFragment;
import com.cpen321.quizzical.mainmenufragments.QuizFragment;
import com.cpen321.quizzical.mainmenufragments.StatisticFragment;
import com.cpen321.quizzical.ui.main.MyHomePagerAdapter;
import com.cpen321.quizzical.utils.OtherUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private ArrayList<Button> class_button_list;
    private ArrayList<Classes> class_list;
    private Classes curr_class;
    private LinearLayout class_scroll_content_layout;
    private TabLayout tabLayout;
    private ImageButton add_class_button;
    private FloatingActionButton class_switch_button;
    private HorizontalScrollView class_scroll_view;
    private boolean is_Instructor;
    private SharedPreferences sp;

    /**
     * This is the class for the main home screen of the APP
     * We use a tab layout for quiz, class statistics/leader board, and profile
     * Instructor and students will have different layout and functionalities
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

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
        class_button_list = new ArrayList<>(0);
        parseClassListFromString();

        if (class_list.size() == 0) {
            promptForClassCode();
        } else {
            //set up the class list and default class
            generateUIClassListOnCreate();
            String curr_class_string = sp.getString(getString(R.string.CURR_CLASS), "");
            if (!OtherUtils.stringIsNullOrEmpty(curr_class_string)) {
                curr_class = new Classes(curr_class_string);
            }
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
                    class_scroll_view.setAnimation(toBottom);
                    class_scroll_view.setVisibility(View.INVISIBLE);
                    for (Button b : class_button_list) {
                        if (b.getVisibility() == View.VISIBLE) {
                            b.setVisibility(View.INVISIBLE);
                            b.setClickable(false);
                        }
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                /* not used*/
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                /* not used*/
            }
        });

    }

    private void parseClassListFromString() {
        class_list = new ArrayList<>();
        String classListString = sp.getString(getString(R.string.CLASS_LIST), "");
        if (OtherUtils.stringIsNullOrEmpty(classListString)) {
            String url = getString(R.string.GET_URL) + "/users&"
                    + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "")
                    + "&type=" + getString(R.string.CLASS_LIST);
            classListString = OtherUtils.readFromURL(url);
        }

        if (OtherUtils.stringIsNullOrEmpty(classListString)) {
            return;
        }

        try {
            String[] classes = classListString.split(";");
            for (String c : classes) {
                class_list.add(new Classes(c));
            }
        } catch (Exception e) {
            Log.d("parse", "cannot parse class list");
        }
    }

    private String parseClassListToString() {
        StringBuilder strb = new StringBuilder();
        for (Classes c : class_list) {
            strb.append(c.toJson()).append(";");
        }
        strb.deleteCharAt(strb.length() - 1);

        Log.d("parse_class_list", strb.toString());
        return strb.toString();
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
        editText.setHint(R.string.UI_example_course_code);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        TextView errorText = new TextView(this);
        errorText.setText("");
        errorText.setTextColor(getResources().getColor(R.color.colorCrimson));

        layout.addView(editText);
        layout.addView(errorText);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle(R.string.UI_enter_class_code_msg)
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> {
                }));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            int class_code = Integer.parseInt(editText.getText().toString());
            Classes newClass = parseClassInfoForStudents(class_code);
            if (newClass != null) {
                appendNewClassToList(newClass);
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.UI_invalid_class_code_msg);
            }
        });

    }

    private Classes parseClassInfoForStudents(int class_code) {
        // when a student join a class by class code, get the general class info from the server

        new Thread(() -> OtherUtils.uploadToServer(sp.getString(getString(R.string.UID), ""),
                getString(R.string.JOIN_CLASS),
                String.valueOf(class_code)
        )).start();

        String classInfoLink = getString(R.string.GET_URL) + "/classes&" + getString(R.string.CLASS_CODE) + "=" + class_code;
        String classInfoString = OtherUtils.readFromURL(classInfoLink);

        try {
            JSONArray jsonArray = new JSONArray(classInfoString);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            return new Classes(jsonObject.toString());

        } catch (JSONException e) {
            Log.d("parse_json", "parse class info failed");
        }
        //TODO: need to change to return null here after the server is working
        return null;
    }

    private void createClass() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        //use spinner to select a course category

        TextView course_category_text = new TextView(this);
        course_category_text.setText(R.string.UI_select_course_category_msg);
        course_category_text.setLayoutParams(layoutParams);

        Spinner course_category_list = new Spinner(this);
        course_category_list.setLayoutParams(layoutParams);
        String[] course_categories = getResources().getStringArray(R.array.course_category_array);
        ArrayAdapter<String> course_category_adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, course_categories);
        course_category_list.setAdapter(course_category_adapter);

        TextView grade_text = new TextView(this);
        grade_text.setText(R.string.UI_select_grade_level_msg);
        grade_text.setLayoutParams(layoutParams);

        Spinner grade_list = new Spinner(this);
        grade_list.setLayoutParams(layoutParams);
        String[] grade_levels = getResources().getStringArray(R.array.grades_array);
        ArrayAdapter<String> grade_level_adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, grade_levels);
        grade_list.setAdapter(grade_level_adapter);

        TextView class_name_text = new TextView(this);
        class_name_text.setText(R.string.UI_enter_class_name_msg);
        class_name_text.setLayoutParams(layoutParams);

        EditText class_name_input = new EditText(this);
        class_name_input.setLayoutParams(layoutParams);
        class_name_input.setHint(R.string.UI_example_class_name);
        class_name_input.setInputType(InputType.TYPE_CLASS_TEXT);
        class_name_input.setMaxLines(1);

        TextView class_name_error_text = new TextView(this);
        class_name_error_text.setLayoutParams(layoutParams);
        class_name_error_text.setText("");
        class_name_error_text.setTextColor(getResources().getColor(R.color.colorCrimson));

        layout.addView(course_category_text);
        layout.addView(course_category_list);
        layout.addView(grade_text);
        layout.addView(grade_list);
        layout.addView(class_name_text);
        layout.addView(class_name_input);
        layout.addView(class_name_error_text);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle(R.string.UI_creating_class_msg)
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> dialogInterface.dismiss()));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            String class_name = class_name_input.getText().toString();
            if (OtherUtils.checkClassName(class_name)) {
                createNewClassCode(
                        course_categories[course_category_list.getSelectedItemPosition()],
                        grade_levels[grade_list.getSelectedItemPosition()],
                        class_name_input.getText().toString()
                );
                alertDialog.dismiss();
            } else {
                class_name_error_text.setText(R.string.UI_class_name_invalid_msg);
            }
        });
    }

    private CourseCategory convertCategoryStringToEnum(String course_category) {
        String[] courseCategories = getResources().getStringArray(R.array.course_category_array);
        if (course_category.equals(courseCategories[0])) {
            return CourseCategory.Math;
        } else if (course_category.equals(courseCategories[1])) {
            return CourseCategory.English;
        } else if (course_category.equals(courseCategories[2])) {
            return CourseCategory.QuantumPhysic;
        } else {
            return CourseCategory.DontCare;
        }
    }

    private void createNewClassCode(String course_category, String grade_level, String class_name) {

        int curr_class_code = (course_category.hashCode() + grade_level.hashCode() + class_name.hashCode()) % 65536;
        CourseCategory courseCategory = convertCategoryStringToEnum(course_category);

        Classes mClass = new Classes(sp.getString(getString(R.string.UID), ""),
                curr_class_code,
                class_name,
                courseCategory);

        new AlertDialog.Builder(this).setTitle(R.string.UI_create_class_success_title)
                .setMessage(String.format(getString(R.string.UI_create_class_success_msg), curr_class_code))
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();
        appendNewClassToList(mClass);


        new Thread(() -> OtherUtils.uploadToServer(sp.getString(getString(R.string.UID), ""), getString(R.string.CREATE_CLASS), mClass.toJson())).start();
    }

    private void generateUIClassListOnCreate() {
        for (Classes c : class_list) {
            Button newClassButton = new Button(this);
            String class_name = c.getClassName();

            if (OtherUtils.stringIsNullOrEmpty(class_name)) {
                class_name = String.valueOf(c.getClassCode());
            }

            newClassButton.setText(class_name);
            newClassButton.setAllCaps(false);
            newClassButton.setOnClickListener(v -> switchClass(c));
            class_button_list.add(newClassButton);
        }

        class_scroll_content_layout.removeAllViews();

        for (Button b : class_button_list) {
            class_scroll_content_layout.addView(b);
        }

        class_scroll_content_layout.addView(add_class_button);

        switchClass(class_list.get(0));
    }

    private void appendNewClassToList(Classes mClass) {
        if (class_list.contains(mClass)) {
            Toast.makeText(this, R.string.UI_class_joined_already_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        class_list.add(mClass);
        String class_list_string = parseClassListToString();
        sp.edit().putString(getString(R.string.CLASS_LIST), class_list_string).apply();

        new Thread(() -> OtherUtils.uploadToServer(
                sp.getString(getString(R.string.UID), ""),
                getString(R.string.CLASS_LIST),
                class_list_string
        )).start();

        Button newClassButton = new Button(this);

        String class_name = "";
        if (OtherUtils.stringIsNullOrEmpty(mClass.getClassName())) {
            class_name = String.valueOf(mClass.getClassCode());
        } else {
            class_name = mClass.getClassName();
        }

        newClassButton.setText(class_name);
        newClassButton.setAllCaps(false);
        newClassButton.setOnClickListener(v -> switchClass(mClass));

        class_button_list.add(newClassButton);

        class_scroll_content_layout.removeAllViews();

        for (Button b : class_button_list) {
            class_scroll_content_layout.addView(b);
        }

        class_scroll_content_layout.addView(add_class_button);

        switchClass(mClass);
    }

    private void switchClass(Classes mClass) {
        sp.edit().putString(getString(R.string.CURR_CLASS), mClass.toJson()).apply();
        Log.d("home activity", "curr selected class code " + mClass.getClassCode());
    }

    private void setupTab(String tabText) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(tabText);
        tabLayout.addTab(tab);
    }

    private void setupAllTabs() {
        setupTab(getString(R.string.UI_quizzes));
        if (is_Instructor) {
            setupTab(getString(R.string.UI_class_statistics));
        } else {
            setupTab(getString(R.string.UI_leader_board));
        }
        setupTab(getString(R.string.UI_profile));
    }

    private void onClassSwitchButtonClicked() {
        if (class_scroll_view.getVisibility() == View.INVISIBLE) {
            class_switch_button.setAnimation(rotateOpen);
            class_scroll_view.setAnimation(fromBottom);
            class_scroll_view.setVisibility(View.VISIBLE);
            for (Button b : class_button_list) {
                b.setVisibility(View.VISIBLE);
                b.setClickable(true);
            }
        } else {
            class_switch_button.setAnimation(rotateClose);
            class_scroll_view.setAnimation(toBottom);
            class_scroll_view.setVisibility(View.INVISIBLE);
            for (Button b : class_button_list) {
                if (b.getVisibility() == View.VISIBLE) {
                    b.setVisibility(View.INVISIBLE);
                    b.setClickable(false);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ProfileFragment.quizNumAndExpChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(ProfileFragment.quizNumAndExpChangeListener);
        if (StatisticFragment.classCodeChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(StatisticFragment.classCodeChangeListener);
        if (QuizFragment.quizFramentClassCodeChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(QuizFragment.quizFramentClassCodeChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ProfileFragment.quizNumAndExpChangeListener != null)
            sp.registerOnSharedPreferenceChangeListener(ProfileFragment.quizNumAndExpChangeListener);
        if (StatisticFragment.classCodeChangeListener != null)
            sp.registerOnSharedPreferenceChangeListener(StatisticFragment.classCodeChangeListener);
        if (QuizFragment.quizFramentClassCodeChangeListener != null)
            sp.registerOnSharedPreferenceChangeListener(QuizFragment.quizFramentClassCodeChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ProfileFragment.quizNumAndExpChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(ProfileFragment.quizNumAndExpChangeListener);
        if (StatisticFragment.classCodeChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(StatisticFragment.classCodeChangeListener);
        if (QuizFragment.quizFramentClassCodeChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(QuizFragment.quizFramentClassCodeChangeListener);
    }
}