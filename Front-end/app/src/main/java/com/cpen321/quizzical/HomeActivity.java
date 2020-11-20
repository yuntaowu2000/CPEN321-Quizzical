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
    private ArrayList<Button> classButtonList;
    private ArrayList<Classes> classList;
    private LinearLayout classScrollContentLayout;
    private TabLayout tabLayout;
    private ImageButton addClassButton;
    private FloatingActionButton classSwitchButton;
    private HorizontalScrollView classScrollView;
    private boolean isInstructor;
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
        isInstructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        classScrollView = findViewById(R.id.class_scroll_view);
        classScrollContentLayout = findViewById(R.id.class_scroll_linear_layout);
        addClassButton = findViewById(R.id.add_class_button);

        classSwitchButton = findViewById(R.id.class_switch_fab);
        classSwitchButton.setOnClickListener(v -> onClassSwitchButtonClicked());

        if (isInstructor) {
            addClassButton.setOnClickListener(v -> createClass());
        } else {
            addClassButton.setOnClickListener(v -> joinClass());
        }

        //we need to initialize the list based on server info
        classButtonList = new ArrayList<>(0);
        parseClassListFromString();

        if (classList.size() == 0) {
            promptForClassCode();
        } else {
            //set up the class list and default class
            generateUIClassListOnCreate();
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
                if (classScrollView.getVisibility() == View.VISIBLE) {
                    classSwitchButton.setAnimation(rotateClose);
                    classScrollView.setAnimation(toBottom);
                    classScrollView.setVisibility(View.INVISIBLE);
                    classScrollContentLayout.removeAllViews();
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
        classList = new ArrayList<>();
        String classListString = sp.getString(getString(R.string.CLASS_LIST), "");
//        if (OtherUtils.stringIsNullOrEmpty(classListString)) {
//            String url = getString(R.string.GET_URL) + "/users?"
//                    + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "")
//                    + "&type=" + getString(R.string.CLASS_LIST);
//            classListString = OtherUtils.readFromURL(url);
//        }

        if (OtherUtils.stringIsNullOrEmpty(classListString)) {
            return;
        }

        try {
            String[] classes = classListString.split(";");
            for (String c : classes) {
                classList.add(new Classes(c));
            }
        } catch (Exception e) {
            Log.d("parse", "cannot parse class list");
        }
    }

    private String parseClassListToString() {
        StringBuilder strb = new StringBuilder();
        for (Classes c : classList) {
            strb.append(c.toJson()).append(";");
        }
        if (strb.length() > 0) {
            strb.deleteCharAt(strb.length() - 1);
        }

        Log.d("parse_class_list", strb.toString());
        return strb.toString();
    }

    private void promptForClassCode() {

        //this will be used for a new user initial setup
        //and for future enrollment by clicking on some button
        if (isInstructor) {
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
            boolean success = true;
            try {
                int classCode = Integer.parseInt(editText.getText().toString());
                Classes newClass = parseClassInfoForStudents(classCode);
                if (newClass != null) {
                    appendNewClassToList(newClass);
                    alertDialog.dismiss();
                } else {
                    success = false;
                }
            } catch (NumberFormatException e) {
                success = false;
            }
            if (!success) {
                errorText.setText(R.string.UI_invalid_class_code_msg);
            }
        });

    }

    private Classes parseClassInfoForStudents(int classCode) {
        // when a student join a class by class code, get the general class info from the server
        for (Classes c : classList) {
            if (c.getClassCode() == classCode) {
                Toast.makeText(this, getText(R.string.UI_class_joined_already_msg), Toast.LENGTH_LONG).show();
                return null;
            }
        }

        OtherUtils.uploadToServer(getString(R.string.CLASS_ENDPOINT),
                sp.getString(getString(R.string.UID), ""),
                getString(R.string.JOIN_CLASS),
                String.valueOf(classCode)
        );

        String classInfoLink = getString(R.string.GET_URL) + "classes?" + getString(R.string.CLASS_CODE) + "=" + classCode;
        String classInfoString = OtherUtils.readFromURL(classInfoLink);

        try {
            JSONArray jsonArray = new JSONArray(classInfoString);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            return new Classes(jsonObject.toString());

        } catch (JSONException e) {
            Log.d("parse_json", "parse class info failed");
        }

        return null;
    }

    private void createClass() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        //use spinner to select a course category

        TextView courseCategoryText = new TextView(this);
        courseCategoryText.setText(R.string.UI_select_course_category_msg);
        courseCategoryText.setLayoutParams(layoutParams);

        Spinner courseCategorySpinner = new Spinner(this);
        courseCategorySpinner.setLayoutParams(layoutParams);
        String[] courseCategoriesArray = getResources().getStringArray(R.array.course_category_array);
        ArrayAdapter<String> courseCategoryAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, courseCategoriesArray);
        courseCategorySpinner.setAdapter(courseCategoryAdapter);

        TextView selectGradeLevelText = new TextView(this);
        selectGradeLevelText.setText(R.string.UI_select_grade_level_msg);
        selectGradeLevelText.setLayoutParams(layoutParams);

        Spinner gradeListSpinner = new Spinner(this);
        gradeListSpinner.setLayoutParams(layoutParams);
        String[] gradeLevelsArray = getResources().getStringArray(R.array.grades_array);
        ArrayAdapter<String> grade_level_adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, gradeLevelsArray);
        gradeListSpinner.setAdapter(grade_level_adapter);

        TextView classNameText = new TextView(this);
        classNameText.setText(R.string.UI_enter_class_name_msg);
        classNameText.setLayoutParams(layoutParams);

        EditText classNameInput = new EditText(this);
        classNameInput.setLayoutParams(layoutParams);
        classNameInput.setHint(R.string.UI_example_class_name);
        classNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        classNameInput.setMaxLines(1);

        TextView classNameErrorText = new TextView(this);
        classNameErrorText.setLayoutParams(layoutParams);
        classNameErrorText.setText("");
        classNameErrorText.setTextColor(getResources().getColor(R.color.colorCrimson));

        layout.addView(courseCategoryText);
        layout.addView(courseCategorySpinner);
        layout.addView(selectGradeLevelText);
        layout.addView(gradeListSpinner);
        layout.addView(classNameText);
        layout.addView(classNameInput);
        layout.addView(classNameErrorText);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this).setTitle(R.string.UI_creating_class_msg)
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> dialogInterface.dismiss()));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            String className = classNameInput.getText().toString();
            if (checkClassName(className)) {
                createNewClassCode(
                        courseCategoriesArray[courseCategorySpinner.getSelectedItemPosition()],
                        gradeLevelsArray[gradeListSpinner.getSelectedItemPosition()],
                        classNameInput.getText().toString()
                );
                alertDialog.dismiss();
            } else {
                classNameErrorText.setText(R.string.UI_class_name_invalid_msg);
            }
        });
    }

    private boolean checkClassName(String className) {
        if (!OtherUtils.checkUserName(className)) {
            return false;
        }
        for (Classes c : classList) {
            if (c.getClassName().equals(className)) {
                return false;
            }
        }
        return true;
    }

    private CourseCategory convertCategoryStringToEnum(String courseCategoryString) {
        String[] courseCategories = getResources().getStringArray(R.array.course_category_array);
        if (courseCategoryString.equals(courseCategories[0])) {
            return CourseCategory.Math;
        } else if (courseCategoryString.equals(courseCategories[1])) {
            return CourseCategory.English;
        } else if (courseCategoryString.equals(courseCategories[2])) {
            return CourseCategory.QuantumPhysic;
        } else {
            return CourseCategory.DontCare;
        }
    }

    private void createNewClassCode(String courseCategoryString, String gradeLevelString, String className) {

        int curr_classCode = (courseCategoryString.hashCode() + gradeLevelString.hashCode() + className.hashCode()) % 65536;
        curr_classCode = Math.abs(curr_classCode);
        CourseCategory courseCategory = convertCategoryStringToEnum(courseCategoryString);

        Classes mClass = new Classes(sp.getString(getString(R.string.UID), ""),
                curr_classCode,
                className,
                courseCategory);

        new AlertDialog.Builder(this).setTitle(R.string.UI_create_class_success_title)
                .setMessage(String.format(getString(R.string.UI_create_class_success_msg), curr_classCode))
                .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();
        appendNewClassToList(mClass);
    }

    private void generateNewClassButton(Classes c) {
        Button newClassButton = new Button(this);
        String className = c.getClassName();

        if (OtherUtils.stringIsNullOrEmpty(className)) {
            className = String.valueOf(c.getClassCode());
        }

        newClassButton.setText(className);
        newClassButton.setAllCaps(false);
        newClassButton.setOnClickListener(v -> switchClass(c));
        newClassButton.setOnLongClickListener(v -> requestDeleteClass(c));
        classButtonList.add(newClassButton);
    }

    private void generateClassButtonLayout() {
        for (Button b : classButtonList) {
            classScrollContentLayout.addView(b);
        }

        classScrollContentLayout.addView(addClassButton);
    }

    private void generateUIClassListOnCreate() {
        for (Classes c : classList) {
            generateNewClassButton(c);
        }

        classScrollContentLayout.removeAllViews();

        String savedClass = sp.getString(getString(R.string.CURR_CLASS), "");
        if (!OtherUtils.stringIsNullOrEmpty(savedClass)) {
            Classes currClass = new Classes(savedClass);
            switchClass(currClass);
        } else {
            switchClass(classList.get(0));
        }
    }

    private void appendNewClassToList(Classes mClass) {
        if (classList.contains(mClass)) {
            Toast.makeText(this, R.string.UI_class_joined_already_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        classList.add(mClass);
        String classListString = parseClassListToString();
        sp.edit().putString(getString(R.string.CLASS_LIST), classListString).apply();

        new Thread(() -> {
            OtherUtils.uploadToServer(
                    getString(R.string.CLASS_ENDPOINT),
                    sp.getString(getString(R.string.UID), ""),
                    getString(R.string.CLASS_LIST),
                    classListString
            );
            if (isInstructor) {
                OtherUtils.uploadToServer(
                        getString(R.string.CLASS_ENDPOINT),
                        sp.getString(getString(R.string.UID), ""),
                        getString(R.string.CREATE_CLASS),
                        mClass.toJson());
            }
        }).start();

        generateNewClassButton(mClass);

        classScrollContentLayout.removeAllViews();

        generateClassButtonLayout();

        switchClass(mClass);
        Toast.makeText(this, R.string.UI_class_joined_msg, Toast.LENGTH_SHORT).show();
    }

    private boolean requestDeleteClass(Classes mClass) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.UI_warning);

        if (isInstructor) {
            alertDialogBuilder.setMessage(R.string.UI_teacher_delete_course);
        } else {
            alertDialogBuilder.setMessage(R.string.UI_student_delete_course);
        }

        alertDialogBuilder.setPositiveButton(R.string.YES, ((dialogInterface, i) -> deleteClass(mClass)));
        alertDialogBuilder.setNegativeButton(R.string.NO, ((dialogInterface, i) -> dialogInterface.dismiss()));

        alertDialogBuilder.show();

        return true;
    }

    private void deleteClass(Classes mClass) {
        classList.remove(mClass);

        String classListString = parseClassListToString();
        sp.edit().putString(getString(R.string.CLASS_LIST), classListString).apply();

        String moduleId = mClass.getClassCode() + getString(R.string.QUIZ_MODULES);
        sp.edit().remove(moduleId).apply();

        new Thread(() -> {
            OtherUtils.uploadToServer(
                    getString(R.string.CLASS_ENDPOINT),
                    sp.getString(getString(R.string.UID), ""),
                    getString(R.string.CLASS_LIST),
                    classListString
            );
            String params = getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "")
                    + "&" + getString(R.string.TYPE) + getString(R.string.DELETE_CLASS)
                    + "&" + getString(R.string.IS_INSTRUCTOR) + "=" + sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false)
                    + "&" + getString(R.string.CLASS_CODE) + "=" + mClass.getClassCode();
            OtherUtils.deleteRequest(params);
        }).start();

        for (Button b : classButtonList) {
            if (b.getText().equals(mClass.getClassName())) {
                classButtonList.remove(b);
                break;
            }
        }

        classScrollContentLayout.removeAllViews();
        generateClassButtonLayout();

        if (classList.size() == 0) {
            if (isInstructor) {
                createClass();
            } else {
                joinClass();
            }
        } else {
            switchClass(classList.get(0));
        }
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
        if (isInstructor) {
            setupTab(getString(R.string.UI_class_statistics));
        } else {
            setupTab(getString(R.string.UI_leader_board));
        }
        setupTab(getString(R.string.UI_profile));
    }

    private void onClassSwitchButtonClicked() {
        if (classScrollView.getVisibility() == View.INVISIBLE) {
            classSwitchButton.setAnimation(rotateOpen);
            classScrollView.setAnimation(fromBottom);
            classScrollView.setVisibility(View.VISIBLE);
            generateClassButtonLayout();
        } else {
            classSwitchButton.setAnimation(rotateClose);
            classScrollView.setAnimation(toBottom);
            classScrollContentLayout.removeAllViews();
            classScrollView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ProfileFragment.profileFragmentOnSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(ProfileFragment.profileFragmentOnSPChangeListener);
        if (StatisticFragment.statisticFragmentOnSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(StatisticFragment.statisticFragmentOnSPChangeListener);
        if (QuizFragment.quizFragmentSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(QuizFragment.quizFragmentSPChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ProfileFragment.profileFragmentOnSPChangeListener != null)
            sp.registerOnSharedPreferenceChangeListener(ProfileFragment.profileFragmentOnSPChangeListener);
        if (StatisticFragment.statisticFragmentOnSPChangeListener != null)
            sp.registerOnSharedPreferenceChangeListener(StatisticFragment.statisticFragmentOnSPChangeListener);
        if (QuizFragment.quizFragmentSPChangeListener != null)
            sp.registerOnSharedPreferenceChangeListener(QuizFragment.quizFragmentSPChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ProfileFragment.profileFragmentOnSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(ProfileFragment.profileFragmentOnSPChangeListener);
        if (StatisticFragment.statisticFragmentOnSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(StatisticFragment.statisticFragmentOnSPChangeListener);
        if (QuizFragment.quizFragmentSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(QuizFragment.quizFragmentSPChangeListener);
    }
}