package com.cpen321.quizzical.Utils.ButtonWrappers;

import katex.hourglass.in.mathlib.MathView;

public class MathButtonWrapper implements IButtons {

    MathView view;

    public MathButtonWrapper(MathView view)
    {
        this.view = view;
    }
    @Override
    public ButtonTypes GetButtonType() {
        return ButtonTypes.Math;
    }

    @Override
    public IButtons GetButton() {
        return this;
    }

    @Override
    public void SetBackGroundColor(int color) {
        view.setViewBackgroundColor(color);
    }

    public MathView GetButtonAsMathButton()
    {
        return view;
    }
}
