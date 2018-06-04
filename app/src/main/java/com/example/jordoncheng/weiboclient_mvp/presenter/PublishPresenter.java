package com.example.jordoncheng.weiboclient_mvp.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.PublishContract;
import com.example.jordoncheng.weiboclient_mvp.PublishType;
import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.model.PublishInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class PublishPresenter implements PublishContract.Presenter, Recyclable {

    private static final int ADD_PIC = 100;
    private static final int REPLACE_PIC = 101;

    private Uri mUri;
    private EditText mEditText;
    private PublishContract.View mView;
    private PublishInterface mPublisher;
    private PublishType mPublishType;

    public PublishPresenter(PublishContract.View view, PublishType publishType) {
        this.mView = view;
        this.mPublishType = publishType;
        mPublisher = Injection.providePublisher();
    }

    @Override
    public void sendStatus() {
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(mUri);
        mPublisher.publish(this, new PublishInterface.PublishCallback() {
            @Override
            public void onPublished() {
                mView.onSendStatusComplete();
            }

            @Override
            public void onError() {
                mView.onSendStatusFailed();

            }
        }, mPublishType, uris, mEditText.getText().toString());
        mView.onSendStatusStart();
    }

    @Override
    public void addPicture(Activity activity) {
        if(mUri == null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            Bundle request = new Bundle();
            request.putBoolean("return-data", true);
            intent.putExtras(request);
            activity.startActivityForResult(intent, ADD_PIC);
        }
    }

    @Override
    public void replacePicture(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        Bundle request = new Bundle();
        request.putBoolean("return-data", true);
        intent.putExtras(request);
        activity.startActivityForResult(intent, REPLACE_PIC);
    }

    @Override
    public void callOnActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            mUri = data.getData();
            if(requestCode == ADD_PIC) {
                mView.onAddPictureComplete(mUri);
            } else if(requestCode == REPLACE_PIC) {
                mView.onReplacePictureComplete(mUri);
            }
        }
    }

    @Override
    public void onViewDestroy() {
        mPublisher.onDestroy(this);
    }

    @Override
    public void start() {
        mEditText = mView.getEditText();
        mView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = 0;
                try {
                    length = s.toString().getBytes("gbk").length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if(length > 0 && length <= 140) {
                    mView.onTextCountChanged(length / 2, true);
                } else {
                    mView.onTextCountChanged(length / 2, false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
