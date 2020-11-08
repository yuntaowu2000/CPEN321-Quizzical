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


router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  
  let timeout = 2000;

  if (type === null) {
    db.collection("user info").find({ uid: { $eq: uid }}).project({profileImage:0, _id:0}).maxTimeMS(timeout).toArray((err,data) => {
      if (err) {
        throw err;
      } else {
        res.send(data);
      }
    });
  }
  else if (type === "userInfo") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({_id:0}).maxTimeMS(timeout).toArray((err,data) => {
      if (err) {
        throw err;
      } else {
        res.send(data);
      }
    });
  }
  
});

router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  
  let timeout = 2000;

  if (type === "username") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({username:1, _id:0}).maxTimeMS(timeout).toArray((err, username) => {
      if (err) {
        throw err;
      } else {
        username = Object.values(username[0])[0];
        res.send(""+username);
      }
    });
  }
  else if (type === "Email") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({Email:1, _id:0}).maxTimeMS(timeout).toArray((err, email) => {
      if (err) {
        throw err;
      } else {
        email = Object.values(email[0])[0];
        res.send(""+email);
      }
    });
  }
  
});

router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  
  let timeout = 2000;

  if (type === "isInstructor") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({isInstructor:1, _id:0}).maxTimeMS(timeout).toArray((err, isInstructor) => {
      if (err) {
        throw err;
      } else {
        isInstructor = Object.values(isInstructor[0])[0];
        res.send(""+isInstructor);
      }
    });
  }
  else if (type === "userQuizCount") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({userQuizCount:1, _id:0}).maxTimeMS(timeout).toArray((err, quizCount) => {
      if (err) {
        throw err;
      } else {
        quizCount = Object.values(quizCount[0])[0];
        res.send(""+quizCount);
      }
    });
  }
  
});

router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  
  let timeout = 2000;
  
  if (type === "EXP") {
    db.collection("user info").find({ uid: { $eq: uid }}).project({EXP:1, _id:0}).maxTimeMS(timeout).toArray((err, exp) => {
      if (err) {
        throw err;
      } else {
        exp = Object.values(exp[0])[0];
        res.send(""+exp);
      }
    });
  }
  else if (type === "classCode") {
    db.collection("class info").find({ uid: { $eq: uid }}).project({classCode:1, _id:0}).maxTimeMS(timeout).toArray((err, classCode) => {
      if (err) {
        throw err;
      } else {
        classCode = Object.values(classCode[0])[0];
        res.send(""+classCode);
      }
    });
  }

});

router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  
  let timeout = 2000;
  
   if (type === "notificationFrequency") {
    db.collection("notification frequency").find({ uid: { $eq: uid }}).project({notificationFrequency:1, _id:0}).maxTimeMS(timeout).toArray((err, frequency) => {
      if (err) {
        throw err;
      } else {
        frequency = Object.values(frequency[0])[0];
        res.send(""+frequency);
      }
    });
  }

});

/* GET users listing. */
/*eslint complexity: ["error", 10]*/
router.get("/", (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get("userId");
  let type = url.searchParams.get("type");
  
  let timeout = 2000;
  if (type === "profileImage") {
    let filepath = "images/" + uid + "/profile_img.jpg";
    let string = "";
    if (fs.existsSync(filepath)) {
      let bitmap = fs.readFileSync(filepath);
      string = Buffer(bitmap).toString("base64");
    }
    res.send(string);
  }
});

module.exports = router;
