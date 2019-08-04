package com.maktabat.al.huda.customfonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;


public class MyTextView_Ubuntu_Italic extends android.support.v7.widget.AppCompatTextView {

    public MyTextView_Ubuntu_Italic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyTextView_Ubuntu_Italic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextView_Ubuntu_Italic(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ubuntu.italic.ttf");
            setTypeface(tf);
        }
    }

}