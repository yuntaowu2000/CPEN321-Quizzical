package com.cpen321.quizzical.MainMenuFragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
    private static final int permissioncode = 1;
    SharedPreferences sp;
    private Button logoutButton;
    private ImageButton imageButton;
    private Uri imageUri;

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

        imageButton = Objects.requireNonNull(getView()).findViewById(R.id.profile_pic);
        imageButton.setOnClickListener(v -> decideSetUpProfileImage());

        sp = Objects.requireNonNull(getActivity()).getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);

        TextView usernameText = getView().findViewById(R.id.profile_username);
        usernameText.setText(sp.getString(getString(R.string.USERNAME), getString(R.string.USERNAME)));

        TextView emailText = getView().findViewById(R.id.profile_email);
        emailText.setText(sp.getString(getString(R.string.Email), getString(R.string.EXAMPLE_EMAIL)));

        String encodedProfileImg = sp.getString(getString(R.string.Profile_Image), "");
        if (!OtherUtils.StringIsNullOrEmpty(encodedProfileImg)) {
            Bitmap profileImg = OtherUtils.decodeImage(encodedProfileImg);
            profileImg = OtherUtils.scaleImage(profileImg);
            imageButton.setImageBitmap(profileImg);
        }
    }

    private void logOut() {
        sp.edit().putBoolean(getString(R.string.LOGGED), false).apply();

        //need to use server to get these info
        sp.edit().remove(getString(R.string.Profile_Image)).apply();
        sp.edit().remove(getString(R.string.IS_INSTRUCTOR)).apply();
        sp.edit().remove(getString(R.string.USERNAME)).apply();
        sp.edit().remove(getString(R.string.Email)).apply();
        sp.edit().remove(getString(R.string.course_category)).apply();
        sp.edit().remove(getString(R.string.class_code)).apply();

        HomeActivity parentAct = (HomeActivity) getActivity();

        Intent i = new Intent(parentAct, InitActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        assert parentAct != null;
        ActivityCompat.finishAffinity(parentAct);

    }

    private void decideSetUpProfileImage() {
        new AlertDialog.Builder(this.getContext()).setTitle(R.string.Profile_Image).setMessage(R.string.Change_Profile_Image_msg)
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

                //used for saving the bitmap for future logins
                Bitmap finalBitmap = bitmap;
                new Thread(() -> OtherUtils.uploadBitmapToServer(finalBitmap)).start();

                String encoded = OtherUtils.encodeImage(bitmap);
                sp.edit().putString(getString(R.string.Profile_Image), encoded).apply();

                //scale the image and make it round to fit into the image button.
                bitmap = OtherUtils.scaleImage(bitmap);
                imageButton.setImageBitmap(bitmap);


            } catch (IOException e) {
                Log.d("Image error", "bit map conversion error");
            }
        }
    }

    private void checkPermissions() {
        //we need read permission to get access to user's images in user's phone storage
        int readPermission = ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (!(readPermission == PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, permissioncode);
        } else {
            setUpProfileImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == permissioncode) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpProfileImage();
                }
            }
        }
    }
}
