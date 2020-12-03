package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.cpen321.quizzical.data.Classes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class StudentClassTest {

    /*Note: if you want to run the tests on your own machine,
    you may need to replace the client id with your own google client id at line 108 in CPEN321-Quizzical\Front-end\app\src\main\res\values\strings.xml
    and sign in the app properly.
    You need a student account for the tests here, and please join a class (e.g. 35608) before triggering the test.
    */

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
            Classes classes = new Classes(c); //TODO: For some reason sometimes it doesn't clean up properly. The buttons are there but they don't appear in the log, even though I can still get 'you already joined this class' messages
            Log.d("testclass", ""+classes.getClassName());
            if (classes.getClassName().equals(validClassName1) ||
                classes.getClassName().equals(validClassName2)) {
                leaveClass(classes.getClassName());
                Log.d("testclass", "removed");
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
    public void joinClassAlreadyTest() throws InterruptedException {

        Activity activity = activityTestRule.getActivity();

        joinClass(validClassCode1);

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        joinClass(validClassCode1);
        Espresso.pressBack();
        Thread.sleep(1000);

        Espresso.onView(ViewMatchers.withText((R.string.UI_invalid_class_code_msg)))
                .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));

        Espresso.pressBack();

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
        Assert.assertTrue(true);
    }

    @Test
    public void invalidClassTest() {

        Activity activity = activityTestRule.getActivity();

        joinClass(invalidClassCode);

        Espresso.onView(ViewMatchers.withText((R.string.UI_invalid_class_code_msg)))
                .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                .check(matches(isDisplayed()));
        Assert.assertTrue(true);
    }

    @Test
    public void swapClassTest() {

        joinClass(validClassCode1);

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(allOf(ViewMatchers.withText(String.format(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(R.string.UI_current_class_name), validClassName1)), withResourceName("quiz_page_class_info_text")))
                .check(matches(isDisplayed()));

        joinClass(validClassCode2);

        Espresso.onView(allOf(ViewMatchers.withText(String.format(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(R.string.UI_current_class_name), validClassName2)), withResourceName("quiz_page_class_info_text")))
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withText(validClassName1))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(allOf(ViewMatchers.withText(String.format(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(R.string.UI_current_class_name), validClassName1)), withResourceName("quiz_page_class_info_text")))
                .check(matches(isDisplayed()));

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
                .perform(ViewActions.scrollTo(), ViewActions.click());

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
