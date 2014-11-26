package edu.wildlifesecurity.trapdevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.wildlifesecurity.framework.SurveillanceClientManager;
import edu.wildlifesecurity.framework.communicatorclient.ICommunicatorClient;
import edu.wildlifesecurity.framework.detection.IDetection;
import edu.wildlifesecurity.framework.detection.impl.DefaultDetection;
import edu.wildlifesecurity.framework.identification.IIdentification;
import edu.wildlifesecurity.framework.identification.impl.HOGIdentification;
import edu.wildlifesecurity.framework.mediasource.IMediaSource;
import edu.wildlifesecurity.framework.tracking.impl.KalmanTracking;
import edu.wildlifesecurity.framework.tracking.impl.SerializableCapture;
import edu.wildlifesecurity.trapdevice.communicatorclient.impl.Communicator;
import edu.wildlifesecurity.trapdevice.mediasource.impl.AndroidMediaSource;
import edu.wildlifesecurity.trapdevice.mediasource.impl.VideoMediaSource;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SurveillanceService extends Service {
	private final IBinder binder = new SurveillanceServiceBinder();
	
	public boolean started = false;
	public SurveillanceClientManager manager;
	public IMediaSource mediaSource;
	public IDetection detection;
	public IIdentification identification;
	public ICommunicatorClient communicator;
	public KalmanTracking tracker;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		
		// If not already started, start the service
		if(!started)
			startService();
		
		// Tell the android system to restart the service if it needs to terminate it
		return Service.START_STICKY;
	}

	private void startService(){
		started = true;
		
		// Inject dependencies
		SerializableCapture.encoder = new PngEncoder();
		
		// Create components
		mediaSource = new VideoMediaSource("/storage/sdcard0/Camera1_2.mp4");
		//mediaSource = new AndroidMediaSource();
		detection = new DefaultDetection();
		identification = new HOGIdentification();
		communicator =  new Communicator();
		tracker = new KalmanTracking();
		
		// Load communicator pre configuration
		Map<String,Object> preconfig = new HashMap<String,Object>();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		for(Entry<String, ?> entry : sharedPref.getAll().entrySet())
			preconfig.put(entry.getKey(), entry.getValue());
		communicator.loadConfiguration(preconfig);
		
		// Create manager
		manager = new SurveillanceClientManager(mediaSource, detection, identification, communicator, tracker);
		manager.start();
	}
	
    @Override
    public void onDestroy() {
    	manager.stop();
    	
        // Tell the user we stopped.
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }
    
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	public class SurveillanceServiceBinder extends Binder {
		SurveillanceService getService() {
			return SurveillanceService.this;
		}
	}

}
