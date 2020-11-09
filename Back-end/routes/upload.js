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
    db.createCollection("classInfo", (err, res) => {
      if (err) {
        //console.error(err);
      }
    });
    db.createCollection("userInfo", (err, res) => {
      if (err) {
        //console.error(err);
      }
    });
    db.createCollection("notificationFrequency", (err, res) => {
      if (err) {
        //console.error(err);
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

router.post("/profileImg", (req, res, next) => {
  if (req.body.type === "profileImage")
  {
    let path = "images/" + req.body.uid;
    if (!fs.existsSync(path))
    {
      fs.mkdirSync(path, {recursive:true});
    }
    let filename = path + "/profile_img.jpg";
    fs.writeFileSync(filename, req.body.data, {encoding: "base64"});
  }

  res.statusCode = 200;
  res.end();
});

router.post("/user", (req, res, next) => {
  if (req.body.type === "userInfo") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }
  
  else if (req.body.type === "Email") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {Email: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }
  
  else if (req.body.type === "username") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {username: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }
  res.statusCode = 200;
  res.end();
});

router.post("/notifications", (req, res, next) => {
  if (req.body.type === "notificationFrequency") {
    try {
      db.collection("notificationFrequency").updateOne({uid: req.body.uid}, {$set: Object.assign({}, JSON.parse(req.body.data), {uid: req.body.uid})}, {upsert: true}, (err, res) => {
        if (err) {
          // console.error(err);
        }
      });
    } catch (ex) {
      db.collection("notificationFrequency").updateOne({uid: req.body.uid}, {$set: req.body}, {upsert: true}, (err, res) => {
        if (err) {
          // console.error(err);
        }
      });
    }
  }

  res.statusCode = 200;
  res.end();
});

router.post("/class", (req, res, next) => {
  if (req.body.type === "createClass") {
    let data = JSON.parse(req.body.data);
    db.collection("classInfo")
      .updateOne(
	{$and: [{uid: req.body.uid},{classCode: data.classCode}]},
	{$set: Object.assign({}, data, {uid: req.body.uid})},
	{upsert: true},
	(err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  } else if (req.body.type === "classList") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {"classList": req.body.data}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  } else if (req.body.type === "joinClass") {
    let data = JSON.parse(req.body.data);
    db.collection("userInfo")
      .updateOne(
	{$and: [{uid: req.body.uid}]},
	{$set: Object.assign({}, data, {uid: req.body.uid})},
	{upsert: true},
	(err, res) => {
      if (err) {
        // console.error(err);
      }
    });

  res.statusCode = 200;
  res.end();
});

router.post("/stats", (req, res, next) => {
  if (req.body.type === "EXP") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {EXP: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  } else if (req.body.type === "userQuizCount") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {"userQuizCount": req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }

  res.statusCode = 200;
  res.end();
});

/*eslint complexity: ["error", 10]*/
router.post("/quiz", (req, res, next) => {
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
        // console.error(err);
      }
    });
  } else if (req.body.type === "quizModules") {
    let quizModuleData = JSON.parse(req.body.data);
    let quizModuleClassCode = quizModuleData.classCode;

    db.collection("classInfo").updateOne({classCode: quizModuleClassCode}, {$set: {"quizModules":req.body.data}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }

  res.statusCode = 200;
  res.end();
});

module.exports = router;
