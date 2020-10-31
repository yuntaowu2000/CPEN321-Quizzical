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

router.use(express.json());

router.post('/', (req, res, next) => {
  console.log('Data: ' + req.body);
  console.log('UID: ' + req.body.uid);
  console.log('Type: ' + req.body.type);

  db.collection('testCollection').insertOne(req.body, (err, res) => {
    if (err) {
      console.log(err);
    }
  });

  db.collection('testCollection').find().toArray((err, items) => {});

  if (req.body.type == 'Profile_Image') {
    fs.writeFileSync(req.body.uid + '_profile_img.jpg', req.body.data, {encoding: 'base64'});
  } else {
    fs.writeFileSync(req.body.uid + '_' + req.body.type + '.txt', req.body.data);
  }

  res.statusCode = 200;
  res.end();
});

router.get('/', (req, res, next) => {
  res.send('Upload page');
});

module.exports = router;
