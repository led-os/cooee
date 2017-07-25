package com.cooee.widgetnative.tango;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.widgetnative.tango.manager.ClockCalendarManager;
import com.cooee.widgetnative.tango.manager.TwinkleClockwidgetManager;


public class ReStartBroadcast extends BroadcastReceiver
{
	
	private static final String TAG = "ReStartBroadcast";
	
	//	String ONRESUME_UPDATE_WIGET_VIEW = "com.cooee.phenix.onResume.UpdateWidgetView";
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.v( TAG , " action = " + action );
		ClockCalendarManager.getInstance( context ).clockTimeChanged();
		TwinkleClockwidgetManager.getInstance( context ).updateAppWidget();
	}
}
