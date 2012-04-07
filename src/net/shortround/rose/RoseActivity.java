package net.shortround.rose;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class RoseActivity extends Activity {
	
	public static final int MESSAGE_DECAY = 0;
	public static final int MESSAGE_REVERT = 1;
	public static final int MESSAGE_TOGGLE_DISPLAY = 2;
	
	private BroadcastReceiver batteryReceiver;
	private RoseView roseView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Build the view
        roseView = new RoseView(this);
        roseView.setFocusable(true);
        
        // Build the message handler
        class RoseHandler extends Handler {
        	@Override
        	public void handleMessage(Message msg) {
        		switch (msg.what) {
        			case MESSAGE_DECAY:
        				roseView.decay();
        				break;
        			case MESSAGE_REVERT:
        				roseView.revert();
        				break;
        			case MESSAGE_TOGGLE_DISPLAY:
        				roseView.toggleDisplay();
        				break;
        		}
        	}
        }
        
        // Build the battery receiver
        batteryReceiver = new BroadcastReceiver() {
        	@Override
        	public void onReceive(Context context, Intent intent) {
        		int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        		
        		int level = -1;
        		if (rawLevel  >= 0 && scale > 0) {
        			level = (rawLevel * 100) / scale;
        		}
        		roseView.setBattery(level);
        	}
        };
        
        // Spool up the web server
        WebServer server = WebServer.getInstance();
        server.setAssetManager(this.getAssets());
        server.setHandler(new RoseHandler());
        server.setView(roseView);
        
        // Show the view
        setContentView(roseView);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	// Unregister the battery receiver
    	unregisterReceiver(batteryReceiver);
    }
    
    @Override 
    protected void onResume() {
    	super.onResume();
    	
    	// Register the battery receiver
    	registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    	
        // Go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Keep the screen on
        roseView.setKeepScreenOn(true);
        
        // Hide the soft buttons
        // roseView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
        roseView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }
}