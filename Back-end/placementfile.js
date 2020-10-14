const http = require("http");

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
        console.log(data)
        console.log(JSON.parse(data))

        // respond to the sender
        res.statusCode = 200
        res.end()
    })
}).listen(9090);
