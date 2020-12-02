const studentLeaderboardModule = require("../routes/studentLeaderboard.js");
const MongoClient = require("mongodb").MongoClient;

const app = require("../app.js"); // link to server file
const supertest = require("supertest");
const request = supertest(app);

// const { setupDB } = require("../test-setup.js");

// Setup a Test Database
// setupDB("classes");

describe("test student leaderboard", () => {
  
  beforeAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var classDb = await client.db("classes");

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
    done();
  });

  afterAll(async(done) => {
    var client = await MongoClient.connect("mongodb://localhost:27017",  {useNewUrlParser: true, useUnifiedTopology: true});
    var db = await client.db("classes");
    await db.dropCollection("class1");
    await client.close();
    done();
  });

  test("get leaderboard with student highest ", async (done) => {
    let response = await request.get("/studentLeaderboard").query({ classCode: "1", userId: "1", isInstructor: "false"});
    expect(response.text).toBe("[{\"uid\":\"1\",\"username\":\"student1\",\"EXP\":10,\"score\":100},{\"uid\":\"2\",\"username\":\"student2\",\"EXP\":9,\"score\":100},{\"uid\":\"3\",\"username\":\"student3\",\"EXP\":8,\"score\":100},{\"uid\":\"4\",\"username\":\"student4\",\"EXP\":7,\"score\":100},{\"uid\":\"5\",\"username\":\"student5\",\"EXP\":6,\"score\":100},{\"uid\":\"6\",\"username\":\"student6\",\"EXP\":5,\"score\":100},{\"uid\":\"7\",\"username\":\"student7\",\"EXP\":4,\"score\":100},{\"uid\":\"8\",\"username\":\"student8\",\"EXP\":3,\"score\":100},{\"uid\":\"9\",\"username\":\"student9\",\"EXP\":2,\"score\":100},{\"uid\":\"10\",\"username\":\"student10\",\"EXP\":1,\"score\":100},1,{\"uid\":\"1\",\"username\":\"student1\",\"EXP\":10,\"score\":100}]");
    expect(response.status).toBe(200);
    done();
  });

  test("get leaderboard with student lowest (out of 10) ", async (done) => {
    let response = await request.get("/studentLeaderboard").query({ classCode: "1", userId: "11", isInstructor: "false"});
    expect(response.text).toBe("[{\"uid\":\"1\",\"username\":\"student1\",\"EXP\":10,\"score\":100},{\"uid\":\"2\",\"username\":\"student2\",\"EXP\":9,\"score\":100},{\"uid\":\"3\",\"username\":\"student3\",\"EXP\":8,\"score\":100},{\"uid\":\"4\",\"username\":\"student4\",\"EXP\":7,\"score\":100},{\"uid\":\"5\",\"username\":\"student5\",\"EXP\":6,\"score\":100},{\"uid\":\"6\",\"username\":\"student6\",\"EXP\":5,\"score\":100},{\"uid\":\"7\",\"username\":\"student7\",\"EXP\":4,\"score\":100},{\"uid\":\"8\",\"username\":\"student8\",\"EXP\":3,\"score\":100},{\"uid\":\"9\",\"username\":\"student9\",\"EXP\":2,\"score\":100},{\"uid\":\"10\",\"username\":\"student10\",\"EXP\":1,\"score\":100},11,{\"uid\":\"11\",\"username\":\"student11\",\"EXP\":0,\"score\":100}]");
    expect(response.status).toBe(200);
    done();
  });

  test("get leaderboard with student somewhere in between ", async (done) => {
    let response = await request.get("/studentLeaderboard").query({ classCode: "1", userId: "5", isInstructor: "false"});
    expect(response.text).toBe("[{\"uid\":\"1\",\"username\":\"student1\",\"EXP\":10,\"score\":100},{\"uid\":\"2\",\"username\":\"student2\",\"EXP\":9,\"score\":100},{\"uid\":\"3\",\"username\":\"student3\",\"EXP\":8,\"score\":100},{\"uid\":\"4\",\"username\":\"student4\",\"EXP\":7,\"score\":100},{\"uid\":\"5\",\"username\":\"student5\",\"EXP\":6,\"score\":100},{\"uid\":\"6\",\"username\":\"student6\",\"EXP\":5,\"score\":100},{\"uid\":\"7\",\"username\":\"student7\",\"EXP\":4,\"score\":100},{\"uid\":\"8\",\"username\":\"student8\",\"EXP\":3,\"score\":100},{\"uid\":\"9\",\"username\":\"student9\",\"EXP\":2,\"score\":100},{\"uid\":\"10\",\"username\":\"student10\",\"EXP\":1,\"score\":100},5,{\"uid\":\"5\",\"username\":\"student5\",\"EXP\":6,\"score\":100}]");
    expect(response.status).toBe(200);
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

    expect(studentLeaderboardModule.getUserPosition(input, 1)).toEqual(output1);
    expect(studentLeaderboardModule.getUserPosition(input, 2)).toEqual(output2);
    expect(studentLeaderboardModule.getUserPosition(input, 3)).toEqual(output3);

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

    expect(studentLeaderboardModule.refactorData(input, 1)).toEqual(output1);
    expect(studentLeaderboardModule.refactorData(input, 2)).toEqual(output2);
    expect(studentLeaderboardModule.refactorData(input, 3)).toEqual(output3);

  });
});
