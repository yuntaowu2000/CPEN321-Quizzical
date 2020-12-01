const express = require("express");
const quizModule = require("../routes/studentLeaderboard.js");
const MongoClient = require("mongodb").MongoClient;

const app = require("../routes/studentLeaderboard.js"); // link to server file
const server = express();
server.use("/", app);
server.listen(3002);
const supertest = require("supertest");
const request = supertest(server);

// const { setupDB } = require("../test-setup.js");

// Setup a Test Database
// setupDB("classes");

// test GET of "/"
describe("test student leaderboard", () => {
  
  beforeAll(async() => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var classDb = await client.db("classes");

    await classDb.createCollection("class1", (err, res) => {
      if (err) {throw err;}
    });

    await classDb.collection("class1").insertOne({ "uid" : "1", "username" : "student1", "EXP" : 10, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "2", "username" : "student2", "EXP" : 9, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "3", "username" : "student3", "EXP" : 8, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "4", "username" : "student4", "EXP" : 7, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "5", "username" : "student5", "EXP" : 6, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "6", "username" : "student6", "EXP" : 5, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "7", "username" : "student7", "EXP" : 4, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "8", "username" : "student8", "EXP" : 3, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "9", "username" : "student9", "EXP" : 2, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "10", "username" : "student10", "EXP" : 1, "score" : 100});

    await classDb.collection("class1").insertOne({ "uid" : "11", "username" : "student11", "EXP" : 0, "score" : 100});
  });

  afterAll(async() => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("classes");
    await db.collection("class1").drop();
    await client.close();
  });

  // test GET of "/"
  test("get leaderboard with student highest ", async (done) => {
    let response = await request.get("/").send({ classCode: "1", userId: "1", isInstructor: "false"});
    expect(response.body.message).toBe("[{\"uid\":\"1\",\"username\":\"student1\",\"score\":100,\"EXP\":10},{\"uid\":\"2\",\"username\":\"student2\",\"score\":100,\"EXP\":9},{\"uid\":\"3\",\"username\":\"student3\",\"score\":100,\"EXP\":8},{\"uid\":\"4\",\"username\":\"student4\",\"score\":100,\"EXP\":7},{\"uid\":\"5\",\"username\":\"student5\",\"score\":100,\"EXP\":6},{\"uid\":\"6\",\"username\":\"student6\",\"score\":100,\"EXP\":5},{\"uid\":\"7\",\"username\":\"student7\",\"score\":100,\"EXP\":4},{\"uid\":\"8\",\"username\":\"student8\",\"score\":100,\"EXP\":3},{\"uid\":\"9\",\"username\":\"student9\",\"score\":100,\"EXP\":2},{\"uid\":\"10\",\"username\":\"student10\",\"score\":100,\"EXP\":1},1,{\"uid\":\"1\",\"username\":\"student1\",\"score\":100,\"EXP\":10}]");
    done();
  });

  test("get leaderboard with student lowest (out of 10) ", async (done) => {
    let response = await request.get("/").send({ classCode: "1", userId: "11", isInstructor: "false"});
    expect(response.body.message).toBe("[{\"uid\":\"1\",\"username\":\"student1\",\"score\":100,\"EXP\":10},{\"uid\":\"2\",\"username\":\"student2\",\"score\":100,\"EXP\":9},{\"uid\":\"3\",\"username\":\"student3\",\"score\":100,\"EXP\":8},{\"uid\":\"4\",\"username\":\"student4\",\"score\":100,\"EXP\":7},{\"uid\":\"5\",\"username\":\"student5\",\"score\":100,\"EXP\":6},{\"uid\":\"6\",\"username\":\"student6\",\"score\":100,\"EXP\":5},{\"uid\":\"7\",\"username\":\"student7\",\"score\":100,\"EXP\":4},{\"uid\":\"8\",\"username\":\"student8\",\"score\":100,\"EXP\":3},{\"uid\":\"9\",\"username\":\"student9\",\"score\":100,\"EXP\":2},{\"uid\":\"10\",\"username\":\"student10\",\"score\":100,\"EXP\":1},11,{\"uid\":\"11\",\"username\":\"student11\",\"score\":100,\"EXP\":0}]");
    done();
  });

  test("get leaderboard with student somewhere in between ", async (done) => {
    let response = await request.get("/").send({ classCode: "1", userId: "5", isInstructor: "false"});
    expect(response.body.message).toBe("[{\"uid\":\"1\",\"username\":\"student1\",\"score\":100,\"EXP\":10},{\"uid\":\"2\",\"username\":\"student2\",\"score\":100,\"EXP\":9},{\"uid\":\"3\",\"username\":\"student3\",\"score\":100,\"EXP\":8},{\"uid\":\"4\",\"username\":\"student4\",\"score\":100,\"EXP\":7},{\"uid\":\"5\",\"username\":\"student5\",\"score\":100,\"EXP\":6},{\"uid\":\"6\",\"username\":\"student6\",\"score\":100,\"EXP\":5},{\"uid\":\"7\",\"username\":\"student7\",\"score\":100,\"EXP\":4},{\"uid\":\"8\",\"username\":\"student8\",\"score\":100,\"EXP\":3},{\"uid\":\"9\",\"username\":\"student9\",\"score\":100,\"EXP\":2},{\"uid\":\"10\",\"username\":\"student10\",\"score\":100,\"EXP\":1},5,{\"uid\":\"5\",\"username\":\"student5\",\"score\":100,\"EXP\":6}]");
    done();
  });

});


// non-endpoint tests
describe("User Position function", () => {
  test("it should return position of matching uid user in input data array", () => {
    const input = [
      { uid: 1 },
      { uid: 2 },
      { uid: 3 }
    ];

    const output1 = [1, { uid: 1 }];
    const output2 = [2, { uid: 2 }];
    const output3 = [3, { uid: 3 }];

    expect(quizModule.getUserPosition(input, 1)).toEqual(output1);
    expect(quizModule.getUserPosition(input, 2)).toEqual(output2);
    expect(quizModule.getUserPosition(input, 3)).toEqual(output3);

  });
});

describe("Refactor Data function", () => {
  test("it should return an array with up to the first 10 elements of the input array, with the output of getUserPosition appended", () => {
    const input = [
      { uid: 1 },
      { uid: 2 },
      { uid: 3 },
      { uid: 4 },
      { uid: 5 },
      { uid: 6 },
      { uid: 7 },
      { uid: 8 },
      { uid: 9 },
      { uid: 10 },
      { uid: 11 },
    ];

    const output1 = [ { uid: 1 }, { uid: 2 }, { uid: 3 }, { uid: 4 }, { uid: 5 }, { uid: 6}, { uid: 7 }, { uid: 8 }, { uid: 9 }, { uid: 10 }, 1, { uid: 1 }];
    const output2 = [ { uid: 1 }, { uid: 2 }, { uid: 3 }, { uid: 4 }, { uid: 5 }, { uid: 6}, { uid: 7 }, { uid: 8 }, { uid: 9 }, { uid: 10 }, 2, { uid: 2 }];
    const output3 = [ { uid: 1 }, { uid: 2 }, { uid: 3 }, { uid: 4 }, { uid: 5 }, { uid: 6}, { uid: 7 }, { uid: 8 }, { uid: 9 }, { uid: 10 }, 3, { uid: 3 }];

    expect(quizModule.refactorData(input, 1)).toEqual(output1);
    expect(quizModule.refactorData(input, 2)).toEqual(output2);
    expect(quizModule.refactorData(input, 3)).toEqual(output3);

  });
});

/*
function getUserPosition(data, uid) {
    var userRank = 1;
    var userData;
    for (var user of data) {
        if (Object.values(user)[0] === uid) {
            userData = user;
            break;
        }
        userRank += 1;
    }
    return [userRank, userData];
}

function refactorData(data, uid) {
    var newData = data;
    var userValues = getUserPosition(data, uid);
    if (data.length > 10) {
        newData = data.slice(0, 10);
    }
    newData.push(userValues[0]);
    newData.push(userValues[1]);
    return newData;
}
*/
