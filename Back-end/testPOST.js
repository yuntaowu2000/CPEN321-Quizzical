const http = require("http");
const fs = require("fs")

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
        var obj = JSON.parse(str.replace(" ", ""))
        

        console.log("Username: " + obj.username)
        console.log("Type: " + obj.type)

        if (obj.type == "ProfileImage")
        {
            fs.writeFileSync(obj.username + "_profile_img.jpg", obj.data, {encoding:'base64'});
        } else if (obj.type == "Question")
        {
            console.log(JSON.parse(obj.data))
        }

        // respond to the sender
        res.statusCode = 200
        res.end()
    })
}).listen(9090);
