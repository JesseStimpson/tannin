package com.bandwidth.tannin.db;

import java.util.ArrayList;
import java.util.List;

import com.bandwidth.tannin.data.CallEvent;
import com.bandwidth.tannin.data.DataEvent;
import com.bandwidth.tannin.data.SmsEvent;
import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.data.UnusedWifiEvent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;

public class DatabaseHandler extends SQLiteOpenHelper {
    
    private static final int VERSION = 5;
    private static final String NAME = "tannin.db";
    
    private static final String TABLE_TRANSITIONS = "transitions";
    private static final String TABLE_CALLS = "calls";
    private static final String TABLE_UNUSED_WIFI = "unused_wifi";
    private static final String TABLE_SMS = "sms";
    private static final String TABLE_DATA = "data";
    
    private static final String KEY_ID = "id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_CONNECTIVITY_TYPE = "connectivity_type";
    private static final String KEY_WIFI_AVAILABLE = "wifi_available";
    private static final String KEY_CALL_STATE = "call_state";
    private static final String KEY_WIFI_SECURITY = "wifi_security";
    private static final String KEY_SMS_TYPE = "sms_type";
    private static final String KEY_NUM_BYTES_RX = "num_bytes_rx";
    private static final String KEY_NUM_BYTES_TX = "num_bytes_tx";
    private static final String KEY_INTERFACE = "interface";
    
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
    
    private static final String[] SMS_PROJECTION = new String[]{
    	KEY_ID,
    	KEY_TIMESTAMP,
    	KEY_SMS_TYPE
    };
    
    private static final String[] DATA_PROJECTION = new String[]{
        KEY_ID,
        KEY_TIMESTAMP,
        KEY_INTERFACE,
        KEY_NUM_BYTES_RX,
        KEY_NUM_BYTES_TX
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
    
    private static final int SMS_COLUMN_ID = 0;
    private static final int SMS_COLUMN_TIMESTAMP = 1;
    private static final int SMS_COLUMN_TYPE = 2;
    
    private static final int DATA_COLUMN_ID = 0;
    private static final int DATA_COLUMN_TIMESTAMP = 1;
    private static final int DATA_COLUMN_INTERFACE = 2;
    private static final int DATA_COLUMN_NUM_BYTES_RX = 3;
    private static final int DATA_COLUMN_NUM_BYTES_TX = 4;
    
    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTransitions(db);
        createCalls(db);
        createSms(db);
        createData(db);
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

    private void createSms(SQLiteDatabase db) {
        String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_SMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_TIMESTAMP + " INTEGER, "
                + KEY_SMS_TYPE + " INTEGER" + ")";
        db.execSQL(CREATE_SMS_TABLE);
        
    }
    
    private void createData(SQLiteDatabase db) {
        String CREATE_DATA_TABLE = "CREATE TABLE " + TABLE_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_TIMESTAMP + " INTEGER, "
                + KEY_INTERFACE + " INTEGER, "
                + KEY_NUM_BYTES_RX + " INTEGER, "
                + KEY_NUM_BYTES_TX + " INTEGER" + ")";
        db.execSQL(CREATE_DATA_TABLE);
    }
    
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2) {
            createCalls(db);
            return;
        } else if (oldVersion == 2 && newVersion == 3) {
            createSms(db);
            String CREATE_UNUSED_WIFI_TABLE = "CREATE TABLE " + TABLE_UNUSED_WIFI + "("
                    + KEY_ID + " INTEGER PRIMARY KEY, " 
                    + KEY_TIMESTAMP + " INTEGER, "
                    + KEY_WIFI_SECURITY + " INTEGER" + ")";
            db.execSQL(CREATE_UNUSED_WIFI_TABLE);
            return;
        } else if (oldVersion == 3 && newVersion == 4) {
            createData(db);
            return;
        } else if (oldVersion == 4 && newVersion == 5) {
            dropData(db);
            createData(db);
            return;
        }
        dropTransitions(db);
        dropCalls(db);
        dropSms(db);
        dropData(db);
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
    }
    
    private void dropSms(SQLiteDatabase db) {
        String DROP_SMS_TABLE = "DROP TABLE IF EXISTS " + TABLE_SMS;
        db.execSQL(DROP_SMS_TABLE);
    }
    
    private void dropData(SQLiteDatabase db) {
        String DROP_DATA_TABLE = "DROP TABLE IF EXISTS " + TABLE_DATA;
        db.execSQL(DROP_DATA_TABLE);
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
    
    public void addSmsEvent(SmsEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, event.getTimestamp());
        values.put(KEY_SMS_TYPE, event.getType());
        db.insert(TABLE_SMS, null, values);
        db.close();
    }
    
    public void addDataEvent(DataEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, event.getTimestamp());
        values.put(KEY_INTERFACE, event.getInterface());
        values.put(KEY_NUM_BYTES_RX, event.getNumBytesRx());
        values.put(KEY_NUM_BYTES_TX, event.getNumBytesTx());
        db.insert(TABLE_DATA, null, values);
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
    
    public SmsEvent getSmsEvent(int id) {
        SQLiteDatabase db = getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_SMS, SMS_PROJECTION, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if(cursor!=null)
            cursor.moveToFirst();
        
        int idInt = Integer.parseInt(cursor.getString(SMS_COLUMN_ID));
        long timestamp = Long.parseLong(cursor.getString(SMS_COLUMN_TIMESTAMP));
        int type = Integer.parseInt(cursor.getString(SMS_COLUMN_TYPE));
        
        return new SmsEvent(idInt, timestamp, type);
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
    
    public DataEvent getFirstDataEventBefore(long timestamp) {
        String selectQuery = "SELECT * FROM " + TABLE_DATA + " WHERE "+KEY_TIMESTAMP+" < ?";
        String [] selectArgs = new String[] {String.valueOf(timestamp) };
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        if(cursor.moveToLast()) {
            int id = Integer.parseInt(cursor.getString(DATA_COLUMN_ID));
            long tstamp = Long.parseLong(cursor.getString(DATA_COLUMN_TIMESTAMP));
            int iface = Integer.parseInt(cursor.getString(DATA_COLUMN_INTERFACE));
            long bytesRx = Long.parseLong(cursor.getString(DATA_COLUMN_NUM_BYTES_RX));
            long bytesTx = Long.parseLong(cursor.getString(DATA_COLUMN_NUM_BYTES_TX));
            return new DataEvent(id, tstamp, iface, bytesRx, bytesTx);
        }
        return null;
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
    
    public List<DataEvent> getDataEvents(long fromTimestamp, long toTimestamp) {
        List<DataEvent> eventList = new ArrayList<DataEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_DATA + " WHERE "+KEY_TIMESTAMP+" >= ? AND "+KEY_TIMESTAMP+" <= ? ";
        String [] selectArgs = new String[] {String.valueOf(fromTimestamp), String.valueOf(toTimestamp)};
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        
        if(cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(DATA_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(DATA_COLUMN_TIMESTAMP));
                int iface = Integer.parseInt(cursor.getString(DATA_COLUMN_INTERFACE));
                long bytesRx = Long.parseLong(cursor.getString(DATA_COLUMN_NUM_BYTES_RX));
                long bytesTx = Long.parseLong(cursor.getString(DATA_COLUMN_NUM_BYTES_TX));

                eventList.add(new DataEvent(id, timestamp, iface, bytesRx, bytesTx));
            } while(cursor.moveToNext());
        }
        return eventList;
    }
    
    public UnusedWifiEvent getFirstUnusedWifiEventBefore(long timestamp) {
    	List<UnusedWifiEvent> eventList = new ArrayList<UnusedWifiEvent>();
    	String selectQuery = "SELECT * FROM " + TABLE_UNUSED_WIFI + " WHERE " + KEY_TIMESTAMP + " < ?";
    	String[] selectArgs = new String[] { String.valueOf(timestamp) };
    	
    	SQLiteDatabase db = getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, selectArgs);
    	if(cursor.moveToLast()) {
    		int idInt = Integer.parseInt(cursor.getString(UNUSED_WIFI_COLUMN_ID));
    		long ts = Long.parseLong(cursor.getString(UNUSED_WIFI_COLUMN_TIMESTAMP));
    		int unusedWifiInt = Integer.parseInt(cursor.getString(UNUSED_WIFI_COLUMN_WIFI_SECURITY));
    		return new UnusedWifiEvent(idInt, ts, unusedWifiInt);
    	}
    	return null;
    }
    
    public List<UnusedWifiEvent> getUnusedWifiEvents(long fromTimestamp, long toTimestamp) {
    	List<UnusedWifiEvent> eventList = new ArrayList<UnusedWifiEvent>();
    	String selectQuery = "SELECT * FROM " + TABLE_UNUSED_WIFI + " WHERE " + KEY_TIMESTAMP + " >= ? AND " + KEY_TIMESTAMP + " <= ?";
    	String[] selectArgs = new String[] {String.valueOf(fromTimestamp), String.valueOf(toTimestamp)};
    	
    	SQLiteDatabase db = getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, selectArgs);
    	
    	if(cursor.moveToFirst()) {
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
    
    public List<SmsEvent> getAllSmsEvents() {
        List<SmsEvent> eventList = new ArrayList<SmsEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_SMS;
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if(cursor.moveToFirst()) {
            do {
                int idInt = Integer.parseInt(cursor.getString(SMS_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(SMS_COLUMN_TIMESTAMP));
                int typeInt = Integer.parseInt(cursor.getString(SMS_COLUMN_TYPE));
                
                SmsEvent event = new SmsEvent(idInt, 
                        timestamp,
                        typeInt);
                eventList.add(event);
            } while(cursor.moveToNext());
        }
        return eventList;
    }
    
    public SmsEvent getFirstSmsEventBefore(long timestamp) {
        List<SmsEvent> eventList = new ArrayList<SmsEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_SMS + " WHERE "+KEY_TIMESTAMP+" < ?";
        String [] selectArgs = new String[] {String.valueOf(timestamp) };
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        if(cursor.moveToLast()) {
            int idInt = Integer.parseInt(cursor.getString(SMS_COLUMN_ID));
            long ts = Long.parseLong(cursor.getString(SMS_COLUMN_TIMESTAMP));
            int typeInt = Integer.parseInt(cursor.getString(SMS_COLUMN_TYPE));
            return new SmsEvent(idInt, ts, typeInt);
        }
        return null;
    }
    
    public List<SmsEvent> getSmsEvents(long fromTimestamp, long toTimestamp) {
        List<SmsEvent> eventList = new ArrayList<SmsEvent>();
        String selectQuery = "SELECT * FROM " + TABLE_SMS + " WHERE "+KEY_TIMESTAMP+" >= ? AND "+KEY_TIMESTAMP+" <= ? ";
        String [] selectArgs = new String[] {String.valueOf(fromTimestamp), String.valueOf(toTimestamp)};
        
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectArgs);
        
        if(cursor.moveToFirst()) {
            do {
                int idInt = Integer.parseInt(cursor.getString(SMS_COLUMN_ID));
                long timestamp = Long.parseLong(cursor.getString(SMS_COLUMN_TIMESTAMP));
                int typeInt = Integer.parseInt(cursor.getString(SMS_COLUMN_TYPE));
                
                SmsEvent event = new SmsEvent(idInt, 
                        timestamp,
                        typeInt);

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
    
    public int getSmsEventCount() {
        String countQuery = "SELECT * FROM " + TABLE_SMS;
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

    public void deleteSmsEvent(SmsEvent event) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SMS, KEY_ID + " = ?",
                new String[] { String.valueOf(event.getId()) });
        db.close();
    }
}
