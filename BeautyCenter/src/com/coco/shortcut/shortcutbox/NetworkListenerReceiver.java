package com.coco.shortcut.shortcutbox;


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


public class NetworkListenerReceiver extends BroadcastReceiver
{
	
	private final String ACTION_UPDATE = "com.coco.shortcut.dorequest";
	
	@Override
	public void onReceive(
			final Context context ,
			Intent intent )
	{
		String action = intent.getAction();
		if( UtilsBase.showOperateLog )
			Log.v( "OPCenter" , "OPCenter--------onReceive:" + action );
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
		long addshortcut = preferences.getLong( "addshortcut" , 0 );
		long update_interval = preferences.getLong( "update_interval" , 0 );
		long request_time = preferences.getLong( "requestTime" , System.currentTimeMillis() );
		Log.v( "OPCenter	" , "OPCenter--------update_interval = " + update_interval + "; request_time = " + request_time + "; currentTime = " + System.currentTimeMillis() );
		Log.v( "Online" , "System.currentTimeMillis() - request_time ==" + ( System.currentTimeMillis() - request_time ) );
		if( System.currentTimeMillis() - request_time < update_interval )
		{
			Log.v( "Online" , "System.currentTimeMillis() - request_time < update_interval!!" );
			return;
		}
		if( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) )
		{
			NetworkInfo info = intent.getParcelableExtra( ConnectivityManager.EXTRA_NETWORK_INFO );
			if( info != null && info.getState() == State.CONNECTED )
			{
				ShortCutProxy proxy = ShortCutProxy.getInstance( context );
				if( proxy.isWatchNetwork() )
				{
					if( UtilsBase.showOperateLog )
						Log.v( "OPCenter" , "OPCenter--------network ok" );
					proxy.setWatchNetwork( false );
					AlarmManager am = (AlarmManager)context.getSystemService( Context.ALARM_SERVICE );
					if( proxy.getPendingIntent() != null )
						am.cancel( proxy.getPendingIntent() );
					//Log.v( "minghui	" , "OPCenter-------- before doRequest!" );
					proxy.doRequest();
				}
			}
		}
		else if( action.equals( ACTION_UPDATE ) )
		{
			if( UtilsBase.showOperateLog )
				Log.v( "OPCenter" , "OPCenter--------action update" );
			if( System.currentTimeMillis() - addshortcut < 7 * 24 * 60 * 60 * 1000 )
			{
				Log.v( "OPCenter" , "OPCenter---ACTION_UPDATE-----return:" );
				return;
			}
			ShortCutProxy proxy = ShortCutProxy.getInstance( context );
			proxy.setWatchNetwork( false );
			preferences.edit().putLong( "next_update_time" , 0 ).commit();
			proxy.doRequest();
		}
		else if( action.equals( Intent.ACTION_SCREEN_OFF ) )
		{
			if( preferences.getBoolean( "addShortcut" , false ) )
			{
				ShortCutProxy proxy = ShortCutProxy.getInstance( context );
				proxy.removeShortcut();
				proxy.addShortcut();
				preferences.edit().putBoolean( "addShortcut" , false ).commit();
			}
			else if( preferences.getBoolean( "removeShortcut" , false ) )
			{
				ShortCutProxy proxy = ShortCutProxy.getInstance( context );
				proxy.removeShortcut();
				preferences.edit().putBoolean( "removeShortcut" , false ).commit();
			}
		}
	}
}
