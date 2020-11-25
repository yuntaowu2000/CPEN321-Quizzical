const express = require("express");
const quizModule = require("../routes/quiz.js");
//import * as quizModule from "../routes/quiz.js";
const {MongoClient} = require("mongodb");

const app = require("../routes/quiz.js"); // link to server file
const server = express();
server.use("/", app);
server.listen(3001);
const supertest = require("supertest");
const request = supertest(server);

const { setupDB } = require("../test-setup.js");

// Setup a Test Database
setupDB("classes");


// add sample data to test database
let res;
request.post("/")
	.send({
      classCode: "",
      quizCode: ""
      // etc
    }).then((output) => {
      res = output;
    });

// test GET of "/" and "/studentWrongCounts"
it("fetchDataForTeachers case of router.get(\"/\") ", async (done) => {
  let response = await request.get("/").send({ classCode: "", quizCode: "", type: "", userId: "", isInstructor: "", });
  expect(response.body.message).toBeUndefined();

  response = await request.get("/studentWrongCounts").send({ classCode: "", quizCode: "", });
  expect(response.body.message).toBe("");

  done();
});




// non-endpoint tests
describe("Calculate Average function", () => {
  test("it should calculate the average score of quizScoreField values from the input data array", () => {
    const input = [
      { score1: 1, score2: 20 },
      { score1: 2, score2: 1  },
      { score1: 3, score2: 6  }
    ];

    const output1 = 2;
    const output2 = 9;

    expect(quizModule.calculateAverage(input, "score1")).toEqual(output1);
    expect(quizModule.calculateAverage(input, "score2")).toEqual(output2);

  });
});

describe("Find Max Score function", () => {
  test("it should find the highest quizScoreField value in the input data array", () => {
    const input = [
      { score1: 1, score2: 20 },
      { score1: 2, score2: 1  },
      { score1: 3, score2: 7  }
    ];

    const output1 = 3;
    const output2 = 20;

    expect(quizModule.findMaxScore(input, "score1")).toEqual(output1);
    expect(quizModule.findMaxScore(input, "score2")).toEqual(output2);

  });
});


describe("Find Student Score function", () => {
  test("it should check through the input data array for an element with matching studentID and return its quizScoreField value", () => {
    const input = [
      { uid: 1, Math: 100, Chemistry: 47, English: 77 },
      { uid: 2, Math: 87, Chemistry: 53, English: 84 },
      { uid: 3, Math: 93, Chemistry: 60, English: 90 }
    ];

    expect(quizModule.findStudentScore(input, 1, "Math")).toEqual(100);
    expect(quizModule.findStudentScore(input, 1, "Chemistry")).toEqual(47);
    expect(quizModule.findStudentScore(input, 1, "English")).toEqual(77);

    expect(quizModule.findStudentScore(input, 2, "Math")).toEqual(87);
    expect(quizModule.findStudentScore(input, 2, "Chemistry")).toEqual(53);
    expect(quizModule.findStudentScore(input, 2, "English")).toEqual(84);

    expect(quizModule.findStudentScore(input, 3, "Math")).toEqual(93);
    expect(quizModule.findStudentScore(input, 3, "Chemistry")).toEqual(60);
    expect(quizModule.findStudentScore(input, 3, "English")).toEqual(90);
  });
});


// left: fetchDataForTeachers; fetchWrongQuestions; findStudentScore; fetchDataForStudents;
