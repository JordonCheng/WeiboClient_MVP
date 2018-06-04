package com.example.jordoncheng.weiboclient_mvp.model;

import android.net.Uri;

import com.example.jordoncheng.weiboclient_mvp.PublishType;

import java.util.ArrayList;

public class PublishBean {

    private ArrayList<Uri> mUris = new ArrayList<>();
    private String mContent;
    private PublishType mPublishType;

    public PublishBean(PublishType publishType) {
        this.mPublishType = publishType;
    }

    public void addImageUri(Uri uri) {
        if(mUris.size() < 9) {
            mUris.add(uri);
        } else {

        }
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
