package com.cpen321.quizzical.mainmenufragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cpen321.quizzical.HomeActivity;
import com.cpen321.quizzical.InitActivity;
import com.cpen321.quizzical.R;
import com.cpen321.quizzical.data.Classes;
import com.cpen321.quizzical.utils.OtherUtils;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMG = 10;
    private static final int PERMISSION_CODE = 1;
    public static SharedPreferences.OnSharedPreferenceChangeListener profileFragmentOnSPChangeListener;
    private SharedPreferences sp;
    private ImageButton profileImageButton;
    private TextView usernameText;
    private TextView emailText;
    private TextView quizNumText;
    private TextView expText;
    private Spinner pushNotificationSpinner;
    private ArrayList<Classes> classList;
    private LinearLayout profileClassLayout;
    private boolean isInstructor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button logoutButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_log_out_btn);
        logoutButton.setOnClickListener(v -> logOut());

        profileImageButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_pic);
        profileImageButton.setOnClickListener(v -> decideSetUpProfileImage());

        ImageButton changeUsernameButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_username_change_btn);
        changeUsernameButton.setOnClickListener(v -> changeUsername());

        ImageButton changeEmailButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_email_change_btn);
        changeEmailButton.setOnClickListener(v -> changeEmail());

        sp = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);

        isInstructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        usernameText = Objects.requireNonNull(getView()).findViewById(R.id.profile_username);
        usernameText.setText(sp.getString(getString(R.string.USERNAME), getString(R.string.UI_username)));

        emailText = Objects.requireNonNull(getView()).findViewById(R.id.profile_email);
        emailText.setText(sp.getString(getString(R.string.EMAIL), getString(R.string.UI_example_email)));

        pushNotificationSpinner = Objects.requireNonNull(getView()).findViewById(R.id.notification_settings);
        String[] notificationFrequencyArray = getResources().getStringArray(R.array.notification_frequency);
        ArrayAdapter<String> pushNotificationArrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                notificationFrequencyArray);
        pushNotificationSpinner.setAdapter(pushNotificationArrayAdapter);
        getNotificationFrequency();

        pushNotificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setNotificationFrequency(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                getNotificationFrequency();
            }
        });

        profileClassLayout = getView().findViewById(R.id.profile_class_layout);
        parseClassListFromString();
        setupClassLayout();

        quizNumText = getView().findViewById(R.id.profile_quiz_num_text);
        if (isInstructor)
            quizNumText.setText(String.format(getString(R.string.UI_quiz_made), sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0)));
        else
            quizNumText.setText(String.format(getString(R.string.UI_quiz_taken), sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0)));

        expText = getView().findViewById(R.id.profile_exp_text);
        expText.setText(String.format(getString(R.string.UI_total_exp), sp.getInt(getString(R.string.EXP), 0)));

        profileFragmentOnSPChangeListener = (sp, key) -> onProfileFragmentSPChange(key);

        sp.registerOnSharedPreferenceChangeListener(profileFragmentOnSPChangeListener);

        new Thread(() -> {

            Bitmap profileImg = null;

            if (sp.getBoolean(getString(R.string.PROFILE_IMG_UP_TO_DATE), false)) {
                //the local cache is up to date try getting the profile image from local cache
                String encodedProfileImg = sp.getString(getString(R.string.PROFILE_IMG), "");
                if (!OtherUtils.stringIsNullOrEmpty(encodedProfileImg)) {
                    profileImg = OtherUtils.decodeImage(encodedProfileImg);
                }
            } else {
                //try getting the profile image from the server,
                // this part will now be called only when user login after logged out or first log in
                String url = getString(R.string.GET_URL) + getString(R.string.PROFILE_ENDPOINT)
                        + "?" + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "")
                        + "&" + getString(R.string.TYPE) + getString(R.string.PROFILE_IMG);
                String img = OtherUtils.readFromURL(url);
                Log.d("server_response", "img: " + img);
                if (!OtherUtils.stringIsNullOrEmpty(img)) {
                    //we have the value from the server, then we can just use the value
                    sp.edit().putString(getString(R.string.PROFILE_IMG), img).apply();
                    sp.edit().putBoolean(getString(R.string.PROFILE_IMG_UP_TO_DATE), true).apply();
                    profileImg = OtherUtils.decodeImage(img);
                } else {
                    //safe guard if we cannot get anything from the server,
                    //because we are sure that the front end will cache a default profile image
                    String encodedProfileImg = sp.getString(getString(R.string.PROFILE_IMG), "");
                    if (!OtherUtils.stringIsNullOrEmpty(encodedProfileImg)) {
                        profileImg = OtherUtils.decodeImage(encodedProfileImg);
                    }
                }
            }

            if (profileImg != null) {
                profileImg = OtherUtils.scaleImage(profileImg);
                final Bitmap finalBitmap = profileImg;
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> profileImageButton.setImageBitmap(finalBitmap));
            }
        }).start();
    }

    private String parseNotificationInfo(int freq, String firebase_token) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty(getString(R.string.NOTIFICATION_FREQ), freq);
            jsonObject.addProperty(getString(R.string.FIREBASE_TOKEN), firebase_token);
        } catch (Exception e) {
            Log.d("parse_notification_info", "failed");
        }
        return jsonObject.toString();
    }

    private void setNotificationFrequency(int i) {
        sp.edit().putInt(getString(R.string.NOTIFICATION_FREQ), i).apply();
        String jsonRepresentation = parseNotificationInfo(i, sp.getString(getString(R.string.FIREBASE_TOKEN), ""));
        new Thread(() -> OtherUtils.uploadToServer(
                getString(R.string.NOTIFICATION_ENDPOINT),
                sp.getString(getString(R.string.UID), ""),
                getString(R.string.NOTIFICATION_FREQ), jsonRepresentation)).start();
    }

    private void getNotificationFrequency() {
        //set to default weekly notification or previously set frequency
        int defaultNotificationFreq = sp.getInt(getString(R.string.NOTIFICATION_FREQ), 2);
        if (defaultNotificationFreq == 2) {
            //try get the value from the sever, if none, still keep it 2
            new Thread(() -> {
                String url = getString(R.string.GET_URL) + "users" + getString(R.string.NOTIFICATION_ENDPOINT)
                        + "?" + getString(R.string.UID) + "=" + sp.getString(getString(R.string.UID), "")
                        + "&" + getString(R.string.TYPE) + getString(R.string.NOTIFICATION_FREQ);
                String server_response = OtherUtils.readFromURL(url);
                Log.d("server_response", "notification freq: " + server_response);
                try {
                    int new_freq = Integer.parseInt(server_response);
                    sp.edit().putInt(getString(R.string.NOTIFICATION_FREQ), new_freq).apply();
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> pushNotificationSpinner.setSelection(new_freq));
                } catch (NumberFormatException e) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> pushNotificationSpinner.setSelection(2));
                    String jsonRepresentation = parseNotificationInfo(defaultNotificationFreq,
                            sp.getString(getString(R.string.FIREBASE_TOKEN), ""));
                    OtherUtils.uploadToServer(
                            getString(R.string.NOTIFICATION_ENDPOINT),
                            sp.getString(getString(R.string.UID), ""),
                            getString(R.string.NOTIFICATION_FREQ),
                            jsonRepresentation);
                }
            }).start();
        } else {
            pushNotificationSpinner.setSelection(defaultNotificationFreq);
        }
    }

    private void logOut() {

        sp.edit().clear().apply();

        if (profileFragmentOnSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(profileFragmentOnSPChangeListener);
        if (StatisticFragment.statisticFragmentOnSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(StatisticFragment.statisticFragmentOnSPChangeListener);
        if (QuizFragment.quizFragmentSPChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(QuizFragment.quizFragmentSPChangeListener);

        HomeActivity parentAct = (HomeActivity) getActivity();

        Intent i = new Intent(parentAct, InitActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        assert parentAct != null;
        ActivityCompat.finishAffinity(parentAct);

    }

    private void decideSetUpProfileImage() {
        new AlertDialog.Builder(Objects.requireNonNull(this.getContext()))
                .setTitle(R.string.PROFILE_IMG).
                setMessage(R.string.UI_change_profile_image_msg)
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    checkPermissions();
                })
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setUpProfileImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMG && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(this.getContext()).getContentResolver(), imageUri);

                String encoded = OtherUtils.encodeImage(bitmap);

                //the image doesn't change, no need to waste time on post or scaling
                if (encoded.equals(sp.getString(getString(R.string.PROFILE_IMG), ""))) {
                    return;
                }

                new Thread(() -> OtherUtils.uploadToServer(
                        getString(R.string.PROFILE_IMAGE_ENDPOINT),
                        sp.getString(getString(R.string.UID), ""),
                        getString(R.string.PROFILE_IMG),
                        encoded)).start();
                sp.edit().putString(getString(R.string.PROFILE_IMG), encoded).apply();

                //scale the image and make it round to fit into the image button.
                bitmap = OtherUtils.scaleImage(bitmap);
                profileImageButton.setImageBitmap(bitmap);


            } catch (IOException e) {
                Log.d("Image_error", "bit map conversion error");
            }
        }
    }

    private void checkPermissions() {
        //we need read permission to get access to user's images in user's phone storage
        int readPermission = ContextCompat.checkSelfPermission(Objects.requireNonNull(this.getContext()), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            setUpProfileImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setUpProfileImage();
        }
    }

    private void changeUsername() {

        Context thisContext = this.getContext();
        assert thisContext != null;
        LinearLayout layout = new LinearLayout(thisContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText editText = new EditText(thisContext);
        editText.setHint(R.string.UI_example_username);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        TextView errorText = new TextView(thisContext);
        errorText.setText("");
        errorText.setTextColor(getResources().getColor(R.color.colorCrimson));

        layout.addView(editText);
        layout.addView(errorText);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(thisContext).setTitle(R.string.UI_username_msg)
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> {
                }));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            String newUsername = editText.getText().toString();

            if (newUsername.equals(sp.getString(getString(R.string.USERNAME), ""))) {
                //check username will help screen out the invalid "" username
                //the new username is the same as the previous one, no need to change
                alertDialog.dismiss();
                return;
            }
            if (OtherUtils.checkUserName(newUsername)) {

                usernameText.setText(newUsername);
                new Thread(() -> OtherUtils.uploadToServer(
                        getString(R.string.USER_ENDPOINT),
                        sp.getString(getString(R.string.UID), ""),
                        getString(R.string.USERNAME),
                        newUsername)).start();
                sp.edit().putString(getString(R.string.USERNAME), newUsername).apply();
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.UI_username_invalid_msg);
            }
        });
    }

    private void changeEmail() {
        Context thisContext = this.getContext();
        assert thisContext != null;
        LinearLayout layout = new LinearLayout(thisContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText editText = new EditText(thisContext);
        editText.setHint(R.string.UI_example_email);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        TextView errorText = new TextView(thisContext);
        errorText.setText("");
        errorText.setTextColor(getResources().getColor(R.color.colorCrimson));

        layout.addView(editText);
        layout.addView(errorText);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(thisContext).setTitle(R.string.UI_prompt_for_valid_email)
                .setView(layout)
                .setPositiveButton(R.string.UI_submit, ((dialogInterface, i) -> {
                }));

        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            String newEmail = editText.getText().toString();

            if (newEmail.equals(sp.getString(getString(R.string.EMAIL), ""))) {
                //check email will help screen out the invalid "" email
                //the new email is the same as the previous one, no need to change
                alertDialog.dismiss();
                return;
            }
            if (OtherUtils.checkEmail(newEmail)) {

                emailText.setText(newEmail);
                new Thread(() -> OtherUtils.uploadToServer(
                        getString(R.string.USER_ENDPOINT),
                        sp.getString(getString(R.string.UID), ""),
                        getString(R.string.EMAIL),
                        newEmail)).start();
                sp.edit().putString(getString(R.string.EMAIL), newEmail).apply();
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.UI_email_invalid_msg);
            }
        });
    }

    private void onProfileFragmentSPChange(String key) {
        if (getContext() == null)
            return;

        if (key.equals(getString(R.string.USER_QUIZ_COUNT)) || key.equals(getString(R.string.EXP))) {
            if (isInstructor)
                Objects.requireNonNull(getActivity()).runOnUiThread(
                        () -> quizNumText.setText(String.format(getString(R.string.UI_quiz_made), sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0)))
                );
            else
                Objects.requireNonNull(getActivity()).runOnUiThread(
                        () -> quizNumText.setText(String.format(getString(R.string.UI_quiz_taken), sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0)))
                );

            Objects.requireNonNull(getActivity()).runOnUiThread(
                    () -> expText.setText(String.format(getString(R.string.UI_total_exp), sp.getInt(getString(R.string.EXP), 0)))
            );
        } else if (key.equals(getString(R.string.CLASS_LIST))) {
            parseClassListFromString();
            setupClassLayout();
        }
    }

    private void parseClassListFromString() {
        classList = new ArrayList<>();
        String classListString = sp.getString(getString(R.string.CLASS_LIST), "");

        if (OtherUtils.stringIsNullOrEmpty(classListString)) {
            return;
        }

        try {
            String[] classes = classListString.split(";");
            for (String c : classes) {
                classList.add(new Classes(c));
            }
        } catch (Exception e) {
            Log.d("parse", "cannot parse class list");
        }
    }

    private void setupClassLayout() {
        Context thisContext = this.getContext();
        assert thisContext != null;

        profileClassLayout.removeAllViews();

        for (Classes c : classList) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(10, 10, 10, 10);
            TextView textView = new TextView(thisContext);
            textView.setText(c.getClassName());
            textView.setBackground(getResources().getDrawable(R.drawable.textview_rect_border));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(layoutParams);

            profileClassLayout.addView(textView);
        }
    }
}
