let express = require('express');
let MongoClient = require('mongodb').MongoClient;
let fs = require('fs');
let router = express.Router();
let db;

/* GET users listing. */
router.get('/', (req, res, next) => {
  let url = new URL(req.originalUrl, `http://${req.headers.host}`);
  let uid = url.searchParams.get('user_id');
  let type = url.searchParams.get('type');
  if (type === 'notification_frequency') {
    res.send('3');
  } else if (type === 'Profile_Image') {
    let bitmap = fs.readFileSync('/home/site/wwwroot/images/105960354998423944600_profile_img.jpg');
    let string = Buffer(bitmap).toString('base64');
    res.end(string);
  } else if (type === full) {
    res.send(db.collection.find({ uid: { $eq: uid } }));
  }
  else if (type === username) {
    res.send(db.collection.find({ uid: { $eq: uid } }, {username:1, _id:0}));
  }
  else if (type === Email) {
    res.send(db.collection.find({ uid: { $eq: uid } }, {Email:1, _id:0}));
  }
  else if (type === IS_INSTRUCTOR) {
    res.send(db.collection.find({ uid: { $eq: uid } }, {IS_INSTRUCTOR:1, _id:0}));
  }
  else if (type === user_quiz_count) {
    res.send(db.collection.find({ uid: { $eq: uid } }, {user_quiz_count:1, _id:0}));
  }
  else if (type === EXP) {
    res.send(db.collection.find({ uid: { $eq: uid } }, {EXP:1, _id:0}));
  }
  else if (type === class_code) {
    res.send(db.collection.find({ uid: { $eq: uid } }, {class_code:1, _id:0}));
  }
});

module.exports = router;
