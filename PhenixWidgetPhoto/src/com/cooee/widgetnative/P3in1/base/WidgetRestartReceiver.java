package com.cooee.widgetnative.P3in1.base;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class WidgetRestartReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "WidgetRestartReceiver";
	
	//	String ONRESUME_UPDATE_WIGET_VIEW = "com.cooee.phenix.onResume.UpdateWidgetView";
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.w( TAG , "cyk action = " + action );
		WidgetManager.getInstance( context ).updateAppWidget( context );
	}
}
