package com.cpen321.quizzical.utils.buttonwrappers;

import android.widget.Button;

public class TextButtonWrapper implements IButtons {

    private Button button;

    public TextButtonWrapper(Button button) {
        this.button = button;
    }

    @Override
    public ButtonTypes getButtonType() {
        return ButtonTypes.Text;
    }

    @Override
    public IButtons getButton() {
        return this;
    }

    @Override
    public void myButtonSetBackGroundColor(int color) {
        button.setBackgroundColor(color);
    }

    @Override
    public void setButtonId(int id) {
        button.setId(id);
    }

    public Button getButtonAsTextButton() {
        return this.button;
    }
}
