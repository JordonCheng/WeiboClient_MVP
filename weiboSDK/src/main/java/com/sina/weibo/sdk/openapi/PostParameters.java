package com.sina.weibo.sdk.openapi;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.sina.weibo.sdk.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class PostParameters {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private LinkedHashMap<String, Object> mParams = new LinkedHashMap();
    private String mAnonymousCookie;
    private String mAppKey;

    public PostParameters(String appKey) {
        this.mAppKey = appKey;
    }

    public String getAppKey() {
        return this.mAppKey;
    }

    public LinkedHashMap<String, Object> getParams() {
        return this.mParams;
    }

    public void setParams(LinkedHashMap<String, Object> params) {
        this.mParams = params;
    }

    /** @deprecated */
    @Deprecated
    public void add(String key, String val) {
        this.mParams.put(key, val);
    }

    /** @deprecated */
    @Deprecated
    public void add(String key, int value) {
        this.mParams.put(key, String.valueOf(value));
    }

    /** @deprecated */
    @Deprecated
    public void add(String key, long value) {
        this.mParams.put(key, String.valueOf(value));
    }

    /** @deprecated */
    @Deprecated
    public void add(String key, Object val) {
        this.mParams.put(key, val.toString());
    }

    public void put(String key, String val) {
        this.mParams.put(key, val);
    }

    public void put(String key, int value) {
        this.mParams.put(key, String.valueOf(value));
    }

    public void put(String key, long value) {
        this.mParams.put(key, String.valueOf(value));
    }

    public void put(String key, Bitmap bitmap) {
        this.mParams.put(key, bitmap);
    }

    public void put(String key, Object val) {
        this.mParams.put(key, val);
    }

    public Object get(String key) {
        return this.mParams.get(key);
    }

    public void remove(String key) {
        if (this.mParams.containsKey(key)) {
            this.mParams.remove(key);
            this.mParams.remove(this.mParams.get(key));
        }

    }

    public Set<String> keySet() {
        return this.mParams.keySet();
    }

    public boolean containsKey(String key) {
        return this.mParams.containsKey(key);
    }

    public boolean containsValue(String value) {
        return this.mParams.containsValue(value);
    }

    public int size() {
        return this.mParams.size();
    }

    public String encodeUrl() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        Iterator var3 = this.mParams.keySet().iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }

            Object value = this.mParams.get(key);
            if (value instanceof String) {
                String param = (String)value;
                if (!TextUtils.isEmpty(param)) {
                    try {
                        sb.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(param, "UTF-8"));
                    } catch (UnsupportedEncodingException var8) {
                        var8.printStackTrace();
                    }
                }

                LogUtil.i("encodeUrl", sb.toString());
            }
        }

        return sb.toString();
    }

    public boolean hasBinaryData() {
        Set<String> keys = this.mParams.keySet();
        Iterator var2 = keys.iterator();

        Object value;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            String key = (String)var2.next();
            value = this.mParams.get(key);
        } while(!(value instanceof ByteArrayOutputStream) && !(value instanceof Bitmap));

        return true;
    }

    public void setAnonymousCookie(String cookie) {
        this.mAnonymousCookie = cookie;
    }

    public String getAnonymousCookie() {
        return this.mAnonymousCookie;
    }
}
