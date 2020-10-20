package com.cpen321.quizzical.MainMenuFragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.cpen321.quizzical.Utils.OtherUtils;

import java.io.IOException;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMG = 10;
    private static final int permission_code = 1;
    SharedPreferences sp;
    private Button logoutButton;
    private ImageButton profileImageButton;
    private ImageButton changeUsernameButton;
    private ImageButton changeEmailButton;
    private TextView usernameText;
    private TextView emailText;
    private TextView quizNumText;
    private TextView expText;
    private Uri imageUri;

    private boolean is_instructor;

    public static SharedPreferences.OnSharedPreferenceChangeListener quizNumAndExpChangeListener;

    public ProfileFragment() {

    }

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
        logoutButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_log_out_btn);
        logoutButton.setOnClickListener(v -> logOut());

        profileImageButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_pic);
        profileImageButton.setOnClickListener(v -> decideSetUpProfileImage());

        changeUsernameButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_username_change_btn);
        changeUsernameButton.setOnClickListener(v -> changeUsername());

        changeEmailButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_email_change_btn);
        changeEmailButton.setOnClickListener(v -> changeEmail());

        sp = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);

        is_instructor = sp.getBoolean(getString(R.string.IS_INSTRUCTOR), false);

        usernameText = getView().findViewById(R.id.profile_username);
        usernameText.setText(sp.getString(getString(R.string.USERNAME), getString(R.string.UI_username)));

        emailText = getView().findViewById(R.id.profile_email);
        emailText.setText(sp.getString(getString(R.string.EMAIL), getString(R.string.UI_example_email)));

        quizNumText = getView().findViewById(R.id.profile_quiz_num_text);
        if (is_instructor)
            quizNumText.setText(String.format(getString(R.string.UI_quiz_made), 0));
        else
            quizNumText.setText(String.format(getString(R.string.UI_quiz_taken),0));

        expText = getView().findViewById(R.id.profile_exp_text);
        expText.setText(String.format(getString(R.string.UI_total_exp), sp.getInt(getString(R.string.EXP), 0)));

        quizNumAndExpChangeListener = (sp, key)->updateQuizNumbersAndExpUI(key);

        sp.registerOnSharedPreferenceChangeListener(quizNumAndExpChangeListener);

        String encodedProfileImg = sp.getString(getString(R.string.PROFILE_IMG), "");
        if (!OtherUtils.stringIsNullOrEmpty(encodedProfileImg)) {
            Bitmap profileImg = OtherUtils.decodeImage(encodedProfileImg);
            profileImg = OtherUtils.scaleImage(profileImg);
            profileImageButton.setImageBitmap(profileImg);
        }
    }

    private void logOut() {
        sp.edit().putBoolean(getString(R.string.LOGGED), false).apply();

        //TODO: need to use server to get these info
        sp.edit().remove(getString(R.string.PROFILE_IMG)).apply();
        sp.edit().remove(getString(R.string.IS_INSTRUCTOR)).apply();
        sp.edit().remove(getString(R.string.USERNAME)).apply();
        sp.edit().remove(getString(R.string.EMAIL)).apply();
        sp.edit().remove(getString(R.string.CLASS_CODE)).apply();
        sp.edit().remove(getString(R.string.EXP)).apply();
        sp.edit().remove(getString(R.string.USER_QUIZ_COUNT)).apply();

        if (quizNumAndExpChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(quizNumAndExpChangeListener);
        if (StatisticFragment.classCodeChangeListener != null)
            sp.unregisterOnSharedPreferenceChangeListener(StatisticFragment.classCodeChangeListener);

        HomeActivity parentAct = (HomeActivity) getActivity();

        Intent i = new Intent(parentAct, InitActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        assert parentAct != null;
        ActivityCompat.finishAffinity(parentAct);

    }

    private void decideSetUpProfileImage() {
        new AlertDialog.Builder(this.getContext()).setTitle(R.string.PROFILE_IMG).setMessage(R.string.UI_change_profile_image_msg)
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
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), imageUri);

                String encoded = OtherUtils.encodeImage(bitmap);

                //the image doesn't change, no need to waste time on post or scaling
                if (encoded.equals(sp.getString(getString(R.string.PROFILE_IMG), ""))) {
                    return;
                }

                new Thread(() -> OtherUtils.uploadToServer(sp.getString(getString(R.string.UID), ""),
                        getString(R.string.PROFILE_IMG), encoded)).start();
                sp.edit().putString(getString(R.string.PROFILE_IMG), encoded).apply();

                //scale the image and make it round to fit into the image button.
                bitmap = OtherUtils.scaleImage(bitmap);
                profileImageButton.setImageBitmap(bitmap);


            } catch (IOException e) {
                Log.d("Image error", "bit map conversion error");
            }
        }
    }

    private void checkPermissions() {
        //we need read permission to get access to user's images in user's phone storage
        int readPermission = ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (!(readPermission == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, permission_code);
        } else {
            setUpProfileImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == permission_code) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpProfileImage();
                }
            }
        }
    }

    private void changeUsername() {

        Context thisContext = this.getContext();
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
                new Thread(() -> OtherUtils.uploadToServer(sp.getString(getString(R.string.UID), ""),
                        getString(R.string.USERNAME), newUsername)).start();
                sp.edit().putString(getString(R.string.USERNAME), newUsername).apply();
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.UI_username_invalid_msg);
            }
        });
    }

    private void changeEmail() {
        Context thisContext = this.getContext();
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
                new Thread(() -> OtherUtils.uploadToServer(sp.getString(getString(R.string.UID), ""),
                        getString(R.string.EMAIL), newEmail)).start();
                sp.edit().putString(getString(R.string.EMAIL), newEmail).apply();
                alertDialog.dismiss();
            } else {
                errorText.setText(R.string.UI_email_invalid_msg);
            }
        });
    }

    private void updateQuizNumbersAndExpUI(String key) {
        //TODO: same bug as in StatisticFragment
        if (getContext() == null)
            return;

        if (key.equals(getString(R.string.USER_QUIZ_COUNT)) || key.equals(getString(R.string.EXP))) {
            int newQuizNum = sp.getInt(getString(R.string.USER_QUIZ_COUNT), 0);
            if (is_instructor)
                getActivity().runOnUiThread(()->quizNumText.setText(String.format(getString(R.string.UI_quiz_made), newQuizNum)));
            else
                getActivity().runOnUiThread(()->quizNumText.setText(String.format(getString(R.string.UI_quiz_taken), newQuizNum)));

            int newEXP = sp.getInt(getString(R.string.EXP), 0);
            getActivity().runOnUiThread(()->expText.setText(String.format(getString(R.string.UI_total_exp), newEXP)));
        }
    }
}
