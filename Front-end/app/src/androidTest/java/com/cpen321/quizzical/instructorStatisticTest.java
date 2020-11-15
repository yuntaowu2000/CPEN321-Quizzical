package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class instructorStatisticTest {

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
