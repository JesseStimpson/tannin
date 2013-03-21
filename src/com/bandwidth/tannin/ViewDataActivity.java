package com.bandwidth.tannin;

import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import rajawali.RajawaliFragmentActivity;

public class ViewDataActivity extends RajawaliFragmentActivity {
    private ViewDataRenderer mRenderer;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMultisamplingEnabled = true;
        super.onCreate(savedInstanceState);
        mRenderer = new ViewDataRenderer(this);
        mRenderer.setSurfaceView(mSurfaceView);
        super.setRenderer(mRenderer);
        
        View v = getLayoutInflater().inflate(R.layout.view_data_overlay, mLayout);
        TextView dateText = (TextView) v.findViewById(R.id.date_text);
        Date date = new Date(System.currentTimeMillis());
        int monthInt = date.getMonth();
        int dateInt = date.getDate();
        dateText.setText(buildDateString(monthInt, dateInt));
        
        Button showHistory = (Button) findViewById(R.id.show_history);
        showHistory.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                mRenderer.toggleHistory();
            }
        });
        
    }
    
    private String buildDateString(int month, int date) {
        return getMonth(month) + " " + date;
    }
    
    private String getMonth(int month) {
        switch(month) {
        case 0: return "JAN";
        case 1: return "FEB";
        case 2: return "MAR";
        case 3: return "APR";
        case 4: return "MAY";
        case 5: return "JUN";
        case 6: return "JUL";
        case 7: return "AUG";
        case 8: return "SEP";
        case 9: return "OCT";
        case 10: return "NOV";
        case 11: return "DEC";
        }
        return "WTF";
    }

}
