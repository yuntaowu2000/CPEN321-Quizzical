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
  } else if (type === null) {
    res.send("{\"username\":\"yuntao_wu\",\"Email\":\"yuntaowu2000@gmail.com\",\"IS_INSTRUCTOR\":true,\"user quiz count\":1,\"EXP\":13, \"class_code\":10234}");
  }
});

module.exports = router;
