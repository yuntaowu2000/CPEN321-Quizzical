package com.cpen321.quizzical.Data.Questions;

import com.cpen321.quizzical.Data.CourseCategory;

public interface IQuestion {

    int getID();

    CourseCategory getCategory();

    QuestionType getQuestionType();

    String getQuestion();

    boolean hasPic();

    String getPicSrc();

    String toJsonString();

}
