package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {
    @Rule
    public ActivityTestRule<HomeActivity> activityTestRule = new ActivityTestRule<HomeActivity>(HomeActivity.class);

    private SharedPreferences sp;
    @Before
    public void setupSharedPreferences() {
        Activity activity = activityTestRule.getActivity();
        sp = activity.getSharedPreferences("currLoginUser", Context.MODE_PRIVATE);
    }

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
        String newUserName = "Va";
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

    @Test
    public void testLogOutInfo() {
        Espresso.onView(ViewMatchers.withText(R.string.UI_profile)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.UI_log_out))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_warning))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Do not log out
        Espresso.onView(ViewMatchers.withText(R.string.NO))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_log_out))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testChangeProfileImg() {
        Espresso.onView(ViewMatchers.withText(R.string.UI_profile)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.profile_pic)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.UI_change_profile_image_msg))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        String prevImg = sp.getString(activityTestRule.getActivity().getString(R.string.PROFILE_IMG), "");

        Espresso.onView(ViewMatchers.withText(R.string.NO))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        String currImg = sp.getString(activityTestRule.getActivity().getString(R.string.PROFILE_IMG), "");
        Assert.assertEquals(prevImg, currImg);
    }

    @Test
    public void testChangeNotificationFrequency() {
        Espresso.onView(ViewMatchers.withText(R.string.UI_profile)).perform(ViewActions.click());

        String[] notificationFrequencyArray = activityTestRule.getActivity().getResources().getStringArray(R.array.notification_frequency);
        for (int i = 0; i < notificationFrequencyArray.length; i++) {
            checkNotificationFrequency(notificationFrequencyArray[i], i);
        }

        //reset the sp
        checkNotificationFrequency(notificationFrequencyArray[1], 1);
        Assert.assertTrue(true);
    }

    private void checkNotificationFrequency(String value, int i) {
        Espresso.onView(ViewMatchers.withId(R.id.notification_settings)).perform(ViewActions.click());
        Espresso.onData(Matchers.is(value)).perform(ViewActions.click());
        Assert.assertEquals(i, sp.getInt("notificationFrequency", 0));
    }
}
