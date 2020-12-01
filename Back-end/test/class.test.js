const express = require("express");
const MongoClient = require("mongodb").MongoClient;

const app = require("../app.js"); // link to server file
const supertest = require("supertest");
const request = supertest(app);

describe("class test", () => {
  
    beforeAll(async(done) => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var classDb = await client.db("data");

      await classDb.collection("classInfo").insertOne({"classCode" : 1, "uid" : "1", "category" : "Math", "className" : "testClass1", "instructorUID" : "1", "quizModules" : "{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}"});
  
      await classDb.collection("classInfo").insertOne({"classCode" : 2, "uid" : "2", "category" : "English", "className" : "testClass2", "instructorUID" : "2", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});
  
      await client.close();
      done();
    });
  
    afterAll(async(done) => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var db = await client.db("data");
      await db.dropCollection("classInfo");
      await db.close();
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
  
  beforeAll(async() => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var classDb = await client.db("data");

    await classDb.collection("classInfo").insertOne({"classCode" : 1, "uid" : "1", "category" : "Math", "className" : "testClass1", "instructorUID" : "1", "quizModules" : "{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}"});

    await classDb.collection("classInfo").insertOne({"classCode" : 2, "uid" : "2", "category" : "English", "className" : "testClass2", "instructorUID" : "2", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});

    await client.close();
  });

  afterAll(async() => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("data");
    await db.dropCollection("classInfo");
    await db.close();
    await client.close();
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

