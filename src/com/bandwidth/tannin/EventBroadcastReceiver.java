package com.bandwidth.tannin;

import com.bandwidth.tannin.data.CallEvent;
import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.db.DatabaseHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

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
        } else if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            handlePhoneState(intent);
        }
    }
    
    private void handleConnectivityAction(Intent intent) {
        ConnectivityManager conman = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conman.getActiveNetworkInfo();
        int connType = -1;
        int wifiAvailability = -1;
        if(info != null && info.isConnectedOrConnecting()) {
            connType = info.getType();
        }
        TransitionEvent event = new TransitionEvent(-1, System.currentTimeMillis(), connType, wifiAvailability);
        mDbHandler.addTransitionEvent(event);
    }
    
    private void handlePhoneState(Intent intent) {
        TelephonyManager telman = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int callState = telman.getCallState();
        if(callState == TelephonyManager.CALL_STATE_RINGING) return;
        CallEvent event = new CallEvent(-1, System.currentTimeMillis(), callState);
        mDbHandler.addCallEvent(event);
    }

}
