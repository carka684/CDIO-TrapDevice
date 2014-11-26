package edu.wildlifesecurity.trapdevice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import edu.wildlifesecurity.framework.ILogger;
import edu.wildlifesecurity.framework.communicatorclient.ICommunicatorClient;

/**
 * A logger that sends log messages to the server if a connection is available. Otherwise, shows them in a dialog.
 * 
 * @author Tobias
 *
 */
public class ClientLogger implements ILogger {

	private ICommunicatorClient communicator;
	private Context context;
	
	public ClientLogger(ICommunicatorClient communicator, Context context){
		this.communicator = communicator;
		this.context = context;
	}
	
	@Override
	public void info(String message) {
		if(communicator.isConnected())
			communicator.info(message);
		else
			showDialog("INFO", message);
	}

	@Override
	public void warn(String message) {
		if(communicator.isConnected())
			communicator.info(message);
		else
			showDialog("WARNING", message);
	}

	@Override
	public void error(String message) {
		if(communicator.isConnected())
			communicator.info(message);
		else
			showDialog("ERROR", message);
	}
	
	private void showDialog(String prio, String msg){

		// Start LogDialog activity that shows the dialog
		
		Intent dialogIntent = new Intent(context, LogDialog.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle b = new Bundle();
		b.putString("prio", prio);
		b.putString("message", msg);
		dialogIntent.putExtras(b);
		
		context.startActivity(dialogIntent);
		
	}

}
