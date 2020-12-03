package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.cpen321.quizzical.data.Classes;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class InstructorCreateClassTest {
    /*Note: if you want to run the tests on your own machine,
        you may need to replace the client id with your own google client id at line 108 in CPEN321-Quizzical\Front-end\app\src\main\res\values\strings.xml
        and sign in the app properly.
        You need an instructor account for the tests here.
        */
    @Rule
    public ActivityTestRule<HomeActivity> activityTestRule = new ActivityTestRule<HomeActivity>(HomeActivity.class);
    private static final String testClassName = "CLASSINTEST2";

    @Before
    public void cleanUp() {
        Activity activity = activityTestRule.getActivity();
        SharedPreferences sp = activity.getSharedPreferences(activity.getString(R.string.curr_login_user), Context.MODE_PRIVATE);
        String classListString = sp.getString(activity.getString(R.string.CLASS_LIST), "");
        String[] classes = classListString.split(";");
        for (String c : classes) {
            Classes classes1 = new Classes(c);
            if (classes1.getClassName().equals(testClassName)) {
                deleteClass();
                break;
            }
        }
    }

    private void deleteClass() {
        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(testClassName))
                .perform(ViewActions.scrollTo(), ViewActions.longClick());

        Espresso.onView(ViewMatchers.withText(R.string.YES))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());
    }

    @Test
    public void checkClassNameInvalid() {
        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.add_class_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_creating_class_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_class_name))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText("a"));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_class_name_invalid_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Assert.assertTrue(true);
    }

    @Test
    public void addNewClassTest() {
        createClass();

        deleteClass();
        Assert.assertTrue(true);
    }

    private void createClass() {
        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.add_class_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_creating_class_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_class_name))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText(testClassName));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_create_class_success_title))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());
    }

    @Test
    public void addDuplicateClassTest() {
        createClass();

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.add_class_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_creating_class_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_class_name))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText(testClassName));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_class_name_invalid_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Assert.assertTrue(true);
    }

    @Test
    public void addQuizNoModuleTest() {
        createClass();
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.edit_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_create_new_module))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Assert.assertTrue(true);
    }

    @Test
    public void addModuleTest() {
        createClass();
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.module_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_create_new_module))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_module_name))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText("module1"));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        checkEachQuizModuleButtonNoData(R.string.UI_wrong_questions);
        checkEachQuizModuleButtonNoData(R.string.UI_stats);
        checkEachQuizModuleButtonNoData(R.string.go_to_quiz);

        testDeleteModule();
        deleteClass();
        Assert.assertTrue(true);
    }

    private void checkEachQuizModuleButtonNoData(int textId) {
        Espresso.onView(ViewMatchers.withText(textId))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_no_data))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());
    }

    private void testDeleteModule() {
        Espresso.onView(ViewMatchers.withId(R.id.delete_module_button))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_warning))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.NO))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.delete_module_button))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_warning))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.YES))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

    }

    @Test
    public void addQuizAfterCreateModule() {
        createClass();
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.module_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_create_new_module))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_module_name))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText("module1"));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.edit_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_enter_module_name))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Assert.assertTrue(true);

    }
}
