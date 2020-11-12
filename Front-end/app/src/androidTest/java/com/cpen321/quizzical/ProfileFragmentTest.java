package com.cpen321.quizzical;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {
    @Rule
    public ActivityScenarioRule<HomeActivity> activityScenarioRule = new ActivityScenarioRule<HomeActivity>(HomeActivity.class);

    @Test
    public void testValidUsernameChange() {
        String newUserName = "Valhalla";
        performChange(newUserName, R.id.profile_username_change_btn, R.string.UI_example_username);

        Espresso.onView(ViewMatchers.withId(R.id.profile_username))
                .check(ViewAssertions.matches(ViewMatchers.withText(newUserName)));

        Assert.assertTrue(true);
    }

    @Test
    public void testInvalidUsernameChange() {
        String newUserName = "Va+";
        performChange(newUserName, R.id.profile_username_change_btn, R.string.UI_example_username);

        Espresso.onView(ViewMatchers.withText(R.string.UI_username_invalid_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testValidEmailChange() {
        String newEmail = "Valhalla@hotmail.com";
        performChange(newEmail, R.id.profile_email_change_btn, R.string.UI_example_email);

        Espresso.onView(ViewMatchers.withId(R.id.profile_email))
                .check(ViewAssertions.matches(ViewMatchers.withText(newEmail)));

        Assert.assertTrue(true);
    }

    @Test
    public void testInvalidEmailChange() {
        String newEmail = "Va+";
        performChange(newEmail, R.id.profile_email_change_btn, R.string.UI_example_email);

        Espresso.onView(ViewMatchers.withText(R.string.UI_email_invalid_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    private void performChange(String newEmail, int buttonId, int textId) {
        Espresso.onView(ViewMatchers.withText(R.string.UI_profile)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(buttonId)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withHint(textId))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText(newEmail));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Assert.assertTrue(true);
    }
}
