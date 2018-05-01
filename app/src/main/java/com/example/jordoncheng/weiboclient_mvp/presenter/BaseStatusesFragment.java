package com.example.jordoncheng.weiboclient_mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.jordoncheng.weiboclient_mvp.StatusesContract;
import com.sina.weibo.sdk.openapi.models.StatusList;

public class BaseStatusesFragment extends Fragment implements StatusesContract.Presenter{

    private StatusList mStatuesList;
    private long loadMoreID;

    @Override
    public void getMoreStatuses() {

    }

    @Override
    public void getStatusesFirst() {

    }

    @Override
    public void getStatusesRefresh() {

    }

    private void start() {

    }

    private void resume() {

    }

    private void pause() {

    }

    private void close() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start();
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }
}
