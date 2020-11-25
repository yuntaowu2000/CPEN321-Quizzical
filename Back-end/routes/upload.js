let express = require("express");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let fs = require("fs");
let path = require("path");
let util = require("util");
let firebaseFunctions = require("./firebasePush");
let emailFunctions = require("./emailSending");
let MongoClient = require("mongodb").MongoClient;
let db;
let classesDb;

MongoClient.connect(
    "mongodb://localhost:27017",
    {useUnifiedTopology: true},
    (err, client) => {
      db = client.db("data");
      classesDb = client.db("classes");
      db.createCollection("classInfo", (err, res) => {
        if (err) {
          //console.error(err);
        }
      });
      db.createCollection("userInfo", (err, res) => {
        if (err) {
          //console.error(err);
        }
      });
      db.createCollection("notificationFrequency", (err, res) => {
        if (err) {
          //console.error(err);
        }
      });
      db.createCollection("quizzes", (err, res) => {
        if (err) {
          //console.log(err);
        }
      });
    }
);

function sendQuizModulePushNotification(classCode) {
  let timeout = 2000;

  db.collection("classInfo").find({classCode: { $eq: classCode }}).project({className:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
    if (err) {
      throw err;
    } else {
      let className = Object.values(retval[0])[0] + "";
      let message =  util.format("Quiz modules in %s has been updated.", className);

      //get all the students here and send the message to all students
      classesDb.collection("class" + classCode).find({}).project({_id:0, uid: 1})
      .toArray((err, data) => {
        let userIds = [];
        for (var d of data) {
          userIds.push(Object.values(d)[0]);
        }
        firebaseFunctions.sendMessage(userIds, message);
      });
    }
  });
}

function parseCreateClassEmailContent(username, className, classCode) {
  return util.format("<p>Congratulations, %s!</p><p>You have just created a new Class: %s</p><p>Your class code is: %d.</p><p>share it with the students and begin your quizzes!</p>", username, className, classCode);
}

function sendCreateClassEmail(uid, className, classCode) {
  let timeout = 2000;
  db.collection("userInfo").find({ uid: { $eq: uid }}).project({username:1, Email:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
    if (err) {
      throw err;
    } else {
      let username = Object.values(retval[0])[1];
      let email = Object.values(retval[0])[0];
      let parsedContent = parseCreateClassEmailContent(username, className, classCode);
      emailFunctions.sendEmail(email, "Quizzical: New class created", parsedContent);
    }
  });
}

router.use(express.json());

router.post("/profileImg", (req, res, next) => {
  if (req.body.type === "profileImage")
  {
    const folderPath = "images/" + req.body.uid; // needed to pass eslint
    /* eslint no-detect-non-literal-fs-filename: "error" */
    if (!fs.existsSync(folderPath))
    {
      fs.mkdirSync(folderPath, {recursive:true});
    }
    const filename = folderPath + "/profile_img.jpg";
    fs.writeFileSync(filename, req.body.data, {encoding: "base64"});
  }

  res.statusCode = 200;
  res.end();
});

router.post("/user", (req, res, next) => {
  if (req.body.type === "userInfo") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }

  else if (req.body.type === "Email") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {Email: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }

  else if (req.body.type === "username") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {username: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }
  res.statusCode = 200;
  res.end();
});

router.post("/notifications", (req, res, next) => {
  if (req.body.type === "notificationFrequency") {
    try {
      db.collection("notificationFrequency").updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
        if (err) {
          // console.error(err);
        }
      });
    } catch (ex) {
      db.collection("notificationFrequency").updateOne({uid: req.body.uid}, {$set: req.body}, {upsert: true}, (err, res) => {
        if (err) {
          // console.error(err);
        }
      });
    }
  }

  res.statusCode = 200;
  res.end();
});

function createClassFunction(reqData, userId) {
  let data = JSON.parse(reqData);
  db.collection("classInfo")
    .updateOne(
    {$and: [{uid: userId},{classCode: data.classCode}]},
    {$set: Object.assign({}, data, {uid: userId})},
    {upsert: true},
    (err, res) => {
    if (err) {
      // console.error(err);
    }
  });
  classesDb.createCollection("class" + data.classCode, (err, res) => {
    if (err) {
      //console.error(err);
    }
  });
  sendCreateClassEmail(userId, data.className, data.classCode);
}

function joinClassFunction(classCode, studentuid) {
  db.collection("userInfo").find({uid: studentuid})
  .project({_id:0, username: 1})
  .toArray((err, data) => {
    let studentUsername = Object.values(data[0])[0];
    classesDb.collection("class" + classCode)
    .insertOne({uid: studentuid, username: studentUsername, userQuizCount: 0, score: 0, EXP: 0},
      (err, res) => {
        if (err) {
          throw err;
        }
      });

  });
}

router.post("/class", (req, res, next) => {
  if (req.body.type === "createClass") {
    createClassFunction(req.body.data, req.body.uid);
  } else if (req.body.type === "classList") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {"classList": req.body.data}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  } else if (req.body.type === "joinClass") {
    joinClassFunction(req.body.data, req.body.uid);
  }

  res.statusCode = 200;
  res.end();
});

router.post("/instructorStats", (req, res, next) => {
  if (req.body.type === "EXP") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {EXP: Number(req.body.data), uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  } else if (req.body.type === "userQuizCount") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {userQuizCount: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }

  res.statusCode = 200;
  res.end();
});

function updateStudentStats(student, studentQuizResult, studentUid) {
  let prevScore = student.score;
  let prevUserQuizCount = student.userQuizCount;
  let prevEXP = student.EXP;

  let currScore = studentQuizResult.score;

  let newUserQuizCount = prevUserQuizCount + 1;

  let newScore = (prevScore * prevUserQuizCount + currScore) / newUserQuizCount;
  let additionalEXP = Math.round((10 + 3 / (1 + Math.exp(50 -currScore)) + 1 / (1 + Math.exp(67 - currScore)) + 1 / (1 + Math.exp(90 - currScore))));
  let newEXP = prevEXP + additionalEXP;

  let quizCode = studentQuizResult.quizCode;
  let quizScoreFieldName = "quiz" + quizCode + "score";
  let quizWrongQuestionFieldName = "quiz" + quizCode + "wrongQuestionIds";
  let wrongQuestionIds = studentQuizResult.wrongQuestionIds;

  classesDb.collection("class" + studentQuizResult.classCode).updateOne({uid: studentUid},
    {$set: {EXP: newEXP}},
    {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });

  //update the score only if it does not exist
  classesDb.collection("class" + studentQuizResult.classCode).updateOne(
    {$and: [{uid: { $eq: studentUid }},{[quizScoreFieldName]: {$exists: false}}]},
    {$set: {userQuizCount: newUserQuizCount, score: newScore, [quizScoreFieldName]: currScore, [quizWrongQuestionFieldName]: wrongQuestionIds}}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
}

router.post("/studentStats", (req, res, next) => {
  let studentQuizResult = JSON.parse(req.body.data);
  let classCode = studentQuizResult.classCode;

  db.collection("userInfo").updateOne({uid: req.body.uid},
    {$set: {userQuizCount: studentQuizResult.userQuizCount, EXP: studentQuizResult.EXP, uid: req.body.uid}},
    {upsert: true}, (err, res) => {
    if (err) {
      // console.error(err);
    }
  });

  classesDb.collection("class" + classCode).find({uid: req.body.uid}).toArray((err, students) => {
    updateStudentStats(students[0], studentQuizResult, req.body.uid);
  });


  res.statusCode = 200;
  res.end();
});

/*eslint complexity: ["error", 10]*/
router.post("/quiz", (req, res, next) => {

  if (req.body.type === "createQuiz") {
    let quizData = JSON.parse(req.body.data);
    db.collection("quizzes").updateOne(
      {$and: [{uid: req.body.uid},{moduleName: quizData.moduleName},{classCode: quizData.classCode}]},
      {$set: Object.assign({}, quizData, {uid: req.body.uid}, {classCode: quizData.classCode}, {moduleName: quizData.moduleName})},
      {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
    let classDbName = "class" + quizData.classCode;
    let quizScoreField = "quiz" + quizData.quizCode + "score";
    let quizWrongQuestionFieldName = "quiz" + quizData.quizCode + "wrongQuestionIds";
    db.collection(classDbName).updateMany({[quizScoreField]: {$exists: true}},
      {$unset: {[quizScoreField]: 1, [quizWrongQuestionFieldName]: ""}},
    (err, db) => {
      if (err) {
        throw err;
      }
    });
  } else if (req.body.type === "quizModules") {
    db.collection("classInfo").updateOne({classCode: Number(req.body.uid)}, {$set: {"quizModules":req.body.data}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
    sendQuizModulePushNotification(Number(req.body.uid));
  }

  res.statusCode = 200;
  res.end();
});

router.post("/like", (req, res, next) => {
  if (req.body.type === "like") {
    let likeDetails = JSON.parse(req.body.data);
    let instructorUID = likeDetails.instructorUID;
    let classCode = Number(likeDetails.classCode);
    let quizCode = Number(likeDetails.quizCode);
    let likePersonUid = req.body.uid;

    db.collection("quizzes").find({$and: [{classCode: { $eq: classCode}}, {quizCode: {$eq: quizCode}}]}).project({_id:0,liked:1}).toArray((err, arr) => {
      if (err) {
        throw err;
      }
      var likedPersons = Object.values(arr[0])[0];
      if (!Array.isArray(likedPersons)) {
        likedPersons = [];
      }

      if (!likedPersons.includes(likePersonUid)) {
        likedPersons.push(likePersonUid);
        db.collection("quizzes").updateOne({$and: [{classCode: { $eq: classCode}}, {quizCode: {$eq: quizCode}}]},
          {$set: {liked: likedPersons} },
          (err, res) => {
            if (err) {
              throw err;
            }
          });
        db.collection("userInfo").updateOne(
          {uid: {$eq: instructorUID}},
          {$inc: {EXP: 5}},
          {upsert: true}, (err, res) => {
            if (err) {
              throw err;
            }
        });
        let userIds = [instructorUID];
        firebaseFunctions.sendMessage(userIds, "Someone liked your quiz and you earned 5 EXP!");
      }
    });
  }

  res.statusCode = 200;
  res.end();
});

module.exports = router;

module.exports.sendQuizModulePushNotification = sendQuizModulePushNotification;
module.exports.parseCreateClassEmailContent = parseCreateClassEmailContent;
module.exports.sendCreateClassEmail = sendCreateClassEmail;
module.exports.createClassFunction = createClassFunction;
module.exports.joinClassFunction = joinClassFunction;
module.exports.updateStudentStats = updateStudentStats;
