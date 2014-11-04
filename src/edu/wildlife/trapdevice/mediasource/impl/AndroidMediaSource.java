package edu.wildlife.trapdevice.mediasource.impl;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import edu.wildlifesecurity.framework.AbstractComponent;
import edu.wildlifesecurity.framework.EventDispatcher;
import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.mediasource.IMediaSource;
import edu.wildlifesecurity.framework.mediasource.MediaEvent;
import edu.wildlifesecurity.trapdevice.R;

public class AndroidMediaSource extends AbstractComponent implements
		IMediaSource {

	private EventDispatcher<MediaEvent> dispatcher = new EventDispatcher<MediaEvent>();
	
	private VideoCapture mCamera;
	private Mat image;
	
	private Timer timer = new Timer();
	
	@Override
	public void init(){
		// Sets up the camera
		setupCamera();
		
		// Starts timer to take pictures at a configurable rate
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
			    takeSnapshot();
			    System.out.println("Took photo");
			  }
			}, Integer.parseInt(configuration.get("MediaSource_FrameRate").toString()), Integer.parseInt(configuration.get("MediaSource_FrameRate").toString()));
	}
	
	@Override
	public ISubscription addEventHandler(EventType type,
			IEventHandler<MediaEvent> handler) {
		return dispatcher.addEventHandler(type, handler);
	}

	@Override
	public Mat takeSnapshot() {
		snapShotGray();
		dispatcher.dispatch(new MediaEvent(MediaEvent.NEW_SNAPSHOT, image));
		return image;
	}
	
	private void snapShotGray(){
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);	
	}
	
	private void snapShot(View view){
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);		
	}

	private void setupCamera() {
		 
	    if (mCamera != null) {
	       VideoCapture camera = mCamera;
	       mCamera = null; // Make it null before releasing...
	      camera.release();
	    }
	 
	    mCamera = new VideoCapture(0);
	 
	    List<Size> previewSizes = mCamera.getSupportedPreviewSizes();
	    double smallestPreviewSize = 1280 * 720; // We should be smaller than this...
	    double smallestWidth = 720; // Let's not get smaller than this...
	    Size mPreviewSize = null;
		for (Size previewSize : previewSizes) {
	        if (previewSize.area() < smallestPreviewSize && previewSize.width >= smallestWidth) {
	            mPreviewSize = previewSize;
	        }
	    }
	 
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mPreviewSize.width);
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mPreviewSize.height);
		
		// Don't know why needed but must be done...
		image=Mat.eye(3,3,0);
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
	}

	@Override
	public void destroy() {
		timer.cancel();
		timer.purge();
		
		mCamera.release();
	}


}
