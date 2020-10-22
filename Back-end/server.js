const http = require("http");
const fs = require("fs");
var MongoClient = require("mongodb").MongoClient;

// used for testing http post in app
http.createServer(function(req, res) {
    let data = [];
    req.on('data', chunk =>
    {
        data.push(chunk);
    });
    req.on('end', ()=>
    {   
        //print the values in the console
        //data is in utf-8 format hexadecimals
        //JSON.parse parses the data into human readable strings
        var str = data.join("");
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
        res.end();
    });
}).listen(9090);
