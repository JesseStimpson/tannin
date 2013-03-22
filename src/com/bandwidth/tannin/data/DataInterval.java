package com.bandwidth.tannin.data;

public class DataInterval {
    private long startTimestamp;
    private long endTimestamp;
    private long numBytesRx;
    private long numBytesTx;
    private DataEvent event;
    
    public DataInterval(long startTimestamp, long endTimestamp,
            long numBytesRx, long numBytesTx, DataEvent event) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.numBytesRx = numBytesRx;
        this.numBytesTx = numBytesTx;
        this.event = event;
    }
    
    public long getStartTimestamp() { return startTimestamp; }
    public long getEndTimestamp() { return endTimestamp; }
    public long getNumBytesRx() { return numBytesRx; }
    public long getNumBytesTx() { return numBytesTx; }
    public DataEvent getEvent() { return event; }
}
