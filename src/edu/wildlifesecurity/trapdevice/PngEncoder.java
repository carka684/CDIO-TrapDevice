package edu.wildlifesecurity.trapdevice;

import java.io.ByteArrayOutputStream;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.graphics.Bitmap;
import edu.wildlifesecurity.framework.IImageEncoder;

public class PngEncoder implements IImageEncoder {

	@Override
	public byte[] encode(Mat image) {
		Bitmap bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);	
		Utils.matToBitmap(image, bmp);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
	    return baos.toByteArray();
	}

}
