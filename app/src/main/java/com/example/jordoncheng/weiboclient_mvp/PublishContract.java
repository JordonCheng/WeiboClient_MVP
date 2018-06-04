package com.example.jordoncheng.weiboclient_mvp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;

public interface PublishContract {

    interface Presenter extends Contract {

        void sendStatus();

        void addPicture(Activity activity);

        void replacePicture(Activity activity);

        void callOnActivityResult(int requestCode, int resultCode, Intent data);

        void onViewDestroy();

    }

    interface View {

        void createPublishPresenter(Presenter presenter);

        EditText getEditText();

        void onReplacePictureComplete(Uri uri);

        void onAddPictureComplete(Uri uri);

        void onTextCountChanged(int count, boolean isLegal);

        void onSendStatusStart();

        void onSendStatusComplete();

        void onSendStatusFailed();
    }
}
