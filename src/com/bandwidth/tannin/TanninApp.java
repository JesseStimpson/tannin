package com.bandwidth.tannin;

import android.app.Application;

public class TanninApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EventBroadcastReceiver.setDataAlarm(getBaseContext());
    }

}
