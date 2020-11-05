package com.cpen321.quizzical.utils;

import android.util.Log;

import com.cpen321.quizzical.data.CourseCategory;
import com.cpen321.quizzical.data.questions.IQuestion;
import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizPackage {

    private static Random rng = new Random();
    /**
     * This class provides an interface for questions and quizzes
     * When an instructor creates a quiz, addQuestions will be called once a question is created
     * When a student/instructor do a quiz, GetQuestions will be called
     */

    private int quizCode;
    private int class_code;
    private CourseCategory courseCategory;
    private String instructorUID;
    private String moduleName;
    private List<IQuestion> questionList;

    public QuizPackage() {
        this.quizCode = 0;
        this.class_code = 0;
        this.courseCategory = CourseCategory.DontCare;
        this.instructorUID = "";
        this.moduleName = "";
        questionList = new ArrayList<>();
    }

    public QuizPackage(String quizJson) {
        try {
            Gson g = new GsonBuilder().registerTypeAdapter(IQuestion.class, new InterfaceAdapter()).create();
            JSONObject jsonObject = new JSONObject(quizJson);
            this.quizCode = jsonObject.getInt("quizCode");
            this.class_code = jsonObject.getInt("class_code");
            this.courseCategory = getCourseCategoryByString(jsonObject.getString("courseCategory"));
            this.instructorUID = jsonObject.getString("instructorUID");
            this.moduleName = jsonObject.getString("moduleName");

            this.questionList = new ArrayList<>();
            JSONArray ql = jsonObject.getJSONArray("questionList");
            for (int i = 0; i < ql.length(); i++) {
                String questionJson = ql.get(i).toString();
                IQuestion q = g.fromJson(questionJson, IQuestion.class);
                questionList.add(q);
            }
        } catch (Exception e) {
            Log.d("quiz_package", "parsing failed, "+ e.getMessage());
            this.quizCode = 0;
            this.class_code = 0;
            this.questionList = new ArrayList<>();
        }
    }

    private CourseCategory getCourseCategoryByString(String category) {
        switch (category) {
            case "Math":
                return CourseCategory.Math;
            case "English":
                return CourseCategory.English;
            case "QuantumPhysic":
                return CourseCategory.QuantumPhysic;
            default:
                return CourseCategory.DontCare;
        }
    }

    public QuizPackage(int quizCode, int class_code) {
        this.quizCode = quizCode;
        this.class_code = class_code;
        this.moduleName = "";
        this.instructorUID = "";
        questionList = new ArrayList<>();
    }

    public QuizPackage(int class_code, CourseCategory courseCategory, String instructorUID, String moduleName, List<IQuestion> questions) {
        this.quizCode = 0;
        this.class_code = class_code;
        this.courseCategory = courseCategory;
        this.instructorUID = instructorUID;
        this.moduleName = moduleName;
        this.questionList = questions;
    }

    public void setQuizCode(int quizCode) {
        this.quizCode = quizCode;
    }

    public int getQuizCode() {
        return this.quizCode;
    }

    public List<IQuestion> getQuestionList() {
        return this.questionList;
    }

    public List<IQuestion> getQuestionsByCategory(CourseCategory category, int questionNumber) {
        Collections.shuffle(this.questionList, rng);
        List<IQuestion> questions = new ArrayList<>();
        for (IQuestion q : questionList) {
            if (q.getCategory().equals(category))
                questions.add(q);
            if (questions.size() >= questionNumber)
                break;
        }

        return questions;
    }

    public void addQuestion(IQuestion question) {
        questionList.add(question);
    }

    public void addMCQuestion(CourseCategory category, String question,
                              boolean hasPic, String picSrc,
                              List<ChoicePair> choices, int correctAnsNum) throws Exception {
        if (correctAnsNum < 0 || correctAnsNum >= choices.size())
            throw new IllegalArgumentException("correct answer does not match any of the answers");
        QuestionsMC q = new QuestionsMC(category, question, hasPic, picSrc, choices, correctAnsNum);
        questionList.add(q);
    }

    public void addMCQuestion(int id, CourseCategory category, String question,
                              boolean hasPic, String picSrc,
                              List<ChoicePair> choices, int correctAnsNum) throws Exception {
        if (correctAnsNum < 0 || correctAnsNum >= choices.size())
            throw new IllegalArgumentException("correct answer does not match any of the answers");
        QuestionsMC q = new QuestionsMC(id, category, question, hasPic, picSrc, choices, correctAnsNum);
        questionList.add(q);
    }

    public void addMCQuestion(String jsonString) {
        Gson gson = new Gson();
        QuestionsMC q = gson.fromJson(jsonString, QuestionsMC.class);
        questionList.add(q);
    }

    public String toJson() {
        Gson g = new Gson();
        return g.toJson(this);
    }

}
