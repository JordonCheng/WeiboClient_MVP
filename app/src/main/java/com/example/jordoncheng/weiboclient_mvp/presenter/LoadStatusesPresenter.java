package com.example.jordoncheng.weiboclient_mvp.presenter;

import android.util.Log;

import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.TimelineContract;
import com.example.jordoncheng.weiboclient_mvp.TimelineType;
import com.example.jordoncheng.weiboclient_mvp.model.TimelineDataSourse;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

public class LoadStatusesPresenter implements TimelineContract.Presenter {

    private TimelineContract.View mView;
    private TimelineDataSourse mTimelineDataSourse;
    private final TimelineType mTimelineType;

    public LoadStatusesPresenter(TimelineContract.View view, TimelineType timelineType){
        mTimelineType = timelineType;
        mView = view;
        mTimelineDataSourse = Injection.provideStatusesRepository();
    }

    private TimelineContract.View getView() {
        if(mView == null) {
            throw new IllegalArgumentException("load statuses view can not be null");
        }
        return mView;
    }

    @Override
    public void getMoreStatuses() {
        mTimelineDataSourse.loadStatuses(this, new TimelineDataSourse.LoadStatusesCallback() {
            @Override
            public void onStatusesLoaded(ArrayList<Status> result) {
                getView().onLoadStatusesComplete(result, false);
            }

            @Override
            public void onError(String error) {
                getView().onLoadStatusesError();
            }
        }, mTimelineType, getView().getCurrentCache().get(getView().getCurrentCache().size() - 1).id);
    }

    @Override
    public void getStatusesFirst() {
        mTimelineDataSourse.loadStatuses(this, new TimelineDataSourse.LoadStatusesCallback() {
            @Override
            public void onStatusesLoaded(ArrayList<Status> result) {
                getView().onLoadStatusesComplete(result, true);
                Log.d("LoadStatusesCallback", "onStatusesLoaded complete");
            }

            @Override
            public void onError(String error) {

            }
        }, mTimelineType, true);
        getView().onLoadStatusesStart();
    }

    @Override
    public void getStatusesRefresh() {
        mTimelineDataSourse.loadStatuses(this, new TimelineDataSourse.LoadStatusesCallback() {
            @Override
            public void onStatusesLoaded(ArrayList<Status> result) {
                getView().onLoadStatusesComplete(result, true);
            }

            @Override
            public void onError(String error) {

            }
        }, mTimelineType, false);
        getView().onLoadStatusesStart();
    }

    @Override
    public void onViewDestroy() {
        mTimelineDataSourse.onDestroy(this);
    }
}
