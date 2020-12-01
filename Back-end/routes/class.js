let express = require("express");
let MongoClient = require("mongodb").MongoClient;
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let util = require("util");
let firebaseFunction = require("./firebasePush");
let db;
let classDb;

MongoClient.connect(
  "mongodb://localhost:27017",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
    classDb = client.db("classes");
  }
);

/* GET users listing. */
router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  let className = url.searchParams.get("className");
  let classCode = Number(url.searchParams.get("classCode"));
  let timeout = 2000;

  if (type === null) {
    db.collection("classInfo").find({ classCode: { $eq: classCode }}).project({quizModules:0, _id:0}).maxTimeMS(timeout).toArray((err, classInfo) => {
      if (err) {
        throw err;
      } else {
        res.send(classInfo);
      }
    });
  } else if (type === "quizModules") {
    db.collection("classInfo").find({ classCode: { $eq: classCode }}).project({quizModules:1, _id:0}).maxTimeMS(timeout).toArray((err, quizModules) => {
      if (err) {
        throw err;
      } else {
        if (quizModules === null || quizModules.length === 0) {
          res.send("");
        } else {
          quizModules = Object.values(quizModules[0]);
          res.send("" + quizModules);
        }
      }
    });
  } else {
    res.send("invalid request");
  }
});

function sendClassDeletedNotification(classCode, uid) {
  let timeout = 2000;

  db.collection("classInfo").find({classCode: { $eq: classCode }}).project({className:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
    if (err) {
      throw err;
    } else {
      let className = Object.values(retval[0])[0] + "";
      let message =  util.format("Class %s has been deleted. If your class list has not been correctly populated, please restart the app or delete the class yourself.", className);

      //get all the students here and send the message to all students
      classDb.collection("class" + classCode).find({}).project({_id:0, uid: 1})
      .toArray((err, data) => {
        let userIds = [];
        for (var d of data) {
          userIds.push(Object.values(d)[0]);
        }
        firebaseFunction.sendMessage(userIds, message);
        //wait for everything is done, then delete. 
        db.collection("classInfo").deleteOne(
          {$and: [{instructorUID: { $eq: uid }},{classCode: { $eq: classCode }}]},
          (err, db) => {
            if (err) {
              throw err;
            }
          });
      });
    }
  });
}

function handleDeleteClass(isInstructor, classCode, uid) {
  if (isInstructor === "true") {
    sendClassDeletedNotification(classCode, uid);

    db.collection("quizzes").deleteMany(
      {$and: [{instructorUID: { $eq: uid }},{classCode: { $eq: classCode }}]},
      (err, db) => {
        if (err) {
          throw err;
        }
      });

    classDb.collection("class" + classCode).find().project({_id:0,uid:1}).toArray((err, docs) => {
      let uidList = [];
      for (var user of docs) {
        uidList.push(Object.values(user)[0]);
      }
      db.collection("userInfo").find({uid: {$in: uidList}}).project({_id:0, uid:1, classList:1}).toArray((err, result) => {
        if (result === null || result.length === 0) {
          return;
        }
        result.forEach((doc) => {
          let classList = [];
          let classListString = doc.classList;
          classList = classListString.split(";");
          classListString = "";
          for (var userClass of classList) {
            if (userClass.search("" + classCode) === -1) {
              classListString += userClass + ";";
            }
          }
          classListString = classListString.substring(0,classListString.length-1);
          db.collection("userInfo").updateOne({uid: {$eq: doc.uid}}, {$set: {classList: classListString}});
        });

      });

      //wait for everything is done, then delete. 
      classDb.collection("class" + classCode).drop((err, delOK) => {
        if (err) {
          throw err;
        }
      });
    });

  } else {
    //delete the student from the class.
    classDb.collection("class" + classCode).deleteOne({uid: {$eq: uid}}, (err, db) => {
      if (err) {
        throw err;
      }
    });
  }
}

router.delete("/delete", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  let classCode = Number(url.searchParams.get("classCode"));

  if (type === "deleteClass") {
    let isInstructor = url.searchParams.get("isInstructor");
    handleDeleteClass(isInstructor, classCode, uid);
  } else if (type === "deleteQuiz") {
    let quizCode = Number(url.searchParams.get("quizModules"));
    db.collection("quizzes").deleteOne(
      {$and: [{instructorUID: { $eq: uid }},{classCode: { $eq: classCode }}, {quizCode: { $eq: quizCode }}]},
      (err, db) => {
        if (err) {
          throw err;
        }
      });
    let classDbName = "class" + classCode;
    let quizScoreField = "quiz" + quizCode + "score";
    let quizWrongQuestionFieldName = "quiz" + quizCode + "wrongQuestionIds";
    db.collection(classDbName).updateMany({[quizScoreField]: {$exists: true}},
      {$unset: {[quizScoreField]: 1, [quizWrongQuestionFieldName]: ""}},
      (err, db) => {
        if (err) {
          throw err;
        }
      });
  }

  res.statusCode = 204;
  res.end();
});

module.exports = router;
