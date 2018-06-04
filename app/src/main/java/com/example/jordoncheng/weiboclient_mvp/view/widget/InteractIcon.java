package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.TintContextWrapper;
import android.util.AttributeSet;

import com.example.jordoncheng.weiboclient_mvp.R;

/**
 * Created by Administrator on 3/17/2018.
 */

public class InteractIcon extends android.support.v7.widget.AppCompatImageView {

    public int listItemContent;

    public InteractIcon(Context context) {
        this(context, null);
    }

    public InteractIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public InteractIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);
        int[] listItemContentAttrs = {R.attr.listItemContent};
        final TypedArray a = context.obtainStyledAttributes(attrs, listItemContentAttrs, defStyleAttr, 0);
        listItemContent = a.getColor(0, Color.WHITE);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = ((BitmapDrawable)getDrawable()).getBitmap();

        Rect src = new Rect();
        src.right = bitmap.getWidth();
        src.bottom = bitmap.getHeight();
        Rect dst = new Rect();
        int width = getWidth();
        int height = getHeight();
        dst.right = width;
        dst.bottom = height;

        Paint paint = new Paint();
        paint.setColor(listItemContent);

        int sc = canvas.saveLayer(0, 0, width, width, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(bitmap,src,dst,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(dst, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(sc);
    }
}
