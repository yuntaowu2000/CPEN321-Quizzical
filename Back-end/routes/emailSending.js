let nodemailer = require("nodemailer");

let transporter = nodemailer.createTransport({
    host: "smtp.ethereal.email",
    port: 587,
    auth: {
      user: "jessika.reichert39@ethereal.email",
      pass: "gtZXRfDehhW2KBYEQy"
    }
});
  
  
function sendEmail(emailAddr, emailSubject, emailHtml) {
    let mailOptions = {
      from: "test@quizzical.com",
      to: emailAddr,
      subject: emailSubject,
      html: emailHtml
    };
  
    transporter.sendMail(mailOptions, (err, info) => {
      if (err) {
        throw err;
      }
    });
}

module.exports.sendEmail = sendEmail;