package com.cpen321.quizzical.utils.buttonwrappers;

import android.widget.ImageButton;

public class ImageButtonWrapper implements IButtons {

    private ImageButton imageButton;

    public ImageButtonWrapper(ImageButton imageButton) {
        this.imageButton = imageButton;
    }

    @Override
    public ButtonTypes getButtonType() {
        return ButtonTypes.Image;
    }

    @Override
    public IButtons getButton() {
        return this;
    }

    @Override
    public void myButtonSetBackGroundColor(int color) {
        imageButton.setBackgroundColor(color);
    }

    @Override
    public void setButtonId(int id) {
        imageButton.setId(id);
    }

    public ImageButton getButtonAsImageButton() {
        return imageButton;
    }
}
