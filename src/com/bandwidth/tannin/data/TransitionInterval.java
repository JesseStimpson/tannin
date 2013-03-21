package com.bandwidth.tannin.data;

public class TransitionInterval {
    private long startTimestamp;
    private long endTimestamp;
    private TransitionEvent event;
    
    public TransitionInterval(long start, long end, TransitionEvent event) {
        this.startTimestamp = start;
        this.endTimestamp = end;
        this.event = event;
    }
    
    public long getStartTimestamp() { return startTimestamp; }
    public long getEndTimestamp() { return endTimestamp; }
    public TransitionEvent getEvent() { return event; }
}
