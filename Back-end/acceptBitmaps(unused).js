const http = require("http");
const fs = require("fs")

// used for testing http post in app
http.createServer(function(req, res) {
    let data = ""
    req.on('data', chunk =>
    {
        data += chunk
        
    })
    req.on('end', ()=>
    {   
        //parse the encoded image to jpg format
        console.log(data)
        fs.writeFileSync("test.jpg", data, {encoding:'base64'});
        // respond to the sender
        res.statusCode = 200
        res.end()
    })
}).listen(7070);