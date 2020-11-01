let express = require("express");
let router = express.Router();
let fs = require("fs");
let MongoClient = require("mongodb").MongoClient;
let db;

MongoClient.connect(
  "mongodb://module6quizzical:uLBSjjivRdyY5sa3C1CQa6EsirCm2MNSQFa7oilEzad11rPNoO4zE8xKMEhibcvHiiw3xahYI4FiQs43U1WBgg==@module6quizzical.documents.azure.com:10250/mean?ssl=true",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
    db.createCollection("testCollection", (err, res) => {
      if (err) {
	console.log(err);
      }
    });
  }
);

router.use(express.json());

router.post("/", (req, res, next) => {
  console.log("UID: " + req.body.uid);
  console.log("Type: " + req.body.type);

  if (req.body.type === "Profile_Image") {
    let path = "images/" + req.body.uid;
    if (!fs.existsSync(path)) {
      fs.mkdirSync(path);
    }
    let filename = path + "/profile_img.jpg";
    fs.writeFileSync(filename, req.body.data, {encoding: "base64"});
  } else {
    db.collection("testCollection").insertOne(req.body, (err, res) => {
      if (err) {
        console.log(err);
      }
    });
  }

  res.statusCode = 200;
  res.end();
});

module.exports = router;
