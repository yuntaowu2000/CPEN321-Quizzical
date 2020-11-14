let MongoClient = require("mongodb").MongoClient;
let db;
let classesDb;

MongoClient.connect(
    "mongodb://localhost:27017",
    {useUnifiedTopology: true},
    (err, client) => {
      db = client.db("data");
      classesDb = client.db("classes");
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
      module.exports.db = db;
      module.exports.classesDb = classesDb;
    }
);


