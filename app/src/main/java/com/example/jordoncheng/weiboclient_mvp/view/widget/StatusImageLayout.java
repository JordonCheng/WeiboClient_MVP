package com.example.jordoncheng.weiboclient_mvp.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;

//使用步骤：先调用setImageCount设置图片的数量，再调用getChild获得ImageView来设置图片
//设置图片必须调用setImageBitmap，不能使用setDrawable或者setImageUrl，否则图片宽高无法自适应

public class StatusImageLayout extends LinearLayout {

    private int imageCount;

    public StatusImageLayout(Context context) {
        this(context, null);
    }

    public StatusImageLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusImageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public StatusImageLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageCount(int count) {
        imageCount = count;
    }

    public int getImageCount() {
        return imageCount;
    }


    String path;
    File file;
    long size;
    public StatusImageView addImage(Uri uri) {
        int index = getImageCount();
        setImageCount(index + 1);
        StatusImageView imageView = (StatusImageView)getChildAt(index);
        try {
            imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageView;
    }


    public void removeImage(int index) {
        StatusImageView view = (StatusImageView)getChildAt(index);
        setImageCount(imageCount - 1);
        removeView(view);
        addView(view);
        view.setImageTag(0);
        view.setImageBitmap(null);
        view.setClickable(false);
        requestLayout();
        invalidate();
    }

    public StatusImageView replaceImage(int index, Uri newImageUri) {
        StatusImageView imageView = (StatusImageView)getChildAt(index);
        try {
            imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), newImageUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childCount = getChildCount();
        int imageCount = getImageCount();
        int childL;
        int childT;
        int childR;
        int childB;

        if(imageCount == 0) return;

        if(imageCount == 1) {
            View child = getChildAt(0);
            child.layout(0, 0, r - l,  b - t );
            for(int c = imageCount - childCount, childNum = 1; childNum < c ; childNum++) {
                getChildAt(childNum).layout(0,0,0,0);
            }
            return;
        }

        int k;
        if(imageCount == 2 || imageCount == 4)
            k = 2;
        else k = 3;
        int distance = (r - l)/k;
        for(int i = 0,ii=k - 1, childNum = 0; i < k; i++, ii--) {
            for(int j = 0, ji=k - 1; j < k; j++, ji--) {
                childL = distance * j;
                childT = distance * i;
                childR = r - l - (ji * distance);
                childB = (k - ii) * distance;
                getChildAt(childNum).layout(childL, childT, childR, childB);
                if ( ++childNum == imageCount) {
                    while (childNum < childCount) getChildAt(childNum++).layout(0,0,0,0);
                    return;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int imageCount = getImageCount();

        if(imageCount > 0 ) {
            if (imageCount == 1) {
                ImageView child = (ImageView)getChildAt(0);
                if (child.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable)child.getDrawable()).getBitmap();
                    if (bitmap != null) {
                        int oriWidth = bitmap.getWidth();
                        int oriHeight = bitmap.getHeight();
                        if (oriWidth > oriHeight) {
                            int width =  MeasureSpec.getSize(widthMeasureSpec);
                            int height = oriHeight * width / oriWidth;
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                        }
                    }
                } else heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
            } else if (imageCount == 2) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) / 2, MeasureSpec.EXACTLY);
            } else if (imageCount == 3) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) / 3, MeasureSpec.EXACTLY);
            } else if (imageCount == 5 || imageCount == 6) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec) / 3) * 2, MeasureSpec.EXACTLY);
            } else {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
