package com.cpen321.quizzical;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

public class PictureActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RETURN_CODE = 2;
    private static final int CHOICE_RETURN_CODE = 3;
    private int currRequestNum;
    private int questionNum;
    private int choiceNum;
    private LinearLayout linearLayout;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private Bitmap croppedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        Button picButton = findViewById(R.id.test_page_take_photo_button);
        imageView = findViewById(R.id.test_image);
        linearLayout = findViewById(R.id.test_page_view);
        picButton.setOnClickListener(view -> takePic());

        Button continueButton = findViewById(R.id.test_page_continue);
        continueButton.setOnClickListener(view -> onContinueClicked());

        questionNum = getIntent().getIntExtra(getString(R.string.QUESTION_NUM), 0);
        imageBitmap = (Bitmap) Objects.requireNonNull(getIntent().getExtras()).get(getString(R.string.ORIGINAL_IMG));
        if (imageBitmap != null) {
            askForModification();
        }

        currRequestNum = getIntent().getIntExtra(getString(R.string.REQUEST_CODE), RETURN_CODE);
        if (currRequestNum == CHOICE_RETURN_CODE) {
            choiceNum = getIntent().getIntExtra(getString(R.string.CHOICE_NUM), 0);
        }
    }

    private void takePic() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            assert extras != null;

            imageBitmap = (Bitmap) extras.get("data");
            askForModification();
        }
    }

    private void askForModification() {
        imageView.setImageBitmap(imageBitmap);

        new AlertDialog.Builder(this).setTitle(R.string.UI_modify_image_title).setMessage(R.string.UI_modify_image_msg)
                .setPositiveButton(R.string.YES, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    cropSetup();
                })
                .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void cropSetup() {
        final CropImageView cropImageView = new CropImageView(this);
        cropImageView.setImageBitmap(imageBitmap);
        linearLayout.addView(cropImageView);
        cropImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

        final Button cropButton = new Button(this);
        cropButton.setText(R.string.UI_crop);
        cropButton.setOnClickListener(view -> {
            croppedBitmap = cropImageView.getCroppedImage();
            imageView.setImageBitmap(croppedBitmap);
        });
        linearLayout.addView(cropButton);

        final Button rotateButton = new Button(this);
        rotateButton.setText(R.string.UI_rotate);
        rotateButton.setOnClickListener(view -> {
            cropImageView.rotateImage(90);
            croppedBitmap = cropImageView.getCroppedImage();
            imageView.setImageBitmap(croppedBitmap);
        });
        linearLayout.addView(rotateButton);

    }

    private void onContinueClicked() {
        Intent i = new Intent();
        if (croppedBitmap != null) {
            i.putExtra(getString(R.string.MODIFIED_IMG), croppedBitmap);
        } else {
            i.putExtra(getString(R.string.MODIFIED_IMG), imageBitmap);
        }
        i.putExtra(getString(R.string.ORIGINAL_IMG), imageBitmap);
        i.putExtra(getString(R.string.QUESTION_NUM), questionNum);

        if (currRequestNum == RETURN_CODE) {
            setResult(RETURN_CODE, i);
        } else {
            i.putExtra(getString(R.string.CHOICE_NUM), choiceNum);
            setResult(CHOICE_RETURN_CODE, i);
        }

        finish();
    }
}
