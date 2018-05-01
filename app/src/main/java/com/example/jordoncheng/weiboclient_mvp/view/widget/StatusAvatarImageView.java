package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


public class StatusAvatarImageView extends ImageTagView {

    public Bitmap srcBitmap;
    private Rect srcRect = new Rect();
    private Rect dstRect = new Rect();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PorterDuffXfermode mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    static int i = 0;
    static int j = 0;
    static int k = 0;

    public StatusAvatarImageView(Context context) {
        this(context, null);
    }

    public StatusAvatarImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusAvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

            paint.setColor(Color.BLACK);

            int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
            canvas.drawCircle(width/2, height/2,height/2, paint);
            paint.setXfermode(mPorterDuffXfermode);
            canvas.drawBitmap(srcBitmap, srcRect, dstRect, paint);
            paint.setXfermode(null);
            canvas.restoreToCount(sc);

            Log.d("AvatarImage:onDraw绘制", String.valueOf(++i));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d("AvatarImage:onLayout布局", String.valueOf(++j));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("AvatarImage:onMeasure测量", String.valueOf(++k));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm != null) {
            srcBitmap = bm;
        } else srcBitmap = null;
        super.setImageBitmap(bm);
    }
}
