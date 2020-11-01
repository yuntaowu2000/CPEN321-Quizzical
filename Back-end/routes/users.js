let express = require("express");
let MongoClient = require("mongodb").MongoClient;
let fs = require("fs");
let router = express.Router();
let db;

MongoClient.connect(
  "mongodb://module6quizzical:uLBSjjivRdyY5sa3C1CQa6EsirCm2MNSQFa7oilEzad11rPNoO4zE8xKMEhibcvHiiw3xahYI4FiQs43U1WBgg==@module6quizzical.documents.azure.com:10250/mean?ssl=true",
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
  
  if (type === "Profile_Image") {
    let filepath = "images/" + uid + + "/profile_img.jpg";
    let bitmap = fs.readFileSync(filepath);
    let string = Buffer(bitmap).toString("base64");
    res.send(string);
  } else if (type === "user_info") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({_id:0}).toArray((err,data) => {
      if (err) {
	throw err;
      }
    });
  }
  else if (type === "username") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({username:1, _id:0}).toArray((err, username) => {
      if (err) {
	throw err;
      } else {
	res.send(username);
      }
    });
  }
  else if (type === "Email") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({Email:1, _id:0}).toArray((err, email) => {
      if (err) {
	throw err;
      } else {
	res.send(email);
      }
    });
  }
  else if (type === "IS_INSTRUCTOR") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({IS_INSTRUCTOR:1, _id:0}).toArray((err, isInstructor) => {
      if (err) {
	throw err;
      } else {
	res.send(isInstructor);
      }
    });
  }
  else if (type === "user_quiz_count") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({user_quiz_count:1, _id:0}).toArray((err, quizCount) => {
      if (err) {
	throw err;
      } else {
	res.send(quizCount);
      }
    });
  }
  else if (type === "EXP") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({EXP:1, _id:0}).toArray((err, exp) => {
      if (err) {
	throw err;
      } else {
	res.send(exp);
      }
    });
  }
  else if (type === "class_code") {
    db.collection("class info").find({ uid: { $eq: uid }}).project({class_code:1, _id:0}).toArray((err, classCode) => {
      if (err) {
	throw err;
      } else {
	res.send(classCode);
      }
    });
  }
  else if (type === "notification_frequency") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({notification_frequency:1, _id:0}).toArray((err, frequency) => {
      if (err) {
	throw err;
      } else {
	res.send(frequency);
      }
    });
  }
});

module.exports = router;
