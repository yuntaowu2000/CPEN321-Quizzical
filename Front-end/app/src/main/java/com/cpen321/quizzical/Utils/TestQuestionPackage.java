package com.cpen321.quizzical.Utils;

import com.cpen321.quizzical.Data.CourseCategory;

import java.util.ArrayList;
import java.util.List;

public class TestQuestionPackage {
    /**
     * This class is used for testing the functionality of Latex rendering and quiz set up
     */

    private QuestionPackage questionPackage;

    public TestQuestionPackage() {
        questionPackage = new QuestionPackage();

        List<ChoicePair> choicePairList = new ArrayList<>();

        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">2</p>"));
        choicePairList.add(new ChoicePair(true, "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png"));
        choicePairList.add(new ChoicePair(false, "<p>https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png</p>"));
        choicePairList.add(new ChoicePair(false, "$$ c = \\sqrt{a^2 + b^2} $$"));

        try {
            questionPackage.AddMCQuestion(CourseCategory.Math, "calculate: $$1+1=$$", false, "", choicePairList, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        choicePairList = new ArrayList<>();
        choicePairList.add(new ChoicePair(false, "$$2$$"));
        choicePairList.add(new ChoicePair(false, "$$3$$"));
        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">plain text</p>"));
        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">$5$ with some text</p>"));

        try {
            questionPackage.AddMCQuestion(CourseCategory.Math, "calculate: $\\frac{\\sqrt{4}+2}{2}$",
                    false, "",
                    choicePairList, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            questionPackage.AddMCQuestion(CourseCategory.Math, "",
                    true, "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png",
                    choicePairList, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public QuestionPackage GetPackage() {
        return this.questionPackage;
    }
}
