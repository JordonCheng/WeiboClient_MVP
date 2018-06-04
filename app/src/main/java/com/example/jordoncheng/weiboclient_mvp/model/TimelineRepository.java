package com.example.jordoncheng.weiboclient_mvp.model;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.TimelineType;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.sina.weibo.sdk.statistic.WBAgent.TAG;

public class TimelineRepository implements TimelineDataSourse {

    private static final int STATUS_COUNT = 30;

    private StatusesAPI mStatusesAPI;
    private int mRequestCode = 0;
    private HashMap<String, ArrayList<Status>> mStatusesCaches = new HashMap<>();
    private SparseArray<LoadStatusesCallback> mCallbacks = new SparseArray<>();
    private HashMap<Recyclable, ArrayList<Integer>> mRequestCodeLists = new HashMap<>();
    private static TimelineRepository INSTANCE = null;
    private Application appContext;

    private TimelineRepository(Application appCcontext) {
        mStatusesAPI = new StatusesAPI(appCcontext, WBConstants.APP_KEY, AccessTokenKeeper.readAccessToken(appCcontext));
        this.appContext = appCcontext;
    }

    public static TimelineRepository getInstance(Application appCcontext) {
        if(INSTANCE == null) INSTANCE = new TimelineRepository(appCcontext);
        return INSTANCE;
    }

    @Override
    public void loadStatuses(Recyclable recyclable, LoadStatusesCallback callback, final TimelineType timelineType, final long maxId) {

        final int requestCode = ++mRequestCode;
        ArrayList<Integer> requestCodes = mRequestCodeLists.get(recyclable);
        if(requestCodes == null){
            requestCodes = new ArrayList<>();
            mRequestCodeLists.put(recyclable, requestCodes);
        }
        requestCodes.add(requestCode);
        mCallbacks.put(requestCode, callback);

        Observable.just(new RxParams(requestCode, timelineType))
                .map(params -> {
                    RxResult rxResult = null;
                    String s = null;
                    switch (params.timelineType) {
                        case HOME_TIMELINE:
                            s = mStatusesAPI.homeTimelineSync(0, maxId, STATUS_COUNT, 1, false, 0, false);
                            Log.d("OnSubscribe", "hometimeline start");
                            break;
                        case PUBLIC_TIME:
                            s = mStatusesAPI.publicTimelineSync(STATUS_COUNT, 1, false);
                            Log.d("OnSubscribe", "publictimeline start");
                            break;
                        case BILATERAL_TIMELINE:
                            s = mStatusesAPI.bilateralTimelineSync(0, maxId, STATUS_COUNT, 1, false, 0, false);
                            Log.d("OnSubscribe", "bilateral start");
                            break;
                    }

                    if(s != null) {

                        ArrayList<Status> result = null;

                        if(!TextUtils.isEmpty(s)) {
                            LogUtil.i(TAG, s);
                            StatusList sl = StatusList.parse(s);
                            if(sl != null) result = sl.statusList;

                            rxResult = new RxResult(params.requestCode, params.timelineType, result);
                        }
                    }

                    return rxResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(rxResult -> {
                    LoadStatusesCallback callback1;
                    if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                        ArrayList<Status> cache = mStatusesCaches.get(rxResult.timelineType.getPath());
                        rxResult.result.remove(0);
                        cache.addAll(rxResult.result);
                        callback1.onStatusesLoaded(cache);
                    }
                });
    }

    @Override
    public void loadStatuses(Recyclable recyclable, LoadStatusesCallback callback, final TimelineType timelineType, final boolean isFirst) {

        final int requestCode = ++mRequestCode;
        ArrayList<Integer> requestCodes = mRequestCodeLists.get(recyclable);
        if(requestCodes == null){
            requestCodes = new ArrayList<>();
            mRequestCodeLists.put(recyclable, requestCodes);
        }
        requestCodes.add(requestCode);
        mCallbacks.put(requestCode, callback);

        Observable.just(new RxParams(requestCode, timelineType))
                .map(params -> {
                    RxResult rxResult = null;
                    String s = null;

                    //第一次读取，从从缓存文件中读取
                    if(isFirst) {
                        try (
                                FileInputStream fis = appContext.openFileInput(params.timelineType.getPath());
                                InputStreamReader isr = new InputStreamReader(fis);
                                BufferedReader br = new BufferedReader(isr)
                        ) {
                            s = br.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //若没有缓存文件s返回null，或者不是第一次读取s依然为空，则从微薄服务器获得
                    if(s == null) {
                        switch (params.timelineType) {
                            case HOME_TIMELINE:
                                s = mStatusesAPI.homeTimelineSync(0, 0, STATUS_COUNT, 1, false, 0, false);
                                Log.d("OnSubscribe", "hometimeline start");
                                break;
                            case PUBLIC_TIME:
                                s = mStatusesAPI.publicTimelineSync(STATUS_COUNT, 1, false);
                                Log.d("OnSubscribe", "publictimeline start");
                                break;
                            case BILATERAL_TIMELINE:
                                s = mStatusesAPI.bilateralTimelineSync(0, 0, STATUS_COUNT, 1, false, 0, false);
                                Log.d("OnSubscribe", "bilateral start");
                                break;
                        }

                        if(s != null) {
                            try (
                                    FileOutputStream fos = appContext.openFileOutput(params.timelineType.getPath(), Context.MODE_PRIVATE)
                            ) {
                                fos.write(s.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(s != null) {

                        ArrayList<Status> result = null;

                        if(!TextUtils.isEmpty(s)) {
                            LogUtil.i(TAG, s);
                            StatusList sl = StatusList.parse(s);
                            if(sl != null) result = sl.statusList;

                            rxResult = new RxResult(params.requestCode, params.timelineType, result);
                        }
                    }

                    return rxResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(rxResult -> {
                    LoadStatusesCallback callback1;
                    if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                        callback1.onStatusesLoaded(rxResult.result);
                        mStatusesCaches.put(rxResult.timelineType.getPath(), rxResult.result);
                        Log.d("Subscriber", "onNext for " + rxResult.timelineType.getPath());
                    }
                });
    }

    private class RxParams {

        int requestCode;
        TimelineType timelineType;

        RxParams(int requestCode, TimelineType timelineType) {
            this.requestCode = requestCode;
            this.timelineType = timelineType;
        }
    }

    private class RxResult {

        int requestCode;
        ArrayList<Status> result;
        TimelineType timelineType;

        RxResult(int requestCode, TimelineType timelineType, ArrayList<Status> result) {
            this.requestCode = requestCode;
            this.timelineType = timelineType;
            this.result = result;
        }
    }

    @Override
    public void onDestroy(Recyclable recyclable) {
        ArrayList<Integer> requestCodes = mRequestCodeLists.remove(recyclable);
        if(requestCodes != null) {
            for(int requestCode : requestCodes) {
                mCallbacks.remove(requestCode);
            }
            System.gc();
        }
    }
}
