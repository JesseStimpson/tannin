package com.bandwidth.tannin.data;

public class UnusedWifiInterval {
    private long startTimestamp;
    private long endTimestamp;
    private UnusedWifiEvent event;
    
    public UnusedWifiInterval(long start, long end, UnusedWifiEvent event) {
        this.startTimestamp = start;
        this.endTimestamp = end;
        this.event = event;
    }
    
    public long getStartTimestamp() { return startTimestamp; }
    public long getEndTimestamp() { return endTimestamp; }
    public UnusedWifiEvent getEvent() { return event; }
}
