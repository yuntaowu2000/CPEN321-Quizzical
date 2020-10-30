let express = require('express');
let router = express.Router();
let fs = require('fs');
let MongoClient = require('mongodb').MongoClient;
let db;

MongoClient.connect(
  'mongodb://module6quizzical:uLBSjjivRdyY5sa3C1CQa6EsirCm2MNSQFa7oilEzad11rPNoO4zE8xKMEhibcvHiiw3xahYI4FiQs43U1WBgg==@module6quizzical.documents.azure.com:10250/mean?ssl=true',
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db('data');
    db.createCollection('testCollection', (err, res) => {
      if (err) {
	console.log(err);
      }
    });
  }
);

router.post('/', (req, res, next) => {
  let data = [];
  req.on('data', chunk => {
    data.push(chunk);
    console.log('Pushed this chunk: ' + chunk);
  });
  req.on('end', () => {
    let str = data.join('');
    let obj;
    try {
      obj = JSON.parse(str);
    } catch (ex) {
      obj = JSON.parse(str.replace(' ', ''));
    }

    console.log('UID: ' + obj.uid);
    console.log('Type: ' + obj.type);

    db.collection('testCollection').insertOne(obj, (err, res) => {
      if (err) {
	console.log(err);
      }
    });

    db.collection('testCollection').find().toArray((err, items) => {});

    if (obj.type === 'Profile Image') {
      fs.writeFileSync(obj.uid + '_profile_img.jpg', obj.data, {encoding: 'base64'});
      console.log(obj.type);
    } else {
      fs.writeFileSync(obj.uid + '_' + obj.type + '.txt', obj.data);
      console.log(obj.type);
    }

    res.statusCode = 200;
    res.end();
  });
});

module.exports = router;
