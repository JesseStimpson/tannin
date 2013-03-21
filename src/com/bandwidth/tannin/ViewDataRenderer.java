package com.bandwidth.tannin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

import com.bandwidth.tannin.data.CallEvent;
import com.bandwidth.tannin.data.CallInterval;
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
    
    private float callRadius = fgOuterRadius+0.75f*(bgInRadius-fgOuterRadius);
    
    private float[] segmentColor = new float[] {80.f/255, 185.f/255, 72.f/255, 1.f};
    private float[] callColor = new float[] {0.f,0.f,0.f,0.5f};
    private float[] openWifiColor = new float[] {224.f/255, 224.f/255, 224.f/255, 1.f};
    
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
        drawHistory();
        drawCallSegments();
        drawAnnotations();
        drawForeground();
    }
    
    private void drawBackground() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        
        setBackgroundColor(0xffededed);
        DonutSegment seg = new DonutSegment(bgInRadius, bgOutRadius, 0.f, (float)(2*Math.PI), new float[] {1.f,1.f,1.f,1.f}, y, 1);
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
            MyLog.d(i.getEvent().toString());
            boolean isWifi = i.getEvent().getConnectivityType() == ConnectivityManager.TYPE_WIFI;
            if(!isWifi) continue;
            float[] color = isWifi ? openWifiColor :
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
            MyLog.d(i.getEvent().toString());
            boolean isOpen = i.getEvent().getWifiSecurity() == UnusedWifiEvent.WIFI_OPEN;
            if(!isOpen) continue;
            float[] color = isOpen ? segmentColor :
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
    
    private void drawAnnotations() {
        /*
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        int numHours = 1+hourMax-hourMin;
        for(int i = 0; i < numHours-1; ++i) {
            int hr = hourMin+i;
            float angle = hourToAngle(hr);
            Stack<Number3D> pts = new Stack<Number3D>();
            pts.add(new Number3D(outerRadius*Math.cos(angle), y+outerRadius*Math.sin(angle), 0.f));
            float r = outerRadius+0.015f;
            pts.add(new Number3D(r*Math.cos(angle), y+r*Math.sin(angle), 0.f));
            Line3D tick = new Line3D(pts, 1.f, 0xffffffff);
            tick.setMaterial(material);
            addChild(tick);
        }
        */
    }
    
    private List<TransitionInterval> collapseCoverageEvents(int history) {
        List<TransitionInterval> ivals = new ArrayList<TransitionInterval>();
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setDate(nowDate.getDate()-history); // TODO - HACK - won't work across month boundaries!
        nowDate.setHours(0);
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
    
    private List<UnusedWifiInterval> collapseUnusedWifiEvents(int history) {
        List<UnusedWifiInterval> ivals = new ArrayList<UnusedWifiInterval>();
        
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        nowDate.setDate(nowDate.getDate()-history); // TODO - HACK - won't work across month boundaries!
        nowDate.setHours(0);
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
