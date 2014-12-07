package edu.wildlifesecurity.trapdevice;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.communicatorclient.ConnectEvent;
import edu.wildlifesecurity.framework.detection.DetectionEvent;
import edu.wildlifesecurity.framework.tracking.TrackingEvent;


public class MainActivity extends Activity {
	
	private SurveillanceService service;
	private Drawer drawer;
	private boolean showRaw = true;
	private boolean showTrack = true;
	private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trap_device);
		
		if(isMyServiceRunning(SurveillanceService.class)){
			Intent intent= new Intent(this, SurveillanceService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();	
		
	    //Intent intent= new Intent(this, SurveillanceService.class);
		//bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(service != null)
			unbindService(mConnection);
	}
/*
	@Override
	protected void onPause() {
	    super.onPause();
	    unbindService(mConnection);
	}*/
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
	    public void onServiceConnected(ComponentName className, IBinder binder) {
			SurveillanceService.SurveillanceServiceBinder b = (SurveillanceService.SurveillanceServiceBinder) binder;
	        service = b.getService();
	        
	        // Update connection label
	        updateConnectionLabel(service.communicator.isConnected());
	        service.communicator.addConnectionEventHandler(ConnectEvent.CONNECTED, new IEventHandler<ConnectEvent>(){

				@Override
				public void handle(final ConnectEvent event) {
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							updateConnectionLabel(event.isConnected());
						}
						
					});
				}
	        	
	        });
	        
	        if(service.tracker != null)
	        {
	        	service.tracker.addEventHandler(TrackingEvent.NEW_TRACK, new IEventHandler<TrackingEvent>(){
	        		
					@Override
					public void handle(final TrackingEvent event) {
						
						runOnUiThread(new Runnable() {
	
							@Override
							public void run() {		
								if(showTrack)
								drawer.addRect(event.getRegion(),event.getCapture().classification);
							}
						});
					}
			    });
	        }
	        if(service.detection != null){
		        service.detection.addEventHandler(DetectionEvent.NEW_DETECTION, new IEventHandler<DetectionEvent>(){
	
					@Override
					public void handle(final DetectionEvent event) {
						runOnUiThread(new Runnable() {
	
							@Override
							public void run() {
								if(drawer != null)
								{
									Bitmap bm = drawer.getBitmap();
									ImageView iv = (ImageView) findViewById(R.id.imageView1);
									iv.setImageBitmap(bm);		
								}
								drawer = new Drawer();
								if(showRaw)
									drawer.setBackground(event.getDetectionResult().getRawDetection());
								else
									drawer.setBackground(event.getDetectionResult().getOriginalImage());
														
							}
							
						});
					}
			    });
			}
	    }
		
		@Override
	    public void onServiceDisconnected(ComponentName className) {
			service = null;
	    }

	  };
	  
	private void updateConnectionLabel(boolean isConnected){
        if(isConnected)
        	menu.findItem(R.id.serviceConnected).setTitle("CONNECTED"); //set no connection to connected
        else
        	menu.findItem(R.id.serviceConnected).setTitle("NO CONNECTION");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trap_device, menu);
		this.menu = menu;
		
		// Update start/stop button
		MenuItem startStopMenuItem = menu.findItem(R.id.startStop);
		if(isMyServiceRunning(SurveillanceService.class))
			startStopMenuItem.setTitle("Stop");
		else
			startStopMenuItem.setTitle("Start");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.IdentifiedImage1) {
			//Settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
			return true;
		}
		if (id == R.id.startStop) {
			MenuItem startStopMenuItem = menu.findItem(R.id.startStop);
			if(isMyServiceRunning(SurveillanceService.class)){
				// Stop service if running
				Intent i= new Intent(this, SurveillanceService.class);
				if(service != null)
					unbindService(mConnection);
				stopService(i);
				startStopMenuItem.setTitle("Start");
				menu.findItem(R.id.serviceConnected).setTitle("NO CONNECTION");
				
			}else{
				// Start service / bind service
				Intent i= new Intent(this, SurveillanceService.class);
				startService(i); 
				bindService(i, mConnection, Context.BIND_AUTO_CREATE);
				
				startStopMenuItem.setTitle("Stop");
				
			}
			return true;
		}
		if (id == R.id.swapBackground) {
			showRaw = !showRaw;
			//TODO: Switch some global variable
			return true;
		}
		if (id == R.id.showTracking) {
			showTrack = !showTrack;
			//TODO: Switch some global variable
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
