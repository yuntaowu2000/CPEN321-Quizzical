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
    Activity currActivity;
    SharedPreferences sp;
    String[] moduleNames;

    @Before
    public void setupSharedPreference() {
        currActivity = activityTestRule.getActivity();
        sp = currActivity.getSharedPreferences(currActivity.getString(R.string.curr_login_user), Context.MODE_PRIVATE);

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

    private void checkModules(String value, int i) {
        Espresso.onView(ViewMatchers.withId(R.id.module_list)).perform(ViewActions.click());
        Espresso.onData(Matchers.is(value)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(value)).
                inRoot(RootMatchers.withDecorView(Matchers.not(currActivity.getWindow().getDecorView()))).check(ViewAssertions.matches((ViewMatchers.isDisplayed())));
    }

    @Test
    public void testChangingModules() throws InterruptedException {

        for (int i = 0; i < moduleNames.length; i++) {
            checkModules(moduleNames[i], i);
            //make sure the previous toast has disappeared
            Thread.sleep(2000);
        }

        Assert.assertTrue(true);
    }
}
