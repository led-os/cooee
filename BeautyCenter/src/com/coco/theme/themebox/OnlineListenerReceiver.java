package com.coco.theme.themebox;


import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;
import android.util.Log;


public class OnlineListenerReceiver extends BroadcastReceiver
{
	
	private final String ACTION_UPDATE = "com.coco.onlinetab.dorequest";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.v( "Online" , "Online--------onReceive:" + action );
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
		long openOnline = preferences.getLong( "openOnline" , 0 );
		long update_interval = preferences.getLong( "update_interval" , 24 * 60 * 60 * 1000 );
		long request_time = preferences.getLong( "requestTime" , System.currentTimeMillis() );
		//Log.v( "Online	" , "Online--------update_interval = " + update_interval + " request_time = " + request_time );
		if( System.currentTimeMillis() - request_time < update_interval )
		{
			Log.v( "Online" , "System.currentTimeMillis() - request_time ==" + ( System.currentTimeMillis() - request_time ) );
			return;
		}
		if( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) )
		{
			NetworkInfo info = intent.getParcelableExtra( ConnectivityManager.EXTRA_NETWORK_INFO );
			if( info != null && info.getState() == State.CONNECTED )
			{
				OnlineProxy proxy = OnlineProxy.getInstance( context );
				if( proxy.isWatchNetwork() )
				{
					Log.v( "Online" , "Online--------network ok" );
					proxy.setWatchNetwork( false );
					AlarmManager am = (AlarmManager)context.getSystemService( Context.ALARM_SERVICE );
					if( proxy.getPendingIntent() != null )
						am.cancel( proxy.getPendingIntent() );
					//Log.v( "Online	" , " onLine-----doRequest!" );
					proxy.doRequest();
				}
			}
		}
		else if( action.equals( ACTION_UPDATE ) )
		{
			OnlineProxy proxy = OnlineProxy.getInstance( context );
			proxy.setWatchNetwork( false );
			preferences.edit().putLong( "next_online_update_time" , 0 ).commit();
			proxy.doRequest();
		}
	}
}
