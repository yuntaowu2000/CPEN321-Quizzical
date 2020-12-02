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

function calculateAverage(data, quizScoreField) {
  let totalScore = 0;
  for (var value of data) {
    totalScore += value[quizScoreField + ""];
  }
  return totalScore / data.length;
}

function findMaxScore(data, quizScoreField) {
  let maxScore = -1;
  for (var value of data) {
    if (value[quizScoreField + ""] > maxScore) {
      maxScore = value[quizScoreField + ""];
    }
  }
  return maxScore;
}

function fetchDataForTeachers(res, classCode, quizCode, type) {
  let classDbName = "class" + classCode;
  let quizScoreField = "quiz" + quizCode + "score";
  if (type === "score") {
    classesDb.collection(classDbName).find({[quizScoreField]: {$ne: null}})
    .project({_id:0, [quizScoreField]: 1, username: 1})
    .toArray((err, data) => {
      let resultArr = new Array();
      resultArr.push(data);
      resultArr.push(calculateAverage(data, quizScoreField));
      resultArr.push(findMaxScore(data, quizScoreField));
      res.send(resultArr);
    });
  } else {
    db.collection("quizzes")
    .find({$and: [{classCode}, {quizCode}]})
    .project({_id:0})
    .toArray((err, data) => {
      //just send all questions back
      if (data.length === 0 || data[0] === null) {
        res.send("");
      } else {
        res.send(data[0]["questionList"]);
      }
    });
  }
}

function fetchWrongQuestions(res, classCode, quizCode, wrongQuestionIds) {
  db.collection("quizzes")
  .find({$and: [{classCode}, {quizCode}]})
  .project({_id:0})
  .toArray((err, data) => {
    if (err || wrongQuestionIds[0] === null) {
      res.send("");
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

function findStudentScore(data, studentId, quizScoreField) {
  for (var value of data) {
    if (value["uid"] === studentId) {
      return value[quizScoreField + ""];
    }
  }
}

function fetchDataForStudents(res, studentUid, classCode, quizCode, type) {
  let classDbName = "class" + classCode;
  let quizScoreField = "quiz" + quizCode + "score";
  if (type === "score") {
    classesDb.collection(classDbName).find({[quizScoreField]: {$ne: null}})
    .project({_id:0, [quizScoreField]: 1, uid: 1})
    .toArray((err, data) => {
      if (err) {
        throw err;
      } else {
        let resultArr = new Array();
        resultArr.push(calculateAverage(data, quizScoreField));
        resultArr.push(findMaxScore(data, quizScoreField));
        resultArr.push(findStudentScore(data, studentUid, quizScoreField));
        res.send(resultArr);
      }
    });
  } else {
    classesDb.collection(classDbName).find({uid: {$eq: studentUid}})
    .project({_id:0, ["quiz" + quizCode + "wrongQuestionIds"]: 1})
    .toArray((err, data) => {
      if (data === null || data.length === 0) {
        res.send("");
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
      res.send(data);
    });
  }

});

router.get("/studentWrongCounts", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let classCode = Number(url.searchParams.get("classCode"));
  let quizCode = Number(url.searchParams.get("quizCode"));
  let classDbName = "class" + classCode;

  db.collection("quizzes")
    .find({$and: [{classCode}, {quizCode}]})
    .project({_id:0, questionList: 1})
    .toArray((err, data) => {
      if (data.length === 0) {
        res.send("");
      } else {
        var questionList = data[0]["questionList"];
        var len = questionList.length;

        classesDb.collection(classDbName).find({["quiz" + quizCode + "wrongQuestionIds"]: {$ne: null}})
          .project({_id:0, ["quiz" + quizCode + "wrongQuestionIds"]: 1})
          .toArray((err, wrongIds) => {
            let count = [];
            for (var i = 0; i < len; i++) {
              count.push(0);
            }
            for (var d of wrongIds) {
              let val = Object.values(d)[0];
              let currIds = val.substring(1, val.length - 1).split(",");
              for (var id of currIds) {
                count[Number(id) - 1] += 1;
              }
            }
            res.send(count);
          });
      }
  });

});

module.exports = router;

module.exports.calculateAverage = calculateAverage;
module.exports.findMaxScore = findMaxScore;
module.exports.fetchDataForTeachers = fetchDataForTeachers;
module.exports.fetchWrongQuestions = fetchWrongQuestions;
module.exports.findStudentScore = findStudentScore;
module.exports.fetchDataForStudents = fetchDataForStudents;
