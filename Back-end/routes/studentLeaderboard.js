let express = require("express");
let MongoClient = require("mongodb").MongoClient;
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let classesDb;

MongoClient.connect(
  "mongodb://localhost:27017",
  {useUnifiedTopology: true},
  (err, client) => {
    classesDb = client.db("classes");
  }
);

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

router.get("/", (req, res, next) => {
    let url = new URL(req.originalUrl, `http://${req.headers.host}`);
    let uid = url.searchParams.get("userId");
    let isInstructor = url.searchParams.get("isInstructor");
    let classCode = url.searchParams.get("classCode");
    let timeout = 2000;
    
    classesDb.collection("class" + classCode)
      .find({})
      .project({_id:0, username: 1, EXP: 1, uid: 1, score: 1})
      .sort({EXP: -1})
      .maxTimeMS(timeout)
      .toArray((err, data) => {
      if (err) {
        throw err;
      } else {
        if (isInstructor === "false") {
            data = refactorData(data, uid);
        }
        res.send(data);
      }
    });
});


module.exports = router;