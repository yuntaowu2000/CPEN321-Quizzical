/*eslint new-cap: ["error", { "capIsNew": false }]*/
let firebaseAdmin = require("firebase-admin");
let scheduler = require("node-schedule");
let MongoClient = require("mongodb").MongoClient;
let db;

MongoClient.connect(
    "mongodb://localhost:27017",
    {useUnifiedTopology: true},
    (err, client) => {
      db = client.db("data");
    }
);

let serviceAccount = require("../plated-inn-286021-firebase-adminsdk-oxi0q-0e23826d54.json");
firebaseAdmin.initializeApp({
  credential: firebaseAdmin.credential.cert(serviceAccount),
  databaseURL: "https://plated-inn-286021.firebaseio.com"
});

function sendMessage(userIds, message) {
  let timeout = 2000;
  if (userIds === null || userIds.length === 0) {
    return;
  }
  db.collection("notificationFrequency").find({uid: {$in: userIds }}).project({firebaseToken:1, _id:0}).maxTimeMS(timeout).toArray((err, retval) => {
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
    if (userTokens.length === 0) {
      return;
    }
    firebaseAdmin.messaging().sendMulticast(payload);
  });
}

//everyday push notification at 8pm
scheduler.scheduleJob("0 0 20 * * *", function() {
    db.collection("notificationFrequency").find({notificationFrequency: {$eq : 1}})
    .project({uid: 1, _id: 0})
    .toArray((err, retval) => {
        let userIds = [];
        for (var val of retval) {
            userIds.push(Object.values(val)[0]);
        }
        sendMessage(userIds, "This is your daily reminder for Quizzical. Check your leaderboard position and keep up!");
    });
});

//Weekly: every Monday push notification at 8pm
scheduler.scheduleJob("0 0 20 * * 1", function() {
    db.collection("notificationFrequency").find({notificationFrequency: {$eq : 2}})
    .project({uid: 1, _id: 0})
    .toArray((err, retval) => {
        let userIds = [];
        for (var val of retval) {
            userIds.push(Object.values(val)[0]);
        }
        sendMessage(userIds, "This is your weekly reminder for Quizzical. Check your leaderboard position and keep up!");
    });
});

//Monthly: every 1st day of month push notification at 8pm
scheduler.scheduleJob("0 0 20 1 * *", function() {
    db.collection("notificationFrequency").find({notificationFrequency: {$eq : 3}})
    .project({uid: 1, _id: 0})
    .toArray((err, retval) => {
        let userIds = [];
        for (var val of retval) {
            userIds.push(Object.values(val)[0]);
        }
        sendMessage(userIds, "This is your monthly reminder for Quizzical. Check your leaderboard position and keep up!");
    });
});

module.exports.sendMessage = sendMessage;