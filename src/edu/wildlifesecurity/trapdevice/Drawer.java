package edu.wildlifesecurity.trapdevice;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.wildlifesecurity.framework.identification.Classes;
import android.graphics.Bitmap;

public class Drawer{
	Mat image;
	
	public Drawer(Mat background)
	{
		Imgproc.cvtColor(background, image, Imgproc.COLOR_GRAY2BGR);
	}
	public Drawer()
	{
		image = new Mat(1,1,CvType.CV_8UC3);
	}
	public void setBackground(Mat background)
	{
		if(background.type() == CvType.CV_8UC1) // If background is gray, convert it to BGR
			Imgproc.cvtColor(background, image, Imgproc.COLOR_GRAY2BGR);
		else 
			image = background;
	}
	public void addRect(Rect rect, Classes classification)
	{
		Core.rectangle(image, rect.tl(), rect.br(),getColor(classification),5);
	}
	public Bitmap getBitmap()
	{
		Bitmap bm = Bitmap.createBitmap(image.cols(), image.rows(),	Bitmap.Config.ARGB_8888);		
		Utils.matToBitmap(image, bm);
		return bm;
	}
	private Scalar getColor(Classes classification)
	{
		
		Scalar color;
		if(classification == Classes.HUMAN)
			color = new Scalar(255,0,0); 
		else if(classification == Classes.RHINO)
			color = new Scalar(0,255,0);
		else if(classification == Classes.UNIDENTIFIED)
			color = new Scalar(0,0,255);
		else
			color = new Scalar(125,125,125);

		return color;
	}
}
