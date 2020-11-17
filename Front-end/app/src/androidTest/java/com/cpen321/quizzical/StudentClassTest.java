package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.cpen321.quizzical.data.Classes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.not;

public class StudentClassTest {

    @Rule
    public ActivityTestRule<HomeActivity> activityTestRule = new ActivityTestRule<HomeActivity>(HomeActivity.class);
    private static final String validClassCode1 = "35607";
    private static final String validClassCode2 = "18489";
    private static final String invalidLongClassCode = "99999999999"; // Invalid code, long
    private static final String invalidClassCode = "99999"; // Invalid code

    private static final String validClassName1 = "test1";
    private static final String validClassName2 = "CLASSINTEST";

    @Before
    public void cleanUp() {
        Activity activity = activityTestRule.getActivity();
        SharedPreferences sp = activity.getSharedPreferences(activity.getString(R.string.curr_login_user), Context.MODE_PRIVATE);
        String classListString = sp.getString(activity.getString(R.string.CLASS_LIST), "");
        String[] classList = classListString.split(";");
        for (String c : classList) {
            Classes classes = new Classes(c);
            if (classes.getClassName().equals(validClassName1) ||
                classes.getClassName().equals(validClassName2)) {
                leaveClass(classes.getClassName());
                break;
            }
        }
    }

    @Test
    public void joinClassTest() {

        Activity activity = activityTestRule.getActivity();

        joinClass(validClassCode1);

        Espresso.onView(ViewMatchers.withText(R.string.UI_class_joined_msg))
                .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        leaveClass(validClassName1);
        Assert.assertTrue(true);
    }

    @Test
    public void joinClassAlreadyTest() {

        Activity activity = activityTestRule.getActivity();

        joinClass(validClassCode1);

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        joinClass(validClassCode1);

        Espresso.onView(ViewMatchers.withText(R.string.UI_class_joined_already_msg))
                .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        leaveClass(validClassName1);
        Assert.assertTrue(true);
    }

    @Test
    public void invalidLongClassTest() {

        Activity activity = activityTestRule.getActivity();

        joinClass(invalidLongClassCode);

        Espresso.onView(ViewMatchers.withText(R.string.UI_invalid_class_code_msg))
                .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invalidClassTest() {

        Activity activity = activityTestRule.getActivity();

        joinClass(invalidClassCode);

        Espresso.onView(ViewMatchers.withText(R.string.UI_invalid_class_code_msg))
                .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void swapClassTest() {
        joinClass(validClassCode1);

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        joinClass(validClassCode2);

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        // click the other class
        // check if you are now in the other class

        leaveClass(validClassName1);

        leaveClass(validClassName2);

        Assert.assertTrue(true);
    }

    private void leaveClass(String className) {
        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(className))
                .perform(ViewActions.scrollTo(), ViewActions.longClick());

        Espresso.onView(ViewMatchers.withText(R.string.YES))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());
    }

    private void joinClass(String classCode) {
        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.add_class_button))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_enter_class_code_msg))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_course_code))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText(classCode));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());
    }
}
