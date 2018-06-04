package com.example.jordoncheng.weiboclient_mvp.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jordoncheng.weiboclient_mvp.ImageContract;
import com.example.jordoncheng.weiboclient_mvp.Injection;
import com.example.jordoncheng.weiboclient_mvp.LoadUserContract;
import com.example.jordoncheng.weiboclient_mvp.PublishContract;
import com.example.jordoncheng.weiboclient_mvp.PublishType;
import com.example.jordoncheng.weiboclient_mvp.R;
import com.example.jordoncheng.weiboclient_mvp.TimelineContract;
import com.example.jordoncheng.weiboclient_mvp.Utility;
import com.example.jordoncheng.weiboclient_mvp.model.WBConstants;
import com.example.jordoncheng.weiboclient_mvp.presenter.LoadUserPresenter;
import com.example.jordoncheng.weiboclient_mvp.presenter.PublishPresenter;
import com.example.jordoncheng.weiboclient_mvp.view.widget.StatusAvatarImageView;
import com.example.jordoncheng.weiboclient_mvp.view.widget.StatusImageLayout;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.User;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 3/28/2018.
 */

public class CreateStatusActivity extends BaseActivity implements LoadUserContract.View,
        ImageContract.View, PublishContract.View {

    private LoadUserContract.Presenter mLoadUserPresenter;
    private ImageContract.Presenter mLoadImagePresenter;
    private PublishContract.Presenter mPublishPresenter;
    private StatusAvatarImageView avatar;

    private ImageView image;
    private boolean isLegal;
    private TextView charCount;

    @Override
    protected int onSetDimMode() {
        return DIMMODE_3;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_status);
        avatar = findViewById(R.id.line_list_item_avatar_on_large);
        charCount = findViewById(R.id.line_edit_char_count);
        image = findViewById(R.id.line_list_item_status_image);
        findViewById(R.id.line_edit_send).setOnClickListener(v -> {
            if(isLegal) mPublishPresenter.sendStatus();
        });

        image.setOnClickListener(v -> mPublishPresenter.replacePicture(this));

        //对状态栏有影响
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //设置好所有Presenter
        createUserPresenter(new LoadUserPresenter(this, Utility.getUid(this)));
        mLoadUserPresenter.getUser();
        createLoadImagePresenter(Injection.provideLoadImagePresenter());
        createPublishPresenter(new PublishPresenter(this, PublishType.STATUS));

        mPublishPresenter.start();
    }

    public void getImage(View v) {
        mPublishPresenter.addPicture(this);
    }

    public void getAt(View v) {
    }

    public void post(View v) {
        if(isLegal) {
            mPublishPresenter.sendStatus();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPublishPresenter.callOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void createLoadImagePresenter(ImageContract.Presenter presenter) {
        this.mLoadImagePresenter = presenter;
    }

    @Override
    public void createUserPresenter(LoadUserContract.Presenter presenter) {
        this.mLoadUserPresenter = presenter;
    }

    @Override
    public void onUserStart() {

    }

    @Override
    public void onLoadUserComplete(User result) {
        mLoadImagePresenter.setImage(this, avatar, result.avatar_hd);
    }

    @Override
    public void onLoadUserError() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublishPresenter.onViewDestroy();
    }

    @Override
    public void createPublishPresenter(PublishContract.Presenter presenter) {
        this.mPublishPresenter = presenter;
    }

    @Override
    public EditText getEditText() {
        return findViewById(R.id.input_box);
    }

    @Override
    public void onReplacePictureComplete(Uri uri) {
        image.setImageURI(uri);
    }

    @Override
    public void onAddPictureComplete(Uri uri) {
        image.setImageURI(uri);
    }

    @Override
    public void onTextCountChanged(int count, boolean isLegal) {
        charCount.setText(String.valueOf(count));
        this.isLegal = isLegal;
    }

    @Override
    public void onSendStatusStart() {

    }

    @Override
    public void onSendStatusComplete() {
        Snackbar.make(getWindow().getDecorView(), "发送成功", Snackbar.LENGTH_LONG)
                .setAction("确定", null).show();
        finish();
    }

    @Override
    public void onSendStatusFailed() {
        Snackbar.make(getWindow().getDecorView(), "发送失败", Snackbar.LENGTH_LONG)
                .setAction("确定", null).show();
    }


}
