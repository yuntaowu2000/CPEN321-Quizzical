let express = require("express");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let fs = require("fs");
let MongoClient = require("mongodb").MongoClient;
let db;
let classesDb;

MongoClient.connect(
  "mongodb://localhost:27017",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
    classesDb = client.db("classes");
  }
);

function fetchDataForTeachers(res, classCode, quizCode, type) {

}

function fetchDataForStudents(res, studentUid, classCode, quizCode, type) {
 let classDbName = "class" + classCode;
 let fieldName = "quiz" + quizCode + type;
 classesDb.collection(classDbName).find({uid: {$eq: studentUid}})
 .project({_id:0, [fieldName]: 1})
 .toArray((err, data) => {
  if (err) {
    throw err;
  } else {
    res.send(data);
  }
 });
}

/* GET quiz listing. */
router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let classCode = Number(url.searchParams.get("classCode"));
  let quizCode = Number(url.searchParams.get("quizCode"));
  let type = url.searchParams.get("type");
  let uid = url.searchParams.get("userId");
  let isInstructor = url.searchParams.get("isInstructor");
  let timeout = 2000;

  if (isInstructor === "true") {
    fetchDataForTeachers(res, classCode, quizCode, type);
  } else if (isInstructor === "false") {
    fetchDataForStudents(res, uid, classCode, quizCode, type);
  } else {
    db.collection("quizzes")
      .find({$and: [{classCode}, {quizCode}]})
      .project({_id:0, liked:0})
      .maxTimeMS(timeout)
      .toArray((err, data) => {
      if (err) {
        throw err;
      } else {
        res.send(data);
      }
    });
  }
  
});

module.exports = router;
