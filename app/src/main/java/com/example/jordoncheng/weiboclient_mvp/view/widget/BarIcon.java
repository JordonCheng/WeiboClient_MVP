package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.TintContextWrapper;
import android.util.AttributeSet;

/**
 * Created by Administrator on 3/21/2018.
 */

public class BarIcon extends android.support.v7.widget.AppCompatImageView {
    public int listItemContent;

    public BarIcon(Context context) {
        this(context, null);
    }

    public BarIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public BarIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap srcBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
        Rect srcRect = new Rect();
        srcRect.right = srcBitmap.getWidth();
        srcRect.bottom = srcBitmap.getHeight();
        Rect dstRect = new Rect();
        dstRect.right = getWidth();
        dstRect.bottom = getHeight();

        Matrix matrix = new Matrix();
        matrix.setScale((float)0.7, (float)0.7);
        Paint paint = new Paint();
        paint.setAlpha(128);

        canvas.setMatrix(matrix);
        super.onDraw(canvas);
    }
}
