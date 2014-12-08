package edu.wildlifesecurity.trapdevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import edu.wildlifesecurity.framework.SensorData;
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

public class SurveillanceService extends Service {
	private final IBinder binder = new SurveillanceServiceBinder();
	
	public boolean started = false;
	public SurveillanceClientManager manager;
	public IMediaSource mediaSource;
	public IDetection detection;
	public IIdentification identification;
	public ICommunicatorClient communicator;
	public KalmanTracking tracker;

	private ClientLogger logger;
	
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
		
		// Create persisitent notification
		createPersisitentNotification();
		
		// Inject dependencies
		SerializableCapture.encoder = new PngEncoder();
		
		// Create components
		mediaSource = new VideoMediaSource("/storage/sdcard0/Camera1_2.mp4");
		//mediaSource = new AndroidMediaSource();
		detection = new DefaultDetection();
		identification = new HOGIdentification();
		communicator =  new Communicator();
		tracker = new KalmanTracking();
		
		// Create client logger
		logger = new ClientLogger(communicator, this);
				
		// Check and update sd assets
		updateSDAssets();
		
		// Load communicator pre configuration
		Map<String,Object> preconfig = new HashMap<String,Object>();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		for(Entry<String, ?> entry : sharedPref.getAll().entrySet())
			preconfig.put(entry.getKey(), entry.getValue());
		communicator.loadConfiguration(preconfig);
		
		// Create manager
		manager = new SurveillanceClientManager(mediaSource, detection, identification, communicator, tracker, logger);
		
		// Start waiting for sensors..
		// Starts manager when the accuracy is < minAccuracy  or after maxWaitTime (ms)
		SensorListener sensorListener = new SensorListener(1000, 1000*30,8);
		sensorListener.startlistening();		
		
	}
	
	/**
	 * Checks if the assets on the SD card are outdated. If so, update them
	 */
	private void updateSDAssets(){
		AssetManager assetManager = getAssets();
		
		String[] files = new String[] {"wHumanOther.txt", "wRhinoHuman.txt", "wRhinoOther.txt" };
		
		try{
			
			for(String file : files){
				
				// If present on SD, check MD5
				if(new File("storage/sdcard0/"+file).exists()){
					byte[] md5Asset = calcMD5(assetManager.open(file));
					byte[] md5SD = calcMD5(new FileInputStream("storage/sdcard0/"+file));
					
					if(Arrays.equals(md5Asset, md5SD))
						continue;
				}
				// Copy from assets to SD
				copyAssetToSD(assetManager.open(file), file);
			}
			
		}catch(IOException ex){
			logger.warn("Could not check/update SD assets: " + ex.getMessage());
		}

		
	}
	
    @Override
    public void onDestroy() {
    	manager.stop();
    	
    	// Remove persistent notification
    	removePersisitentNotification();
    	
        // Tell the user we stopped.
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }
    
    private byte[] calcMD5(InputStream file) throws IOException{
        InputStream inputStream = file;
        try {
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte [] md5Bytes = digest.digest();
            return md5Bytes;
        } catch (Exception e) {
            return null;
        } finally {
                    inputStream.close();
        }
    }
    
    private void copyAssetToSD(InputStream asset, String name) throws IOException{
	    final FileOutputStream output = new FileOutputStream("storage/sdcard0/" + name);
        try {
            final byte[] buffer = new byte[1024];
            int read;
            while ((read = asset.read(buffer)) != -1)
                output.write(buffer, 0, read);
            output.flush();
        } finally {
            output.close();
        }
    }
    
	private void createPersisitentNotification() //Persistent notification 
	{
	    Intent intent = new Intent(this, MainActivity.class);
	    intent.setAction(Intent.ACTION_MAIN);
	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, 0);
	    
	    Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("TrapDevice");
        builder.setContentText("TrapDevice is running");
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.wildlifesecurity_icon);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(0);
        
        Notification notification = builder.build();
        /*NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); 
        notificationManager.notify(01, notification);*/
        startForeground(1, notification);
	}
    
	private void removePersisitentNotification(){
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(01);
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
	private class SensorListener implements LocationListener, SensorEventListener
	{
		public int heading;
		public double[] GPSPosition;
	    private SensorManager mSensorManager;
	    private Sensor mAccelerometer,mField;
	    private LocationManager locationManager;
	    private long startTime;
	    private long maxWaitTime;
	    private long timeBetweenChecks;
	    private int minAccuracy;
		public SensorListener(int timeBetweenChecks, long maxWaitTime, int minAccuracy) {
	        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	        mField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        this.maxWaitTime = maxWaitTime;
	        this.timeBetweenChecks = timeBetweenChecks;
	        this.minAccuracy = minAccuracy;
	        
	        Timer timer = new Timer();
	        timer.schedule(new TimerTask() {
	          @Override
	          public void run() {
	        	  stopListening();
	          }
	        }, maxWaitTime);
		}
		
		public void startlistening()
		{
		    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeBetweenChecks, 0, this); //Updates GPS postion every 1 second
	        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	        mSensorManager.registerListener(this, mField, SensorManager.SENSOR_DELAY_UI);
	        startTime = System.currentTimeMillis();
	        System.out.println("start");
		}
		
		float[] mGravity = null;
		float[] mGeomagnetic = null;
		@Override
		public void onSensorChanged(SensorEvent event) {
			 switch(event.sensor.getType()) {
		        case Sensor.TYPE_ACCELEROMETER:
		            mGravity = event.values.clone();
		            break;
		        case Sensor.TYPE_MAGNETIC_FIELD:
		        	mGeomagnetic = event.values.clone();
		            break;
		        default:
		            return;
		        }
		        
		        if(mGravity != null && mGeomagnetic != null) {
		            updateDirection();
		        }
		}
	    private void updateDirection() {
	        float[] temp = new float[9];
	        float[] R = new float[9];
	        //Load rotation matrix into R
	        SensorManager.getRotationMatrix(temp, null, mGravity, mGeomagnetic);
	        //Remap to camera's point-of-view
	        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);
	        //Return the orientation values
	        float[] values = new float[3];
	        SensorManager.getOrientation(R, values);
	        //Convert to degrees
	        for (int i=0; i < values.length; i++) {
	            Double degrees = (values[i] * 180) / Math.PI;
	            values[i] = degrees.floatValue();
	        }
	        this.heading = (int) values[0] + 180;	        
	    }
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}

		@Override
		public void onLocationChanged(Location location) {
			double latitude =  (location.getLatitude());
			double longitude =  (location.getLongitude());
			double accuarcy = location.getAccuracy();
			double postion[] = {latitude,longitude,accuarcy};
			this.GPSPosition = postion;
			String s = "Latitude: " + latitude + ", Longitude: " + longitude + " accuary " + accuarcy; 
			if(accuarcy < minAccuracy  || maxWaitTime > (System.currentTimeMillis() - startTime))
				stopListening();
		}
		public void stopListening()
		{
			System.out.println("Stopping");
			locationManager.removeUpdates(this);
			mSensorManager.unregisterListener(this);
			SensorData.GPSPostion = getGPSPosition();
			SensorData.heading = getHeading();
			manager.start();
		}
		public double[] getGPSPosition()
		{
			return GPSPosition;
		}
		public int getHeading()
		{
			return heading;
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onProviderDisabled(String provider) {}
		
	}

}
