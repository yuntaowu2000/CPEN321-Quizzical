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
    });
  
    afterAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = await client.db("data");
        await db.dropCollection("userInfo");
        await client.close();
        done();
    });
  
    test("test create account and get user data (username, email, EXP, user quiz count)", async (done) => {
        let response = await request.post("/upload/user").send({ uid: "1", type: "userInfo", data: {username: "student1", email: "student1@ubc.ca", isInstructor: false, userQuizCount: 0, EXP: 0}});
        expect(response.status).toBe(200);

        response = await request.get("/users").query({userId: "1"});
        expect(response.text).toBe("{\"uid\":\"1\",\"EXP\":0,\"Email\":\"student1@ubc.ca\",\"isInstructor\":false,\"userQuizCount\":0,\"username\":\"student1\"}");
        expect(response.status).toBe(200);

        response = await request.get("/users").query({userId: "1", type: "userInfo"});
        expect(response.text).toBe("{\"uid\":\"1\",\"EXP\":0,\"Email\":\"student1@ubc.ca\",\"isInstructor\":false,\"userQuizCount\":0,\"username\":\"student1\"}");
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

        response = await request.get("/users/userQuizCount").query({type: "EXP", userId: "1"});
        expect(response.text).toBe("0");
        expect(response.status).toBe(200);

        done();
    });

    test("test invalid get request", async (done) => {
        let response = await request.get("/users").query({type: "user", userId: "1"});
        expect(response.text).toBe("request invalid");

        response = await request.get("/users/contact").query({type: "EXP", userId: "1"});
        expect(response.text).toBe("request invalid");

        response = await request.get("/users/classDetails").query({type: "username", userId: "1"});
        expect(response.text).toBe("request invalid");

        response = await request.get("/users/userQuizCount").query({type: "Email", userId: "1"});
        expect(response.text).toBe("request invalid");
        done();
    });

    test("test change username", async (done) => {
        let response = await request.post("/upload/user").send({ uid: "1", type: "username", data: "newUserName1"});
        expect(response.status).toBe(200);

        response = await request.get("/users/contact").query({type: "username", userId: "1"});
        expect(response.text).toBe("newUserName1");

        await request.post("/upload/user").send({ uid: "1", type: "username", data: "student1"});
        done();
    });

    test("test change email", async (done) => {
        let response = await request.post("/upload/user").send({ uid: "1", type: "Email", data: "cpen321@ece.ubc.ca"});
        expect(response.status).toBe(200);

        response = await request.get("/users/contact").query({type: "Email", userId: "1"});
        expect(response.text).toBe("cpen321@ece.ubc.ca");
        done();
    });

    test("test class list", async(done) => {
        let response = await request.post("/upload/class").send({ uid: "1", type: "classList", data: "{\"category\":\"Math\",\"classCode\":35608,\"className\":\"test2\",\"instructorUID\":\"2\"}"});
        expect(response.status).toBe(200);

        response = await request.get("/users/classList").query({userId: "1"});
        expect(response.text).toBe("{\"category\":\"Math\",\"classCode\":35608,\"className\":\"test2\",\"instructorUID\":\"2\"}");
        done();
    });
  
});
  

describe("test notification related post/get", () => {
    beforeAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = client.db("data");
        //insert dummy variable to implicitly create a database to avoid troubles caused by not properly drop the database
        await db.collection("notificationFrequency").insertOne({ "uid" : "0", "notificationFrequency" : 0, firebaseToken: "aaa"});
    });
  
    afterAll(async(done) => {
        var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
        var db = await client.db("data");
        await db.dropCollection("notificationFrequency");
        await client.close();
        done();
    });

    test("test notification frequency post and get", async(done) => {
        let response = await request.post("/upload/notifications").send({ uid: "1", type: "notificationFrequency", data: {notificationFrequency: 0, firebaseToken: "aaa"}});
        expect(response.status).toBe(200);

        response = await request.post("/upload/notifications").send({ uid: "1", type: "notificationFrequency", data: 0});
        expect(response.status).toBe(200);

        response = await request.post("/upload/notifications").send({ uid: "1", type: "notificationFrequency", data: {notificationFrequency: 0, firebaseToken: "aaa"}});
        expect(response.status).toBe(200);

        response = await request.get("/users/notifications").query({type: "notificationFrequency", userId: "1"});
        expect(response.text).toBe("0");
        done();
    });

    test("test notification frequency update", async(done) => {
        let response = await request.post("/upload/notifications").send({ uid: "1", type: "notificationFrequency", data: {notificationFrequency: 1, firebaseToken: "aaa"}});
        expect(response.status).toBe(200);

        response = await request.get("/users/notifications").query({type: "notificationFrequency", userId: "1"});
        expect(response.text).toBe("1");
        done();
    });

    test("test invalid notification frequency get request", async(done) => {
        let response = await request.post("/upload/notifications").send({ uid: "1", type: "notificationFrequency", data: {notificationFrequency: 1, firebaseToken: "aaa"}});
        expect(response.status).toBe(200);

        response = await request.get("/users/notifications").query({type: "username", userId: "1"});
        expect(response.text).toBe("request invalid");
        done();
    });
});