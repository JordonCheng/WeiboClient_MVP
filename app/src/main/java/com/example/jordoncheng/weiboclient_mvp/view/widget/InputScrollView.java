package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class InputScrollView extends ScrollView {

    private onSizeChangedListener mOnSizeChangedListener;

    public InputScrollView(Context context) {
        super(context);
    }

    public InputScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InputScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InputScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void setSizeChangedListener(onSizeChangedListener sizeChangedListener) {
        this.mOnSizeChangedListener = sizeChangedListener;
    }

    public interface onSizeChangedListener {
        void onSizeChanged(int w, int h, int oldw, int oldh);
    }
}
