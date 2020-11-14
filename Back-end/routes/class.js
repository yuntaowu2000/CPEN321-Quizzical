let express = require("express");
let MongoClient = require("mongodb").MongoClient;
let fs = require("fs");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
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
        if (quizModules == null) {
          res.send("");
        }
        quizModules = Object.values(quizModules[0]);
        res.send("" + quizModules);
      }
    });
  }
  else if (type === "classList") 
  {
    db.collection("classInfo").find({ classCode: { $eq: classCode }}).project({classList:1, _id:0}).maxTimeMS(timeout).toArray((err, classList) => {
      if (err) {
        throw err;
      } else {
        res.send(classList);
      }
    });
  } else {
    res.send("invalid request");
  } 
});

function handleDeleteClass(isInstructor, classCode, uid) {
  if (isInstructor === "true") {
    db.collection("classInfo").deleteOne(
      {$and: [{instructorUID: { $eq: uid }},{classCode: { $eq: classCode }}]},
      (err, db) => {
        if (err) {
          throw err;
        }
    });
    
    db.collection("quizzes").deleteMany(
      {$and: [{instructorUID: { $eq: uid }},{classCode: { $eq: classCode }}]},
      (err, db) => {
        if (err) {
          throw err;
        }
    });

    classDb.collection("class" + classCode).drop((err, delOK) => {
      if (err) {
        throw err;
      }
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
    db.collection(classDbName).updateMany({[quizScoreField]: {$exists: true}}, {$unset: {[quizScoreField]: ""}},
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
