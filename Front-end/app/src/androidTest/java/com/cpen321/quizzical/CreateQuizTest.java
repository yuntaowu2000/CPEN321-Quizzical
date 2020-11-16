package com.cpen321.quizzical;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.data.QuizModules;
import com.cpen321.quizzical.quizactivities.CreateQuizActivity;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class CreateQuizTest {

    @Rule
    public ActivityTestRule<CreateQuizActivity> activityTestRule = new ActivityTestRule<CreateQuizActivity>(CreateQuizActivity.class);
    private Activity currActivity;
    private String[] moduleNames;

    @Before
    public void setupSharedPreference() {
        currActivity = activityTestRule.getActivity();
        SharedPreferences sp = currActivity.getSharedPreferences(currActivity.getString(R.string.curr_login_user), Context.MODE_PRIVATE);

        Classes currClass = new Classes(sp.getString(currActivity.getString(R.string.CURR_CLASS), ""));

        String moduleId = currClass.getClassCode() + currActivity.getString(R.string.QUIZ_MODULES);
        String moduleListString = sp.getString(moduleId, "");
        String[] modules = moduleListString.split(";");
        ArrayList<QuizModules> quizModulesArrayList = new ArrayList<>();
        for (String str : modules) {
            quizModulesArrayList.add(new QuizModules(str));
        }
        moduleNames = new String[quizModulesArrayList.size()];
        for (int i = 0; i < quizModulesArrayList.size();i ++) {
            moduleNames[i] = quizModulesArrayList.get(i).getModuleName();
        }
    }

    private void checkModules(String value) {
        Espresso.onView(ViewMatchers.withId(R.id.module_list)).perform(ViewActions.click());
        Espresso.onData(Matchers.is(value)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(value)).
                inRoot(RootMatchers.withDecorView(Matchers.not(currActivity.getWindow().getDecorView())))
                .check(ViewAssertions.matches((ViewMatchers.isDisplayed())));
    }

    @Test
    public void testChangingModules() throws InterruptedException {

        for (int i = 0; i < moduleNames.length; i++) {
            checkModules(moduleNames[i]);
            //make sure the previous toast has disappeared
            Thread.sleep(5000);
        }

        Assert.assertTrue(true);
    }

    @Test
    public void backPressGuardingCheck() {
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.UI_quit_quiz_editing_warning))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.NO))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_finish))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void backPressGuardingCheck2() {
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.UI_quit_quiz_editing_warning))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.YES))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.quiz_page_refresh_layout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testSubmitNoQuestions() {
        Espresso.onView(ViewMatchers.withId(R.id.quiz_create_finish))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.UI_invalid_quiz))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.quiz_create_finish))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testTooManyQuestions() {
        for (int i = 0; i < 20; i++) {
            Espresso.onView(ViewMatchers.withId(R.id.add_question_button))
                    .perform(ViewActions.scrollTo(), ViewActions.click());
        }
        Espresso.onView(ViewMatchers.withId(R.id.add_question_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withText(String.format(currActivity.getString(R.string.UI_too_many_questions), 20)))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.quiz_create_finish))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testTooManyChoices() throws InterruptedException {

        Espresso.onView(ViewMatchers.withId(R.id.add_question_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        for (int i = 0; i < 6; i++) {
            addNewAnswers(i);
            Thread.sleep(1000);
        }

        Espresso.onView(ViewMatchers.withId(R.id.answer_input_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withText(String.format(currActivity.getString(R.string.UI_too_many_choices), 6)))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.quiz_create_finish))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    private void addNewAnswers(int i) {
        Espresso.onView(ViewMatchers.withId(R.id.answer_input_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_answer))
                .perform(ViewActions.typeText(String.valueOf(i)));

        Espresso.onView(ViewMatchers.withText(R.string.UI_submit))
                .perform(ViewActions.click());
    }

    @Test
    public void testNoQuestionInput() {

        Espresso.onView(ViewMatchers.withId(R.id.add_question_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.quiz_create_finish))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withSubstring("no question field"))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testTooFewChoices() {

        Espresso.onView(ViewMatchers.withId(R.id.add_question_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_question))
                .perform(ViewActions.typeText("aaa"));

        checkFewAnswer();

        addNewAnswers(0);

        checkFewAnswer();

        Assert.assertTrue(true);
    }

    private void checkFewAnswer() {
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withText(R.string.UI_finish))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withSubstring("has less than 2 choices."))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText(R.string.OK))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());
    }

    @Test
    public void testNoCorrectAnswer() throws InterruptedException {

        Espresso.onView(ViewMatchers.withId(R.id.add_question_button))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        Espresso.onView(ViewMatchers.withHint(R.string.UI_example_question))
                .perform(ViewActions.typeText("aaa"));

        for (int i = 0; i < 3; i++) {
            addNewAnswers(i);
            Thread.sleep(1000);
        }
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withId(R.id.quiz_create_finish))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withSubstring("has no correct answer."))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }
}
