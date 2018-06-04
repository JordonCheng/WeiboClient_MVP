package com.example.jordoncheng.weiboclient_mvp.model;

import android.app.Application;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.SparseArray;

import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.sina.weibo.sdk.statistic.WBAgent.TAG;

public class CommentsRepository implements CommentsDataSourse {

    private static final int COMMENT_COUNT = 30;

    private CommentsAPI mCommentsAPI;
    private int mRequestCode = 0;
    private LongSparseArray<ArrayList<Comment>> mCommentsCaches = new LongSparseArray<>();
    private SparseArray<LoadCommentsCallback> mCallbacks = new SparseArray<>();
    private HashMap<Recyclable, ArrayList<Integer>> mRequestCodeLists = new HashMap<>();
    private static CommentsRepository INSTANCE = null;

    private CommentsRepository(Application appCcontext) {
        mCommentsAPI = new CommentsAPI(appCcontext, WBConstants.APP_KEY, AccessTokenKeeper.readAccessToken(appCcontext));
    }

    public static CommentsRepository getInstance(Application appCcontext) {
        if(INSTANCE == null) INSTANCE = new CommentsRepository(appCcontext);
        return INSTANCE;
    }

    @Override
    public void loadComments(Recyclable recyclable, LoadCommentsCallback callback, final long mid, final long maxId) {

        final int requestCode = ++mRequestCode;
        ArrayList<Integer> requestCodeList = mRequestCodeLists.get(recyclable);
        if(requestCodeList == null){
            requestCodeList = new ArrayList<>();
            mRequestCodeLists.put(recyclable, requestCodeList);
        }
        requestCodeList.add(requestCode);
        mCallbacks.put(requestCode, callback);

        Observable.just(new RxParams(requestCode, mid))
                .map(params -> {
                    RxResult rxResult = null;
                    String s = mCommentsAPI.showSync(params.mid, 0, maxId, COMMENT_COUNT, 1, 0);

                    ArrayList<Comment> result = null;

                    if(!TextUtils.isEmpty(s)) {
                        LogUtil.i(TAG, s);
                        CommentList cl = CommentList.parse(s);
                        if(cl != null) result = cl.commentList;

                        rxResult = new RxResult(params.requestCode, params.mid, result);
                    }

                    return rxResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(rxResult ->  {
                    LoadCommentsCallback callback1;
                        if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                        ArrayList<Comment> cache = mCommentsCaches.get(rxResult.mid);
                        rxResult.result.remove(0);
                        cache.addAll(rxResult.result);
                        callback1.onCommentsLoaded(cache);
                    }
                });
    }

    @Override
    public void loadComments(Recyclable recyclable, LoadCommentsCallback callback, long mid) {

        final int requestCode = ++mRequestCode;
        ArrayList<Integer> requestCodeList = mRequestCodeLists.get(recyclable);
        if(requestCodeList == null){
            requestCodeList = new ArrayList<>();
            mRequestCodeLists.put(recyclable, requestCodeList);
        }
        requestCodeList.add(requestCode);
        mCallbacks.put(requestCode, callback);

        Observable.just(new RxParams(requestCode, mid))
                .map(params -> {
                    RxResult rxResult = null;
                    String s = mCommentsAPI.showSync(params.mid, 0, 0, COMMENT_COUNT, 1, 0);

                    ArrayList<Comment> result = null;

                    if(!TextUtils.isEmpty(s)) {
                        LogUtil.i(TAG, s);
                        CommentList cl = CommentList.parse(s);
                        if(cl != null) result = cl.commentList;

                        rxResult = new RxResult(params.requestCode, params.mid, result);
                    }

                    return rxResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(rxResult -> {
                    LoadCommentsCallback callback1;
                    if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                        callback1.onCommentsLoaded(rxResult.result);
                        mCommentsCaches.put(rxResult.mid, rxResult.result);
                    }
                });

    }

    private class RxParams {

        int requestCode;
        long mid;

        RxParams(int requestCode, long mid) {
            this.requestCode = requestCode;
            this.mid = mid;
        }
    }

    private class RxResult {

        int requestCode;
        ArrayList<Comment> result;
        long mid;

        RxResult(int requestCode, long mid, ArrayList<Comment> result) {
            this.requestCode = requestCode;
            this.mid = mid;
            this.result = result;
        }
    }

    @Override
    public void onDestroy(Recyclable recyclable) {
        ArrayList<Integer> requestCodes = mRequestCodeLists.remove(recyclable);

        //若未查看过评论，此处requestCodes将为是null
        if(requestCodes != null) {
            for(int requestCode : requestCodes) {
                mCallbacks.remove(requestCode);
            }
            System.gc();
        }
    }
}
