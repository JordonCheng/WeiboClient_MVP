package com.example.jordoncheng.weiboclient_mvp;

import android.app.Activity;

public class Utility {
    public static long getUid(Activity activity) {
        return Long.parseLong(activity.getSharedPreferences("com_weibo_sdk_android", Activity.MODE_PRIVATE).getString("uid", null));
    }
}
