let express = require("express");
let MongoClient = require("mongodb").MongoClient;
/*eslint new-cap: ["error", { "capIsNew": false }]*/
let router = express.Router();
let db;

MongoClient.connect(
  "mongodb://localhost:27017",
  {useUnifiedTopology: true},
  (err, client) => {
    db = client.db("data");
  }
);

router.get("/", (req, res, next) => {
    let url = new URL(req.originalUrl, `http://${req.headers.host}`);
    let instructorUID = url.searchParams.get("userId");
    let timeout = 2000;
    let userRank = 1;
    
    db.collection("userInfo")
      .find({isInstructor: true})
      .project({_id:0, username: 1, EXP: 1, uid: 1})
      .sort({EXP: -1})
      .maxTimeMS(timeout)
      .toArray((err, data) => {
      if (err) {
        throw err;
      } else {
        var userData;
        for (var user of data) {
            if (Object.values(user)[0] === instructorUID) {
                userData = user;
                break;
            }
            userRank += 1;
        }
        if (data.length > 10) {
            data = data.slice(0, 10);
        }
        data.push(userRank);
        data.push(userData)
        res.send(data);
      }
    });
});


module.exports = router;
