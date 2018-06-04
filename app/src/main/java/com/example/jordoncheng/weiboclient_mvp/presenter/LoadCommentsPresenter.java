package com.example.jordoncheng.weiboclient_mvp.presenter;

import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.LoadCommentsContract;
import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.model.CommentsDataSourse;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.ArrayList;

public class LoadCommentsPresenter implements LoadCommentsContract.Presenter, Recyclable {

    private long mMid;
    private LoadCommentsContract.View mView;
    private CommentsDataSourse mCommentsDataSourse;

    public LoadCommentsPresenter(LoadCommentsContract.View view, long mid) {
        this.mMid = mid;
        this.mView = view;
        mCommentsDataSourse = Injection.provideCommentsRepository();
    }

    private LoadCommentsContract.View getView() {
        if(mView == null) {
            throw new IllegalArgumentException("load statuses view can not be null");
        }
        return mView;
    }

    @Override
    public void getMoreComments() {
        mCommentsDataSourse.loadComments(this, new CommentsDataSourse.LoadCommentsCallback() {
            @Override
            public void onCommentsLoaded(ArrayList<Comment> result) {
                getView().onLoadCommentsComplete(result, false);
            }

            @Override
            public void onError(String error) {
                getView().onLoadCommentsError();
            }
        }, mMid, getView().getCurrentCache().get(getView().getCurrentCache().size()-1).id);
    }

    @Override
    public void getCommentsRefresh() {
        mCommentsDataSourse.loadComments(this, new CommentsDataSourse.LoadCommentsCallback() {
            @Override
            public void onCommentsLoaded(ArrayList<Comment> result) {
                getView().onLoadCommentsComplete(result, true);
            }

            @Override
            public void onError(String error) {
                getView().onLoadCommentsError();
            }
        }, mMid);
        getView().onLoadCommentsStart();
    }

    @Override
    public void onViewDestroy() {
        mCommentsDataSourse.onDestroy(this);
    }
}
