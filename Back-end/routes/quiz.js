let express = require("express");
let router = express.Router();
let fs = require("fs");
let MongoClient = require("mongodb").MongoClient;
let db;

MongoClient.connect(
  "mongodb://module6quizzical:uLBSjjivRdyY5sa3C1CQa6EsirCm2MNSQFa7oilEzad11rPNoO4zE8xKMEhibcvHiiw3xahYI4FiQs43U1WBgg==@module6quizzical.documents.azure.com:10250/mean?ssl=true",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
    db.createCollection("quizzes", (err, res) => {
      if (err) {
	console.log(err);
      }
    });
  }
);

router.use(express.json());

router.post("/", (req, res, next) => {
  console.log("UID: " + req.body.uid);
  console.log("Type: " + req.body.type);
  console.log("Data: %j", req.body);

  if (req.body.type === "quiz") {
      db.collection("quizzes").insertOne(Object.assign({}, req.body.data, {uid: req.body.uid}), (err, res) => {
	      if (err) {
	        console.log(err);
	      }
      });
  }

  res.statusCode = 200;
  res.end();
});

module.exports = router;
