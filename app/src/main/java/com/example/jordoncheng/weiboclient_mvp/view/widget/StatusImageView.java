package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


public class StatusImageView extends ImageTagView {

    private Bitmap srcBitmap;
    private Rect srcRect;
    private Rect dstRect;
    private Paint paint = new Paint();

    public StatusImageView(Context context) {
        this(context, null);
    }

    public StatusImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (((StatusImageLayout)getParent()).getImageCount() == 1 && measureWidth > measureHeight) setMeasuredDimension(measureWidth, measureHeight);
        else setMeasuredDimension(measureWidth, measureWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (srcBitmap != null && getVisibility() == View.VISIBLE && getMeasuredWidth() == getMeasuredHeight()) {
            dstRect.right = dstRect.bottom = getWidth();
            dstRect.bottom = getHeight();
            canvas.drawBitmap(srcBitmap, srcRect, dstRect, paint);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        //对微博图片进行裁剪
        if(bm != null) {
            srcRect = new Rect();
            srcBitmap = bm;
            int srcWidth = bm.getWidth();
            int srcHeight = bm.getHeight();
            int squareLength = Math.min(srcWidth, srcHeight);
            srcRect.left = (srcWidth - squareLength) / 2;
            srcRect.top = (srcHeight - squareLength) / 2;
            srcRect.right = srcRect.left + squareLength;
            srcRect.bottom = srcRect.top + squareLength;

            dstRect = new Rect();
        } else srcBitmap = null;
        super.setImageBitmap(bm);
    }

    public Bitmap getBitmap() {
        return srcBitmap;
    }
}
