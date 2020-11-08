let express = require("express");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let fs = require("fs");
let MongoClient = require("mongodb").MongoClient;
let db;

MongoClient.connect(
  "mongodb://localhost:27017",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
  }
);

/* GET quiz listing. */
router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let classCode = Number(url.searchParams.get("classCode"));
  let quizCode = Number(url.searchParams.get("quizCode"));
  let timeout = 2000;
  
  db.collection("quizzes")
    .find({$and: 
           [
             {
             classCode
             :classCode}, 
            {
              quizCode
              :quizCode}]})
    .project({_id:0})
    .maxTimeMS(timeout)
    .toArray((err, data) => {
    if (err) {
      throw err;
    } else {
      res.send(data);
    }
  });
});

module.exports = router;
