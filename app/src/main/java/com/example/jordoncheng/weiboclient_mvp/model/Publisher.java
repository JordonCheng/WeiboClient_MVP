package com.example.jordoncheng.weiboclient_mvp.model;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.example.jordoncheng.weiboclient_mvp.PublishType;
import com.example.jordoncheng.weiboclient_mvp.Recyclable;
import com.example.jordoncheng.weiboclient_mvp.TimelineType;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboHttpException;
import com.sina.weibo.sdk.net.ConnectionFactory;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;
import com.sina.weibo.sdk.openapi.HttpUtility;
import com.sina.weibo.sdk.openapi.PostParameters;
import com.sina.weibo.sdk.openapi.models.Status;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Publisher implements PublishInterface {

    private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";
    private static final String SHARE_SERVER = "https://api.weibo.com/2/statuses/share.json";
    private static final String COMMENT_CREATE_SERVER = "https://api.weibo.com/2/comments/create.json";

    private static Publisher INSTANCE;
    private Oauth2AccessToken mAccessToken;
    private String mAppKey;
    private Application mContext;
    private int mRequestCode = 0;
    private SparseArray<PublishCallback> mCallbacks = new SparseArray<>();
    private HashMap<Recyclable, ArrayList<Integer>> mRequestCodeLists = new HashMap<>();

    private Publisher(Application appContext) {
        this.mContext = appContext;
        mAccessToken = AccessTokenKeeper.readAccessToken(appContext);
        mAppKey = WBConstants.APP_KEY;
    }

    public static Publisher getInstance(Application appCcontext) {
        if(INSTANCE == null) INSTANCE = new Publisher(appCcontext);
        return INSTANCE;
    }

    @Override
    public void publish(Recyclable recyclable, PublishCallback callback, PublishType publishType, ArrayList<Uri> pics, String content) {
        final int requestCode = ++mRequestCode;
        ArrayList<Integer> requestCodes = mRequestCodeLists.get(recyclable);
        if(requestCodes == null){
            requestCodes = new ArrayList<>();
            mRequestCodeLists.put(recyclable, requestCodes);
        }
        requestCodes.add(requestCode);
        mCallbacks.put(requestCode, callback);

        Uri picUri = pics.get(0);

        switch (publishType) {
            case STATUS:
                Observable.just(new RxParams(requestCode, picUri, content, SHARE_SERVER))
                        .map(rxParams -> {
                            addAction(mContext, mAppKey);
                            int resultCode = executePost(rxParams.url, buildShareParams(rxParams.content, rxParams.uri));
                            return new RxResult(rxParams.requestCode, resultCode);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(rxResult -> {
                            PublishCallback callback1;
                            if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                                if(rxResult.resultCode == 200) {
                                    callback1.onPublished();
                                } else {
                                    callback1.onError();
                                }
                            }
                        });

                break;

            case COMMENT:
                Observable.just(new RxParams(requestCode, picUri, content, COMMENT_CREATE_SERVER))
                        .map(rxParams -> {
                            addAction(mContext, mAppKey);
                            int resultCode = executePost(rxParams.url, buildShareParams(rxParams.content, rxParams.uri));
                            return new RxResult(rxParams.requestCode, resultCode);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(rxResult -> {
                            PublishCallback callback1;
                            if((callback1 = mCallbacks.get(rxResult.requestCode)) != null) {
                                if(rxResult.resultCode == 200) {
                                    callback1.onPublished();
                                } else {
                                    callback1.onError();
                                }
                            }
                        });

                break;
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

    private class RxParams {

        int requestCode;
        Uri uri;
        String content;
        String url;

        RxParams(int requestCode, Uri uri, String content, String url) {
            this.requestCode = requestCode;
            this.uri = uri;
            this.content = content;
            this.url = url;
        }
    }

    private class RxResult {

        int requestCode;
        int resultCode;

        RxResult(int requestCode, int resultCode) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
        }
    }

    private void addAction(Context context, String appId) {
        try {
            Class wbActivator = Class.forName("com.sina.weibo.sdk.cmd.WbAppActivator");
            Method method = wbActivator.getMethod("getInstance", Context.class, String.class);
            Object object = method.invoke((Object)null, context, appId);
            Method activateApp = object.getClass().getMethod("activateApp");
            activateApp.invoke((Object)null);
        } catch (Exception var7) {
            ;
        }

    }

    private int executePost(String urlStr, PostParameters params) {
        int statusCode = 0;
        try {
            HttpURLConnection conn;
            StringBuilder sb = new StringBuilder();
            ArrayList<ArrayList<Object>> fileInfos = new ArrayList<>();
            long fileSize = 0;

            for(String key : params.keySet()) {
                if(params.get(key) instanceof Uri && params.get(key) != null) {

                    String path = HttpUtility.getRealFilePath(mContext, (Uri) params.get(key));
                    String sb2 = ("--" + BOUNDARY + "\r\n") +
                            "Content-Disposition: form-data; name=\"pic\"; " + "filename=\"" + HttpUtility.getFileName(path) + "\"" + "\r\n" +
                            "Content-Type: image/" + HttpUtility.getImageType(path) + "\r\n" +
                            "\r\n";

                    byte[] fileHeader = sb2.getBytes("UTF-8");
                    fileSize = fileSize + fileHeader.length + new File(path).length();

                    ArrayList<Object> fileInfo = new ArrayList<>();
                    fileInfo.add(fileHeader);
                    fileInfo.add(params.get(key));

                    fileInfos.add(fileInfo);

                } else if(key.equals("status") && params.get(key) != null){
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key)).append(" http://www.jordoncheng.com/ \r\n");
                } else if(params.get(key) != null) {
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key)).append("\r\n");
                }
            }

            byte[] content = sb.toString().getBytes("UTF-8");
            byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
            fileSize = fileSize + content.length + endInfo.length;

            conn = ConnectionFactory.createConnect(urlStr, mContext);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setConnectTimeout(25000);
            conn.setRequestProperty("Content-Length", String.valueOf(fileSize));
            conn.setDoOutput(true);
            conn.connect();

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(content);
            for (ArrayList<Object> fileInfo : fileInfos) {
                dos.write((byte[]) fileInfo.get(0));
                InputStream in = mContext.getContentResolver().openInputStream((Uri) fileInfo.get(1));
                if(in != null) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) != -1)
                        dos.write(buf, 0, len);
                    in.close();
                }
            }
            dos.write(endInfo);
            dos.flush();
            dos.close();

            if((statusCode = conn.getResponseCode()) == 200) {
                Log.d("AbsOpenAPI", "上传成功");
            } else {
                String response = null;
                response = AbsOpenAPI.readConnectResponse(conn, true);
                Log.d("AbsOpenAPI", "上传失败，错误码：" + String.valueOf(statusCode));
                throw new WeiboHttpException(response, statusCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return statusCode;
    }

    private PostParameters buildShareParams(String content, Uri uri) {
        PostParameters params = new PostParameters(mAppKey);
        params.put("access_token", mAccessToken.getToken());
        params.put("status", content);
        params.put("pic", uri);

        return params;
    }
}
