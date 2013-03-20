package com.bandwidth.tannin;

import java.util.List;

import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.db.DatabaseHandler;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    DatabaseHandler mDb = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDb = new DatabaseHandler(getBaseContext());
        
        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        refresh();
    }
    
    private void refresh() {
        List<TransitionEvent> events = mDb.getAllTransitionEvents();
        StringBuilder b = new StringBuilder();
        for(TransitionEvent e : events) {
            b.append(e.toString());
            b.append('\n');
        }
        
        TextView text = (TextView) findViewById(R.id.text);
        text.setText(b.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
