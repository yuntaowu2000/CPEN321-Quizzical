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
    let filepath = "/home/site/wwwroot/images/" + uid + + "/profile_img.jpg";
    let string = '';
    if (fs.existsSync(filepath)) {
      let bitmap = fs.readFileSync(filepath);
      string = Buffer(bitmap).toString("base64");
    }
    res.send(string);
  }
  else if (type === null) {
    let timeout = 2000
    db.collection("user info").find({ uid: { $eq: uid }}).project({Profile_Image:0, _id:0}).maxTimeMS(timeout).toArray((err,data) => {
      if (err) {
	throw err;
      } else {
	res.send(data);
      }
    });
  }
  else if (type === "user_info") {
    let timeout = 2000
    db.collection("user info").find({ uid: { $eq: uid }}).project({_id:0}).maxTimeMS(timeout).toArray((err,data) => {
      if (err) {
	throw err;
      } else {
	res.send(data);
      }
    });
  }
  else if (type === "username") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({username:1, _id:0}).maxTimeMS(timeout).toArray((err, username) => {
      if (err) {
	throw err;
      } else {
	username = Object.values(username[0])
	res.send(username);
      }
    });
  }
  else if (type === "Email") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({Email:1, _id:0}).maxTimeMS(timeout).toArray((err, email) => {
      if (err) {
	throw err;
      } else {
	email = Object.values(email[0])
	res.send(email);
      }
    });
  }
  else if (type === "IS_INSTRUCTOR") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({IS_INSTRUCTOR:1, _id:0}).maxTimeMS(timeout).toArray((err, isInstructor) => {
      if (err) {
	throw err;
      } else {
	isInstructor = Object.values(isInstructor[0])
	res.send(isInstructor);
      }
    });
  }
  else if (type === "user_quiz_count") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({user_quiz_count:1, _id:0}).maxTimeMS(timeout).toArray((err, quizCount) => {
      if (err) {
	throw err;
      } else {
	quizCount = Object.values(quizCount[0])
	res.send(quizCount);
      }
    });
  }
  else if (type === "EXP") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({EXP:1, _id:0}).maxTimeMS(timeout).toArray((err, exp) => {
      if (err) {
	throw err;
      } else {
	exp = Object.values(exp)[0]
	res.send(exp);
      }
    });
  }
  else if (type === "class_code") {
    db.collection("class info").find({ uid: { $eq: uid }}).project({class_code:1, _id:0}).maxTimeMS(timeout).toArray((err, classCode) => {
      if (err) {
	throw err;
      } else {
	classCode = Object.values(classCode)[0]
	res.send(classCode);
      }
    });
  }
  else if (type === "notification_frequency") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({notification_frequency:1, _id:0}).maxTimeMS(timeout).toArray((err, frequency) => {
      if (err) {
	throw err;
      } else {
	frequency = Object.values(frequency)[0]
	res.send(frequency);
      }
    });
  }
});

module.exports = router;
