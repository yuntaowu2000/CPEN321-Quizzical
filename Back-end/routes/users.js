let express = require('express');
let MongoClient = require('mongodb').MongoClient;
let fs = require('fs');
let router = express.Router();
let db;

/* GET users listing. */
router.get('/', (req, res, next) => {
  res.writeHead(200, {'Content-Type': 'text/html'});
  let value = "{\"username\":\"yuntao_wu\",\"Email\":\"yuntaowu2000@gmail.com\",\"IS_INSTRUCTOR\":true,\"user quiz count\":1,\"EXP\":13, \"class_code\":10234}";
  res.end(value);
});

router.get('/Profile_Image', (req, res, next) => {
  let bitmap = fs.readFileSync('105960354998423944600_profile_img.jpg');
  let string = Buffer(bitmap).toString('base64');
  res.end(string);
});

module.exports = router;
