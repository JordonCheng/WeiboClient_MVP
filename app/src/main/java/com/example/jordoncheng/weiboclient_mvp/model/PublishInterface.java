package com.example.jordoncheng.weiboclient_mvp.model;

import android.net.Uri;

import com.example.jordoncheng.weiboclient_mvp.PublishType;
import com.example.jordoncheng.weiboclient_mvp.Recyclable;

import java.util.ArrayList;

public interface PublishInterface {

    interface PublishCallback {

        void onPublished();

        void onError();
    }

    void publish(Recyclable recyclable, PublishCallback callback, PublishType timelineType, ArrayList<Uri> pics, String content);

    void onDestroy(Recyclable recyclable);

}
