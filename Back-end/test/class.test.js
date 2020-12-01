const express = require("express");
const MongoClient = require("mongodb").MongoClient;

const app = require("../app.js"); // link to server file
const server = express();
server.use("/", app);
server.listen(3003);
const supertest = require("supertest");
const request = supertest(server);

describe("class test", () => {
  
    beforeAll(async() => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var classDb = await client.db("data");
  
      await classDb.createCollection("classInfo", (err, res) => {
        if (err) {throw err;}
      });

      await classDb.collection("classInfo").insertOne({"classCode" : 1, "uid" : "1", "category" : "Math", "className" : "testClass1", "instructorUID" : "1", "quizModules" : "{\"category\":\"Math\",\"classCode\":1,\"id\":0,\"moduleName\":\"module1\"}"});
  
      await classDb.collection("classInfo").insertOne({"classCode" : 2, "uid" : "2", "category" : "English", "className" : "testClass2", "instructorUID" : "2", "quizModules" : "{\"category\":\"English\",\"classCode\":2,\"id\":0,\"moduleName\":\"module1\"}"});
  
      await client.close();
    });
  
    afterAll(async() => {
      var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
      var db = await client.db("data");
      await db.collection("classInfo").drop();
      await client.close();
    });
  
    // test GET of "/" and "/studentWrongCounts"
    test("get general class info", async (done) => {
        let response = await request.get("/classes").query({classCode: "2"});
        expect(response.body.message).toBe("[{\"classCode\":2,\"uid\":\"2\",\"category\":\"English\",\"className\":\"testClass2\",\"instructorUID\":\"2\"}]");
        done();
    });
  
    test("get general class info 2", async (done) => {
        let response = await request.get("/classes").query({classCode: "1"});
        expect(response.body.message).toBe("[{\"classCode\":1,\"uid\":\"1\",\"category\":\"Math\",\"className\":\"testClass1\",\"instructorUID\":\"1\"}]");
        done();
    });

    test("get general class info with class undefined", async (done) => {
        let response = await request.get("/classes").query({classCode: "11111"});
        expect(response.body.message).toBe("[]");
        done();
    });
  
});