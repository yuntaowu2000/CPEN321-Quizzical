package com.cpen321.quizzical.PageFunctions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cpen321.quizzical.HomeActivity;
import com.cpen321.quizzical.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 420;
    SharedPreferences sp;
    private Button loginButton;
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView failed_notification;
    private CheckBox debugCheckBox;
    private int counter = 3;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginPageButton);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.pwdInput);
        failed_notification = findViewById(R.id.login_failed_notification);
        debugCheckBox = findViewById(R.id.test_login_check_box);

        loginButton.setOnClickListener(view -> validateAndLogin());

        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        signInButton.setOnClickListener(view -> signIn());
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        validateAndLogin(account);
    }

    private void signIn() {
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

    private void validateAndLogin(GoogleSignInAccount account) {
        if (account == null) {
            failed_notification.setText("");
        } else {
            sp.edit().putBoolean(getString(R.string.LOGGED), true).apply();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    private void validateAndLogin() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (username.equals("admin") && password.equals("123456")) {
            failed_notification.setText("");

            sp.edit().putBoolean(getString(R.string.LOGGED), true).apply();

            if (debugCheckBox.isChecked()) {
                sp.edit().putBoolean(getString(R.string.IS_INSTRUCTOR), true).apply();
            } else {
                sp.edit().putBoolean(getString(R.string.IS_INSTRUCTOR), false).apply();
            }

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            counter--;
            failed_notification.setText("Login failed. You have " + counter + " tries remaining");
        }
    }


}
