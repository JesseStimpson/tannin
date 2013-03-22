package com.bandwidth.tannin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

import com.bandwidth.tannin.data.CallEvent;
import com.bandwidth.tannin.data.CallInterval;
import com.bandwidth.tannin.data.DataEvent;
import com.bandwidth.tannin.data.DataInterval;
import com.bandwidth.tannin.data.SmsEvent;
import com.bandwidth.tannin.data.TransitionEvent;
import com.bandwidth.tannin.data.TransitionInterval;
import com.bandwidth.tannin.data.UnusedWifiEvent;
import com.bandwidth.tannin.data.UnusedWifiInterval;
import com.bandwidth.tannin.db.DatabaseHandler;
import com.bandwidth.tannin.rajawali.Camera2D;
import com.bandwidth.tannin.rajawali.DonutSegment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import rajawali.lights.DirectionalLight;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Number3D;
import rajawali.primitives.Line3D;
import rajawali.renderer.RajawaliRenderer;

public class ViewDataRenderer extends RajawaliRenderer {
    
    private DatabaseHandler mDb;

    public ViewDataRenderer(Context context) {
        super(context);
        
        // Hacky method of determining the aspect ratio required
        WindowManager winman = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = winman.getDefaultDisplay(); 
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        int statusBarHeight = (int)Math.ceil(25 * context.getResources().getDisplayMetrics().density);
        aspectRatio = (double)width/(double)(height-statusBarHeight);
        y = (float)((0.5f/aspectRatio) - (0.5-bgOutRadius)*(53/24f) - bgOutRadius);
        
        setCamera(new Camera2D(aspectRatio));
        setFrameRate(30);
        mDb = new DatabaseHandler(context);
    }
    
    private int hourMin = 0;
    private int hourMax = 24;
    
    private double aspectRatio = 1f;
    
    private float[] innerRadiusList = {0.5f*(1-117/361.f), 0.5f*(1-168/361.f), 0.5f*(1-209/361.f)};
    private float[] outerRadiusList = {0.5f*(1-35/361.f), 0.5f*(1-127/361.f), 0.5f*(1-168/361.f)};
    
    private float bgInRadius = 0.5f*(1-127/361.f);
    private float bgOutRadius = 0.5f*(1-24/361.f);
    
    private float fgOuterRadius = (181/722f)/2f;
    private float fgInnerRadius = (160/722f)/2f;
    
    private float callRadius = fgOuterRadius+0.5f*(bgInRadius-fgOuterRadius);
    private float minDataRadius = fgOuterRadius+0.43f*(bgInRadius-fgOuterRadius);
    private float smsRadius = fgOuterRadius+0.33f*(bgInRadius-fgOuterRadius);
    
    private float needleRadius = 0.495f;
    
    private float dataMaxRadius = 0.5f*(1-168/361.f);
    
    private float[] segmentColor = new float[] {80.f/255, 185.f/255, 72.f/255, 1.f};
    private float[] callColor = new float[] {116/255f,165/255f,124/255f,1f};
    private float[] smsColor = new float[] {0.f,0.f,0.f,0.5f};
    private float[] openWifiColor = new float[] {224.f/255, 224.f/255, 224.f/255, 1.f};
    private float[] dataColor = new float[] {206/255f, 227/255f, 204/255f, 1.f};
    
    private float y = 0.f;
    
    boolean mShowHistory = false;
    List<DonutSegment> historySegments = new ArrayList<DonutSegment>();
    
    long initialDrawTime = 0L;
    
    private float timestampToAngle(long timestamp) {
        Date date = new Date(timestamp);
        int hr = date.getHours();
        int m = date.getMinutes();
        int s = date.getSeconds();
        return hourToAngle(hr+m/60.0+s/(60.0*60.0));
    }
    
    public void showHistory(boolean show) {
        mShowHistory = show;
        if(!mShowHistory) {
            for(DonutSegment seg : historySegments) {
                if(hasChild(seg))
                    removeChild(seg);
            }
        } else {
            for(DonutSegment seg : historySegments) {
               if(!hasChild(seg))
                   addChild(seg);
            }
        }
    }
    
    public void toggleHistory() {
        showHistory(!mShowHistory);
    }
    
    private float hourToAngle(double hr) {
        if(hr < hourMin || hr > hourMax) return -1.f;
        return (float)(
                        (Math.PI/2) + 
                            2*Math.PI*
                                (  (hr-hourMin)/((double)hourMax-hourMin) 
                              )
                      );
    }

    public void initScene() {
        super.initScene();
        
        drawBackground();
        drawWifiCoverage(0);
        drawUnusedWifiCoverage(0);
        drawDataIntervals();
        drawHistory();
        drawCallSegments();
        drawSmsSegments();
        drawAnnotations();
        drawForeground();
    }
    
    private void drawBackground() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        
        setBackgroundColor(0xffededed);
        
        long now = System.currentTimeMillis();
        double angle = timestampToAngle(now);
        DonutSegment seg = new DonutSegment(bgInRadius, bgOutRadius, hourToAngle(0), (float)angle, new float[] {1.f,1.f,1.f,1.f}, y, 1);
        seg.setMaterial(material);
        addChild(seg);
    }
    
    private void drawForeground() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        DonutSegment seg = new DonutSegment(fgInnerRadius, fgOuterRadius, 0.f, (float)(2*Math.PI), new float[] {1.f,1.f,1.f,1.f}, y, -1);
        seg.setMaterial(material);
        addChild(seg);
        
        seg = new DonutSegment(0.f, fgInnerRadius, 0.f, (float)(2*Math.PI), new float[] {51f/255,51f/255,51f/255,1.f}, y, -1.2f);
        seg.setMaterial(material);
        addChild(seg);
    }
    
    private void drawWifiCoverage(int history) {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        
        List<TransitionInterval> ivals = collapseCoverageEvents(history);
        for(TransitionInterval i : ivals) {
            boolean isWifi = i.getEvent().getConnectivityType() == ConnectivityManager.TYPE_WIFI;
            if(!isWifi) continue;
            float[] color = isWifi ? segmentColor :
                                     new float[] {0.2f, 0.2f, 0.2f, 1.f};
            
            long startTs = i.getStartTimestamp();
            Date startDate = new Date(startTs);
            int startHr = startDate.getHours();
            long endTs = i.getEndTimestamp();
            Date endDate = new Date(endTs);
            int endHr = endDate.getHours();
            
            float startAngle = 0;
            if(endHr < startHr) {
                startAngle = hourToAngle(0);
            } else {
                startAngle = timestampToAngle(startTs);
            }
            
            float endAngle = timestampToAngle(i.getEndTimestamp());
            if(startAngle < 0.f || endAngle < 0.f) continue;
            DonutSegment seg = new DonutSegment(innerRadiusList[history], outerRadiusList[history], startAngle, endAngle, color, y, history);
            seg.setMaterial(material);
            
            if(mShowHistory || history == 0) {
                addChild(seg);
            }
            
            if(history > 0) {
                historySegments.add(seg);
            }
        }
    }
    
    private void drawUnusedWifiCoverage(int history){
    	SimpleMaterial material = new SimpleMaterial();
    	material.setUseColor(true);
    	
    	List<UnusedWifiInterval> ivals = collapseUnusedWifiEvents(history);
        for(UnusedWifiInterval i : ivals) {
            boolean isOpen = i.getEvent().getWifiSecurity() == UnusedWifiEvent.WIFI_OPEN;
            if(!isOpen) continue;
            float[] color = isOpen ? openWifiColor :
                                     new float[] {0.2f, 0.2f, 0.2f, 1.f};
            
            long startTs = i.getStartTimestamp();
            Date startDate = new Date(startTs);
            int startHr = startDate.getHours();
            long endTs = i.getEndTimestamp();
            Date endDate = new Date(endTs);
            int endHr = endDate.getHours();
            
            float startAngle = 0;
            if(endHr < startHr) {
                startAngle = hourToAngle(0);
            } else {
                startAngle = timestampToAngle(startTs);
            }
            
            float endAngle = timestampToAngle(i.getEndTimestamp());
            if(startAngle < 0.f || endAngle < 0.f) continue;
            DonutSegment seg = new DonutSegment(innerRadiusList[history], outerRadiusList[history], startAngle, endAngle, color, y, history);
            seg.setMaterial(material);
            
            if(mShowHistory || history == 0) {
                addChild(seg);
            }
            
            if(history > 0) {
                historySegments.add(seg);
            }
        }
    }
    
    private void drawHistory() {
        for(int i = 0; i < innerRadiusList.length; ++i) {
            int history = (innerRadiusList.length-1)-i;
            drawWifiCoverage(history);
        }
    }
    
    private void drawCallSegments() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        List<CallInterval> ivals = collapseCallEvents();
        for(CallInterval i : ivals) {
            boolean isOffhook = i.getEvent().getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
            if(!isOffhook) continue;
            DonutSegment seg = new DonutSegment(0.f, callRadius, 
                    timestampToAngle(i.getStartTimestamp()), 
                    timestampToAngle(i.getEndTimestamp()), 
                    callColor, y, 0.f);
            seg.setMaterial(material);
            addChild(seg);
        }
    }
    
    private void drawSmsSegments() { 
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setHours(0);
        nowDate.setMinutes(0);
        nowDate.setSeconds(0);
        long midnight1 = nowDate.getTime();
        nowDate.setHours(23);
        nowDate.setMinutes(59);
        nowDate.setSeconds(59);
        long midnight2 = nowDate.getTime();
        
        List<SmsEvent> events = mDb.getSmsEvents(midnight1, midnight2);
        for(SmsEvent e : events) {
            long startTimestamp = e.getTimestamp() - 2*60*1000L;
            long endTimestamp = e.getTimestamp() + 2*60*1000L;
            DonutSegment seg = new DonutSegment(0f, 
                    smsRadius, 
                    timestampToAngle(startTimestamp), 
                    timestampToAngle(endTimestamp), 
                    smsColor, 
                    y, 
                    0f);
            seg.setMaterial(material);
            addChild(seg);
        }
    }
    
    private double computeRadius(double area, double angle0, double angle1) {
        return Math.sqrt((2*area)/Math.abs(angle1-angle0));
    }
    
    private double computeRadius2(double area, double angle0, double angle1, double r0) {
        double a = Math.abs(angle0-angle1);
        return Math.sqrt((2*area + a*r0*r0) / a);
    }
    
    private void drawDataIntervals() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        List<DataInterval> ivals = collapseDataEvents();
        
        MyLog.d("num="+ivals.size());
        if(ivals.isEmpty()) return;
        
        // Compute scaling factor
        double maxRadius = 0.f;
        for(DataInterval i : ivals) {
            long numBytes = i.getNumBytesRx() + i.getNumBytesTx();
            if(numBytes == 0L) continue;
            float startAngle = timestampToAngle(i.getStartTimestamp());
            float endAngle = timestampToAngle(i.getEndTimestamp());
            double radius = computeRadius2(numBytes, startAngle, endAngle, fgOuterRadius);
            if(radius > maxRadius) {
                maxRadius = radius;
            }
        }
        double scale = dataMaxRadius/maxRadius;
        
        MyLog.d("scale="+scale);
        
        for(DataInterval i : ivals) {
            long numBytes = i.getNumBytesRx() + i.getNumBytesTx();
            if(numBytes == 0L) continue;
            float startAngle = timestampToAngle(i.getStartTimestamp());
            float endAngle = timestampToAngle(i.getEndTimestamp());
            double radius = scale*computeRadius2(numBytes, startAngle, endAngle, fgOuterRadius);
            radius = Math.max(radius, minDataRadius);
            MyLog.d("radius="+radius);
            DonutSegment seg = new DonutSegment(
                    fgOuterRadius, 
                    (float)(radius), 
                    startAngle, 
                    endAngle, 
                    dataColor, 
                    y, 
                    5.f);
            seg.setMaterial(material);
            addChild(seg);
        }
    }
    
    private void drawAnnotations() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        long now = System.currentTimeMillis();
        double angle = timestampToAngle(now);
        float xN = -(float)(needleRadius*Math.cos(angle)); // Why must this be negated? Something really messed up with our coordinate system... no time for it now
        float yN = (float)(y + needleRadius*Math.sin(angle));
        
        float xH = 0.f;
        float yH = y;
        
        Stack<Number3D> pts = new Stack<Number3D>();
        pts.add(new Number3D(xH, yH, -0.9f));
        pts.add(new Number3D(xN, yN, -0.9f));
        Line3D needle = new Line3D(pts, 5.f, 0xff333333);
        needle.setMaterial(material);
        addChild(needle);
    }
    
    private List<TransitionInterval> collapseCoverageEvents(int history) {
        List<TransitionInterval> ivals = new ArrayList<TransitionInterval>();
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setDate(nowDate.getDate()-history); // TODO - HACK - won't work across month boundaries!
        nowDate.setHours(0);
        nowDate.setMinutes(0);
        nowDate.setSeconds(0);
        long midnight1 = nowDate.getTime();
        nowDate.setHours(23);
        nowDate.setMinutes(59);
        nowDate.setSeconds(59);
        long midnight2 = nowDate.getTime();
        
        TransitionEvent currEvent = null;
        int lastConnectivity = -2;
        List<TransitionEvent> events = mDb.getTransitionEvents(midnight1, midnight2);
        TransitionEvent last = mDb.getFirstTransitionEventBefore(midnight1);
        if(last != null) {
            events.add(0, last);
        }
        for(TransitionEvent e : events) {
            if(lastConnectivity == -2) {
                currEvent = e;
                lastConnectivity = e.getConnectivityType();
                continue;
            }
            
            if(lastConnectivity == e.getConnectivityType()) 
                continue;
            
            ivals.add(new TransitionInterval(currEvent.getTimestamp(), e.getTimestamp(), currEvent));
            lastConnectivity = e.getConnectivityType();
            currEvent = e;
        }
        if(currEvent != null)
            ivals.add(new TransitionInterval(currEvent.getTimestamp(), history == 0 ? System.currentTimeMillis() : midnight2, currEvent));
        return ivals;
    }
    
    private List<DataInterval> collapseDataEvents() {
        List<DataInterval> ivals = new ArrayList<DataInterval>();
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setDate(nowDate.getDate());
        nowDate.setHours(0);
        nowDate.setMinutes(0);
        nowDate.setSeconds(0);
        long midnight1 = nowDate.getTime();
        nowDate.setHours(23);
        nowDate.setMinutes(59);
        nowDate.setSeconds(59);
        long midnight2 = nowDate.getTime();
        
        DataEvent currEvent = null;
        long lastNonZeroUsageTimestamp = 0L;
        long prevEventTimestamp = midnight1;
        long intervalStartTimestamp = 0L;
        
        List<DataEvent> events = mDb.getDataEvents(midnight1, midnight2);
        DataEvent last = mDb.getFirstDataEventBefore(midnight1);
        if(last != null) {
            events.add(0, last);
        }
        
        MyLog.d("numEvents="+events.size());
        
        long numBytesRx = 0L;
        long numBytesTx = 0L;
        
        long dataIntervalTimeout = 10*60*1000L; // 10 mins
        for(DataEvent e : events) {
            MyLog.d("bytes="+(e.getNumBytesRx()+e.getNumBytesTx()));
            boolean thisEventZeroUsage = e.getNumBytesRx() == 0L && e.getNumBytesTx() == 0L;
            if(currEvent == null) {
                currEvent = e;
                numBytesRx = e.getNumBytesRx();
                numBytesTx = e.getNumBytesTx();
                if(!thisEventZeroUsage) lastNonZeroUsageTimestamp = e.getTimestamp();
                prevEventTimestamp = e.getTimestamp();
                intervalStartTimestamp = e.getTimestamp();
                MyLog.d("first event is "+(thisEventZeroUsage?"zero":"nonzero"));
                continue;
            }
            
            boolean thisIntervalZeroUsage = numBytesRx == 0L && numBytesTx == 0L;
            boolean startNewInterval = false;
            
            if(thisIntervalZeroUsage && !thisEventZeroUsage) {
                MyLog.d("found event with usage after period of no usage");
                startNewInterval = true;
            } else if(thisEventZeroUsage && !thisIntervalZeroUsage && (e.getTimestamp()-lastNonZeroUsageTimestamp) >= dataIntervalTimeout) {
                MyLog.d("nonzero interval is over due to long period of zero usage");
                startNewInterval = true;
            }
            
            if(startNewInterval) {
                long startTimestamp = intervalStartTimestamp;
                long endTimestamp = prevEventTimestamp;
                
                startTimestamp = startTimestamp < midnight1 ? midnight1 : startTimestamp;
                
                if(!thisIntervalZeroUsage) {
                    MyLog.d("Adding interval");
                    DataInterval ival = new DataInterval(startTimestamp, endTimestamp, numBytesRx, numBytesTx, currEvent);
                    ivals.add(ival);
                }
                
                MyLog.d("Starting new interval");
                numBytesRx = e.getNumBytesRx();
                numBytesTx = e.getNumBytesTx();
                intervalStartTimestamp = prevEventTimestamp;
                currEvent = e;
            } else {
                MyLog.d("Continuing interval");
                numBytesRx += e.getNumBytesRx();
                numBytesTx += e.getNumBytesTx();
            }

            if(!thisEventZeroUsage) lastNonZeroUsageTimestamp = e.getTimestamp();
            prevEventTimestamp = e.getTimestamp();
        }
        if(numBytesRx+numBytesTx != 0L) {
            MyLog.d("Finishing interval");
            long startTimestamp = intervalStartTimestamp;
            long endTimestamp = prevEventTimestamp;
            startTimestamp = startTimestamp < midnight1 ? midnight1 : startTimestamp;
            DataInterval ival = new DataInterval(startTimestamp, endTimestamp, numBytesRx, numBytesTx, currEvent);
            ivals.add(ival);
        }
        return ivals;
    }
    
    private List<UnusedWifiInterval> collapseUnusedWifiEvents(int history) {
        List<UnusedWifiInterval> ivals = new ArrayList<UnusedWifiInterval>();
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setDate(nowDate.getDate()-history); // TODO - HACK - won't work across month boundaries!
        nowDate.setHours(0);
        nowDate.setMinutes(0);
        nowDate.setSeconds(0);
        long midnight1 = nowDate.getTime();
        nowDate.setHours(23);
        nowDate.setMinutes(59);
        nowDate.setSeconds(59);
        long midnight2 = nowDate.getTime();
        
        UnusedWifiEvent currEvent = null;
        int lastUnusedWifi = -2;
        List<UnusedWifiEvent> events = mDb.getUnusedWifiEvents(midnight1, midnight2);
        UnusedWifiEvent last = mDb.getFirstUnusedWifiEventBefore(midnight1);
        if(last != null) {
            events.add(0, last);
        }
        for(UnusedWifiEvent e : events) {
            if(lastUnusedWifi == -2) {
                currEvent = e;
                lastUnusedWifi = e.getWifiSecurity();
                continue;
            }
            
            if(lastUnusedWifi == e.getWifiSecurity()) 
                continue;
            
            ivals.add(new UnusedWifiInterval(currEvent.getTimestamp(), e.getTimestamp(), currEvent));
            lastUnusedWifi = e.getWifiSecurity();
            currEvent = e;
        }
        if(currEvent != null)
            ivals.add(new UnusedWifiInterval(currEvent.getTimestamp(), history == 0 ? System.currentTimeMillis() : midnight2, currEvent));
        return ivals;
    }
    
    private List<CallInterval> collapseCallEvents() {
        List<CallInterval> ivals = new ArrayList<CallInterval>();
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setHours(0);
        nowDate.setMinutes(0);
        nowDate.setSeconds(0);
        long midnight1 = nowDate.getTime();
        nowDate.setHours(23);
        nowDate.setMinutes(59);
        nowDate.setSeconds(59);
        long midnight2 = nowDate.getTime();
        
        CallEvent currEvent = null;
        int lastState = -2;
        List<CallEvent> events = mDb.getCallEvents(midnight1, midnight2);
        
        CallEvent last = mDb.getFirstCallEventBefore(midnight1);
        if(last != null) {
            events.add(0, last);
        }

        for(CallEvent e : events) {
            if(lastState == -2) {
                currEvent = e;
                lastState = e.getCallState();
                continue;
            }
            
            if(lastState == e.getCallState()) 
                continue;
            
            ivals.add(new CallInterval(currEvent.getTimestamp(), e.getTimestamp(), currEvent));
            lastState = e.getCallState();
            currEvent = e;
        }
        if(currEvent != null)
            ivals.add(new CallInterval(currEvent.getTimestamp(), System.currentTimeMillis(), currEvent));
        return ivals;
    }
    
    private Line3D makeCircle(Number3D center, double radius, int numVerts) {
        double step = 2*Math.PI/numVerts;
        
        Stack<Number3D> pts = new Stack<Number3D>();
        for(int i = 0; i < numVerts; ++i) {
            double angle = i*step;
            Number3D pt = new Number3D(radius*Math.cos(angle), radius*Math.sin(angle), 0);
            pts.add(pt);
        }
        pts.add(pts.firstElement());
        return new Line3D(pts, 1.f, 0xffffff00);
    }
    
    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);
    }
}
