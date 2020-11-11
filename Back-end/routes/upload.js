let express = require("express");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let fs = require("fs");
let MongoClient = require("mongodb").MongoClient;
let db;
let nodemailer = require("nodemailer");
let util = require("util");
let firebaseAdmin = require("firebase-admin");

let serviceAccount = require("../plated-inn-286021-firebase-adminsdk-oxi0q-0e23826d54.json");
firebaseAdmin.initializeApp({
  credential: firebaseAdmin.credential.cert(serviceAccount),
  databaseURL: "https://plated-inn-286021.firebaseio.com"
});

function sendMessage(userIds, message) {
  let timeout = 2000;
  db.collection("notificationFrequency").find({uid: {$in: userIds }}).project({firebaseToken:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
    if (err) {
      throw err;
    } else {
      let userTokens = [];
      for (var val of retval) {
        userTokens.push(Object.values(val)[0]);
      }

      let payload = {
        notification: {
          title: "Quizzical",
          body: message
        },
        tokens: userTokens
      };
      firebaseAdmin.messaging().sendMulticast(payload);
    }
  });
}

function sendQuizModulePushNotification(classCode) {
  let timeout = 2000;

  db.collection("classInfo").find({classCode: { $eq: classCode }}).project({className:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
    if (err) {
      throw err;
    } else {
      let className = Object.values(retval[0])[0] + "";
      let message =  util.format("Quiz modules in %s has been updated.", className);
      let userIds = ["105960354998423944600", "118436222585761741438"];
      //get all the students here and send the message to all students
      sendMessage(userIds, message);
    }
  });
}

let transporter = nodemailer.createTransport({
  host: "smtp.ethereal.email",
  port: 587,
  auth: {
    user: "jessika.reichert39@ethereal.email",
    pass: "gtZXRfDehhW2KBYEQy"
  }
});


function sendEmail(emailAddr, emailSubject, emailHtml) {
  let mailOptions = {
    from: "test@quizzical.com",
    to: emailAddr,
    subject: emailSubject,
    html: emailHtml
  };

  transporter.sendMail(mailOptions, (err, info) => {
    if (err) {
      throw err;
    }
  });
}

function parseCreateClassEmailContent(username, className, classCode) {
  return util.format("<p>Congratulations, %s!</p><p>You have just created a new Class: %s</p><p>Your class code is: %d.</p><p>share it with the students and begin your quizzes!</p>", username, className, classCode);
}

function sendCreateClassEmail(uid, className, classCode) {
  let timeout = 2000;
  db.collection("userInfo").find({ uid: { $eq: uid }}).project({username:1, Email:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
    if (err) {
      throw err;
    } else {
      let username = Object.values(retval[0])[1];
      let email = Object.values(retval[0])[0];
      let parsedContent = parseCreateClassEmailContent(username, className, classCode);
      sendEmail(email, "Quizzical: New class created", parsedContent);
    }
  });
}

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

    sendCreateClassEmail(req.body.uid, data.className, data.classCode);
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
  }

  res.statusCode = 200;
  res.end();
});

router.post("/instructorStats", (req, res, next) => {
  if (req.body.type === "EXP") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {EXP: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  } else if (req.body.type === "userQuizCount") {
    db.collection("userInfo").updateOne({uid: req.body.uid}, {$set: {userQuizCount: req.body.data, uid: req.body.uid}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
  }

  res.statusCode = 200;
  res.end();
});

router.post("/studentStats", (req, res, next) => {
  let studentQuizResult = JSON.parse(req.body.data);
  db.collection("userInfo").updateOne({uid: req.body.uid}, 
    {$set: {userQuizCount: studentQuizResult.userQuizCount, EXP: studentQuizResult.EXP, uid: req.body.uid}},
    {upsert: true}, (err, res) => {
    if (err) {
      // console.error(err);
    }
  });

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
    db.collection("classInfo").updateOne({classCode: Number(req.body.uid)}, {$set: {"quizModules":req.body.data}}, {upsert: true}, (err, res) => {
      if (err) {
        // console.error(err);
      }
    });
    sendQuizModulePushNotification(Number(req.body.uid));
  }

  res.statusCode = 200;
  res.end();
});

module.exports = router;