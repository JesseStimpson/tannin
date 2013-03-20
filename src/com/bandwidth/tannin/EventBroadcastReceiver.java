package com.bandwidth.tannin;

import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.db.DatabaseHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class EventBroadcastReceiver extends BroadcastReceiver {
    private Context mContext;
    private DatabaseHandler mDbHandler = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) return;
        if(intent.getAction() == null) return;
        mContext = context;
        if(mDbHandler == null) {
            mDbHandler = new DatabaseHandler(mContext);
        }
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            handleConnectivityAction(intent);
        }
    }
    
    private void handleConnectivityAction(Intent intent) {
        ConnectivityManager conman = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conman.getActiveNetworkInfo();
        int connType = -1;
        if(info != null) {
            connType = info.getType();
        }
        TransitionEvent event = new TransitionEvent(-1, System.currentTimeMillis(), connType);
        mDbHandler.addTransitionEvent(event);
    }

}
