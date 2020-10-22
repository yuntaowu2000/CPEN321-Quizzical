package com.cpen321.quizzical.data.questions;

import com.cpen321.quizzical.data.CourseCategory;

public interface IQuestion {

    int getID();

    CourseCategory getCategory();

    QuestionType getQuestionType();

    String getQuestion();

    boolean hasPic();

    String getPicSrc();

    String toJsonString();

}
