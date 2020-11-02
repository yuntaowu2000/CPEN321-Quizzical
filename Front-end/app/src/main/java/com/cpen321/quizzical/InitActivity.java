package com.cpen321.quizzical;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.cpen321.quizzical.utils.OtherUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class InitActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 420;
    protected boolean username_input_OK = false;
    protected boolean email_input_OK = false;
    private SharedPreferences sp;
    private GoogleSignInClient mGoogleSignInClient;
    private LinearLayout.LayoutParams layoutParams;
    private LinearLayout linearLayout;
    private ConstraintLayout constraintLayout;
    private RelativeLayout relativeLayout;
    private CheckBox instructorCheckBox;

    /**
     * This is the class for the initial screen which includes login and test buttons
     * Login button will redirect the app to the login screen
     * Test button will redirect the app to our own on phone test/debug screen
     * and it will be removed in the final release.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        layoutParams.setMargins(30, 10, 30, 0);

        linearLayout = findViewById(R.id.init_linear_layout);
        constraintLayout = findViewById(R.id.init_constraint_layout);
        relativeLayout = findViewById(R.id.init_relative_layout);

        //need to delete this afterwards
        Button testButton = findViewById(R.id.test_page_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        signInButton.setOnClickListener(view -> signIn());

        //use shared preference to record user login
        //so that a user does not need to login again on multiple use
        //also record all user-related info such as username, email and profile picture
        //may need to separate shared preference for different users
        //or we can just completely depend on the server to do the job
        sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);

        //used for test and debug only
        testButton.setOnClickListener(view -> {
            Intent intent = new Intent(InitActivity.this, TestPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActivityCompat.finishAffinity(this);
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d("Firebase", "Fetching token failed");
                return;
            }
            String token = task.getResult();
            Log.d("Token", token);
            sp.edit().putString(getString(R.string.FIREBASE_TOKEN), token).apply();
        });

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sp.getBoolean(getString(R.string.LOGGED), false)) {
            //the user has logged in before, and we have the credential
            //can be redirected to home screen directly.
            Intent intent = new Intent(InitActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ActivityCompat.finishAffinity(this);
            //completely stop this activity, so that user will not go back to this activity
            //by clicking the back button
        } else {
            //the user has logged out, we need to sign the user's google account out as well
            mGoogleSignInClient.signOut();
        }
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
            validateAndLogin(null);
        }
    }

    private void validateAndLogin(GoogleSignInAccount account) {
        if (account != null) {
            //TODO: need to get user name and other stuff from the server here
            //use google ID as our default id
            sp.edit().putString(getString(R.string.UID), account.getId()).apply();

            String url = getString(R.string.GET_URL) + "users?" + getString(R.string.UID) + "=" + account.getId();
            String user_info = OtherUtils.readFromURL(url);
            getUserInfo(user_info);

            if (OtherUtils.stringIsNullOrEmpty(sp.getString(getString(R.string.USERNAME), ""))) {
                //default is google credential
                String username = Objects.requireNonNull(account.getDisplayName()).replace(" ", "_");
                String email = account.getEmail();
                new Thread(() -> {
                    Bitmap bitmap = OtherUtils.getBitmapFromUrl(account.getPhotoUrl().toString());
                    String encodedBitmap = OtherUtils.encodeImage(bitmap);
                    sp.edit().putString(getString(R.string.PROFILE_IMG), encodedBitmap).apply();
                }).start();
                username_input_OK = OtherUtils.checkUserName(username);
                email_input_OK = OtherUtils.checkEmail(email);

                sp.edit().putString(getString(R.string.USERNAME), username).apply();
                sp.edit().putString(getString(R.string.EMAIL), email).apply();
                requestUserNameAndEmail();
            } else {
                goToHomeActivity();
            }
        } else {
            signIn();
        }
    }

    private void getUserInfo(String userInfoJson) {
        String username = "";
        String email = "";
        boolean is_instructor = false;
        int user_quiz_count = 0;
        int EXP = 0;
        String class_codes = "";
        try {
            JSONArray jsonArray = new JSONArray(userInfoJson);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            username = jsonObject.getString(getString(R.string.USERNAME));
            email = jsonObject.getString(getString(R.string.EMAIL));
            is_instructor = jsonObject.getBoolean(getString(R.string.IS_INSTRUCTOR));
            user_quiz_count = jsonObject.getInt(getString(R.string.USER_QUIZ_COUNT));
            EXP = jsonObject.getInt(getString(R.string.EXP));
            class_codes = jsonObject.getString(getString(R.string.CLASS_CODE));
        } catch (Exception e) {
            Log.d("parse_user_info", "failed " + e.getMessage());
        }
        sp.edit().putString(getString(R.string.USERNAME), username).apply();
        sp.edit().putString(getString(R.string.EMAIL), email).apply();
        sp.edit().putBoolean(getString(R.string.IS_INSTRUCTOR), is_instructor).apply();
        sp.edit().putInt(getString(R.string.USER_QUIZ_COUNT), user_quiz_count).apply();
        sp.edit().putInt(getString(R.string.EXP), EXP).apply();
        sp.edit().putString(getString(R.string.CLASS_CODE), class_codes).apply();
    }

    private void requestUserNameAndEmail() {
        //reset the view to get user inputs.
        constraintLayout.setBackgroundResource(0);
        linearLayout.removeAllViews();

        setupUsernameInput();

        setupEmailInput();

        //setup instructor checkbox
        instructorCheckBox = new CheckBox(this);
        instructorCheckBox.setText(getString(R.string.UI_is_instructor_msg));
        instructorCheckBox.setLayoutParams(layoutParams);
        linearLayout.addView(instructorCheckBox);

        //setup finish button
        RelativeLayout.LayoutParams relativeLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeLayoutParam.setMargins(30, 30, 30, 30);

        Button finishButton = new Button(this);
        finishButton.setText(R.string.UI_finish);
        finishButton.setAllCaps(false);
        finishButton.setLayoutParams(relativeLayoutParam);
        finishButton.setBackgroundResource(R.drawable.round_corner_button);

        finishButton.setOnClickListener(view -> onFinishClicked());
        relativeLayout.addView(finishButton);


    }

    private void setupUsernameInput() {
        TextView usernameText = new TextView(this);
        EditText usernameInput = new EditText(this);
        TextView usernameErrorText = new TextView(this);
        usernameText.setText(R.string.UI_username_msg);
        usernameText.setLayoutParams(layoutParams);

        usernameInput.setText(sp.getString(getString(R.string.USERNAME), getString(R.string.UI_example_username)));
        usernameInput.setLayoutParams(layoutParams);
        usernameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        usernameInput.setMaxLines(1);

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* not used*/
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* not used*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String username = editable.toString();
                if (OtherUtils.stringIsNullOrEmpty(username)) {
                    usernameErrorText.setText(R.string.UI_username_msg);
                    username_input_OK = false;
                } else if (!OtherUtils.checkUserName(username)) {
                    usernameErrorText.setText(R.string.UI_username_invalid_msg);
                    username_input_OK = false;
                } else {
                    sp.edit().putString(getString(R.string.USERNAME), username).apply();
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
    }

    private void setupEmailInput() {
        TextView emailText = new TextView(this);
        EditText emailInput = new EditText(this);
        TextView emailErrorText = new TextView(this);

        emailText.setText(R.string.UI_email_msg);
        emailText.setLayoutParams(layoutParams);

        emailInput.setText(sp.getString(getString(R.string.EMAIL), getString(R.string.UI_example_email)));
        emailInput.setLayoutParams(layoutParams);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setMaxLines(1);

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* not used*/
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* not used*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                if (OtherUtils.stringIsNullOrEmpty(email)) {
                    emailErrorText.setText(R.string.UI_prompt_for_valid_email);
                    email_input_OK = false;
                } else if (!OtherUtils.checkEmail(email)) {
                    emailErrorText.setText(R.string.UI_email_invalid_msg);
                    email_input_OK = false;
                } else {
                    sp.edit().putString(getString(R.string.EMAIL), email).apply();
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
    }

    private void onFinishClicked() {
        if (username_input_OK && email_input_OK) {
            boolean is_instructor = instructorCheckBox.isChecked();

            sp.edit().putInt(getString(R.string.USER_QUIZ_COUNT), 0).apply();
            sp.edit().putInt(getString(R.string.EXP), 0).apply();
            sp.edit().putBoolean(getString(R.string.IS_INSTRUCTOR), is_instructor).apply();


            //the user is newly created, we need to upload the credentials to the server
            String user_info = parseUserInfo(sp.getString(getString(R.string.USERNAME), ""),
                    sp.getString(getString(R.string.EMAIL), ""),
                    is_instructor);

            new Thread(() -> OtherUtils.uploadToServer(sp.getString(getString(R.string.UID), ""), getString(R.string.USER_INFO), user_info)).start();

            goToHomeActivity();
        } else {
            Toast.makeText(this, R.string.UI_require_valid_username_email_msg, Toast.LENGTH_LONG).show();
        }
    }


    private String parseUserInfo(String username, String email, boolean is_instructor) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty(getString(R.string.USERNAME), username);
            jsonObject.addProperty(getString(R.string.EMAIL), email);
            jsonObject.addProperty(getString(R.string.IS_INSTRUCTOR), is_instructor);
            jsonObject.addProperty(getString(R.string.USER_QUIZ_COUNT), 0);
            jsonObject.addProperty(getString(R.string.EXP), 0);
            jsonObject.addProperty(getString(R.string.PROFILE_IMG), sp.getString(getString(R.string.PROFILE_IMG), ""));
            jsonObject.addProperty(getString(R.string.FIREBASE_TOKEN), sp.getString(getString(R.string.FIREBASE_TOKEN), ""));
        } catch (Exception e) {
            Log.d("parse_credential", "failed");
        }
        return jsonObject.toString();
    }

    private void goToHomeActivity() {
        sp.edit().putBoolean(getString(R.string.LOGGED), true).apply();
        Intent intent = new Intent(InitActivity.this, HomeActivity.class);
        startActivity(intent);
    }

}