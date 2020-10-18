package com.cpen321.quizzical.PageFunctions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cpen321.quizzical.Data.CourseCategory;
import com.cpen321.quizzical.Data.Questions.QuestionsMC;
import com.cpen321.quizzical.InitActivity;
import com.cpen321.quizzical.R;
import com.cpen321.quizzical.Utils.ChoicePair;
import com.cpen321.quizzical.Utils.OtherUtils;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class TestPage extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    LinearLayout linearLayout;
    private Button picButton;
    private Button ContinueButton;
    private Button uploadButton;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private Bitmap croppedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_test);

        picButton = findViewById(R.id.test_page_take_photo_button);
        imageView = findViewById(R.id.test_image);
        linearLayout = findViewById(R.id.test_page_view);
        picButton.setOnClickListener(view -> takePic());

        ContinueButton = findViewById(R.id.test_page_continue);
        ContinueButton.setOnClickListener(view -> onContinueClicked());

        uploadButton = findViewById(R.id.test_upload_button);
        uploadButton.setOnClickListener(view -> testUpload());

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
            imageView.setImageBitmap(imageBitmap);

            new AlertDialog.Builder(this).setTitle(R.string.modify_image).setMessage(R.string.modify_image_hint)
                    .setPositiveButton(R.string.YES, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        cropSetup();
                    })
                    .setNegativeButton(R.string.NO, (dialogInterface, i) -> dialogInterface.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void cropSetup() {
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

    private void onContinueClicked() {
        Intent i = new Intent(this, InitActivity.class);
        if (croppedBitmap != null) {
            i.putExtra(getString(R.string.image), croppedBitmap);
        } else {
            i.putExtra(getString(R.string.image), imageBitmap);
        }
        startActivity(i);
    }

    private void testUpload() {
        List<ChoicePair> choicePairList = new ArrayList<>();

        choicePairList.add(new ChoicePair(false, "<p align=\"middle\">2</p>"));
        choicePairList.add(new ChoicePair(true, "https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png"));
        choicePairList.add(new ChoicePair(false, "<p>https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png</p>"));
        choicePairList.add(new ChoicePair(false, "$$ c = \\sqrt{a^2 + b^2} $$"));

        QuestionsMC testQ = new QuestionsMC(CourseCategory.Math, "calculate: $$1+1=$$", false, "", choicePairList, 1);
        String q = testQ.toJsonString();
        Log.d("question", q);

        new Thread(() -> OtherUtils.uploadToServer(getString(R.string.USERNAME), getString(R.string.question),q)).start();
    }
}
