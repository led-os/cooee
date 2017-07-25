package com.cooee.widgetnative.M3in1.base;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class WidgetRestartReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "WidgetRestartReceiver";
	
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
		//				if( "com.cooee.widgetnative.M3in1.MusicWidgetService".equals( service.service.getClassName() ) )
		//				{
		//					isServiceRunning = true;
		//				}
		//			}
		//			Log.e( TAG , " isServiceRunning = " + isServiceRunning );
		//			if( !isServiceRunning )
		//			{
		Intent intent2 = new Intent( context , WidgetService.class );
		context.startService( intent2 );
		//			}
		//		}
	}
}
