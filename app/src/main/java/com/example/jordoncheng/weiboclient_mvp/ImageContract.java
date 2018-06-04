package com.example.jordoncheng.weiboclient_mvp;

import android.graphics.Bitmap;

import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;

public interface ImageContract {

    interface Presenter {

        void setImage(Recyclable recyclable, ImageTagView imageView, String url);

        void onViewDestroy(Recyclable recyclable);
    }

    interface View extends Recyclable {

        void createLoadImagePresenter(Presenter presenter);
    }
}
