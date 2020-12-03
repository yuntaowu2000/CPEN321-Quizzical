const quizModule = require("../routes/quiz.js");
//import * as quizModule from "../routes/quiz.js";
const MongoClient = require("mongodb").MongoClient;

const app = require("../app.js"); // link to server file
const supertest = require("supertest");
const request = supertest(app);
//const { setupDB } = require("../test-setup.js");

describe("fetchDataForTeachers", () => {
  
  beforeAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var classDb = await client.db("classes");

    await classDb.collection("class1").insertOne({ "uid" : "1", "username" : "student1", "userQuizCount" : 1, "score" : 100, "EXP" : 72, "quiz0score" : 100, "quiz0wrongQuestionIds" : null});

    await classDb.collection("class1").insertOne({ "uid" : "2", "username" : "student2", "userQuizCount" : 1, "score" : 75, "EXP" : 72, "quiz0score" : 75, "quiz0wrongQuestionIds" : "[2]"});

    await classDb.collection("class1").insertOne({ "uid" : "3", "username" : "student3", "userQuizCount" : 2, "score" : 75, "EXP" : 72, "quiz0score" : 80, "quiz0wrongQuestionIds" : "[1]", 
    "quiz1score" : 70, "quiz1wrongQuestionIds" : "[1,2]"});

    await classDb.collection("class2").insertOne({ "uid" : "1", "username" : "student1", "userQuizCount" : 1, "score" : 100, "EXP" : 72, "quiz0score" : 100, "quiz0wrongQuestionIds" : null});
    await client.close();
    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("classes");
    await db.dropCollection("class1");
    await db.dropCollection("class2");
    await client.close();
    done();
  });

  test("fetchDataForTeachers case of router.get(\"/\") with one student class ", async (done) => {
    let response = await request.get("/quiz").query({classCode: "2", quizCode: "0", type: "score", userId: "4", isInstructor: "true"});
    expect(response.status).toBe(200);
    expect(response.text).toBe("[[{\"username\":\"student1\",\"quiz0score\":100}],100,100]");
    done();
  });

  test("fetchDataForTeachers case of router.get(\"/\") with class undefined ", async (done) => {
    let response = await request.get("/quiz").query({classCode: "3", quizCode: "0", type: "score", userId: "4", isInstructor: "true"});
    expect(response.text).toBe("[[],null,-1]");
    expect(response.status).toBe(200);
    done();
  });

  test("fetchDataForTeachers case of router.get(\"/\") with a class with more than 1 student", async (done) => {
    let response = await request.get("/quiz").query({classCode: "1", quizCode: "0", type: "score", userId: "4", isInstructor: "true"});
    expect(response.text).toBe("[[{\"username\":\"student1\",\"quiz0score\":100},{\"username\":\"student2\",\"quiz0score\":75},{\"username\":\"student3\",\"quiz0score\":80}],85,100]");
    expect(response.status).toBe(200);
    done();
  });

  test("fetchDataForTeachers case of students wrong counts", async (done) => {
    let response = await request.get("/quiz/studentWrongCounts").query({classCode: "0", quizCode: "0"});
    expect(response.text).toBe("");
    expect(response.status).toBe(200);
    done();
  });
});

describe("fetchDataForStudents", () => {
  
  beforeAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var classDb = await client.db("classes");
    
    await classDb.collection("class1").insertOne({ "uid" : "1", "username" : "student1", "userQuizCount" : 1, "score" : 100, "EXP" : 72, "quiz0score" : 100, "quiz0wrongQuestionIds" : null});

    await classDb.collection("class1").insertOne({ "uid" : "2", "username" : "student2", "userQuizCount" : 1, "score" : 75, "EXP" : 72, "quiz0score" : 75, "quiz0wrongQuestionIds" : "[2]"});

    await classDb.collection("class1").insertOne({ "uid" : "3", "username" : "student3", "userQuizCount" : 2, "score" : 75, "EXP" : 72, "quiz0score" : 80, "quiz0wrongQuestionIds" : "[1]", 
    "quiz1score" : 70, "quiz1wrongQuestionIds" : "[1,2]"});
    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("classes");
    await db.collection("class1").drop();
    await client.close();
    done();
  });

  // test GET of "/"
  test("fetchDataForStudents case of router.get(\"/\") with student highest ", async (done) => {
    let response = await request.get("/quiz").query({classCode: 1, quizCode:0, type:"score", userId: 1, isInstructor:false});
    expect(response.text).toBe("[85,100,100]");
    expect(response.status).toBe(200);
    done();
  });

  test("fetchDataForStudents case of router.get(\"/\") with some other student ", async (done) => {
    let response = await request.get("/quiz").query({classCode: 1, quizCode:0, type:"score", userId: 2, isInstructor:false});
    expect(response.text).toBe("[85,100,75]");
    expect(response.status).toBe(200);
    done();
  });

  test("fetchDataForStudents case of router.get(\"/\") with class undefined ", async (done) => {
    let response = await request.get("/quiz").query({classCode: 3, quizCode:0, type:"score", userId: 1, isInstructor:false});
    expect(response.text).toBe("[null,-1,null]");
    expect(response.status).toBe(200);
    done();
  });
});

describe("fetch quiz", () => {
  beforeAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");

    await db.collection("quizzes").insertOne({"classCode":1,"moduleName":"module2","uid":"1","courseCategory":"Math","instructorUID":"1","questionList":[{"HasPic":false,"category":"Math","choices":[{"isPic":false,"str":"5"},{"isPic":false,"str":"6"}],"correctAnsNum":1,"index":1,"picSrc":"","question":"2+3=?","questionType":"MC"}],"quizCode":1});

    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");
    await db.dropCollection("quizzes");
    await client.close();
    done();
  });

  test("fetch an existing quiz", async(done) => {
    let response = await request.get("/quiz").query({classCode: 1, quizCode:1});
    expect(response.text).toBe("[{\"classCode\":1,\"moduleName\":\"module2\",\"uid\":\"1\",\"courseCategory\":\"Math\",\"instructorUID\":\"1\",\"questionList\":[{\"HasPic\":false,\"category\":\"Math\",\"choices\":[{\"isPic\":false,\"str\":\"5\"},{\"isPic\":false,\"str\":\"6\"}],\"correctAnsNum\":1,\"index\":1,\"picSrc\":\"\",\"question\":\"2+3=?\",\"questionType\":\"MC\"}],\"quizCode\":1}]");
    expect(response.status).toBe(200);
    done();
  });

  test("fetch an non-existing quiz (quiz module does not exist)", async(done) => {
    let response = await request.get("/quiz").query({classCode: 1, quizCode:0});
    expect(response.text).toBe("[]");
    expect(response.status).toBe(200);
    done();
  });

  test("fetch an non-existing quiz (class not exist)", async(done) => {
    let response = await request.get("/quiz").query({classCode: 2, quizCode:0});
    expect(response.text).toBe("[]");
    expect(response.status).toBe(200);
    done();
  });
});

describe("quiz integration test", () => {
  beforeAll(async(done) => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var classDb = await client.db("classes");
      var db = await client.db("data");
      //insert dummy variable to implicitly create a database to avoid troubles caused by not properly drop the database
      await db.collection("userInfo").insertOne({ "uid" : "0", "username" : "dummy"});
      await db.collection("userInfo").insertOne({"uid":"1", "username": "instructor1", "Email": "yuntaowu2009@hotmail.com", "isInstructor": true, "userQuizCount": "0", "EXP": 0});
      await db.collection("userInfo").insertOne({"uid":"2", "username":"student1", "Email": "test@ece.ubc.ca", "isInstructor": false, "userQuizCount": "0", "EXP": 0});
      await db.collection("classInfo").insertOne({"classCode":1,"uid":"1","category":"Math","className":"testClass1","instructorUID":"1"});
      await classDb.collection("class1").insertOne({ "uid" : "2", "username" : "student1", "userQuizCount" : 0, "score" : 0, "EXP" : 0});

      await classDb.collection("class1").insertOne({ "uid" : "3", "username" : "student2", "userQuizCount" : 1, "score" : 0, "EXP" : 10, "quiz1score" : 0, "quiz1wrongQuestionIds" : "[1]"});

      await db.collection("quizzes").insertOne({"classCode" : 1, "moduleName" : "module1", "uid" : "1", "courseCategory" : "Math", "instructorUID" : "1", "questionList" : [ { "HasPic" : false, "category" : "Math", "choices" : [ { "isPic" : false, "str" : "2" }, { "isPic" : false, "str" : "3" } ], "correctAnsNum" : 1, "index" : 1, "picSrc" : "", "question" : "1+1=?", "questionType" : "MC" } ], "quizCode" : 1 });
      
      done();
  });

  afterAll(async(done) => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var classDb = await client.db("classes");
      var db = await client.db("data");
      await db.dropDatabase();
      await classDb.dropDatabase();
      await client.close();
      done();
  });

  test("get wrong questions", async(done) => {
      let response = await request.get("/quiz/studentWrongCounts").query({classCode:1, quizCode:1});
      expect(response.text).toBe("[1]");
      expect(response.status).toBe(200);

      //get wrong question list for teacher
      response = await request.get("/quiz").query({classCode:1, quizCode:1, isInstructor: true, userId: "1"});
      expect(response.text).toBe("[{\"HasPic\":false,\"category\":\"Math\",\"choices\":[{\"isPic\":false,\"str\":\"2\"},{\"isPic\":false,\"str\":\"3\"}],\"correctAnsNum\":1,\"index\":1,\"picSrc\":\"\",\"question\":\"1+1=?\",\"questionType\":\"MC\"}]");
      expect(response.status).toBe(200);

      //get wrong question list for student
      response = await request.get("/quiz").query({classCode:1, quizCode:1, isInstructor: false, userId: "3"});
      expect(response.text).toBe("[{\"HasPic\":false,\"category\":\"Math\",\"choices\":[{\"isPic\":false,\"str\":\"2\"},{\"isPic\":false,\"str\":\"3\"}],\"correctAnsNum\":1,\"index\":1,\"picSrc\":\"\",\"question\":\"1+1=?\",\"questionType\":\"MC\"}]");
      expect(response.status).toBe(200);

      done();
  });
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

