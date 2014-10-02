package com.example.trapdevice;


import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;

import edu.wildlifesecurity.framework.SurveillanceClientManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TrapDevice extends Activity {

	VideoCapture mCamera;
	Mat image;
	BackgroundSubtractorMOG bgSub;
	

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_trap_device);
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
	    
	    SurveillanceClientManager manager = new SurveillanceClientManager(null, null, null, null);
	    /*bgSub=new BackgroundSubtractorMOG();
	    setupCamera();
	    image=Mat.eye(3,3,0);
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		//	    String myString = image.dump();
		TextView msgTextView = (TextView) findViewById(R.id.textBox);
		String test1 = Integer.toString(image.cols());
		msgTextView.setText(test1);
		Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),	Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(image, bm);
		
		ImageView iv = (ImageView) findViewById(R.id.imageView1);
		iv.setImageBitmap(bm);

		//setContentView(R.layout.activity_trap_device);*/
	    

	    
	}
	
	public void snapShotGray(View view){
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);		
		Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),	Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(image, bm);
		ImageView iv = (ImageView) findViewById(R.id.imageView1);
		iv.setImageBitmap(bm);
		
	}
	
	public void snapShot(View view){
		mCamera.grab();
		mCamera.retrieve(image, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
		Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),	Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(image, bm);
		ImageView iv = (ImageView) findViewById(R.id.imageView1);
		iv.setImageBitmap(bm);
		
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
