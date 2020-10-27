package com.cpen321.quizzical;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class M7demoTest {
    @Rule
    public ActivityScenarioRule<HomeActivity> activityScenarioRule = new ActivityScenarioRule<HomeActivity>(HomeActivity.class);

    @Test
    public void test() {
        String new_userName = "Valhalla";
        Espresso.onView(ViewMatchers.withText(R.string.UI_profile)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.profile_username_change_btn)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_username))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.typeText(new_userName));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.profile_username))
                .check(ViewAssertions.matches(ViewMatchers.withText(new_userName)));
    }
}
