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

function fetchWrongQuestions(res, classCode, quizCode, wrongQuestionIds) {
  db.collection("quizzes")
  .find({$and: [{classCode}, {quizCode}]})
  .project({_id:0})
  .toArray((err, data) => {
    if (err) {
      throw err;
    } else {
      // not sending the correct values
      let questions = new Array();
      let questionList = data[0]["questionList"];
      let ids = wrongQuestionIds[0].substring(1, wrongQuestionIds[0].length - 1).split(",");
      for (var questionId of ids) {
        questions.push(questionList[Number(questionId) - 1]);
      }
      res.send(questions);
    }
  });
}

function fetchDataForStudents(res, studentUid, classCode, quizCode, type) {
  let classDbName = "class" + classCode;
  if (type === "score") {
    classesDb.collection(classDbName).find({uid: {$eq: studentUid}})
    .project({_id:0, ["quiz" + quizCode + "score"]: 1})
    .toArray((err, data) => {
      if (err) {
        throw err;
      } else {
        res.send(data);
      }
    });
  } else {
    classesDb.collection(classDbName).find({uid: {$eq: studentUid}})
    .project({_id:0, ["quiz" + quizCode + "wrongQuestionIds"]: 1})
    .toArray((err, data) => {
      if (err) {
        throw err;
      } else {
        fetchWrongQuestions(res, classCode, quizCode, Object.values(data[0]));
      }
    });
  }
}

router.use(express.json());
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
