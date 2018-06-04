package com.example.jordoncheng.weiboclient_mvp.view;

import android.animation.ValueAnimator;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.jordoncheng.weiboclient_mvp.Injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class BaseActivity extends AppCompatActivity {

    private float transparency1;
    private float transparency2;
    private float transparency3;
    private float transparency4;

    //Activity变暗模式
    private @DimMode int dimMode;
    //打开变暗，返回恢复
    public static final int DIMMODE_1 = 1;
    //打开变暗，返回不变
    public static final int DIMMODE_2 = 2;
    //打开不变，返回不变
    public static final int DIMMODE_3 = 3;

    @IntDef({ DIMMODE_1, DIMMODE_2, DIMMODE_3 })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DimMode {}

    protected void setTransparentBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            /*window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    //设置打开其他Activity时及关闭这个打开的Activity时自身Activity变暗
    private void dimBackground(final float from, final float to) {
        if(from != 0 && to != 0) {
            final Window window = getWindow();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
            valueAnimator.setDuration(300);
            valueAnimator.addUpdateListener(animation -> {
                WindowManager.LayoutParams params = window.getAttributes();
                params.alpha = (Float) animation.getAnimatedValue();
                window.setAttributes(params);
            });

            valueAnimator.start();
        }
    }

    protected abstract @DimMode int onSetDimMode();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDimMode(onSetDimMode());
        Injection.setAppContext((Application)getApplicationContext());
    }

    private void setDimMode(@DimMode int dimMode) {
        switch (dimMode) {
            case DIMMODE_1 : transparency1 = transparency4 = 0.3f; transparency2 = transparency3 = 1.0f;
            break;
            case DIMMODE_2 : transparency1 = 0.3f; transparency2 = 1.0f; transparency3 = transparency4 = 0;
            break;
            case DIMMODE_3 : transparency1 = transparency2 = transparency3 = transparency4 = 0;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dimBackground(transparency1, transparency2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dimBackground(transparency3, transparency4);
    }
}
