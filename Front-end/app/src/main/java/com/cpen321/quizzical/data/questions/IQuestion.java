package com.cpen321.quizzical.data.questions;

import com.cpen321.quizzical.data.CourseCategory;

public interface IQuestion {

    void setID(int id);

    void setCategory(CourseCategory category);

    void setQuestion(String question);

    void setHasPic(boolean hasPic);

    void setPicSrc(String picSrc);

    int getID();

    CourseCategory getCategory();

    QuestionType getQuestionType();

    String getQuestion();

    boolean hasPic();

    String getPicSrc();

    String toJsonString();

}
