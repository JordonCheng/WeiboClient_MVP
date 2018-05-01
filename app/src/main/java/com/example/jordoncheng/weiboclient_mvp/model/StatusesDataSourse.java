package com.example.jordoncheng.weiboclient_mvp.model;

import com.sina.weibo.sdk.openapi.models.StatusList;

public interface StatusesDataSourse {

    interface LoadStatusesCallback {

        void onStatusesLoaded(StatusList result);

        void onError(String error);
    }

    void loadStatuses(long Uid, long MaxId)

}
