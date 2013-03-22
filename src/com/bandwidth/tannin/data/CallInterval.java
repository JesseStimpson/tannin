package com.bandwidth.tannin.data;

public class CallInterval {
    private long startTimestamp;
    private long endTimestamp;
    private CallEvent event;
    
    public CallInterval(long start, long end, CallEvent event) {
        this.startTimestamp = start;
        this.endTimestamp = end;
        this.event = event;
    }
    
    public long getStartTimestamp() { return startTimestamp; }
    public long getEndTimestamp() { return endTimestamp; }
    public CallEvent getEvent() { return event; }
}
