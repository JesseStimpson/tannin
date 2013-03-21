package com.bandwidth.tannin.data;

import java.text.DateFormat;
import java.util.Date;

import android.net.ConnectivityManager;

public class TransitionEvent {
    
    private int id;
    private long timestamp;
    private int connectivityType; // one of ConnectivityManager.TYPE_*, or -1 for "none"
    
    public TransitionEvent(int id, long timestamp, int connectivityType) {
        this.id = id;
        this.timestamp = timestamp;
        this.connectivityType = connectivityType;
    }
    
    public int getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public int getConnectivityType() { return connectivityType; }
    
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setToType(int connectivityType) { this.connectivityType = connectivityType; }
    
    private String connectivityString(int type) {
        switch(type) {
        case ConnectivityManager.TYPE_BLUETOOTH:
            return "bluetooth";
        case ConnectivityManager.TYPE_DUMMY:
            return "dummy";
        case ConnectivityManager.TYPE_ETHERNET:
            return "ethernet";
        case ConnectivityManager.TYPE_MOBILE:
            return "mobile";
        case ConnectivityManager.TYPE_MOBILE_DUN:
            return "mobile_dun";
        case ConnectivityManager.TYPE_MOBILE_HIPRI:
            return "mobile_hipri";
        case ConnectivityManager.TYPE_MOBILE_MMS:
            return "mobile_mms";
        case ConnectivityManager.TYPE_MOBILE_SUPL:
            return "mobile_supl";
        case ConnectivityManager.TYPE_WIFI:
            return "wifi";
        case ConnectivityManager.TYPE_WIMAX:
            return "wimax";
        case -1:
            return "none";
            default:
                return "unknown";
        }
    }
    
    public String toString() {
        Date date = new Date(timestamp);
        return date.toLocaleString() + " " + connectivityString(connectivityType);
    }
}
