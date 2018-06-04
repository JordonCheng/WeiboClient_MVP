/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sina.weibo.sdk.openapi;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboHttpException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 微博 OpenAPI 的基类，每个接口类都继承了此抽象类。
 * 
 * @author SINA
 * @since 2013-11-05
 */
public abstract class AbsOpenAPI {
    private static final String TAG = AbsOpenAPI.class.getName();
    
    /** 访问微博服务接口的地址 */
    protected static final String API_SERVER       = "https://api.weibo.com/2";
    /** POST 请求方式 */
    protected static final String HTTPMETHOD_POST  = "POST";
    /** GET 请求方式 */
    protected static final String HTTPMETHOD_GET   = "GET";
    /** HTTP 参数 */
    protected static final String KEY_ACCESS_TOKEN = "access_token";
    
    /** 当前的 Token */
    protected Oauth2AccessToken mAccessToken;
    protected Context mContext;
    protected String mAppKey;
    
    /**
     * 构造函数，使用各个 API 接口提供的服务前必须先获取 Token。
     * 
     * @param accesssToken 访问令牌
     */
    public AbsOpenAPI(Context context, String appKey, Oauth2AccessToken accessToken) {
        mContext = context;
        mAppKey = appKey;
        mAccessToken = accessToken;
    }

    /**
     * HTTP 异步请求。
     * 
     * @param url        请求的地址
     * @param params     请求的参数
     * @param httpMethod 请求方法
     * @param listener   请求后的回调接口
     */
    protected void requestAsync(String url, WeiboParameters params, String httpMethod, RequestListener listener) {
        if (null == mAccessToken
                || TextUtils.isEmpty(url)
                || null == params
                || TextUtils.isEmpty(httpMethod)
                || null == listener) {
            LogUtil.e(TAG, "Argument error!");
            return;
        }
        
        params.put(KEY_ACCESS_TOKEN, mAccessToken.getToken());
        new AsyncWeiboRunner(mContext).requestAsync(url, params, httpMethod, listener);
    }
    
    /**
     * HTTP 同步请求。
     * 
     * @param url        请求的地址
     * @param params     请求的参数
     * @param httpMethod 请求方法
     * 
     * @return 同步请求后，服务器返回的字符串。
     */
    protected String requestSync(String url, WeiboParameters params, String httpMethod) {
        if (null == mAccessToken
                || TextUtils.isEmpty(url)
                || null == params
                || TextUtils.isEmpty(httpMethod)) {
            LogUtil.e(TAG, "Argument error!");
            return "";
        }
        
        params.put(KEY_ACCESS_TOKEN, mAccessToken.getToken());
        return new AsyncWeiboRunner(mContext).request(url, params, httpMethod);
    }

    protected String doPostSync(String url, PostParameters params) {
        if (null == mAccessToken
                || TextUtils.isEmpty(url)
                || null == params) {
            LogUtil.e(TAG, "Argument error!");
            return "";
        }

        params.put(KEY_ACCESS_TOKEN, mAccessToken.getToken());
        addAction(mContext, params.getAppKey());
        return executePost(url, params);
    }

    private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";

    private String executePost(String urlStr, PostParameters params) {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<ArrayList<Object>> fileInfos = new ArrayList<>();
            long fileSize = 0;
            for(String key : params.keySet()) {
                if(params.get(key) instanceof Uri) {

                    String path = HttpUtility.getRealFilePath(mContext, (Uri) params.get(key));
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("--" + BOUNDARY + "\r\n");
                    sb2.append("Content-Disposition: form-data; name=\"pic\"; ").append("filename=\"").append(HttpUtility.getFileName(path)).append("\"").append("\r\n");
                    sb2.append("Content-Type: image/").append(HttpUtility.getImageType(path)).append("\r\n");
                    sb2.append("\r\n");

                    byte[] fileHeader = sb2.toString().getBytes("UTF-8");
                    fileSize = fileSize + fileHeader.length + new File(path).length();

                    ArrayList<Object> fileInfo = new ArrayList<>();
                    fileInfo.add(fileHeader);
                    fileInfo.add(params.get(key));

                    fileInfos.add(fileInfo);

                } else if(key == "status"){
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key)).append(" http://www.jordoncheng.com/ \r\n");
                } else {
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append("\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key)).append("\r\n");
                }
            }

            byte[] content = sb.toString().getBytes("UTF-8");
            byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
            fileSize = fileSize + content.length + endInfo.length;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
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

            int statusCode = 0;
            if((statusCode = conn.getResponseCode()) == 200) {
                Log.d("AbsOpenAPI", "上传成功");
            } else {
                String response = null;
                response = readConnectResponse(conn, true);
                Log.d("AbsOpenAPI", "上传失败，错误码：" + String.valueOf(statusCode));
                throw new WeiboHttpException(response, statusCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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

    public static String readConnectResponse(HttpURLConnection connection, boolean error) {
        String result = null;
        InputStream inputStream = null;
        ByteArrayOutputStream content = null;

        try {

            byte[] buffer = new byte[8192];
            if (error) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            content = new ByteArrayOutputStream();

            int readBytes;
            while((readBytes = inputStream.read(buffer)) != -1) {
                content.write(buffer, 0, readBytes);
            }

            result = new String(content.toByteArray(), "UTF-8");
            return result;
        } catch (IOException var17) {
            throw new WeiboException(var17);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception var16) {
                ;
            }

            try {
                if (content != null) {
                    content.close();
                }
            } catch (Exception var15) {
                ;
            }

        }
    }
}
