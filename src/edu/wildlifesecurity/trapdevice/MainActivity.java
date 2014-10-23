package edu.wildlifesecurity.trapdevice;


import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;

import edu.wildlife.trapdevice.mediasource.impl.AndroidMediaSource;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.SurveillanceClientManager;
import edu.wildlifesecurity.framework.mediasource.IMediaSource;
import edu.wildlifesecurity.framework.mediasource.MediaEvent;
import edu.wildlifesecurity.trapdevice.R;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private SurveillanceService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_trap_device);
	    //System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	    
	    /*IMediaSource mediaSource = new AndroidMediaSource();
	    mediaSource.addEventHandler(MediaEvent.NEW_SNAPSHOT, new IEventHandler<MediaEvent>(){

			@Override
			public void handle(final MediaEvent event) {
				
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						
						Bitmap bm = Bitmap.createBitmap(event.getImage().cols(), event.getImage().rows(),	Bitmap.Config.ARGB_8888);
						Utils.matToBitmap(event.getImage(), bm);
						ImageView iv = (ImageView) findViewById(R.id.imageView1);
						iv.setImageBitmap(bm);
					}

				});

			}
	    	
	    });
	    
	    SurveillanceClientManager manager = new SurveillanceClientManager(mediaSource, null, null, null);
	    manager.start();*/
	}
	
	public void onStartServiceBtnClick(View view){
		// use this to start and trigger a service
		Intent i= new Intent(this, SurveillanceService.class);
		// potentially add data to the intent
		i.putExtra("KEY1", "Value to be used by the service");
		startService(i); 
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void onStopServiceBtnClick(View view){
		// use this to start and trigger a service
		Intent i= new Intent(this, SurveillanceService.class);
		// potentially add data to the intent
		i.putExtra("KEY1", "Value to be used by the service");
		unbindService(mConnection);
		stopService(i); 
	}
	
	/*@Override
	protected void onResume() {
	    super.onResume();
	    Intent intent= new Intent(this, SurveillanceService.class);
	    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	  }

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
	        ((TextView)findViewById(R.id.statusTextBox)).setText(((TextView)findViewById(R.id.statusTextBox)).getText() + "\nConnected!");
	        
	        if(service.mediaSource != null){
		        service.mediaSource.addEventHandler(MediaEvent.NEW_SNAPSHOT, new IEventHandler<MediaEvent>(){
	
					@Override
					public void handle(final MediaEvent event) {
						
						runOnUiThread(new Runnable() {
	
							@Override
							public void run() {
								
								Bitmap bm = Bitmap.createBitmap(event.getImage().cols(), event.getImage().rows(),	Bitmap.Config.ARGB_8888);
								Utils.matToBitmap(event.getImage(), bm);
								ImageView iv = (ImageView) findViewById(R.id.imageView1);
								iv.setImageBitmap(bm);
							}
	
						});
	
					}
			    	
			    });
			}
	    }

		@Override
	    public void onServiceDisconnected(ComponentName className) {
			service = null;
			//temp.removeHandler();
			((TextView)findViewById(R.id.statusTextBox)).setText(((TextView)findViewById(R.id.statusTextBox)).getText() + "\nDisconnected!");
	    }

	  };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trap_device, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
