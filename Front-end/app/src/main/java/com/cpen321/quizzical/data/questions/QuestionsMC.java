package com.cpen321.quizzical.data.questions;

import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.utils.ChoicePair;
import com.google.gson.Gson;

import java.util.ArrayList;
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

    public QuestionsMC() {
        this.category = CourseCategory.DontCare;
        this.question = "";
        this.HasPic = false;
        this.picSrc = "";
        this.choices = new ArrayList<>();
        this.correctAnsNum = 0;
    }

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
    public void setID(int id) {
        this.index = id;
    }

    @Override
    public void setCategory(CourseCategory category) {
        this.category = category;
    }

    @Override
    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public void setHasPic(boolean hasPic) {
        this.HasPic = hasPic;
    }

    @Override
    public void setPicSrc(String picSrc) {
        this.picSrc = picSrc;
    }

    public void addChoice(ChoicePair choice) {
        this.choices.add(choice);
    }

    public void deleteChoice(int choiceIndex) {
        this.choices.remove(choiceIndex);
    }

    public void setCorrectAnsNum(int correctAnsNum) {
        this.correctAnsNum = correctAnsNum;
    }

    public int getChoiceIndex(ChoicePair choicePair) {
        for (int i = 0; i < this.choices.size(); i++) {
            if (this.choices.get(i).equals(choicePair)) {
                return i;
            }
        }
        return -1;
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
