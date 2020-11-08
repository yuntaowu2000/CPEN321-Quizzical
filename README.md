# CPEN321-Quizzical

## Overview
CPEN321 APP  
Name of Project: Project Quizzical  

Team members of our Project: Jason Bai, Andrew Lance Chan, Ihsan Olawale, Yuntao Wu  

## Current progress  

The progress is written in the sequence of implementation.  

### Front-end  
- Login  
    - Google Authentication implemented  
    - Prompt user for username, email(default set to google username, email)  
    - Checkbox for instructor/teacher  
    - Validate username, email and upload to the server  
    - Firebase push notification skeleton implemented  
- Homepage  
    - Basics UI setup, used tabbed layour for switching among quiz, leader board/class statistics and user profile.  
    - Profile page image, username, email (change functions included) done  
    - Currently leader board/class statistic page shows time and class code(+ course category)   
    - Prompt user to setup/join class on first sign in  
    - Multiple classes supported, user can switch between different classes  
    - In the profile fragment, user can see how many classes he/she is enrolled in, how many quizzes he/she has created/done, how many EXP he/she has earned.  
    - teacher class statistic board set up finished.  
    - teacher leader board and statistic switching finished.  
    - basic quiz/leaderboard refreshing done.(student side not tested yet)    
    - quiz fragment UI finished  
    - leader board/class statistic fragment UI finished  
- Quiz  
    -A Quiz will load a set of questions (currently only multiple choices are supported)  
    - Choices can be either plain text, latex, html formatted, or image  
    - Once a user click on the choice, it will change the color of the choice to highlight the selected choice.  
    - After the user submits a question, they will be informed if they are correct or not. If they are incorrect, correct answer will also be highlighted.  
    - After the user finishes a quiz, they will be shown how they did in the quiz.  
    - The quiz result (including score and wrong question numbers) will be uploaded to the server.  
    - UI and logic set up for teacher creating quiz finished    
- Complex logic  
    - A user is ranked by his/her overall EXP in a semester  
    - The EXP for the student is calculated after each quiz is finished as follows:  
        <img src="pics/score_calculation.png"/>  
    - The EXP for the teacher is calculated after each quiz is created EXP = BASIC_EXP, (BASIC_EXP = 10)  
- Other things  
    - Most parts are translated in Chinese as well.  
    - Some code refactored after checking with Codacy.  
    - Customized buttons to be round cornered

### Back-end  
- Simple POST GET request handling done.  
- user info (username, email, profile image, is_instructor, class taken, class statistics) mostly done  
- quiz info (created quiz parsed and sent onto the server; retrive quiz from the server)   

## TODO list
-  UI set up for home activity  
**Note:** need to get info from the server for these layouts, such as quizzes available, class average scores, user ranking.  
    - maybe we can add some review quizzes for all the wrong questions a student has got, the wrong question ids will be sent to the server  
- Front-Back end communication  
    - class info (class statistics, quiz modules)   
- Push notification  
    - email when account created  
    - email when class created  
    - mobile notification when quiz, leaderboard updated  
- Complex logic  
    - The EXP for the teacher is calculated as follows:  
    - When a student/another teacher liked the quiz, EXP += BONUS_EXP, BONUS_EXP = ?  
