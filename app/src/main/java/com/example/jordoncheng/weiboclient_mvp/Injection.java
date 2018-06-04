package com.example.jordoncheng.weiboclient_mvp;

import android.app.Application;
import android.content.Context;

import com.example.jordoncheng.weiboclient_mvp.model.CommentsRepository;
import com.example.jordoncheng.weiboclient_mvp.model.PublishInterface;
import com.example.jordoncheng.weiboclient_mvp.model.Publisher;
import com.example.jordoncheng.weiboclient_mvp.model.TimelineRepository;
import com.example.jordoncheng.weiboclient_mvp.model.UserRepository;
import com.example.jordoncheng.weiboclient_mvp.presenter.LoadImagePresenter;

public class Injection {

    private static Application appContext;

    public static void setAppContext(Application appContext) {
        Injection.appContext = appContext;
    }

    public static TimelineRepository provideStatusesRepository() {
        if(appContext == null) {
            throw new IllegalArgumentException("please set app context first");
        }
        return TimelineRepository.getInstance(appContext);
    }

    public static CommentsRepository provideCommentsRepository() {
        if(appContext == null) {
            throw new IllegalArgumentException("please set app context first");
        }
        return CommentsRepository.getInstance(appContext);
    }

    public static LoadImagePresenter provideLoadImagePresenter() {
        if(appContext == null) {
            throw new IllegalArgumentException("please set app context first");
        }
        return LoadImagePresenter.getInstance(appContext);
    }

    public static UserRepository provideUserPresenter() {
        if(appContext == null) {
            throw new IllegalArgumentException("please set app context first");
        }
        return UserRepository.getInstance(appContext);
    }

    public static PublishInterface providePublisher() {
        if(appContext == null) {
            throw new IllegalArgumentException("please set app context first");
        }
        return Publisher.getInstance(appContext);
    }
}
