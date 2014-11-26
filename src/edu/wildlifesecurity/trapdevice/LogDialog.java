package edu.wildlifesecurity.trapdevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

public class LogDialog extends Activity {
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        Bundle b = getIntent().getExtras();
        alertDialog.setTitle(b.getString("prio") + " in TrapDevice");
        alertDialog.setMessage(b.getString("message"));
        alertDialog.setIcon(R.drawable.wildlifesecurity_icon);
        alertDialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
        	
        });

        alertDialog.show();
    }
}
