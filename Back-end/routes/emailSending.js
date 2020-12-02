let nodemailer = require("nodemailer");

let transporter = nodemailer.createTransport({
  host: "smtp.gmail.com",
  port: 465,
  secure: true,
  auth: {
    type: "OAuth2",
    user: "cloudwhale321@gmail.com",
    clientId: "723821872071-9dijasglssuk685khvlu2o6mmfarkofm.apps.googleusercontent.com",
    clientSecret: "LLOYnUjD-543NSJrpzobbLRg",
    refreshToken: "1//04AXH_leqr9FyCgYIARAAGAQSNwF-L9IrFSYdmT4TTFArn9t99xw48tUWg1bGrxbu4_JsCf79txpBF7qfAtznL7jb38Gx7xEBJYc"
  }
});


function sendEmail(emailAddr, emailSubject, emailHtml) {
    let mailOptions = {
      from: "test@quizzical.com",
      to: emailAddr,
      subject: emailSubject,
      html: emailHtml
    };

    transporter.sendMail(mailOptions);
}

module.exports.sendEmail = sendEmail;
