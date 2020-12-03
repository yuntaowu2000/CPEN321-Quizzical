const MongoClient = require("mongodb").MongoClient;

const express = require("express");
const server = express();
const app = require("../routes/class.js"); // link to server file
server.use("/classes", app);
const supertest = require("supertest");
const request = supertest(server);
jest.mock("../routes/firebasePush.js");

describe("class test", () => {

    beforeAll(async(done) => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var db = await client.db("data");

      await db.collection("classInfo").insertOne({"classCode" : 1, "uid" : "1", "category" : "Math", "className" : "testClass1", "instructorUID" : "1", "quizModules" : "{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}"});

      await db.collection("classInfo").insertOne({"classCode" : 2, "uid" : "2", "category" : "English", "className" : "testClass2", "instructorUID" : "2", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});

      await client.close();
      done();
    });

    afterAll(async(done) => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var db = await client.db("data");
      await db.dropCollection("classInfo");
      await client.close();
      done();
    });

    test("get general class info", async (done) => {
        let response = await request.get("/classes").query({classCode: "2"});
        expect(response.status).toBe(200);
        expect(response.text).toBe("[{\"classCode\":2,\"uid\":\"2\",\"category\":\"English\",\"className\":\"testClass2\",\"instructorUID\":\"2\"}]");
        done();
    });

    test("get general class info 2", async (done) => {
        let response = await request.get("/classes").query({classCode: "1"});
        expect(response.status).toBe(200);
        expect(response.text).toBe("[{\"classCode\":1,\"uid\":\"1\",\"category\":\"Math\",\"className\":\"testClass1\",\"instructorUID\":\"1\"}]");
        done();
    });

    test("get general class info with class undefined", async (done) => {
        let response = await request.get("/classes").query({classCode: "11111"});
        expect(response.text).toBe("[]");
        expect(response.status).toBe(200);
        done();
    });
});

describe("class quiz module test", () => {

  beforeAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");

    await db.collection("classInfo").insertOne({"classCode" : 1, "uid" : "1", "category" : "Math", "className" : "testClass1", "instructorUID" : "1", "quizModules" : "{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}"});

    await db.collection("classInfo").insertOne({"classCode" : 2, "uid" : "2", "category" : "English", "className" : "testClass2", "instructorUID" : "2", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});

    await client.close();
    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");
    await db.dropCollection("classInfo");
    await client.close();
    done();
  });

  test("get class 2 quiz module", async (done) => {
      let response = await request.get("/classes").query({classCode: "2", type: "quizModules"});
      expect(response.text).toBe("{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}");
      expect(response.status).toBe(200);
      done();
  });

  test("get class 1 quiz module", async (done) => {
      let response = await request.get("/classes").query({classCode: "1", type: "quizModules"});
      expect(response.text).toBe("{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}");
      expect(response.status).toBe(200);
      done();
  });

  test("get class quiz module info with class undefined", async (done) => {
      let response = await request.get("/classes").query({classCode: "11111", type: "quizModules"});
      expect(response.text).toBe("");
      expect(response.status).toBe(200);
      done();
  });

});

describe("delete test", () => {
  beforeAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");
    var classDb = await client.db("classes");

    await db.collection("classInfo").insertOne({"classCode" : 1, "uid" : "1", "category" : "Math", "className" : "testClass1", "instructorUID" : "1", "quizModules" : "{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}"});

    await db.collection("classInfo").insertOne({"classCode" : 2, "uid" : "1", "category" : "English", "className" : "testClass2", "instructorUID" : "1", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});

    await db.collection("classInfo").insertOne({"classCode" : 3, "uid" : "1", "category" : "English", "className" : "testClass2", "instructorUID" : "1", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});

    await db.collection("userInfo").insertOne({"uid": "3", "username" : "student1", "isInstructor": false, "classList": "{\"category\":\"Math\",\"classCode\":1,\"className\":\"testClass1\",\"instructorUID\":\"1\"}"});

    await db.collection("userInfo").insertOne({"uid": "4", "username" : "student2", "Email": "example@ubc.ca", "isInstructor": false, "classList": "{\"category\":\"Math\",\"classCode\":1,\"className\":\"testClass1\",\"instructorUID\":\"1\"}"});

    await db.collection("notificationFrequency").insertOne({"uid": "3", "notificationFrequency": 0, "firebaseToken": "aaa"});
    await db.collection("notificationFrequency").insertOne({"uid": "4", "notificationFrequency": 0, "firebaseToken": "aaa"});

    await classDb.collection("class1").insertOne({ "uid" : "3", "username" : "student1", "userQuizCount" : 0, "score" : 0, "EXP" : 0});

    await classDb.collection("class1").insertOne({ "uid" : "4", "username" : "student2", "userQuizCount" : 0, "score" : 0, "EXP" : 0});

    await classDb.collection("class2").insertOne({ "uid" : "3", "username" : "student1", "userQuizCount" : 0, "score" : 0, "EXP" : 0});

    await classDb.collection("class3").insertOne({ "uid" : "4", "username" : "student2", "userQuizCount" : 0, "score" : 0, "EXP" : 0});

    await db.collection("quizzes").insertOne({"classCode":2,"moduleName":"module2","uid":"1","courseCategory":"Math","instructorUID":"1","questionList":[{"HasPic":false,"category":"Math","choices":[{"isPic":false,"str":"5"},{"isPic":false,"str":"6"}],"correctAnsNum":1,"index":1,"picSrc":"","question":"2+3=?","questionType":"MC"}],"quizCode":1});

    await client.close();
    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");
    var classDb = await client.db("classes");
    await db.dropCollection("classInfo");
    await db.dropCollection("userInfo");
    await db.dropCollection("quizzes");
    await classDb.dropCollection("class1");
    await classDb.dropCollection("class2");
    await client.close();
    done();
  });

  test("student delete a class", async (done) => {
    let response = await request.delete("/classes/delete").query({classCode: "1", type: "deleteClass", uid:"3", isInstructor: "false"});
    expect(response.status).toBe(204);
    done();
  });

  test("teacher delete a class", async (done) => {
    let response = await request.delete("/classes/delete").query({classCode: "3", type: "deleteClass", uid:"1", isInstructor: "true"});
    expect(response.status).toBe(204);
    done();
  });

  test("teacher delete a quiz", async (done) => {
    let response = await request.delete("/classes/delete").query({classCode: "2", type: "deleteQuiz", uid:"1", quizModules: "1"});
    expect(response.status).toBe(204);
    done();
  });

});

describe("test invalid gets", () => {
  test("invalid get", async(done) => {
    let response = await request.get("/classes").query({type: "sometype"});
    expect(response.text).toBe("invalid request");
    done();
  });
});

