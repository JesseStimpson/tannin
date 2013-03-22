package com.bandwidth.tannin.data;

public class SmsEvent {
	public static final int INCOMING = 0;
	public static final int OUTGOING = 1;
	
	private int id;
    private long timestamp;
    private int type;
    
    public SmsEvent(int id, long timestamp, int state) {
    	this.id = id;
    	this.timestamp = timestamp;
    	this.type = state;
    }

	public int getId() { return id; }
	public long getTimestamp() { return timestamp; }
	public int getType() { return type; }
	
	public void setId(int id) { this.id = id; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	public void setType(int type) { this.type = type; }
}
