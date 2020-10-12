package com.useful.client.utils;

import android.util.Log;

public class Logging {

    private static final String TAG = "netty";

    public static void logi(String text) {
        Log.i(TAG, text);
    }

    public static void loge(String text) {
        Log.e(TAG, text);
    }

    public static void logw(String text) {
        Log.w(TAG, text);
    }

    public static void logd(String text) {
        Log.d(TAG, text);
    }
}
