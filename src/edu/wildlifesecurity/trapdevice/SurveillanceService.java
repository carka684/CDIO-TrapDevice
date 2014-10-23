package edu.wildlifesecurity.trapdevice;

import edu.wildlife.trapdevice.mediasource.impl.AndroidMediaSource;
import edu.wildlifesecurity.framework.SurveillanceClientManager;
import edu.wildlifesecurity.framework.communicatorclient.ICommunicatorClient;
import edu.wildlifesecurity.framework.detection.IDetection;
import edu.wildlifesecurity.framework.detection.impl.DefaultDetection;
import edu.wildlifesecurity.framework.identification.IIdentification;
import edu.wildlifesecurity.framework.identification.impl.HOGIdentification;
import edu.wildlifesecurity.framework.mediasource.IMediaSource;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class SurveillanceService extends Service {
	private final IBinder binder = new SurveillanceServiceBinder();
	
	public boolean started = false;
	public SurveillanceClientManager manager;
	public IMediaSource mediaSource;
	public IDetection detection;
	public IIdentification identification;
	public ICommunicatorClient communicator;
	
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
		
		// Create components
		mediaSource = new AndroidMediaSource();
		detection = new DefaultDetection();
		identification = new HOGIdentification();
		communicator = null;
		
		// Create manager
		manager = new SurveillanceClientManager(mediaSource, detection, identification, communicator);
		manager.start();
	}
	
    @Override
    public void onDestroy() {
    	mediaSource.destroy();
    	
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
