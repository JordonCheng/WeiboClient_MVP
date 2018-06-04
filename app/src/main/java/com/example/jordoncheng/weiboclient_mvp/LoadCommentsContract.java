package com.example.jordoncheng.weiboclient_mvp;

import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

public interface LoadCommentsContract {

    interface Presenter extends Recyclable {

        void getMoreComments();

        void getCommentsRefresh();

        void onViewDestroy();
    }

    interface View {

        void createLoadCommentsPresenter(Presenter presenter);

        ArrayList<Comment> getCurrentCache();

        void onLoadCommentsStart();

        void onLoadCommentsComplete(ArrayList<Comment> result, Boolean isRefresh);

        void onLoadCommentsError();
    }
}
