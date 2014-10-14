package edu.wildlife.trapdevice.mediasource.impl;

import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.example.trapdevice.R;

import edu.wildlifesecurity.framework.AbstractComponent;
import edu.wildlifesecurity.framework.EventDispatcher;
import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.mediasource.IMediaSource;
import edu.wildlifesecurity.framework.mediasource.MediaEvent;

public class AndroidMediaSource extends AbstractComponent implements
		IMediaSource {

	private EventDispatcher<MediaEvent> dispatcher = new EventDispatcher<MediaEvent>();
	private VideoCapture mCamera;
	private Mat image;
	
	@Override
	public void init(){
		setupCamera();
		image=Mat.eye(3,3,0);
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
	}
	
	@Override
	public ISubscription addEventHandler(EventType type,
			IEventHandler<MediaEvent> handler) {
		return dispatcher.addEventHandler(type, handler);
	}

	@Override
	public Mat takeSnapshot() {
		// TODO Auto-generated method stub
		snapShotGray();
		dispatcher.dispatch(new MediaEvent(MediaEvent.NEW_SNAPSHOT, image));
		return image;
	}
	
	public void snapShotGray(){
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);		
		//Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),	Bitmap.Config.ARGB_8888);
		//Utils.matToBitmap(image, bm);
		//ImageView iv = (ImageView) findViewById(R.id.imageView1);
		//iv.setImageBitmap(bm);
		
	}
	
	public void snapShot(View view){
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		//Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),	Bitmap.Config.ARGB_8888);
		//Utils.matToBitmap(image, bm);
		//ImageView iv = (ImageView) findViewById(R.id.imageView1);
		//iv.setImageBitmap(bm);
		
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
	}


}
