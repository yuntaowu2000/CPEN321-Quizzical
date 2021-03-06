package com.cpen321.quizzical;

import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.cpen321.quizzical.utils.ChoicePair;
import com.cpen321.quizzical.data.QuizPackage;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class QuizPackageTest {
    @Test
    public void testQuestionPackageCreation() {
        QuizPackage quizPackage = new QuizPackage();

        assertEquals(0, quizPackage.getQuestionList().size());
    }

    @Test
    public void testQuestionPackageCreationWithId() {
        QuizPackage quizPackage = new QuizPackage(12, 0);

        assertEquals(0, quizPackage.getQuestionList().size());
        assertEquals(12, quizPackage.getQuizCode());
    }

    @Test
    public void testQuestionPackageAddQuestions() {
        QuizPackage quizPackage = new QuizPackage(1, 0);

        assertEquals(0, quizPackage.getQuestionList().size());

        List<ChoicePair> choicePairList = new ArrayList<>();

        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">2</p>"));
        choicePairList.add(new ChoicePair(true, "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png"));
        choicePairList.add(new ChoicePair(false, "<p>https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png</p>"));
        choicePairList.add(new ChoicePair(false, "$$ c = \\sqrt{a^2 + b^2} $$"));

        try {
            quizPackage.addMCQuestion(1, CourseCategory.Math, "calculate: $$1+1=$$", false, "", choicePairList, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, quizPackage.getQuestionList().size());

        //we only have one questions here, both functions should return a list of 1 element
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.Math, 1).size());
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.Math, 3).size());
    }

    @Test
    public void testQuestionPackageAddQuestionsWithJsonString() {
        QuizPackage quizPackage = new QuizPackage(1, 0);

        String questionJson = "{\"index\":0,\"category\":\"Math\",\"questionType\":\"MC\",\"question\":\"calculate: $$1+1\\u003d$$\",\"HasPic\":false,\"picSrc\":\"\",\"choices\":[{\"isPic\":false,\"str\":\"$$3$$\"},{\"isPic\":false,\"str\":\"$$2$$\"}],\"correctAnsNum\":2}";

        quizPackage.addMCQuestion(questionJson);

        assertEquals(1, quizPackage.getQuestionList().size());

        //we only have one questions here, both functions should return a list of 1 element
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.Math, 1).size());
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.Math, 3).size());
    }

    @Test
    public void testQuestionPackageAddQuestionsWithJsonString2() {
        QuizPackage quizPackage = new QuizPackage(1, 0);

        String questionJson = "{\"index\":0,\"category\":\"Math\",\"questionType\":\"MC\",\"question\":\"calculate: $$1+1\\u003d$$\",\"HasPic\":false,\"picSrc\":\"\",\"choices\":[{\"isPic\":false,\"str\":\"$$3$$\"},{\"isPic\":false,\"str\":\"$$2$$\"}],\"correctAnsNum\":2}";

        quizPackage.addMCQuestion(questionJson);

        //make sure the question is correctly generated
        QuestionsMC mc = (QuestionsMC) quizPackage.getQuestionList().get(0);

        assertEquals(0, mc.getID());
        assertEquals(CourseCategory.Math, mc.getCategory());
        assertEquals("calculate: $$1+1=$$", mc.getQuestion());
        assertFalse(mc.hasPic());
        assertEquals("", mc.getPicSrc());
        assertEquals(2, mc.getChoices().size());
        assertEquals("$$3$$",mc.getChoices().get(0).getStr());
        assertEquals("$$2$$",mc.getChoices().get(1).getStr());
        assertEquals(2, mc.getCorrectAnsNum());
    }

    @Test
    public void testQPAddQuestionsOfMultipleCategories() {
        QuizPackage quizPackage = new QuizPackage(1, 0);

        String questionJson = "{\"index\":0,\"category\":\"Math\",\"questionType\":\"MC\",\"question\":\"calculate: $$1+1\\u003d$$\",\"HasPic\":false,\"picSrc\":\"\",\"choices\":[{\"isPic\":false,\"str\":\"$$3$$\"},{\"isPic\":false,\"str\":\"$$2$$\"}],\"correctAnsNum\":2}";

        quizPackage.addMCQuestion(questionJson);

        questionJson = "{\"index\":1,\"category\":\"English\",\"questionType\":\"MC\",\"question\":\"calculate: $$1+1\\u003d$$\",\"HasPic\":false,\"picSrc\":\"\",\"choices\":[{\"isPic\":false,\"str\":\"$$3$$\"},{\"isPic\":false,\"str\":\"$$2$$\"}],\"correctAnsNum\":2}";

        quizPackage.addMCQuestion(questionJson);

        assertEquals(2, quizPackage.getQuestionList().size());

        //we only have one questions here, both functions should return a list of 1 element
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.Math, 1).size());
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.Math, 3).size());

        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.English, 1).size());
        assertEquals(1, quizPackage.getQuestionsByCategory(CourseCategory.English, 5).size());
    }

    @Test
    public void testQPAddQuestionsMethods() {
        QuizPackage quizPackage = new QuizPackage(1, 0);

        String questionJson = "{\"index\":0,\"category\":\"Math\",\"questionType\":\"MC\",\"question\":\"calculate: $$1+1\\u003d$$\",\"HasPic\":false,\"picSrc\":\"\",\"choices\":[{\"isPic\":false,\"str\":\"$$3$$\"},{\"isPic\":false,\"str\":\"$$2$$\"}],\"correctAnsNum\":2}";

        quizPackage.addMCQuestion(questionJson);

        List<ChoicePair> choicePairList = new ArrayList<>();

        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">2</p>"));
        choicePairList.add(new ChoicePair(true, "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png"));
        choicePairList.add(new ChoicePair(false, "<p>https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png</p>"));
        choicePairList.add(new ChoicePair(false, "$$ c = \\sqrt{a^2 + b^2} $$"));

        QuestionsMC mc_expected = new QuestionsMC(CourseCategory.English, "calculate: $$1+1=$$", false, "", choicePairList, 1);
        try {
            quizPackage.addMCQuestion(CourseCategory.English, "calculate: $$1+1=$$", false, "", choicePairList, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        QuestionsMC mc = (QuestionsMC) quizPackage.getQuestionsByCategory(CourseCategory.English, 1).get(0);

        assertEquals(mc_expected.getID(), mc.getID());
        assertEquals(mc_expected.getQuestionType(), mc.getQuestionType());
        assertEquals(mc_expected.getQuestion(), mc.getQuestion());
        assertEquals(mc_expected.getPicSrc(), mc.getPicSrc());
        assertEquals(mc_expected.hasPic(), mc.hasPic());
        assertEquals(mc_expected.getCorrectAnsNum(), mc.getCorrectAnsNum());
        assertEquals(mc_expected.getChoices(), mc.getChoices());
    }


}