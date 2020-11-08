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
  let class_code = Number(url.searchParams.get("class_code"));
  let quiz_code = Number(url.searchParams.get("quiz_code"));
  let timeout = 2000;
  
  db.collection("quizzes")
    .find({$and: 
           [{
             class_code
             :class_code}, 
            {
              quizCode
              :quiz_code}]})
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
