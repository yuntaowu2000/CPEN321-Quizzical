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
    let bitmap = fs.readFileSync(filepath);
    let string = Buffer(bitmap).toString("base64");
    res.send(string);
  } else if (type === "user_info") {
    res.send(db.collection.find({ uid: { $eq: uid } }));
  }
  else if (type === "username") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {username:1, _id:0}));
  }
  else if (type === "Email") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {Email:1, _id:0}));
  }
  else if (type === "IS_INSTRUCTOR") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {IS_INSTRUCTOR:1, _id:0}));
  }
  else if (type === "user_quiz_count") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {user_quiz_count:1, _id:0}));
  }
  else if (type === "EXP") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {EXP:1, _id:0}));
  }
  else if (type === "class_code") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {class_code:1, _id:0}));
  }
  else if (type === "notification_frequency") {
    res.send(db.collection.find({ uid: { $eq: uid } }, {notification_frequency:1, _id:0}));
  }
});

module.exports = router;
