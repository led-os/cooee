package com.cooee.widgetnative.enjoy;


import com.cooee.widgetnative.enjoy.manager.WidgetViewManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


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
		//		if( action.equals( "com.cooee.phenix.onResume.UpdateWidgetView" ) )
		//		{
		//			boolean isServiceRunning = false;
		//			Log.i( TAG , " isServiceRunning = " + isServiceRunning );
		//			//检查Service状态   
		//			ActivityManager manager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		//			for( RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) )
		//			{
		//				Log.d( TAG , " getClassName = " + service.service.getClassName() );
		//				if( "com.cooee.weatherclockphotomusicwidget.ClockCalendarMusicService".equals( service.service.getClassName() ) )
		//				{
		//					isServiceRunning = true;
		//				}
		//			}
		//			Log.e( TAG , " isServiceRunning = " + isServiceRunning );
		//			if( !isServiceRunning )
		//			{
		WidgetViewManager.getWidgetViewManager( context ).updateAllWidget();
		//			}
		//		}
	}
}
