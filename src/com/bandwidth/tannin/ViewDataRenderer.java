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
import com.bandwidth.tannin.db.DatabaseHandler;
import com.bandwidth.tannin.rajawali.Camera2D;
import com.bandwidth.tannin.rajawali.DonutSegment;

import android.content.Context;
import android.net.ConnectivityManager;
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
    
    private float innerRadius = 0.5f*(1-117/361.f);
    private float outerRadius = 0.5f*(1-35/361.f);
    private float bgInRadius = 0.5f*(1-127/361.f);
    private float bgOutRadius = 0.5f*(1-24/361.f);
    
    private float fgOuterRadius = (181/722f)/2f;
    private float fgInnerRadius = (160/722f)/2f;
    
    private float callRadius = fgOuterRadius+0.75f*(bgInRadius-fgOuterRadius);
    
    private float[] segmentColor = new float[] {80.f/255, 185.f/255, 72.f/255, 1.f};
    private float[] callColor = new float[] {0.f,0.f,0.f,0.5f};
    
    private float y = 0.f;
    
    private float timestampToAngle(long timestamp) {
        Date date = new Date(timestamp);
        int hr = date.getHours();
        int m = date.getMinutes();
        int s = date.getSeconds();
        return hourToAngle(hr+m/60.0+s/(60.0*60.0));
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
        drawDonutSegments();
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
    
    private void drawDonutSegments() {
        SimpleMaterial material = new SimpleMaterial();
        material.setUseColor(true);
        
        /*
        DonutSegment seg = new DonutSegment(innerRadius, 
                outerRadius, 
                hourToAngle(22),
                hourToAngle(23.5),
                segmentColor, y, 0);
        seg.setMaterial(material);
        addChild(seg);
        */
        
        List<TransitionInterval> ivals = collapseEvents();
        for(TransitionInterval i : ivals) {
            MyLog.d(i.getEvent().toString());
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
            DonutSegment seg = new DonutSegment(innerRadius, outerRadius, startAngle, endAngle, color, y, 0);
            seg.setMaterial(material);
            addChild(seg);
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
    }
    
    private List<TransitionInterval> collapseEvents() {
        List<TransitionInterval> ivals = new ArrayList<TransitionInterval>();
        
        TransitionEvent currEvent = null;
        int lastConnectivity = -2;
        List<TransitionEvent> events = mDb.getAllTransitionEvents();
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
            ivals.add(new TransitionInterval(currEvent.getTimestamp(), System.currentTimeMillis(), currEvent));
        return ivals;
    }
    
    private List<CallInterval> collapseCallEvents() {
        List<CallInterval> ivals = new ArrayList<CallInterval>();
        
        CallEvent currEvent = null;
        int lastState = -2;
        List<CallEvent> events = mDb.getAllCallEvents();
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
