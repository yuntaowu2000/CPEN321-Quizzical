package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class InitActivityTest {

    @Rule
    public ActivityTestRule<InitActivity> activityTestRule = new ActivityTestRule<InitActivity>(InitActivity.class);
    private SharedPreferences sp;

    @Before
    public void cleanUpSharedPreferences() {
        Activity activity = activityTestRule.getActivity();
        sp = activity.getSharedPreferences("currLoginUser", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    @Test
    public void testBasicUsernameEmailPass() {

        Espresso.onView(ViewMatchers.withId(R.id.sign_in_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withSubstring("@gmail.com"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(sp.getString("username", "")))
                .perform(ViewActions.replaceText("valid"));

        Espresso.onView(ViewMatchers.withText("valid"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(sp.getString("Email", "")))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        assert(true);
    }

    @Test
    public void testInvalidUsername() {

        Espresso.onView(ViewMatchers.withId(R.id.sign_in_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withSubstring("@gmail.com"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(sp.getString("username", "")))
                .perform(ViewActions.replaceText("invalid+username"));

        Espresso.onView(ViewMatchers.withText(R.string.UI_username_invalid_msg))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        assert(true);
    }

    @Test
    public void testInvalidEmail() {

        Espresso.onView(ViewMatchers.withId(R.id.sign_in_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withSubstring("@gmail.com"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(sp.getString("Email", "")))
                .perform(ViewActions.replaceText("this is an invalid email"));

        Espresso.onView(ViewMatchers.withText(R.string.UI_email_invalid_msg))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        assert(true);
    }
}
