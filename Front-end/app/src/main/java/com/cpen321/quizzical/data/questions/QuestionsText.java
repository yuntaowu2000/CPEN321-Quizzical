package com.cpen321.quizzical.data.questions;

import com.cpen321.quizzical.data.CourseCategory;

public class QuestionsText implements IQuestion {

    @Override
    public void setID(int id) {

    }

    @Override
    public void setCategory(CourseCategory category) {

    }

    @Override
    public void setQuestion(String question) {

    }

    @Override
    public void setHasPic(boolean hasPic) {

    }

    @Override
    public void setPicSrc(String picSrc) {

    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public CourseCategory getCategory() {
        return null;
    }

    @Override
    public QuestionType getQuestionType() {
        return null;
    }

    @Override
    public String getQuestion() {
        return null;
    }

    @Override
    public boolean hasPic() {
        return false;
    }

    @Override
    public String getPicSrc() {
        return null;
    }

    @Override
    public String toJsonString() {
        return null;
    }


}
