package edu.wildlifesecurity.trapdevice.mediasource.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import edu.wildlifesecurity.framework.AbstractComponent;
import edu.wildlifesecurity.framework.EventDispatcher;
import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.mediasource.IMediaSource;
import edu.wildlifesecurity.framework.mediasource.MediaEvent;

public class VideoMediaSource extends AbstractComponent implements IMediaSource {

	private EventDispatcher<MediaEvent> dispatcher = new EventDispatcher<MediaEvent>();
	private MediaMetadataRetriever retriever;
	private Timer timer = new Timer();
	private int timeOffset = 1;
	private int frameRate;
	
	/**
	 * Constructs a VideoMediaSource using a video file located at filePath
	 * 
	 * @param filePath THe path to the video file
	 */
	public VideoMediaSource(String filePath){
		
		retriever = new MediaMetadataRetriever();
		retriever.setDataSource(filePath);
	}
	
	@Override
	public void init(){
		
		frameRate = Integer.parseInt(configuration.get("MediaSource_FrameRate").toString());
		
		// Starts timer to take pictures at a configurable rate
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
			    takeSnapshot();
			    System.out.println("Took photo");
			  }
			}, frameRate, frameRate);

	}
	
	@Override
	public ISubscription addEventHandler(EventType type, IEventHandler<MediaEvent> handler){
		return dispatcher.addEventHandler(type, handler);
	}

	@Override
	public Mat takeSnapshot() {
	
		Bitmap bmp = retriever.getFrameAtTime(timeOffset,MediaMetadataRetriever.OPTION_CLOSEST);
		Mat frame = new Mat();
		
		Utils.bitmapToMat(bmp, frame);
		timeOffset += frameRate * 1000; // Convert from ms to us
		
		// Dispatch event
		dispatcher.dispatch(new MediaEvent(MediaEvent.NEW_SNAPSHOT, frame));
		
		return frame;
		
	}

	@Override
	public void destroy() {
		retriever.release();
	}

}
