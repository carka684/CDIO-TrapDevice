package edu.wildlifesecurity.trapdevice;


import org.opencv.core.Scalar;

import android.R.menu;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.detection.DetectionEvent;
import edu.wildlifesecurity.framework.tracking.TrackingEvent;


public class MainActivity extends Activity {
	
	private SurveillanceService service;
	private Drawer drawer;
	private boolean showRaw = true;
	private boolean showTrack = true;
	private Menu menu;
	
	public void notificationHandler() //Persistent notification 
	{
		setContentView(R.layout.activity_trap_device);
	    Intent intent = new Intent(this, MainActivity.class);
	    intent.setAction(Intent.ACTION_MAIN);
	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, 0);
	    Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("TrapDevice");
        builder.setContentText("TrapDevice is running");
        builder.setContentIntent(pendingIntent);
        //builder.setTicker("Starting TrapDevice); 
        builder.setSmallIcon(R.drawable.wildlifesecurity_icon);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setPriority(0);
        Notification notification = builder.build();
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); 
        notificationManager.notify(01, notification);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	
	@Override
	protected void onResume() {
	   super.onResume();
	   notificationHandler();
	    //Intent intent= new Intent(this, SurveillanceService.class);
		//bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
      
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
	        menu.findItem(R.id.serviceConnected).setTitle("Connected"); //set no connection to connected
	        //((TextView)findViewById(R.id.statusTextBox)).setText(((TextView)findViewById(R.id.statusTextBox)).getText() + "\nConnected!");
	        
	        
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
								//Bitmap bm = drawer.getBitmap();
								//ImageView iv = (ImageView) findViewById(R.id.imageView1);
								//iv.setImageBitmap(bm);
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
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(01);
			//temp.removeHandler();
			//((TextView)findViewById(R.id.statusTextBox)).setText(((TextView)findViewById(R.id.statusTextBox)).getText() + "\nDisconnected!");
			
	    }

	  };
	  
	  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trap_device, menu);
		this.menu = menu;
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
		if (id == R.id.start) {
			menu.findItem(R.id.serviceConnected).setTitle("Connecting"); //set no connection to connecting
			// use this to start and trigger a service
			Intent i= new Intent(this, SurveillanceService.class);
			// potentially add data to the intent
			i.putExtra("KEY1", "Value to be used by the service");
			startService(i); 
			bindService(i, mConnection, Context.BIND_AUTO_CREATE);
			return true;
		}		
		if (id == R.id.stop) {
			
			// use this to start and trigger a service
			Intent i= new Intent(this, SurveillanceService.class);
			// potentially add data to the intent
			i.putExtra("KEY1", "Value to be used by the service");
			unbindService(mConnection);
			stopService(i); 
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(01);
			
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
}
