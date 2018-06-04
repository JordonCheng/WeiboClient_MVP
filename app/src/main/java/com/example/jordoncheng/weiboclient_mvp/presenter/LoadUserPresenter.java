package com.example.jordoncheng.weiboclient_mvp.presenter;

import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.LoadUserContract;
import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.model.UserDataSourse;
import com.sina.weibo.sdk.openapi.models.User;

public class LoadUserPresenter implements LoadUserContract.Presenter, Recyclable {

    private long mUid;
    private LoadUserContract.View mView;
    private UserDataSourse mUserDataSourse;

    public LoadUserPresenter(LoadUserContract.View view, long uid) {
        this.mUid = uid;
        this.mView = view;
        mUserDataSourse = Injection.provideUserPresenter();
    }

    private LoadUserContract.View getView() {
        if(mView == null) {
            throw new IllegalArgumentException("load user view can not be null");
        }
        return mView;
    }

    @Override
    public void getUser() {
        mUserDataSourse.loadUser(this, new UserDataSourse.LoadUserCallback() {
            @Override
            public void onUserLoaded(User result) {
                getView().onLoadUserComplete(result);
            }

            @Override
            public void onError(String error) {
                getView().onLoadUserError();
            }
        }, mUid);
    }

    @Override
    public void onViewDestroy() {
        mUserDataSourse.onDestroy(this);
    }
}
