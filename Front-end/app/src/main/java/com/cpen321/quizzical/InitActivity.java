package com.cpen321.quizzical;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpen321.quizzical.PageFunctions.LoginActivity;
import com.cpen321.quizzical.PageFunctions.TestPage;
import com.cpen321.quizzical.Utils.OtherUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.regex.Pattern;

public class InitActivity extends AppCompatActivity {

    SharedPreferences sp;

    private static final int RC_SIGN_IN = 420;
    private GoogleSignInClient mGoogleSignInClient;

    private LinearLayout linearLayout;
    protected boolean username_input_OK = false;
    protected boolean email_input_OK = false;

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

        linearLayout = findViewById(R.id.init_linear_layout);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button testButton = (Button) findViewById(R.id.test_page_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        signInButton.setOnClickListener(view->signIn());

        //use shared preference to record user login
        //so that a user does not need to login again on multiple use
        //also record all user-related info such as username, email and profile picture
        sp = getSharedPreferences(getString(R.string.Login), MODE_PRIVATE);


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

    @Override
    public void onStart()
    {
        super.onStart();
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
        } else
        {
            //the user has logged out, we need to sign the user's google account out as well
            mGoogleSignInClient.signOut();
        }
    }

    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            validateAndLogin(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            validateAndLogin(null);
        }
    }

    private void validateAndLogin(GoogleSignInAccount account)
    {
        if (account != null)
        {
            //need to get user name and other stuff from the server
            if (OtherUtils.StringIsNullOrEmpty(sp.getString(getString(R.string.user_name), "")))
            {
                requestUserNameAndEmail();
            }
            else
            {
                goToHomeActivity();
            }
        } else {
            signIn();
        }
    }

    private void requestUserNameAndEmail()
    {

        LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        layoutParams.setMargins(30,10,30,0);

        linearLayout.removeAllViews();

        final TextView usernameText = new TextView(this);
        final EditText usernameInput = new EditText(this);
        final TextView usernameErrorText = new TextView(this);
        usernameText.setText(R.string.USERNAME_MSG);
        usernameText.setTextColor(Color.WHITE);
        usernameText.setLayoutParams(layoutParams);

        usernameInput.setHint(R.string.EXAMPLE_USERNAME);
        usernameInput.setTextColor(Color.WHITE);
        usernameInput.setHintTextColor(Color.WHITE);
        usernameInput.setLayoutParams(layoutParams);

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (OtherUtils.StringIsNullOrEmpty(editable.toString()))
                {
                    usernameErrorText.setText("Please enter a username.");
                    username_input_OK = false;
                }
                else if (!checkUserName(editable.toString()))
                {
                    usernameErrorText.setText("Username invalid: 3-15 characters with a-z,A-Z,0-9 and _ allowed.");
                    username_input_OK = false;
                }
                else
                {
                    usernameErrorText.setText("");
                    username_input_OK = true;
                }
            }
        });

        usernameErrorText.setText("");
        usernameErrorText.setTextColor(getResources().getColor(R.color.colorCrimson));
        linearLayout.addView(usernameText);
        linearLayout.addView(usernameInput);
        linearLayout.addView(usernameErrorText);

        final TextView emailText = new TextView(this);
        final EditText emailInput = new EditText(this);
        final TextView emailErrorText = new TextView(this);

        emailText.setText(R.string.SCHOOL_EMAIL_MSG);
        emailText.setTextColor(Color.WHITE);
        emailText.setLayoutParams(layoutParams);

        emailInput.setHint(R.string.EXAMPLE_EMAIL);
        emailInput.setTextColor(Color.WHITE);
        emailInput.setHintTextColor(Color.WHITE);
        emailInput.setLayoutParams(layoutParams);

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (OtherUtils.StringIsNullOrEmpty(editable.toString()))
                {
                    emailErrorText.setText("Please enter a valid email");
                    email_input_OK = false;
                }
                else if (!checkEmail(editable.toString()))
                {
                    emailErrorText.setText("email invalid");
                    email_input_OK = false;
                } else
                {
                    emailErrorText.setText("");
                    email_input_OK = true;
                }
            }
        });

        emailErrorText.setText("");
        emailErrorText.setTextColor(getResources().getColor(R.color.colorCrimson));
        emailErrorText.setLayoutParams(layoutParams);

        linearLayout.addView(emailText);
        linearLayout.addView(emailInput);
        linearLayout.addView(emailErrorText);


        Button finishButton = new Button(this);
        finishButton.setText(R.string.Finish);
        finishButton.setAllCaps(false);
        finishButton.setLayoutParams(layoutParams);

        finishButton.setGravity(Gravity.BOTTOM | Gravity.CENTER);

        finishButton.setOnClickListener(view->onFinishClicked());
        linearLayout.addView(finishButton);
    }

    private void onFinishClicked()
    {
        if (username_input_OK && email_input_OK)
        {
            checkInstructor();
        }
        else
        {
            Toast.makeText(this, "Please enter valid username and email", Toast.LENGTH_LONG).show();
        }
    }


    private boolean checkUserName(String username)
    {
        //check if the username is valid or not
        //need to check if this username is registered on server or not
        if (!Pattern.matches("^[aA-zZ0-9_-]{3,15}$",username))
        {
            return false;
        }

        sp.edit().putString(getString(R.string.user_name), username).apply();
        Log.d(getString(R.string.user_name), username);
        return true;
    }


    private boolean checkEmail(String email)
    {
        //check if the email is valid or not
        //and upload this email to the server
        //the server should push an authentication email to the email address

        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";

        if (!Pattern.matches(regex, email))
        {
            //the email entered is not a valid format
            return false;
        }


        sp.edit().putString(getString(R.string.Email), email).apply();
        Log.d(getString(R.string.Email), email);
        return true;
    }

    private void checkInstructor()
    {
        new AlertDialog.Builder(this).setMessage(getString(R.string.is_instructor_msg))
                .setPositiveButton(R.string.yes, ((dialogInterface, i) ->
                {
                    sp.edit().putBoolean(getString(R.string.IS_INSTRUCTOR), true).apply();
                    goToHomeActivity();
                    dialogInterface.dismiss();
                }))
                .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                    sp.edit().putBoolean(getString(R.string.IS_INSTRUCTOR), false).apply();
                    goToHomeActivity();
                    dialogInterface.dismiss();
                })
                .show();
    }

    private void goToHomeActivity()
    {
        sp.edit().putBoolean(getString(R.string.LOGGED), true).apply();
        Intent intent = new Intent(InitActivity.this, HomeActivity.class);
        startActivity(intent);
    }

}