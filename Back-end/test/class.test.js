const express = require("express");
const MongoClient = require("mongodb").MongoClient;

const app = require("../app.js"); // link to server file
const supertest = require("supertest");
const request = supertest(app);

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
  
    // test GET of "/" and "/studentWrongCounts"
    test("get general class info", async (done) => {
        let response = await request.get("/classes").query({classCode: "2"});
        expect(response.status).toBe(200);
        done();
    });
  
    test("get general class info 2", async (done) => {
        let response = await request.get("/classes").query({classCode: "1"});
        expect(response.status).toBe(200);
        done();
    });

    test("get general class info with class undefined", async (done) => {
        let response = await request.get("/classes").query({classCode: "11111"});
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

  // test GET of "/" and "/studentWrongCounts"
  test("get class 2 quiz module", async (done) => {
      let response = await request.get("/classes").query({classCode: "2", type: "quizModules"});
      expect(response.status).toBe(200);
      done();
  });

  test("get class 1 quiz module", async (done) => {
      let response = await request.get("/classes").query({classCode: "1", type: "quizModules"});
      expect(response.status).toBe(200);
      done();
  });

  test("get class quiz module info with class undefined", async (done) => {
      let response = await request.get("/classes").query({classCode: "11111", type: "quizModules"});
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

    await db.collection("classInfo").insertOne({"classCode" : 2, "uid" : "2", "category" : "English", "className" : "testClass2", "instructorUID" : "2", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});

    await classDb.collection("class1").insertOne({ "uid" : "3", "username" : "student1", "userQuizCount" : 0, "score" : 0, "EXP" : 0});

    await db.collection("quizzes").insertOne({"classCode":1,"moduleName":"module2","uid":"1","courseCategory":"Math","instructorUID":"1","questionList":[{"HasPic":false,"category":"Math","choices":[{"isPic":false,"str":"5"},{"isPic":false,"str":"6"}],"correctAnsNum":1,"index":1,"picSrc":"","question":"2+3=?","questionType":"MC"}],"quizCode":1});

    await client.close();
    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");
    var classDb = await client.db("classes");
    await db.dropCollection("classInfo");
    await classDb.dropCollection("class1");
    await client.close();
    done();
  });

  // test GET of "/" and "/studentWrongCounts"
  test("student delete a class", async (done) => {
    let response = await request.delete("/classes/delete").query({classCode: "1", type: "deleteClass", uid:"3", isInstructor: "false"});
    expect(response.status).toBe(204);
    done();
  });

  test("teacher delete a class", async (done) => {
    let response = await request.delete("/classes/delete").query({classCode: "1", type: "deleteClass", uid:"1", isInstructor: "true"});
    expect(response.status).toBe(204);
    done();
  });

  test("teacher delete a quiz", async (done) => {
    let response = await request.delete("/classes/delete").query({classCode: "1", type: "deleteQuiz", uid:"1", quizModules: "1"});
    expect(response.status).toBe(204);
    done();
  });

});

