package com.example.jordoncheng.weiboclient_mvp;

import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

public interface TimelineContract {

    interface Presenter extends Recyclable {

        void getMoreStatuses();

        void getStatusesFirst();

        void getStatusesRefresh();

        void onViewDestroy();
    }

    interface View {

        void createTimelinePresenter(Presenter presenter);

        ArrayList<Status> getCurrentCache();

        void onLoadStatusesStart();

        void onLoadStatusesComplete(ArrayList<Status> result, Boolean isRefresh);

        void onLoadStatusesError();
    }
}
