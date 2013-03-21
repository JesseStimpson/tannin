package com.bandwidth.tannin.db;

import java.util.ArrayList;
import java.util.List;

import com.bandwidth.tannin.data.CallEvent;
import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.data.UnusedWifiEvent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;

public class DatabaseHandler extends SQLiteOpenHelper {
    
    private static final int VERSION = 2;
    private static final String NAME = "tannin.db";
    
    private static final String TABLE_TRANSITIONS = "transitions";
    private static final String TABLE_CALLS = "calls";
    private static final String TABLE_UNUSED_WIFI = "unused_wifi";
    
    private static final String KEY_ID = "id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_CONNECTIVITY_TYPE = "connectivity_type";
    private static final String KEY_WIFI_AVAILABLE = "wifi_available";
    private static final String KEY_CALL_STATE = "call_state";
    private static final String KEY_WIFI_SECURITY = "wifi_security";
    
    private static final String[] TRANSITIONS_PROJECTION = new String[]{
        KEY_ID,
        KEY_TIMESTAMP,
        KEY_CONNECTIVITY_TYPE
    };
    
    private static final String[] UNUSED_WIFI_PROJECTION = new String[]{
    	KEY_ID,
    	KEY_TIMESTAMP,
    	KEY_WIFI_SECURITY
    };
    
    private static final String[] CALLS_PROJECTION = new String[]{
        KEY_ID,
        KEY_TIMESTAMP,
        KEY_CALL_STATE
    };
    
    private static final int TRANSITIONS_COLUMN_ID = 0;
    private static final int TRANSITIONS_COLUMN_TIMESTAMP = 1;
    private static final int TRANSITIONS_COLUMN_CONNECTIVITY_TYPE = 2;
    
    private static final int UNUSED_WIFI_COLUMN_ID = 0;
    private static final int UNUSED_WIFI_COLUMN_TIMESTAMP = 1;
    private static final int UNUSED_WIFI_COLUMN_WIFI_SECURITY = 2;
    
    private static final int CALLS_COLUMN_ID = 0;
    private static final int CALLS_COLUMN_TIMESTAMP = 1;
    private static final int CALLS_COLUMN_CALL_STATE = 2;
    
    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTransitions(db);
        createCalls(db);
    }
    
    private void createTransitions(SQLiteDatabase db) {
        String CREATE_TRANSITIONS_TABLE = "CREATE TABLE " + TABLE_TRANSITIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " 
                + KEY_TIMESTAMP + " INTEGER, "
        		+ KEY_CONNECTIVITY_TYPE + " INTEGER" + ")";
        String CREATE_UNUSED_WIFI_TABLE = "CREATE TABLE " + TABLE_UNUSED_WIFI + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " 
                + KEY_TIMESTAMP + " INTEGER, "
        		+ KEY_WIFI_SECURITY + " INTEGER" + ")";
        db.execSQL(CREATE_TRANSITIONS_TABLE);
        db.execSQL(CREATE_UNUSED_WIFI_TABLE);
    }
    
    private void createCalls(SQLiteDatabase db) {
        String CREATE_CALLS_TABLE = "CREATE TABLE " + TABLE_CALLS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_TIMESTAMP + " INTEGER, "
                + KEY_CALL_STATE + " INTEGER" + ")";
        db.execSQL(CREATE_CALLS_TABLE);
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2) {
            createCalls(db);
            return;
        }
        dropTransitions(db);
        dropCalls(db);
        onCreate(db);
    }
    
    private void dropTransitions(SQLiteDatabase db) {
        String DROP_TRANSITIONS_TABLE = "DROP TABLE IF EXISTS " + TABLE_TRANSITIONS;
        String DROP_UNUSED_WIFI_TABLE = "DROP TABLE IF EXISTS " + TABLE_UNUSED_WIFI;
        db.execSQL(DROP_TRANSITIONS_TABLE);
        db.execSQL(DROP_UNUSED_WIFI_TABLE);
    }
    
    private void dropCalls(SQLiteDatabase db) {
        String DROP_TRANSITIONS_TABLE = "DROP TABLE IF EXISTS " + TABLE_CALLS;
        db.execSQL(DROP_TRANSITIONS_TABLE);
        onCreate(db);
    }
    
    public void addTransitionEvent(TransitionEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, event.getTimestamp());
        values.put(KEY_CONNECTIVITY_TYPE, event.getConnectivityType());
        
        db.insert(TABLE_TRANSITIONS, null, values);
        db.close();
    }
    
    public void addCallEvent(CallEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, event.getTimestamp());
        values.put(KEY_CALL_STATE, event.getCallState());
        db.insert(TABLE_CALLS, null, values);
        db.close();
    }
    
    public void addUnusedWifiEvent(UnusedWifiEvent event) {
    	SQLiteDatabase db = getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(KEY_TIMESTAMP, event.getTimestamp());
    	values.put(KEY_WIFI_SECURITY, event.getWifiSecurity());
    	
    	db.insert(TABLE_UNUSED_WIFI, null, values);
    	db.close();
    }
    
    public TransitionEvent getTransitionEvent(int id) {
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_TRANSITIONS, TRANSITIONS_PROJECTION, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if(cursor!=null)
            cursor.moveToFirst();
        
        int idInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_ID));
        long timestamp = Long.parseLong(cursor.getString(TRANSITIONS_COLUMN_TIMESTAMP));
        int connectivityInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_CONNECTIVITY_TYPE));
        
        TransitionEvent event = new TransitionEvent(idInt, timestamp, 
                connectivityInt);
        return event;
    }
    
    public CallEvent getCallEvent(int id) {
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CALLS, CALLS_PROJECTION, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if(cursor!=null)
            cursor.moveToFirst();
        
        int idInt = Integer.parseInt(cursor.getString(CALLS_COLUMN_ID));
        long timestamp = Long.parseLong(cursor.getString(CALLS_COLUMN_TIMESTAMP));
        int callType = Integer.parseInt(cursor.getString(CALLS_COLUMN_CALL_STATE));
        
        return new CallEvent(idInt, timestamp, callType);
    }
    
    public UnusedWifiEvent getUnusedWifiEvent(int id) {
    	SQLiteDatabase db = getReadableDatabase();
    	
    	Cursor cursor = db.query(TABLE_UNUSED_WIFI, UNUSED_WIFI_PROJECTION, KEY_ID + "=?",
    			new String[] { String.valueOf(id) }, null, null, null, null);
    	if(cursor!=null)
    		cursor.moveToFirst();
    	int idInt = Integer.parseInt(cursor.getString(UNUSED_WIFI_COLUMN_ID));
    	long timestamp = Long.parseLong(cursor.getString(UNUSED_WIFI_COLUMN_TIMESTAMP));
    	int unusedWifiInt = Integer.parseInt(cursor.getString(UNUSED_WIFI_COLUMN_WIFI_SECURITY));
    	
    	UnusedWifiEvent event = new UnusedWifiEvent(idInt, timestamp, unusedWifiInt);
    	return event;
    }
    
    public List<TransitionEvent> getAllTransitionEvents() {
        List<TransitionEvent> eventList = new ArrayList<TransitionEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSITIONS;
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if(cursor.moveToFirst()) {
            do {
                int idInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(TRANSITIONS_COLUMN_TIMESTAMP));
                int connectivityInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_CONNECTIVITY_TYPE));
                
                TransitionEvent event = new TransitionEvent(idInt, 
                        timestamp,
                        connectivityInt);
                eventList.add(event);
            } while(cursor.moveToNext());
        }
        return eventList;
    }
    
    public TransitionEvent getFirstTransitionEventBefore(long timestamp) {
        List<TransitionEvent> eventList = new ArrayList<TransitionEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSITIONS + " WHERE "+KEY_TIMESTAMP+" < ?";
        String [] selectArgs = new String[] {String.valueOf(timestamp) };
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        if(cursor.moveToLast()) {
            int idInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_ID));
            long ts = Long.parseLong(cursor.getString(TRANSITIONS_COLUMN_TIMESTAMP));
            int connectivityInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_CONNECTIVITY_TYPE));
            return new TransitionEvent(idInt, ts, connectivityInt);
        }
        return null;
    }
    
    public List<TransitionEvent> getTransitionEvents(long fromTimestamp, long toTimestamp) {
        List<TransitionEvent> eventList = new ArrayList<TransitionEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSITIONS + " WHERE "+KEY_TIMESTAMP+" >= ? AND "+KEY_TIMESTAMP+" <= ? ";
        String [] selectArgs = new String[] {String.valueOf(fromTimestamp), String.valueOf(toTimestamp)};
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        
        if(cursor.moveToFirst()) {
            do {
                int idInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(TRANSITIONS_COLUMN_TIMESTAMP));
                int connectivityInt = Integer.parseInt(cursor.getString(TRANSITIONS_COLUMN_CONNECTIVITY_TYPE));
                
                TransitionEvent event = new TransitionEvent(idInt, 
                        timestamp,
                        connectivityInt);

                eventList.add(event);
            } while(cursor.moveToNext());
        }
        return eventList;
    }
    
    public List<CallEvent> getAllCallEvents() {
        List<CallEvent> eventList = new ArrayList<CallEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_CALLS;
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if(cursor.moveToFirst()) {
            do {
                int idInt = Integer.parseInt(cursor.getString(CALLS_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(CALLS_COLUMN_TIMESTAMP));
                int callType = Integer.parseInt(cursor.getString(CALLS_COLUMN_CALL_STATE));
                
                CallEvent event = new CallEvent(idInt, 
                        timestamp,
                        callType);
                eventList.add(event);
            } while(cursor.moveToNext());
        }
        return eventList;
    }
    
    public List<UnusedWifiEvent> getAllUnusedWifiEvents() {
    	List<UnusedWifiEvent> eventList = new ArrayList<UnusedWifiEvent>();
    	String selectQuery = "SELECT * FROM " + TABLE_UNUSED_WIFI;
    	
    	SQLiteDatabase db = getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);
    	
    	if(cursor.moveToFirst()){
    		do {
    			int idInt = Integer.parseInt(cursor.getString(UNUSED_WIFI_COLUMN_ID));
    			long timestamp = Long.parseLong(cursor.getString(UNUSED_WIFI_COLUMN_TIMESTAMP));
    			int unusedWifiInt = Integer.parseInt(cursor.getString(UNUSED_WIFI_COLUMN_WIFI_SECURITY));
    			
    			UnusedWifiEvent event = new UnusedWifiEvent(idInt, timestamp, unusedWifiInt);
    			eventList.add(event);
    		} while(cursor.moveToNext());
    	}
    	return eventList;
    }
    
    public CallEvent getFirstCallEventBefore(long timestamp) {
        List<CallEvent> eventList = new ArrayList<CallEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_CALLS + " WHERE "+KEY_TIMESTAMP+" < ?";
        String [] selectArgs = new String[] {String.valueOf(timestamp) };
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        if(cursor.moveToLast()) {
            int idInt = Integer.parseInt(cursor.getString(CALLS_COLUMN_ID));
            long ts = Long.parseLong(cursor.getString(CALLS_COLUMN_TIMESTAMP));
            int callState = Integer.parseInt(cursor.getString(CALLS_COLUMN_CALL_STATE));
            return new CallEvent(idInt, ts, callState);
        }
        return null;
    }
    
    public List<CallEvent> getCallEvents(long fromTimestamp, long toTimestamp) {
        List<CallEvent> eventList = new ArrayList<CallEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_CALLS + " WHERE "+KEY_TIMESTAMP+" >= ? AND "+KEY_TIMESTAMP+" <= ? ";
        String [] selectArgs = new String[] {String.valueOf(fromTimestamp), String.valueOf(toTimestamp)};
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        
        if(cursor.moveToFirst()) {
            do {
                int idInt = Integer.parseInt(cursor.getString(CALLS_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(CALLS_COLUMN_TIMESTAMP));
                int callState = Integer.parseInt(cursor.getString(CALLS_COLUMN_CALL_STATE));
                
                CallEvent event = new CallEvent(idInt, 
                        timestamp,
                        callState);
                eventList.add(event);
            } while(cursor.moveToNext());
        }
        return eventList;
    }
    
    public int getTransitionEventCount() {
        String countQuery = "SELECT * FROM " + TABLE_TRANSITIONS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }
    
    public int getUnusedWifiEventCount() {
    	String countQuery = "SELECT * FROM " + TABLE_UNUSED_WIFI;
    	SQLiteDatabase db = getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);
    	cursor.close();
    	return cursor.getCount();
    }
    
    public void deleteTransitionEvent(TransitionEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRANSITIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(event.getId()) });
        db.close();
    }
    
    public void deleteUnusedWifiEvent(UnusedWifiEvent event) {
    	SQLiteDatabase db = getWritableDatabase();
    	db.delete(TABLE_UNUSED_WIFI, KEY_ID + " =?", new String[] { String.valueOf(event.getId()) });
    	db.close();
    }

}
