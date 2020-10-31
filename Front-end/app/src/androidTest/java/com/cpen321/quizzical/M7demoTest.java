package com.cpen321.quizzical;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class M7demoTest {
    @Rule
    public ActivityScenarioRule<HomeActivity> activityScenarioRule = new ActivityScenarioRule<HomeActivity>(HomeActivity.class);

    @Test
    public void testValidUsernameChange() {
        String new_userName = "Valhalla";
        performChange(new_userName, R.id.profile_username_change_btn, R.string.UI_example_username);

        Espresso.onView(ViewMatchers.withId(R.id.profile_username))
                .check(ViewAssertions.matches(ViewMatchers.withText(new_userName)));
    }

    @Test
    public void testInvalidUsernameChange() {
        String new_userName = "Va+";
        performChange(new_userName, R.id.profile_username_change_btn, R.string.UI_example_username);

        Espresso.onView(ViewMatchers.withText(R.string.UI_username_invalid_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testValidEmailChange() {
        String new_email = "Valhalla@hotmail.com";
        performChange(new_email, R.id.profile_email_change_btn, R.string.UI_example_email);

        Espresso.onView(ViewMatchers.withId(R.id.profile_email))
                .check(ViewAssertions.matches(ViewMatchers.withText(new_email)));
    }

    @Test
    public void testInvalidEmailChange() {
        String new_email = "Va+";
        performChange(new_email, R.id.profile_email_change_btn, R.string.UI_example_email);

        Espresso.onView(ViewMatchers.withText(R.string.UI_email_invalid_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    private void performChange(String new_email, int buttonId, int textId) {
        Espresso.onView(ViewMatchers.withText(R.string.UI_profile)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(buttonId)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withHint(textId))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText(new_email));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());
    }
}
