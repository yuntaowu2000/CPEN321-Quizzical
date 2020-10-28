package com.cpen321.quizzical;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cpen321.quizzical.quizactivities.QuizActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class QuizActivityTest {
    @Rule
    public ActivityScenarioRule<QuizActivity> activityScenarioRule = new ActivityScenarioRule<QuizActivity>(QuizActivity.class);

    //currently not working
    @Test
    public void testResponseAllCorrect() {
        String correctAns = "2";

        for (int i = 0; i < 3; i++) {
            Espresso.onView(ViewMatchers.withSubstring(correctAns)).perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withText(R.string.UI_submit)).perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withText(String.format("You are correct. You got %d/%d correct.", i, 3)))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
            if (i != 2) {
                Espresso.onView(ViewMatchers.withText(R.string.UI_next)).perform(ViewActions.click());
            } else {
                Espresso.onView(ViewMatchers.withText(R.string.UI_finish)).perform(ViewActions.click());
            }
        }

        Espresso.onView(ViewMatchers.withId(R.id.quiz_finished_page_response))
                .check(ViewAssertions.matches(ViewMatchers.withText("You got 3/3 correct. Congratulations!")));
        Espresso.onView(ViewMatchers.withId(R.id.quiz_finished_page_exp_earned))
                .check(ViewAssertions.matches(ViewMatchers.withText("You got 15 EXP!")));
    }
}
