package com.maktabat.al.huda.customfonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;


public class Edittext_Ubuntu_Medium extends EditText {

    public Edittext_Ubuntu_Medium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Edittext_Ubuntu_Medium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Edittext_Ubuntu_Medium(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ubuntu.medium.ttf");
            setTypeface(tf);
        }
    }

}