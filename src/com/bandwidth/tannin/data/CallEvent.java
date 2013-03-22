package com.bandwidth.tannin.data;

public class CallEvent {
    private int id;
    private long timestamp;
    private int callState;
    
    public CallEvent(int id, long timestamp, int callState) {
        this.id = id;
        this.timestamp = timestamp;
        this.callState = callState;
    }
    
    public int getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public int getCallState() { return callState;}
    
    public void setId(int id) { this.id = id; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCallState(int callState) { this.callState = callState; }
}
