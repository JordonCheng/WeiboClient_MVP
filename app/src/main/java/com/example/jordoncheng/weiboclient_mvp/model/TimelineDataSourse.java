package com.example.jordoncheng.weiboclient_mvp.model;

import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.TimelineType;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

public interface TimelineDataSourse {

    interface LoadStatusesCallback {

        void onStatusesLoaded(ArrayList<Status> result);

        void onError(String error);
    }

    void loadStatuses(Recyclable recyclable, LoadStatusesCallback callback, TimelineType timelineType, long maxId);

    void loadStatuses(Recyclable recyclable, LoadStatusesCallback callback, TimelineType timelineType, boolean isFirst);

    void onDestroy(Recyclable recyclable);
}
