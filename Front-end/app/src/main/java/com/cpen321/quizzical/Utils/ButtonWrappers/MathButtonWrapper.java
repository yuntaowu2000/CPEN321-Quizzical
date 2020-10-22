package com.cpen321.quizzical.utils.ButtonWrappers;

import katex.hourglass.in.mathlib.MathView;

public class MathButtonWrapper implements IButtons {

    private MathView view;

    public MathButtonWrapper(MathView view) {
        this.view = view;
    }

    @Override
    public ButtonTypes getButtonType() {
        return ButtonTypes.Math;
    }

    @Override
    public IButtons getButton() {
        return this;
    }

    @Override
    public void myButtonSetBackGroundColor(int color) {
        view.setViewBackgroundColor(color);
    }

    public MathView getButtonAsMathButton() {
        return view;
    }
}
