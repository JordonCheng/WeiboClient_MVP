package com.example.jordoncheng.weiboclient_mvp.model;

import android.app.Application;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.SparseArray;

import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.Comment;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.openapi.models.UserList;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.sina.weibo.sdk.statistic.WBAgent.TAG;

public class UserRepository implements UserDataSourse {

    private UsersAPI mUsersAPI;
    private int mRequestCode = 0;
    private SparseArray<LoadUserCallback> mCallbacks = new SparseArray<>();
    private HashMap<Recyclable, ArrayList<Integer>> mRequestCodeLists = new HashMap<>();
    private static UserRepository INSTANCE = null;

    private UserRepository(Application appCcontext) {
        mUsersAPI = new UsersAPI(appCcontext, WBConstants.APP_KEY, AccessTokenKeeper.readAccessToken(appCcontext));
    }

    public static UserRepository getInstance(Application appCcontext) {
        if(INSTANCE == null) INSTANCE = new UserRepository(appCcontext);
        return INSTANCE;
    }

    @Override
    public void loadUser(Recyclable recyclable, LoadUserCallback callback,final long uid) {

        final int requestCode = ++mRequestCode;
        ArrayList<Integer> requestCodeList = mRequestCodeLists.get(recyclable);
        if(requestCodeList == null){
            requestCodeList = new ArrayList<>();
            mRequestCodeLists.put(recyclable, requestCodeList);
        }
        requestCodeList.add(requestCode);
        mCallbacks.put(requestCode, callback);

        Observable.just(new RxParams(requestCode, uid))
                .map(params -> {
                    RxResult rxResult = null;
                    /*String s = mUsersAPI.showSync(params.uid);*/
                    String s = "{\"id\":1967411623,\"idstr\":\"1967411623\",\"class\":1,\"screen_name\":\"Jordon国栋\",\"name\":\"Jordon国栋\",\"province\":\"44\",\"city\":\"1\",\"location\":\"广东 广州\",\"description\":\"申冤在我，我必报应。\",\"url\":\"http:\\/\\/blog.sina.com.cn\\/jordoncheng\",\"profile_image_url\":\"http:\\/\\/tva3.sinaimg.cn\\/crop.0.0.996.996.50\\/754451a7jw8f8mdmj2kokj20ro0rogos.jpg\",\"cover_image\":\"http:\\/\\/ww4.sinaimg.cn\\/crop.69.0.980.300\\/754451a7gw1e8u8zq9s6gj20v108cq5z.jpg\",\"cover_image_phone\":\"http:\\/\\/ww1.sinaimg.cn\\/crop.0.0.640.640.640\\/549d0121tw1egm1kjly3jj20hs0hsq4f.jpg\",\"profile_url\":\"jordoncheng\",\"domain\":\"jordoncheng\",\"weihao\":\"\",\"gender\":\"m\",\"followers_count\":269,\"friends_count\":293,\"pagefriends_count\":6,\"statuses_count\":1176,\"favourites_count\":458,\"created_at\":\"Sun Feb 13 21:58:33 +0800 2011\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1,\"remark\":\"\",\"insecurity\":{\"sexual_content\":false},\"ptype\":0,\"allow_all_comment\":true,\"avatar_large\":\"http:\\/\\/tva3.sinaimg.cn\\/crop.0.0.996.996.180\\/754451a7jw8f8mdmj2kokj20ro0rogos.jpg\",\"avatar_hd\":\"http:\\/\\/tva3.sinaimg.cn\\/crop.0.0.996.996.1024\\/754451a7jw8f8mdmj2kokj20ro0rogos.jpg\",\"verified_reason\":\"\",\"verified_trade\":\"\",\"verified_reason_url\":\"\",\"verified_source\":\"\",\"verified_source_url\":\"\",\"follow_me\":false,\"like\":false,\"like_me\":false,\"online_status\":0,\"bi_followers_count\":91,\"lang\":\"zh-cn\",\"star\":0,\"mbtype\":0,\"mbrank\":0,\"block_word\":0,\"block_app\":0,\"credit_score\":80,\"user_ability\":33554432,\"urank\":28,\"story_read_state\":-1,\"vclub_member\":0}";

                    User result = null;

                    if(!TextUtils.isEmpty(s)) {
                        LogUtil.i(TAG, s);
                        result = User.parse(s);

                        rxResult = new RxResult(params.requestCode, result);
                    }

                    return rxResult;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(rxResult -> {
                    LoadUserCallback callback1;
                    if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                        callback1.onUserLoaded(rxResult.result);
                    }
                });

    }

    private class RxParams {

        int requestCode;
        long uid;

        RxParams(int requestCode, long uid) {
            this.requestCode = requestCode;
            this.uid = uid;
        }
    }

    private class RxResult {

        int requestCode;
        User result;

        RxResult(int requestCode, User result) {
            this.requestCode = requestCode;
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
