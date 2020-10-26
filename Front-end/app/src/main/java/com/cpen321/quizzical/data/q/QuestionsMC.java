package com.cpen321.quizzical.data.Questions;

import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.utils.ChoicePair;
import com.google.gson.Gson;

import java.util.List;

public class QuestionsMC implements IQuestion {

    private int index;
    private CourseCategory category;
    private QuestionType questionType = QuestionType.MC;
    private String question;
    private boolean HasPic;
    private String picSrc;
    private List<ChoicePair> choices;
    private int correctAnsNum;

    public QuestionsMC(CourseCategory category, String question, boolean hasPic, String picSrc, List<ChoicePair> choices, int correctAnsNum) {
        this.category = category;
        this.question = question;
        this.HasPic = hasPic;
        this.picSrc = picSrc;
        this.choices = choices;
        this.correctAnsNum = correctAnsNum;
    }

    public QuestionsMC(int id, CourseCategory category, String question, boolean hasPic, String picSrc, List<ChoicePair> choices, int correctAnsNum) {
        this.index = id;
        this.category = category;
        this.question = question;
        this.HasPic = hasPic;
        this.picSrc = picSrc;
        this.choices = choices;
        this.correctAnsNum = correctAnsNum;
    }

    @Override
    public int getID() {
        return this.index;
    }

    @Override
    public CourseCategory getCategory() {
        return this.category;
    }

    @Override
    public QuestionType getQuestionType() {
        return this.questionType;
    }

    @Override
    public String getQuestion() {
        return this.question;
    }

    @Override
    public boolean hasPic() {
        return this.HasPic;
    }

    @Override
    public String getPicSrc() {
        return this.picSrc;
    }

    @Override
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public List<ChoicePair> getChoices() {
        return this.choices;
    }

    public int getCorrectAnsNum() {
        return this.correctAnsNum;
    }
}
