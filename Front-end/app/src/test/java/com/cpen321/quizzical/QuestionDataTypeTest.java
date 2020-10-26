package com.cpen321.quizzical;

import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.cpen321.quizzical.utils.ChoicePair;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class QuestionDataTypeTest {

    @Test
    public void testToJson() {
        List<ChoicePair> choicePairList = new ArrayList<>();
        choicePairList.add(new ChoicePair(false, "$$3$$"));
        choicePairList.add(new ChoicePair(false, "$$2$$"));

        QuestionsMC mc = new QuestionsMC(CourseCategory.Math, "calculate: $$1+1=$$", false, "", choicePairList, 2);

        String json = mc.toJsonString();

        //003d is the unicode for = sign, and when doing the json conversion, Gson automatically converts to unicode.
        String expected = "{\"index\":0,\"category\":\"Math\",\"questionType\":\"MC\",\"question\":\"calculate: $$1+1\\u003d$$\",\"HasPic\":false,\"picSrc\":\"\",\"choices\":[{\"isPic\":false,\"str\":\"$$3$$\"},{\"isPic\":false,\"str\":\"$$2$$\"}],\"correctAnsNum\":2}";

        assertEquals(expected, json);
    }

    @Test
    public void testToJson1() {

        List<ChoicePair> choicePairList = new ArrayList<>();

        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">2</p>"));
        choicePairList.add(new ChoicePair(true, "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png"));
        choicePairList.add(new ChoicePair(false, "<p>https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png</p>"));
        choicePairList.add(new ChoicePair(false, "$$ c = \\sqrt{a^2 + b^2} $$"));

        QuestionsMC mc = new QuestionsMC(1, CourseCategory.Math, "calculate: $$1+1=$$", false, "", choicePairList, 1);

        String json = mc.toJsonString();
        Gson gson = new Gson();
        QuestionsMC mc2 = gson.fromJson(json, QuestionsMC.class);

        assertEquals(mc.getID(), mc2.getID());
        assertEquals(mc.getCategory(), mc2.getCategory());
        assertEquals(mc.getChoices(), mc2.getChoices());
        assertEquals(mc.getCorrectAnsNum(), mc2.getCorrectAnsNum());
        assertEquals(mc.hasPic(), mc2.hasPic());
        assertEquals(mc.getPicSrc(), mc2.getPicSrc());
        assertEquals(mc.getQuestion(), mc2.getQuestion());
        assertEquals(mc.getQuestionType(), mc2.getQuestionType());
    }
}