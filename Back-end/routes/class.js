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
  let classCode = url.searchParams.get("classCode");
  let timeout = 2000;

  if (type === null) {
    db.collection("classInfo").find({ className: { $eq: className }}).project({classCode:1, _id:0}).maxTimeMS(timeout).toArray((err, classCode) => {
      if (err) {
        throw err;
      } else {
        classCode = Object.values(classCode[0])[0];
        res.send(""+classCode);
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
