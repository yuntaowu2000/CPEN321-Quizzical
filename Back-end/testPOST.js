const http = require("http");
const fs = require("fs")
var MongoClient = require("mongodb").MongoClient;
var db;

MongoClient.connect(
    "mongodb://module6quizzical:uLBSjjivRdyY5sa3C1CQa6EsirCm2MNSQFa7oilEzad11rPNoO4zE8xKMEhibcvHiiw3xahYI4FiQs43U1WBgg==@module6quizzical.documents.azure.com:10250/mean?ssl=true", 
    { useUnifiedTopology: true },
    (err, client) => {
    db = client.db("data"); 
    console.log("mongodb connected\n");
    db.createCollection("testCollection", (err, res)=>{
        if (err)
        {
            console.log(err);
        }
    });
    console.log("test collection created\n");
  });
  
// used for testing http post in app
http.createServer(function(req, res) {
    let data = []
    req.on("data", chunk =>
    {
        data.push(chunk);
    })
    req.on("end", ()=>
    {   
        //print the values in the console
        //data is in utf-8 format hexadecimals
        //JSON.parse parses the data into human readable strings
        var str = data.join("");
        var obj
        try
        {
            obj = JSON.parse(str);
        } catch (ex)
        {
            obj = JSON.parse(str.replace(" ", ""));
        }
        

        console.log("UID: " + obj.uid);
        console.log("Type: " + obj.type);

        // db.collection("data").updateOne(
        //     { uid: obj.uid }, {$set: {type: obj.type, data: obj.data} }, 
        //     (err, request) => {
        //         if (err) return console.log(err);
        //         // if (request.body.task == null || request.body.info == null) {
        //         //     res.status(400).send("Invalid task of info\n");
        //         //        return; 
        //         //    }
        //         //res.end("updated\n");
        //     }
        // );

        db.collection("testCollection").insertOne(obj, (err, res)=> {
            if (err) 
            {
                console.log(err);
            }
        });

        db.collection("testCollection").find().toArray((err, items)=>console.log(items));

        if (obj.type == "Profile_Image")
        {
            fs.writeFileSync(obj.uid + "_profile_img.jpg", obj.data, {encoding:"base64"});
        } 
        else
        {
            fs.writeFileSync(obj.uid + "_" + obj.type + ".txt", obj.data);
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
        res.end();
    })
}).listen(9090);

http.createServer(function(req, res) {
    if (req.url == "/105960354998423944600")
    {
        res.writeHead(200, {"Content-Type": "text/html"});
        var value = "{\"username\":\"yuntao_wu\",\"Email\":\"yuntaowu2000@gmail.com\",\"IS_INSTRUCTOR\":true,\"user quiz count\":1,\"EXP\":13, \"class_code\":10234}";
        res.end(value);
    } else if (req.url == "/105960354998423944600/Profile_Image")
    {
        var bitmap = fs.readFileSync("105960354998423944600_profile_img.jpg");
        var string = Buffer(bitmap).toString("base64");
        res.end(string);
    }
}).listen(7070);
