package com.bandwidth.tannin.data;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;

public class UnusedWifiEvent {
    
    private int id;
    private long timestamp;
    private int wifiSecurity;
    
    public static final int WIFI_PROTECTED = 1;
    public static final int WIFI_OPEN = 2;
    public static final int WIFI_DOES_NOT_EXIST = 0;
    
    public UnusedWifiEvent(int id, long timestamp, int wifiSecurity) {
        this.id = id;
        this.timestamp = timestamp;
        this.wifiSecurity = wifiSecurity;
    }
    
    public int getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public int getWifiSecurity() { return wifiSecurity; }
    
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setWifiSecurity(int wifiSecurity) { this.wifiSecurity = wifiSecurity; }
    
    private String wifiSecurityString(int wifiSecurity) {
    	switch(wifiSecurity) {
    	case WIFI_PROTECTED:
    		return "Protected";
    	case WIFI_OPEN:
    		return "Open";
    	case WIFI_DOES_NOT_EXIST:
    		return "None";
    	default:
    		return "Unknown";
    	}
    }
        
    public String toString() {
        Date date = new Date(timestamp);
        return date.toLocaleString() + " " + wifiSecurityString(wifiSecurity);
    }
}
