let express = require("express");
let MongoClient = require("mongodb").MongoClient;
let fs = require("fs");
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

/* GET users listing. */
router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  let className = url.searchParams.get("className");
  let classCode = Number(url.searchParams.get("classCode"));
  let timeout = 2000;

  if (type === null) {
    db.collection("classInfo").find({ classCode: { $eq: classCode }}).project({_id:0}).maxTimeMS(timeout).toArray((err, classInfo) => {
      if (err) {
        throw err;
      } else {
        res.send(classInfo);
      }
    });
  } 
  // Note: when below cases are uncommented, Codacy complains about complexity(too many paths through code),
  // maybe put in another router.get?
  /*
  else if (type === "classList") 
  {
  } 
  else if (type === "classStatistics") 
  {
  } 
  else if (type === "quizModules") 
  {
  }
  */
  
});

module.exports = router;
