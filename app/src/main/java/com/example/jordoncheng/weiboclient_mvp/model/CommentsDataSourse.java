package com.example.jordoncheng.weiboclient_mvp.model;

import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.ArrayList;

public interface CommentsDataSourse {

    interface LoadCommentsCallback {

        void onCommentsLoaded(ArrayList<Comment> result);

        void onError(String error);
    }

    void loadComments(Recyclable recyclable, LoadCommentsCallback callback, long mid, long maxId);

    void loadComments(Recyclable recyclable, LoadCommentsCallback callback, long mid);

    void onDestroy(Recyclable recyclable);
}
