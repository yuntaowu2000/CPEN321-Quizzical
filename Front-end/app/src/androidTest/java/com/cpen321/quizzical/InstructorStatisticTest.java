package com.cpen321.quizzical;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class InstructorStatisticTest {
    /*Note: if you want to run the tests on your own machine,
    you may need to replace the client id with your own google client id at line 108 in CPEN321-Quizzical\Front-end\app\src\main\res\values\strings.xml
    and sign in the app properly.
    You need an instructor account for the tests here.
     */

    @Rule
    public ActivityTestRule<HomeActivity> activityTestRule = new ActivityTestRule<HomeActivity>(HomeActivity.class);

    @Test
    public void checkStudentStatSetup() {
        Espresso.onView(ViewMatchers.withText(R.string.UI_class_statistics)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.UI_leaderboard_score))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Assert.assertTrue(true);
    }

    @Test
    public void checkSwappingToTeacherLeaderboard() {
        Espresso.onView(ViewMatchers.withText(R.string.UI_class_statistics)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.UI_teachers_leaderboard))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_back_to_statistics))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_leaderboard_score))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Assert.assertTrue(true);
    }

}
