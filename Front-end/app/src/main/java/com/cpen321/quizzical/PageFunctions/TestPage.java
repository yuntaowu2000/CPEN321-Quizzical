package com.cpen321.quizzical.PageFunctions;

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

import com.cpen321.quizzical.InitActivity;
import com.cpen321.quizzical.R;
import com.theartofdev.edmodo.cropper.CropImageView;

public class TestPage extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button picButton;
    private Button ContinueButton;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private Bitmap croppedBitmap;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_test);

        picButton = (Button)findViewById(R.id.test_page_take_photo_button);
        imageView = (ImageView)findViewById(R.id.test_image);
        linearLayout = (LinearLayout)findViewById(R.id.test_page_view);
        picButton.setOnClickListener(view -> TakePic());

        ContinueButton = (Button)findViewById(R.id.test_page_continue);
        ContinueButton.setOnClickListener(view -> OnContinueClicked());
    }

    private void TakePic()
    {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null)
        {
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
            imageView.setImageBitmap(imageBitmap);

            new AlertDialog.Builder(this).setTitle(R.string.modify_image).setMessage(R.string.modify_image_hint)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        CropSetup();
                    })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void CropSetup() {
        final CropImageView cropImageView = new CropImageView(this);
        cropImageView.setImageBitmap(imageBitmap);
        linearLayout.addView(cropImageView);
        cropImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));

        final Button cropButton = new Button(this);
        cropButton.setText(R.string.crop);
        cropButton.setOnClickListener(view -> {
            croppedBitmap = cropImageView.getCroppedImage();
            imageView.setImageBitmap(croppedBitmap);
        });
        linearLayout.addView(cropButton);

        final Button rotateButton = new Button(this);
        rotateButton.setText(R.string.rotate);
        rotateButton.setOnClickListener(view -> cropImageView.rotateImage(90));
        linearLayout.addView(rotateButton);

    }
    
    private void OnContinueClicked()
    {
        Intent i = new Intent(this, InitActivity.class);
        if (croppedBitmap != null)
        {
            i.putExtra(getString(R.string.image), croppedBitmap);
        } else {
            i.putExtra(getString(R.string.image), imageBitmap);
        }
        startActivity(i);
    }
}
