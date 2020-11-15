package com.cpen321.quizzical;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.Root;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class instrcutorQuizFragmentTest {
    //checks whether quiz wrong question list and stats are set up correctly
    @Rule
    public ActivityTestRule<HomeActivity> activityTestRule = new ActivityTestRule<HomeActivity>(HomeActivity.class);

    @Before
    public void switchToMyClass() {
        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText("test1"))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.class_switch_fab))
                .perform(ViewActions.click());
    }

    private <T> Matcher<T> first(final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("should return first matching item");
            }
        };
    }

    @Test
    public void testStats() {
        Espresso.onView(first(ViewMatchers.withText(R.string.UI_stats)))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withSubstring("Average"))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withSubstring("highest"))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withSubstring("username"))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withSubstring("score"))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());
        Assert.assertTrue(true);
    }

    @Test
    public void testWrongQuestions() {
        Espresso.onView(first(ViewMatchers.withText(R.string.UI_wrong_questions)))
                .perform(ViewActions.click());

        Espresso.onView(first(ViewMatchers.withSubstring("people got wrong")))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());
        Assert.assertTrue(true);
    }
}
