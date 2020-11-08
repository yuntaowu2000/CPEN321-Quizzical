let express = require("express");
let MongoClient = require("mongodb").MongoClient;
let fs = require("fs");
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
  let uid = url.searchParams.get("user_id");
  let type = url.searchParams.get("type");
  let class_name = url.searchParams.get("class_name");
  let class_code = url.searchParams.get("class_code");
  let timeout = 2000;

  if (type === null) {
    db.collection("class info").find({ class_name: { $eq: class_name }}).project({class_code:1, _id:0}).maxTimeMS(timeout).toArray((err, classCode) => {
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
  else if (type === "class_list") 
  {
  } 
  else if (type === "class_statistics") 
  {
  } 
  else if (type === "quiz_modules") 
  {
  }
  */
  
});

module.exports = router;
