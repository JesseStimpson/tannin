package com.bandwidth.tannin.db;

import java.util.ArrayList;
import java.util.List;

import com.bandwidth.tannin.data.TransitionEvent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    
    private static final int VERSION = 1;
    private static final String NAME = "tannin.db";
    
    private static final String TABLE_TRANSITIONS = "transitions";
    
    private static final String KEY_ID = "id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_CONNECTIVITY_TYPE = "connectivity_type";
    
    private static final String[] TRANSITIONS_PROJECTION = new String[]{
        KEY_ID,
        KEY_TIMESTAMP,
        KEY_CONNECTIVITY_TYPE
    };
    
    private static final int TRANSITIONS_COLUMN_ID = 0;
    private static final int TRANSITIONS_COLUMN_TIMESTAMP = 1;
    private static final int TRANSITIONS_COLUMN_CONNECTIVITY_TYPE = 2;
    
    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRANSITIONS_TABLE = "CREATE TABLE " + TABLE_TRANSITIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " 
                + KEY_TIMESTAMP + " INTEGER, "
                + KEY_CONNECTIVITY_TYPE + " INTEGER" + ")";
        db.execSQL(CREATE_TRANSITIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_TRANSITIONS_TABLE = "DROP TABLE IF EXISTS " + TABLE_TRANSITIONS;
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
    
    public int getTransitionEventCount() {
        String countQuery = "SELECT * FROM " + TABLE_TRANSITIONS;
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

}
