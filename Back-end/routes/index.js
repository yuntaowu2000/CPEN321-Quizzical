var express = require("express");
/*eslint new-cap: ["error", { "capIsNew": false }]*/
var router = express.Router();

/* GET home page. */
router.get("/", function(req, res, next) {
  res.render("index", { title: "Express" });
});

module.exports = router;
