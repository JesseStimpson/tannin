package com.bandwidth.tannin;

import android.util.Log;

public class MyLog {
    private static final boolean DEBUG = true;
    private static final String TAG = "Tannin";
    public static void d(String s) {
        if(DEBUG) {
            Log.d(TAG, s);
        }
    }
}
