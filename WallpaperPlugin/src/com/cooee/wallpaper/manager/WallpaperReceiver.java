package com.cooee.wallpaper.manager;


import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import cool.sdk.WallpaperControl.WallpaperControlHelper;


public class WallpaperReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(
			final Context context ,
			Intent intent )
	{
		if( intent != null && intent.getAction() != null )
			updateFavoriteSwitch( context , intent );
	}
	
	public void updateFavoriteSwitch(
			final Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		try
		{
			String action = intent.getAction();
			if( action.equals( Intent.ACTION_DATE_CHANGED ) || action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) || action.equals( "android.intent.action.PHONE_STATE" ) || action
					.equals( "android.intent.action.USER_PRESENT" ) || action.equals( "android.provider.Telephony.SMS_RECEIVED" ) )
			{
				Log.v( "COOL" , "" + action );
				if( !allowUpdate( context ) )
				{
					Log.v( "COOL" , "can't allow update" + action );
					return;
				}
				new Thread() {
					
					public void run()
					{
						try
						{
							WallpaperControlHelper helper = WallpaperControlHelper.getInstance( context );
							helper.UpdateSync( false );
						}
						catch( Exception e )
						{
						}
					};
				}.start();
			}
		}
		catch( Exception e )
		{
		}
	}
	
	public static boolean allowUpdate(
			Context context )
	{
		boolean isNeedShowDisclaimer = false;
		try
		{
			Class<?> cls = Class.forName( "com.iLoong.launcher.desktop.Disclaimer" );
			Method method = cls.getMethod( "isNeedShowDisclaimer" );
			isNeedShowDisclaimer = (Boolean)method.invoke( cls );
		}
		catch( Throwable t )
		{
			t.getStackTrace();
			isNeedShowDisclaimer = false;
			Log.v( "COOL" , "allowUpdate class or method NotFoundException" );
		}
		return !isNeedShowDisclaimer;
	}
}
