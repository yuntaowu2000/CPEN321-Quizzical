# CPEN321-Quizzical

## Overview
 CPEN321 APP  
Name of Project: Project Quizzical

Team members of our Project: Jason Bai, Andrew Lance Chan, Ihsan Olawale, Yuntao Wu

## Current progress  

### Front-end  
1. Login  
1.1 Google Authentication implemented  
1.2 Prompt user for username, email  
1.3 Checkbox for instructor/teacher  
2. Homepage  
2.1 Basics UI setup  
2.2 Profile page image, username, email (change functions included) done  
2.3 Currently leader board/class statistic page shows time and class code (+ course category)  
2.4 Prompt user to setup/join class on first sign in  
2.5 Multiple classes supported
3. Quiz  
3.1 A Quiz will load a set of questions (currently on multiple choices are supported)  
3.2 Choices can be either plain text, latex, html, or image  
3.3 Once a user click on the choice, it will change the color of the choice  
3.4 After the user submits a question, they will be informed if they are correct or not. If they are incorrect, correct answer will also be shown.  
3.5 After the user finishes a quiz, they will be shown how they did in the quiz.  

Most parts are translated in Chinese as well.  

### Back-end  
1. Simple POST GET request handling done.


## TODO list
1. UI set up for home activity  
1.1 quiz fragment  
1.2 leader board/class statistic fragment  
**Note:** need to get info from the server for these layouts, such as quizzes available, class average scores, user ranking.
2. UI and logic set up for teacher creating quiz  
**Note:** most picture reading, processing related stuff are in OtherUtils, testActivity
3. Front-Back end communication  
3.1 user info (username, email, profile image, is_instructor, class taken, class statistics)  
3.2 class info (class statistics)
3.3 quiz info (created quiz parsed and sent onto the server; retrive quiz from the server)  

