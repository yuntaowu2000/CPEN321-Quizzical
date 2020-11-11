let express = require("express");
let MongoClient = require("mongodb").MongoClient;
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let db;

MongoClient.connect(
  "mongodb://localhost:27017",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
  }
);

router.get("/", (req, res, next) => {
    let url = new URL(req.originalUrl, `http://${req.headers.host}`);
    let instructorUID = url.searchParams("userId");
    let timeout = 2000;
    
    db.collection("quizzes")
      .find({isInstructor: true})
      .project({_id:0, username: 1, EXP: 1})
      .sort({EXP: -1})
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
