let express = require("express");
let fs = require("fs");
let path = require("path");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let MongoClient = require("mongodb").MongoClient;
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
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");

  let timeout = 2000;

  if (type === null) {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({profileImage:0, _id:0}).maxTimeMS(timeout).toArray((err,data) => {
      if (err) {
        throw err;
      } else {
        res.send(data);
      }
    });
  }
  else if (type === "userInfo") {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({_id:0}).maxTimeMS(timeout).toArray((err,data) => {
      if (err) {
        throw err;
      } else {
        res.send(data);
      }
    });
  }
  else {
    res.send("request invalid");
  }
});

router.get("/contact", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");

  let timeout = 2000;

  if (type === "username") {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({username:1, _id:0}).maxTimeMS(timeout).toArray((err, username) => {
      if (err) {
        throw err;
      } else {
        username = Object.values(username[0])[0];
        res.send(""+username);
      }
    });
  }
  else if (type === "Email") {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({Email:1, _id:0}).maxTimeMS(timeout).toArray((err, email) => {
      if (err) {
        throw err;
      } else {
        email = Object.values(email[0])[0];
        res.send(""+email);
      }
    });
  }
  else {
    res.send("request invalid");
  }
});

router.get("/classDetails", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");

  let timeout = 2000;

  if (type === "isInstructor") {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({isInstructor:1, _id:0}).maxTimeMS(timeout).toArray((err, isInstructor) => {
      if (err) {
        throw err;
      } else {
        isInstructor = Object.values(isInstructor[0])[0];
        res.send(""+isInstructor);
      }
    });
  }
  else if (type === "classCode") {
    db.collection("classInfo").find({ uid: { $eq: uid }}).project({classCode:1, _id:0}).maxTimeMS(timeout).toArray((err, classCode) => {
      if (err) {
        throw err;
      } else {
        classCode = Object.values(classCode[0])[0];
        res.send(""+classCode);
      }
    });
  }
  else {
    res.send("request invalid");
  }
});

router.get("/classStats", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");

  let timeout = 2000;

  if (type === "EXP") {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({EXP:1, _id:0}).maxTimeMS(timeout).toArray((err, exp) => {
      if (err) {
        throw err;
      } else {
        exp = Object.values(exp[0])[0];
        res.send(""+exp);
      }
    });
  }
  else if (type === "userQuizCount") {
    db.collection("userInfo").find({ uid: { $eq: uid }}).project({userQuizCount:1, _id:0}).maxTimeMS(timeout).toArray((err, quizCount) => {
      if (err) {
        throw err;
      } else {
        quizCount = Object.values(quizCount[0])[0];
        res.send(""+quizCount);
      }
    });
  }
  else {
    res.send("request invalid");
  }
});

router.get("/notifications", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");

  let timeout = 2000;

  if (type === "notificationFrequency") {
    db.collection("notificationFrequency").find({ uid: { $eq: uid }}).project({notificationFrequency:1, _id:0}).maxTimeMS(timeout).toArray((err, frequency) => {
      if (err) {
        throw err;
      } else {
        frequency = Object.values(frequency[0])[0];
        res.send(""+frequency);
      }
    });
  }
  else {
    res.send("request invalid");
  }
});

/* GET users listing. */
/*eslint complexity: ["error", 10]*/
router.get("/profile", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");

  let timeout = 2000;
  if (type === "profileImage") {
    let filepath = path.join("images", uid, "profile_img.jpg");
    let string = "";
    if (fs.existsSync(filepath)) {
      let bitmap = fs.readFileSync(filepath);
      string = Buffer(bitmap).toString("base64");
    }
    res.send(string);
  }
  else {
    res.send("request invalid");
  }
});

module.exports = router;
