let express = require("express");
let router = express.Router();
let fs = require("fs");
let MongoClient = require("mongodb").MongoClient;
let db;

MongoClient.connect(
  "mongodb://localhost:27017",
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
    if (req.body.data.id === null){
      db.collection("quizzes").insertOne(Object.assign({}, req.body.data, {uid: req.body.uid}, {quiz_code: db.collection("quizzes").count() }, {class_code: req.body.data.class_code}), (err, res) => {
        if (err) {
          console.log(err);
        }
      });      
    }
    else {
      db.collection("quizzes").insertOne(Object.assign({}, req.body.data, {uid: req.body.uid}, {quiz_code: req.body.data.id}, {class_code: req.body.data.class_code}), (err, res) => {
        if (err) {
          console.log(err);
        }
      });          
    }
  }

  res.statusCode = 200;
  res.end();
});

/* GET quiz listing. */
router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let class_code = url.searchParams.get("class_code");
  let quiz_code = url.searchParams.get("quiz_code");
  let timeout = 2000;
  
  db.collection("quizzes").find({ $and: [ {"class_code": class_code}, {"quiz_code": quiz_code} ] }).maxTimeMS(timeout).toArray((err, frequency) => {
    if (err) {
      throw err;
    } else {
      try {
        frequency = Object.values(frequency[0])[0];
      } catch (ex) {
        frequency = "";
      }
      res.send(frequency+"");
    }
  });
});

module.exports = router;
