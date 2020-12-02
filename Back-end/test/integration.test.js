const MongoClient = require("mongodb").MongoClient;

const app = require("../app.js"); // link to server file
const supertest = require("supertest");
const request = supertest(app);

describe("test account related post/get requests", () => {
  
    beforeAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = client.db("data");
        //insert dummy variable to implicitly create a database to avoid troubles caused by not properly drop the database
        await db.collection("userInfo").insertOne({ "uid" : "0", "username" : "dummy"});
        done();
    });
  
    afterAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = await client.db("data");
        await db.dropCollection("userInfo");
        await client.close();
        done();
    });
  
    test("test create account and get user data (username, email, EXP, user quiz count)", async (done) => {
        let response = await request.post("/upload/user").send({ "uid": "1", "type": "userInfo", "data": "{\"username\": \"student1\", \"Email\": \"student1@ubc.ca\", \"isInstructor\": false, \"userQuizCount\": 0, \"EXP\": 0}"});
        expect(response.status).toBe(200);

        response = await request.get("/users").query({userId: "1"});
        expect(response.text).toBe("[{\"uid\":\"1\",\"EXP\":0,\"Email\":\"student1@ubc.ca\",\"isInstructor\":false,\"userQuizCount\":0,\"username\":\"student1\"}]");
        expect(response.status).toBe(200);

        response = await request.get("/users").query({userId: "1", type: "userInfo"});
        expect(response.text).toBe("[{\"uid\":\"1\",\"EXP\":0,\"Email\":\"student1@ubc.ca\",\"isInstructor\":false,\"userQuizCount\":0,\"username\":\"student1\"}]");
        expect(response.status).toBe(200);

        response = await request.get("/users/contact").query({type: "username", userId: "1"});
        expect(response.text).toBe("student1");
        expect(response.status).toBe(200);

        response = await request.get("/users/contact").query({type: "Email", userId: "1"});
        expect(response.text).toBe("student1@ubc.ca");
        expect(response.status).toBe(200);

        response = await request.get("/users/classDetails").query({type: "isInstructor", userId: "1"});
        expect(response.text).toBe("false");
        expect(response.status).toBe(200);

        response = await request.get("/users/classStats").query({type: "EXP", userId: "1"});
        expect(response.text).toBe("0");
        expect(response.status).toBe(200);

        response = await request.get("/users/classStats").query({type: "userQuizCount", userId: "1"});
        expect(response.text).toBe("0");
        expect(response.status).toBe(200);

        done();
    }, 50000);

    test("test invalid get request", async (done) => {
        let response = await request.get("/users").query({type: "user", userId: "1"});
        expect(response.text).toBe("request invalid");

        response = await request.get("/users/contact").query({type: "EXP", userId: "1"});
        expect(response.text).toBe("request invalid");

        response = await request.get("/users/classDetails").query({type: "username", userId: "1"});
        expect(response.text).toBe("request invalid");

        response = await request.get("/users/classStats").query({type: "Email", userId: "1"});
        expect(response.text).toBe("request invalid");
        done();
    }, 50000);

    test("test change username", async (done) => {
        let response = await request.post("/upload/user").send({ "uid": "1", "type": "username", "data": "newUserName1"});
        expect(response.status).toBe(200);

        response = await request.get("/users/contact").query({type: "username", userId: "1"});
        expect(response.text).toBe("newUserName1");

        await request.post("/upload/user").send({ "uid": "1", "type": "username", "data": "instructor1"});
        done();
    }, 50000);

    test("test change email", async (done) => {
        let response = await request.post("/upload/user").send({ "uid": "1", "type": "Email", "data": "cpen321@ece.ubc.ca"});
        expect(response.status).toBe(200);

        response = await request.get("/users/contact").query({"type": "Email", "userId": "1"});
        expect(response.text).toBe("cpen321@ece.ubc.ca");
        done();
    }, 60000);

    test("test class list", async(done) => {
        let response = await request.post("/upload/class").send({ "uid": "1", "type": "classList", "data": "{\"category\":\"Math\",\"classCode\":35608,\"className\":\"test2\",\"instructorUID\":\"2\"}"});
        expect(response.status).toBe(200);

        response = await request.get("/users/classList").query({userId: "1"});
        expect(response.text).toBe("{\"category\":\"Math\",\"classCode\":35608,\"className\":\"test2\",\"instructorUID\":\"2\"}");
        done();
    }, 70000);
  
});
  
describe("test notification related post/get", () => {
    beforeAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = client.db("data");
        //insert dummy variable to implicitly create a database to avoid troubles caused by not properly drop the database
        await db.collection("notificationFrequency").insertOne({ "uid" : "0", "notificationFrequency" : 0, firebaseToken: "aaa"});
        done();
    });
  
    afterAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = await client.db("data");
        await db.dropCollection("notificationFrequency");
        await client.close();
        done();
    });

    test("test notification frequency post and get", async(done) => {
        let response = await request.post("/upload/notifications").send({ "uid": "1", "type": "notificationFrequency", "data": "{\"notificationFrequency\":0,\"firebaseToken\":\"aaa\"}"});
        expect(response.status).toBe(200);

        response = await request.post("/upload/notifications").send({ "uid": "1", "type": "notificationFrequency", "data": "0"});
        expect(response.status).toBe(200);

        response = await request.post("/upload/notifications").send({ "uid": "1", "type": "notificationFrequency", "data": "{\"notificationFrequency\":0,\"firebaseToken\":\"aaa\"}"});
        expect(response.status).toBe(200);

        response = await request.get("/users/notifications").query({type: "notificationFrequency", userId: "1"});
        expect(response.text).toBe("0");
        done();
    }, 80000);

    test("test notification frequency update", async(done) => {
        let response = await request.post("/upload/notifications").send({ "uid": "1", "type": "notificationFrequency", "data": "{\"notificationFrequency\":1,\"firebaseToken\":\"aaa\"}"});
        expect(response.status).toBe(200);

        response = await request.get("/users/notifications").query({type: "notificationFrequency", userId: "1"});
        expect(response.text).toBe("1");
        done();
    }, 90000);

    test("test invalid notification frequency get request", async(done) => {
        let response = await request.post("/upload/notifications").send({ "uid": "1", "type": "notificationFrequency", "data": "{\"notificationFrequency\":1,\"firebaseToken\":\"aaa\"}"});
        expect(response.status).toBe(200);

        response = await request.get("/users/notifications").query({type: "username", userId: "1"});
        expect(response.text).toBe("request invalid");
        done();
    }, 100000);
});

describe("test create/join class, create quiz modules", () => {
    beforeAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var classDb = await client.db("classes");
        var db = await client.db("data");
        //insert dummy variable to implicitly create a database to avoid troubles caused by not properly drop the database
        await db.collection("userInfo").insertOne({ "uid" : "0", "username" : "dummy"});
        await db.collection("userInfo").insertOne({"uid":"1", "username": "instructor1", "Email": "yuntaowu2009@hotmail.com"});
        await db.collection("userInfo").insertOne({"uid":"2", "username":"student1", "Email": "test@ece.ubc.ca"})
        await db.collection("classInfo").insertOne({"classCode":0,"uid":"0","category":"Math","className":"testClass1","instructorUID":"1"});
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

    test("create class and a student join the class", async(done) => {
        //most functionalities has been tested in class.test.js
        //teacher creates a class
        let response = await request.post("/upload/class").send({"uid":"1","type":"createClass","data":"{\"category\":\"Math\",\"classCode\":1,\"className\":\"testClass1\",\"instructorUID\":\"1\"}"});
        expect(response.status).toBe(200);

        //student joins a class
        response = await request.post("/upload/class").send({"uid":"2","type":"joinClass","data":"1"});
        expect(response.status).toBe(200);

        response = await request.get("/classes").query({classCode: 1});
        expect(response.text).toBe("[{\"classCode\":1,\"uid\":\"1\",\"category\":\"Math\",\"className\":\"testClass1\",\"instructorUID\":\"1\"}]")
        expect(response.status).toBe(200);

        done();
    });

    test("joining a class that does not exist", async(done) => {
        let response = await request.post("/upload/class").send({"uid":"2","type":"joinClass","data":"2"});
        expect(response.status).toBe(200);

        response = await request.get("/classes").query({classCode: 2});
        expect(response.text).toBe("[]")
        expect(response.status).toBe(200);

        done();
    });
});

describe("test instructor leader board", () => {
    beforeAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db= await client.db("data");

        await db.collection("userInfo").insertOne({ "uid" : "1", "username" : "instructor1", "EXP" : 10, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "2", "username" : "instructor2", "EXP" : 9, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "3", "username" : "instructor3", "EXP" : 8, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "4", "username" : "instructor4", "EXP" : 7, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "5", "username" : "instructor5", "EXP" : 6, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "6", "username" : "instructor6", "EXP" : 5, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "7", "username" : "instructor7", "EXP" : 4, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "8", "username" : "instructor8", "EXP" : 3, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "9", "username" : "instructor9", "EXP" : 2, "isInstructor": true});

        await db.collection("userInfo").insertOne({ "uid" : "10", "username" : "instructor10", "EXP" : 1, "isInstructor": true});

        await db.collection("userInfo").insertOne({"uid": "11", "username": "student1", "EXP" : 100, "isInstructor": false})

        await db.collection("userInfo").insertOne({ "uid" : "14", "username" : "instructor11", "EXP" : 0, "isInstructor": true});
        done();
    });

    afterAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = await client.db("data");
        await db.dropCollection("userInfo");
        await client.close();
        done();
    });

    test("get leaderboard with teacher lowest (out of 10) ", async (done) => {
        let response = await request.get("/instructorLeaderboard").query({userId: "14"});
        expect(response.text).toBe("[{\"uid\":\"1\",\"username\":\"instructor1\",\"EXP\":10},{\"uid\":\"2\",\"username\":\"instructor2\",\"EXP\":9},{\"uid\":\"3\",\"username\":\"instructor3\",\"EXP\":8},{\"uid\":\"4\",\"username\":\"instructor4\",\"EXP\":7},{\"uid\":\"5\",\"username\":\"instructor5\",\"EXP\":6},{\"uid\":\"6\",\"username\":\"instructor6\",\"EXP\":5},{\"uid\":\"7\",\"username\":\"instructor7\",\"EXP\":4},{\"uid\":\"8\",\"username\":\"instructor8\",\"EXP\":3},{\"uid\":\"9\",\"username\":\"instructor9\",\"EXP\":2},{\"uid\":\"10\",\"username\":\"instructor10\",\"EXP\":1},11,{\"uid\":\"14\",\"username\":\"instructor11\",\"EXP\":0}]");
        expect(response.status).toBe(200);
        done();
    });

    test("get leaderboard with teacher somewhere in between ", async (done) => {
        let response = await request.get("/instructorLeaderboard").query({userId: "5"});
        expect(response.text).toBe("[{\"uid\":\"1\",\"username\":\"instructor1\",\"EXP\":10},{\"uid\":\"2\",\"username\":\"instructor2\",\"EXP\":9},{\"uid\":\"3\",\"username\":\"instructor3\",\"EXP\":8},{\"uid\":\"4\",\"username\":\"instructor4\",\"EXP\":7},{\"uid\":\"5\",\"username\":\"instructor5\",\"EXP\":6},{\"uid\":\"6\",\"username\":\"instructor6\",\"EXP\":5},{\"uid\":\"7\",\"username\":\"instructor7\",\"EXP\":4},{\"uid\":\"8\",\"username\":\"instructor8\",\"EXP\":3},{\"uid\":\"9\",\"username\":\"instructor9\",\"EXP\":2},{\"uid\":\"10\",\"username\":\"instructor10\",\"EXP\":1},5,{\"uid\":\"5\",\"username\":\"instructor5\",\"EXP\":6}]");
        expect(response.status).toBe(200);
        done();
      });

      test("get leaderboard with less than or equal to 10 teachers ", async (done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db= await client.db("data");

        await db.collection("userInfo").deleteOne({"uid" : "14"});

        let response = await request.get("/instructorLeaderboard").query({userId: "5"});
        expect(response.text).toBe("[{\"uid\":\"1\",\"username\":\"instructor1\",\"EXP\":10},{\"uid\":\"2\",\"username\":\"instructor2\",\"EXP\":9},{\"uid\":\"3\",\"username\":\"instructor3\",\"EXP\":8},{\"uid\":\"4\",\"username\":\"instructor4\",\"EXP\":7},{\"uid\":\"5\",\"username\":\"instructor5\",\"EXP\":6},{\"uid\":\"6\",\"username\":\"instructor6\",\"EXP\":5},{\"uid\":\"7\",\"username\":\"instructor7\",\"EXP\":4},{\"uid\":\"8\",\"username\":\"instructor8\",\"EXP\":3},{\"uid\":\"9\",\"username\":\"instructor9\",\"EXP\":2},{\"uid\":\"10\",\"username\":\"instructor10\",\"EXP\":1},5,{\"uid\":\"5\",\"username\":\"instructor5\",\"EXP\":6}]");
        expect(response.status).toBe(200);
        done();
    });

});

describe("some useless testing for index.js and app.js (just to increase coverage)", () => {

    test("get initial page", async(done) => {
        let response = await request.get("/");
        expect(response.status).toBe(200);
        done();
    });

    test("get error", async(done) => {
        let response = await request.get("/someUnkownEndpoint");
        expect(response.status).toBe(404);
        done();
    })
})
