package edu.wildlifesecurity.trapdevice;

import android.app.Application;

public class TrapDeviceApplication extends Application {
	
	public static SurveillanceService service;
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    // TODO Put your application initialization code here.
	}

}
