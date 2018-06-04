package com.example.jordoncheng.weiboclient_mvp;

public enum TimelineType {

    HOME_TIMELINE,

    PUBLIC_TIME,

    BILATERAL_TIMELINE;

    public String getPath() {
        String path = null;
        switch (this) {
            case HOME_TIMELINE: path = "HOME_TIMELINE";
                break;
            case PUBLIC_TIME: path = "PUBLIC_TIMELINE";
                break;
            case BILATERAL_TIMELINE: path = "BILATERAL_TIMELINE";
                break;
        }
        return path;
    }
}
