package com.example.jordoncheng.weiboclient_mvp;

import com.sina.weibo.sdk.openapi.models.StatusList;

public interface StatusesContract {

    interface Presenter {

        void getMoreStatuses();

        void getStatusesFirst();

        void getStatusesRefresh();
    }

    interface View {

        void setStatuses(StatusList result);
    }
}
