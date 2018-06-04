package com.example.jordoncheng.weiboclient_mvp.model;

import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.ArrayList;

public interface UserDataSourse {

    interface LoadUserCallback {

        void onUserLoaded(User result);

        void onError(String error);
    }

    void loadUser(Recyclable recyclable, LoadUserCallback callback, long uid);

    void onDestroy(Recyclable recyclable);
}
