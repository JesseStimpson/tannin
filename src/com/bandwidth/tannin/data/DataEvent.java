package com.bandwidth.tannin.data;

public class DataEvent {
    private int id;
    private long timestamp;
    private int iface;
    private long numBytesRx;
    private long numBytesTx;
    
    public static final int IFACE_MOBILE = 0;
    
    public DataEvent(int id, long timestamp, int iface, long numBytesRx, long numBytesTx) {
        this.id = id;
        this.timestamp = timestamp;
        this.iface = iface;
        this.numBytesRx = numBytesRx;
        this.numBytesTx = numBytesTx;
    }
    
    public int getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public int getInterface() { return iface; }
    public long getNumBytesRx() { return numBytesRx; }
    public long getNumBytesTx() { return numBytesTx; }
}
