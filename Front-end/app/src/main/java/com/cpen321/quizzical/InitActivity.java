package com.cpen321.quizzical;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import com.cpen321.quizzical.PageFunctions.LoginActivity;
import com.cpen321.quizzical.PageFunctions.TestPage;

public class InitActivity extends AppCompatActivity {

    SharedPreferences sp;

    /**
        This is the class for the initial screen which includes login and test buttons
        Login button will redirect the app to the login screen
        Test button will redirect the app to our own on phone test/debug screen
        and it will be removed in the final release.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button registerButtion = (Button) findViewById(R.id.registerButton); //not used
        Button testButton = (Button) findViewById(R.id.test_page_button);

        //use shared preference to record user login
        //so that a user does not need to login again on multiple use
        sp = getSharedPreferences(getString(R.string.Login), MODE_PRIVATE);


        if (sp.getBoolean(getString(R.string.LOGGED), false))
        {
            //the user has logged in before, and we have the credential
            //can be redirected to home screen directly.
            Intent intent = new Intent(InitActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActivityCompat.finishAffinity(this);
            //completely stop this activity, so that user will not go back to this activity 
            //by clicking the back button
        }

        //login button 
        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(InitActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActivityCompat.finishAffinity(this);
        });
        

        //used for test and debug only
        testButton.setOnClickListener(view -> {
            Intent intent = new Intent(InitActivity.this, TestPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActivityCompat.finishAffinity(this);
        });
    }

}