package com.cpen321.quizzical.data;

import com.cpen321.quizzical.utils.OtherUtils;
import com.google.gson.Gson;

public class QuizModules {

    private String moduleName;
    private int id;
    private int classCode;
    private CourseCategory category;
    private String quizLink;
    private String wrongQuestionLink;
    private String statsLink;
    private String notesLink;

    public QuizModules (String moduleName, int classCode, CourseCategory category) {
        this.id = 0;
        this.moduleName = moduleName;
        this.classCode = classCode;
        this.category = category;
        this.quizLink = "";
        this.wrongQuestionLink = "";
        this.statsLink = "";
        this.notesLink = "";
    }

    public QuizModules (String jsonString) {
        if (OtherUtils.stringIsNullOrEmpty(jsonString)) {
            this.id = 0;
            this.moduleName = "";
            this.classCode = 0;
            this.category = CourseCategory.DontCare;
            this.quizLink = "";
            this.wrongQuestionLink = "";
            this.statsLink = "";
            this.notesLink = "";
        }
        Gson g = new Gson();
        QuizModules q = g.fromJson(jsonString, QuizModules.class);
        this.moduleName = q.moduleName;
        this.classCode = q.classCode;
        this.category = q.category;
        this.quizLink = q.quizLink;
        this.wrongQuestionLink = q.wrongQuestionLink;
        this.statsLink = q.statsLink;
        this.notesLink = q.notesLink;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setQuizLink(String quizLink) {
        this.quizLink = quizLink;
    }

    public void setWrongQuestionLink(String wrongQuestionLink) {
        this.wrongQuestionLink = wrongQuestionLink;
    }

    public void setStatsLink(String statsLink) {
        this.statsLink = statsLink;
    }

    public void setNotesLink(String notesLink) {
        this.notesLink = notesLink;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getQuizLink() {
        return this.quizLink;
    }

    public String getWrongQuestionLink() {
        return this.wrongQuestionLink;
    }

    public String getStatsLink() {
        return this.statsLink;
    }

    public String getNotesLink() {
        return this.notesLink;
    }

    public String toJson(){
        Gson g = new Gson();
        return g.toJson(this);
    }

}
