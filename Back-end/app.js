var createError = require('http-errors');
var express = require('express');
var MongoClient = require("mongodb").MongoClient;
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
let http = require('http');
let fs = require("fs")
var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

MongoClient.connect(
  process.env.MONGODB_URI, 
  { useUnifiedTopology: true },
  (err, client) => {
  db = client.db("data");
  var server = app.listen(4000, function() {
    var port = server.address().port;
    console.log("Listening at %s", port);
  });  
});


/*
// curl -X POST -H "Content-Type: application/json" -d '{"task":"demo","info":"testing"}' localhost:3000/list 
app.post("/data", function (req, res) {
  db.collection("data").insertOne(
    { uid: req.body.uid, type: req.body.type, data: req.body.data }, 
    (err, request) => {
      console.log("Error handler in db.collection.insertOne (line 59) reached with request = " + request);
      if (err) return console.log(err); 
      res.send("saved\n"); 
    }
  );
    
  //print the values in the console
  //data is in utf-8 format hexadecimals
  //JSON.parse parses the data into human readable strings
  console.log("Line 68 reached with req.body.data = " + req.body.data);
  var str = req.body.data.join("");
  console.log("Line 70 reached with str = " + str);
  var obj;
  try
  {
    obj = JSON.parse(str);
  } catch (ex)
  {
    obj = JSON.parse(str.replace(" ", ""));
  }
    
  console.log("UID: " + obj.uid);
  console.log("Type: " + obj.type);
    
  // case for user credential, login
  if (obj.type == "user info") {
    var field_Data; // (data field of obj) should have fields: username, Email, IS_INSTRUCTOR.
    try
    {
      field_Data = JSON.parse(obj.data);
    } catch (ex)
    {
      field_Data = obj.data;
    }

    console.log("Username: " + field_Data.username);
    console.log("Email: " + field_Data.Email);
    console.log("IS_INSTRUCTOR: " + field_Data.IS_INSTRUCTOR);
  }

  if (obj.type == "ProfileImage" || obj.type == "Profile Image")
  {
    fs.writeFileSync("username" + "_profile_img.jpg", obj.data, {encoding:"base64"});
  } 
  else
  {
    fs.writeFileSync("username" + "_" + obj.type + ".txt", obj.data);
    try 
    {
        console.log(JSON.parse(obj.data));
    } catch(ex)
    {
        console.log(obj.data);
    }

  }

  // respond to the sender
  res.statusCode = 200;
  // res.end();
    
}); // end of post
*/


// used for testing http post in app
http.createServer(function(req, res) {
    let data = []
    req.on('data', chunk =>
    {
        data.push(chunk)
    })
    req.on('end', ()=>
    {
        //print the values in the console
        //data is in utf-8 format hexadecimals
        //JSON.parse parses the data into human readable strings
        var str = data.join("")
        var obj
        try
        {
            obj = JSON.parse(str)
        } catch (ex)
        {
            obj = JSON.parse(str.replace(" ", ""))
        }


        console.log("UID: " + obj.uid)
        console.log("Type: " + obj.type)

        if (obj.type == "ProfileImage" || obj.type == "Profile Image")
        {
            fs.writeFileSync("username" + "_profile_img.jpg", obj.data, {encoding:'base64'});
        }
        else
        {
            fs.writeFileSync("username" + "_" + obj.type + ".txt", obj.data);
            try
            {
                console.log(JSON.parse(obj.data))
            } catch(ex)
            {
                console.log(obj.data)
            }

        }

        // respond to the sender
        res.statusCode = 200
        res.end()
    })
}).listen(9090);
 
 
/*
app.get("/data", (req, res) => {
  db.collection("data")
    .find({ uid: { $eq: req.body.uid } })
    .toArray((err, result) => {
      res.send(result);
    });
});
*/

// hardcoded get statement for winston's email
http.createServer(function(req, res) {
    if (req.url == "/105960354998423944600")
    {
        res.writeHead(200, {'Content-Type': 'text/html'});
        var value = "{\"username\",\"yuntao_wu\",\"Email\",\"yuntaowu2000@gmail.com\",\"IS_INSTRUCTOR\":true,\"user quiz count\":0,\"EXP\":0}";
        res.end(value);
    }
}).listen(7070);

// curl -X PUT -H "Content-Type: application/json" -d '{"task":"demo","info":"working"}' localhost:3000/list 
app.put("/data", (req, res) => {
   db.collection("data").updateOne(
       { uid: req.body.uid }, {$set: {type: req.body.type, data: req.body.data} }, 
       (err, request) => {
           if (req.body.task == null || req.body.info == null) {
            res.status(400).send("Invalid task of info\n");
               return; 
           }
           if (err) return console.log(err);
           res.send("updated\n");
       }
   );
});

// curl -X DELETE -H "Content-Type: application/json" -d '{"task":"demo"}' localhost:3000/list 
app.delete("/data", function (req, res) {
  db.collection("data").deleteOne(
    { uid: req.body.uid }, 
    (err, request) => {
      if (req.body.uid == null) {
        res.status(400).send("Invalid uid!!!\n");
        return;
      }
      if (err) return console.log(err); 
      res.send("removed\n"); 
    }
  );
});

module.exports = app;
