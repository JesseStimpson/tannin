package com.bandwidth.tannin;

import com.bandwidth.tannin.data.CallEvent;
import java.util.List;

import com.bandwidth.tannin.data.DataEvent;
import com.bandwidth.tannin.data.SmsEvent;
import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.data.UnusedWifiEvent;
import com.bandwidth.tannin.db.DatabaseHandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class EventBroadcastReceiver extends BroadcastReceiver {
    private Context mContext;
    private DatabaseHandler mDbHandler = null;
    
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    
    private static final String ALARM_ACTION = EventBroadcastReceiver.class.getCanonicalName() + ".ALARM";
    private static final long TARGET_SAMPLE_PERIOD_MSEC = 5*60*1000L;
    
    public static void setDataAlarm(Context context) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, EventBroadcastReceiver.class);
        i.setAction(ALARM_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TARGET_SAMPLE_PERIOD_MSEC, pi);
    }

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
        } else if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
        	handleSmsReceived(intent);
        } else if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
        	if(connectivityTypeNotWifi()) {
        		handleWifiScanResultsAction(intent);
        	}
        } else if (intent.getAction().equals(ALARM_ACTION)) {
            handleDataAlarm();
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
        TransitionEvent event = new TransitionEvent(-1, System.currentTimeMillis(), connType);
        mDbHandler.addTransitionEvent(event);
    }
    
    private void handlePhoneState(Intent intent) {
        TelephonyManager telman = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int callState = telman.getCallState();
        if(callState == TelephonyManager.CALL_STATE_RINGING) return;
        CallEvent event = new CallEvent(-1, System.currentTimeMillis(), callState);
        mDbHandler.addCallEvent(event);
	}

    private boolean connectivityTypeNotWifi() {
    	ConnectivityManager conman = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conman.getActiveNetworkInfo();
        int connType = -1;
        if (info != null) {
        	connType = info.getType();
        }
        if (connType != ConnectivityManager.TYPE_WIFI) {
        	return true;
        }
    	return false;
    }
    
    private int wifiResultsParser(List<ScanResult> results){
    	final String[] securityModes = { "WEP", "PSK", "EAP" };
    	boolean secure = false;
    	
    	if(results.size() == 0){
    		return UnusedWifiEvent.WIFI_DOES_NOT_EXIST;
    	}
    	
    	for(int i = 0; i < results.size(); i++) {
    		for(int j = 0; j < securityModes.length; j++) {
    			secure = false;
    			if(results.get(i).capabilities.contains(securityModes[j])){
    				secure = true;
    				break;
    			}
    		}
    		if(!secure){
    			return UnusedWifiEvent.WIFI_OPEN;
    		}
    	}
    	return UnusedWifiEvent.WIFI_PROTECTED;
    }
    
    private void handleWifiScanResultsAction(Intent intent) {
    	WifiManager wifiman = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    	List<ScanResult> results = wifiman.getScanResults();
    	if(results.size() > 0){
    		UnusedWifiEvent event = new UnusedWifiEvent(-1, System.currentTimeMillis(), wifiResultsParser(results));
    		mDbHandler.addUnusedWifiEvent(event);
    	}
    }

    private void handleSmsReceived(Intent intent) {
    	Bundle bundle = intent.getExtras();
    	if (bundle == null) {
    		return;
    	}

    	SmsEvent event = new SmsEvent(-1, System.currentTimeMillis(), SmsEvent.INCOMING);
    	mDbHandler.addSmsEvent(event);
    }
    
    private long getLastMobileRx() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getLong("mobile_rx", -1L);
    }
    
    private long getLastMobileTx() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getLong("mobile_tx", -1L);
    }
    
    private void setMobileRx(long m) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putLong("mobile_rx", m).commit();
    }
    
    private void setMobileTx(long m) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putLong("mobile_tx", m).commit();
    }
    
    private void handleDataAlarm() {
        long mobileRx = TrafficStats.getMobileRxBytes();
        long mobileTx = TrafficStats.getMobileTxBytes();
        long lastMobileRx = getLastMobileRx();
        long lastMobileTx = getLastMobileTx();
        setMobileRx(mobileRx);
        setMobileTx(mobileTx);
        if(lastMobileRx < 0L || lastMobileTx < 0L) {
            return;
        }
        
        long mobileRxDiff = mobileRx >= lastMobileRx ? mobileRx - lastMobileRx : mobileRx;
        long mobileTxDiff = mobileTx >= lastMobileTx ? mobileTx - lastMobileTx : mobileTx;
        
        MyLog.d("logging data event");
        
        mDbHandler.addDataEvent(new DataEvent(-1, 
                System.currentTimeMillis(), 
                DataEvent.IFACE_MOBILE, 
                mobileRxDiff, 
                mobileTxDiff));
    }
}
