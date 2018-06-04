package com.example.jordoncheng.weiboclient_mvp.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.util.SparseArray;

import com.example.jordoncheng.weiboclient_mvp.ImageContract;
import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.TimelineType;
import com.example.jordoncheng.weiboclient_mvp.view.widget.ImageTagView;
import com.sina.weibo.sdk.openapi.models.Status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoadImagePresenter implements ImageContract.Presenter {

    private static LoadImagePresenter INSTANCE = null;
    private static LruCache<String, Bitmap> mBitmapCache = new LruCache<>(20);
    private static final String RegExp = "([^<>/\\\\\\|:\"\"\\*\\?]+\\.\\w+$)";
    private SparseArray<ImageTagView> mImageViews = new SparseArray<>();
    private HashMap<Object, ArrayList<Integer>> mRequestCodeLists = new HashMap<>();
    private Context appContext;

    private int imageTag = 1;

    private LoadImagePresenter(Context appContext) {
        this.appContext = appContext;
    }

    public static LoadImagePresenter getInstance(Context appContext) {
        if(INSTANCE == null) INSTANCE = new LoadImagePresenter(appContext);
        return INSTANCE;
    }

    @Override
    public void setImage(Recyclable recyclable, ImageTagView imageTagView, final String url) {
        Bitmap bm;
        if((bm = mBitmapCache.get(url)) != null) imageTagView.setImageBitmap(bm);
        else {

            final int requestCode = imageTag;
            imageTagView.setImageTag(imageTag++);

            ArrayList<Integer> requestCodes = mRequestCodeLists.get(recyclable);
            if(requestCodes == null){
                requestCodes = new ArrayList<>();
                mRequestCodeLists.put(recyclable, requestCodes);
            }
            requestCodes.add(requestCode);
            mImageViews.put(requestCode, imageTagView);

            Observable.just(new RxParams(requestCode, url))
                    .map(new Func1<RxParams, RxResult>() {

                        @Override
                        public RxResult call(RxParams params) {

                            //parse to file name
                            Matcher m = Pattern.compile(RegExp).matcher(params.url);
                            m.find();
                            String fileName = m.group(1);

                            Bitmap bitmap = loadLocal(fileName);
                            while(bitmap == null) {
                                downloadImage(params.url, fileName);
                                bitmap = loadLocal(fileName);
                            }
                            return new RxResult(params.requestCode, bitmap);
                        }

                        private Bitmap loadLocal(String fileName) {

                            InputStream is = null;
                            Bitmap bitmap = null;

                            try {
                                is = appContext.openFileInput(fileName);
                                bitmap = BitmapFactory.decodeStream(is);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } finally {
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            return bitmap;
                        }

                        private void downloadImage(String imageURL, String fileName) {
                            try {
                                URL url = new URL(imageURL);
                                try (
                                        InputStream is = url.openStream();
                                        OutputStream os = appContext.openFileOutput(fileName,
                                                Context.MODE_PRIVATE)
                                ) {
                                    byte[] buff = new byte[1024];
                                    int hasRead;
                                    while((hasRead = is.read(buff)) > 0) {
                                        os.write(buff, 0, hasRead);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }

                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(rxResult -> {
                        ImageTagView imageTagView1;
                        if(rxResult.requestCode == (imageTagView1 = mImageViews.get(rxResult.requestCode)).getImageTag()) {
                            imageTagView1.setImageBitmap(rxResult.bitmap);
                        }
                    });
        }
    }

    @Override
    public void onViewDestroy(Recyclable recyclable) {
        ArrayList<Integer> requestCodes = mRequestCodeLists.remove(recyclable);

        //若直接从缓存中读到Bitmap对象，则不会保存Recyclable对象，此处requestCodes将为空
        if(requestCodes != null) {
            for(int requestCode : requestCodes) {
                mImageViews.remove(requestCode);
            }
            System.gc();
        }
    }

    class RxParams {

        int requestCode;
        String url;

        RxParams(int requestCode, String url) {
            this.requestCode = requestCode;
            this.url = url;
        }
    }

    class RxResult {

        int requestCode;
        Bitmap bitmap;

        RxResult(int requestCode, Bitmap bitmap) {
            this.requestCode = requestCode;
            this.bitmap = bitmap;
        }
    }
}
