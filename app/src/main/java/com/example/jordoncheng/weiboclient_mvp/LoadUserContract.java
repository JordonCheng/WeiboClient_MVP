package com.example.jordoncheng.weiboclient_mvp;

import com.sina.weibo.sdk.openapi.models.User;

public interface LoadUserContract {

    interface Presenter extends Recyclable {

        void getUser();

        void onViewDestroy();
    }

    interface View {

        void createUserPresenter(Presenter presenter);

        void onUserStart();

        void onLoadUserComplete(User result);

        void onLoadUserError();
    }
}
