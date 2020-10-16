package com.cpen321.quizzical.Utils.ButtonWrappers;

import android.widget.ImageButton;

public class ImageButtonWrapper implements IButtons {

    private ImageButton imageButton;

    public ImageButtonWrapper(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    @Override
    public ButtonTypes GetButtonType() {
        return ButtonTypes.Image;
    }

    @Override
    public IButtons GetButton() {
        return this;
    }

    @Override
    public void SetBackGroundColor(int color) {
        imageButton.setBackgroundColor(color);
    }

    public ImageButton GetButtonAsImageButton() {
        return imageButton;
    }
}
