package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.example.jordoncheng.weiboclient_mvp.R;


public class AvatarImageView extends ImageTagView {

    public Bitmap srcBitmap;
    private int accentColor;
    Rect srcRect = new Rect();
    Rect dstRect = new Rect();
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PorterDuffXfermode mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    public AvatarImageView(Context context) {
        this(context, null);
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int[] colorAccentAttrs = {R.attr.colorAccent};
        final TypedArray a = context.obtainStyledAttributes(attrs, colorAccentAttrs, defStyleAttr, 0);
        accentColor = a.getColor(0, Color.WHITE);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (srcBitmap != null && getVisibility() == View.VISIBLE) {
            int width = getWidth();
            int height = getHeight();

            srcRect.right = srcBitmap.getWidth();
            srcRect.bottom = srcBitmap.getHeight();
            dstRect.right = width;
            dstRect.bottom = height;

            paint.setColor(accentColor);
            canvas.drawCircle(width/2, height/2,height/2 + 5, paint);
            int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
            canvas.drawCircle(width/2, height/2,height/2, paint);
            paint.setXfermode(mPorterDuffXfermode);
            canvas.drawBitmap(srcBitmap, srcRect, dstRect, paint);
            paint.setXfermode(null);
            canvas.restoreToCount(sc);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm != null) {
            srcBitmap = bm;
        } else srcBitmap = null;
        super.setImageBitmap(bm);
    }
}
