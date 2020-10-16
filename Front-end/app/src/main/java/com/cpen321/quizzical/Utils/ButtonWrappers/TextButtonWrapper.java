package com.cpen321.quizzical.Utils.ButtonWrappers;

import android.widget.Button;

public class TextButtonWrapper implements IButtons {
    private Button button;

    public TextButtonWrapper(Button button) {
        this.button = button;
    }

    @Override
    public ButtonTypes GetButtonType() {
        return ButtonTypes.Text;
    }

    @Override
    public IButtons GetButton() {
        return this;
    }

    @Override
    public void SetBackGroundColor(int color) {
        button.setBackgroundColor(color);
    }

    public Button GetButtonAsTextButton() {
        return this.button;
    }
}
