const express = require("express");
const quizModule = require("../routes/studentLeaderboard.js");
const {MongoClient} = require("mongodb");

const app = require("../routes/studentLeaderboard.js"); // link to server file
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

// test GET of "/"
it("fetchDataForTeachers case of router.get(\"/\") ", async (done) => {
  const response = await request.get("/").send({ classCode: "", type: "", userId: "", isInstructor: "", });
  expect(response.body.message).toBeUndefined();

  done();
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
