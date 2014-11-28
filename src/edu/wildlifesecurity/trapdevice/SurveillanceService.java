package edu.wildlifesecurity.trapdevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Environment;
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
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		for(Entry<String, ?> entry : sharedPref.getAll().entrySet())
			preconfig.put(entry.getKey(), entry.getValue());
		communicator.loadConfiguration(preconfig);
		
		// Create manager
		manager = new SurveillanceClientManager(mediaSource, detection, identification, communicator, tracker, logger);
		manager.start();
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
