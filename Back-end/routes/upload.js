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
    db.createCollection("class info", (err, res) => {
      if (err) {
        console.error(err);
      }
    });
    db.createCollection("user info", (err, res) => {
      if (err) {
        console.error(err);
      }
    });
    db.createCollection("notification_frequency", (err, res) => {
      if (err) {
        console.error(err);
      }
    });
    db.createCollection("quizzes", (err, res) => {
      if (err) {
        //console.log(err);
      }
    });
  }
);

router.use(express.json());

router.post("/", (req, res, next) => {
  if (req.body.type === "ProfileImage") 
  {
    let path = "images/" + req.body.uid;
    let filename = path + "/profile_img.jpg";
    fs.writeFileSync( path + "/profile_img.jpg" , req.body.data, {encoding: "base64"});
    
    if (!fs.existsSync( "images/" + req.body.uid )) 
    {
      fs.mkdirSync( "images/" + req.body.uid , {recursive:true});
    }
  }

  res.statusCode = 200;
  res.end();
});



router.post("/", (req, res, next) => {
  
  if (req.body.type === "userInfo") {
    db.collection("user info").updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  }
  
  else if (req.body.type === "Email") {
    db.collection("user info").updateOne({uid: req.body.uid}, {$set: {Email: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  }
  
  res.statusCode = 200;
  res.end();
});


router.post("/", (req, res, next) => {
  
  else if (req.body.type === "notificationFrequency") {
    try {
      db.collection(req.body.type).updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
        if (err) {
          console.error(err);
        }
      });
    } catch (ex) {
      db.collection(req.body.type).updateOne({uid: req.body.uid}, {$set: req.body}, {upsert: true}, (err, res) => {
        if (err) {
          console.error(err);
        }
      });
    }
  }
  
  res.statusCode = 200;
  res.end();
});

router.post("/", (req, res, next) => {
  
   if (req.body.type === "username") {
    db.collection("user info").updateOne({uid: req.body.uid}, {$set: {username: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  }
  
  else if (req.body.type === "joinClass" || req.body.type === "createClass") {
    db.collection("class info").updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  } 
  
  res.statusCode = 200;
  res.end();
});

router.post("/", (req, res, next) => {

  if (req.body.type === "EXP") {
    db.collection("user info").updateOne({uid: req.body.uid}, {$set: {EXP: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  } else if (req.body.type === "userQuizCount") {
    db.collection("user info").updateOne({uid: req.body.uid}, {$set: {"userQuizCount": req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  }
 
  res.statusCode = 200;
  res.end();
});

  
/*eslint complexity: ["error", 10]*/
router.post("/", (req, res, next) => {
  //console.error("UID: " + req.body.uid);
  //console.error("Type: " + req.body.type);
  //console.error("Data: %j", req.body);
  //console.error(Object.entries(req.body));
  /*
  else if (req.body.type === "class_list") 
  {
  }
  */
  if (req.body.type === "createQuiz") {
    let quizData = JSON.parse(req.body.data);
    db.collection("quizzes").updateOne(
      {$and: [{uid: req.body.uid},{moduleName: quizData.moduleName},{classCode: quizData.classCode}]},
      {$set: Object.assign({}, quizData, {uid: req.body.uid}, {classCode: quizData.classCode}, {moduleName: quizData.moduleName})},
      {upsert: true}, (err, res) => {
      if (err) {
        console.error(err);
      }
    });
  } 

  res.statusCode = 200;
  res.end();
});

module.exports = router;
