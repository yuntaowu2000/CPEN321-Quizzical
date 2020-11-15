quizModule = require('../routes/studentLeaderboard.js');
const {MongoClient} = require('mongodb');


describe("User Position function", () => {
  test("it should return position of matching uid user in input data array", () => {
    const input = [
      { uid: 1 },
      { uid: 2 },
      { uid: 3 }
    ];

    const output1 = [1, { uid: 1 }];
    const output2 = [2, { uid: 2 }];
    
    expect(quizModule.getUserPosition(input, 1)).toEqual(output1);
    expect(quizModule.getUserPosition(input, 2)).toEqual(output2);
    
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
