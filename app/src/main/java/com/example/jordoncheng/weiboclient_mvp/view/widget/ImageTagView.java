package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class ImageTagView extends AppCompatImageView {

    private int imageTag;

    public ImageTagView(Context context) {
        this(context, null);
    }

    public ImageTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public final void setImageTag(int tag) {
        imageTag = tag;
    }

    public final int getImageTag() {
        return imageTag;
    }
}
