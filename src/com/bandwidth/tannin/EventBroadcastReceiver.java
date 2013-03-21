package com.bandwidth.tannin;

import java.util.List;

import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.data.UnusedWifiEvent;
import com.bandwidth.tannin.db.DatabaseHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

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
        if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
        	if(connectivityTypeNotWifi()) {
        		handleWifiScanResultsAction(intent);
        	}
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

}
