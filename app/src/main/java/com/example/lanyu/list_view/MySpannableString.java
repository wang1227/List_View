package com.example.lanyu.list_view;

import android.text.SpannableString;

public class MySpannableString extends SpannableString {

    int tag = 0;

    public MySpannableString(CharSequence source) {
        super(source);
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
