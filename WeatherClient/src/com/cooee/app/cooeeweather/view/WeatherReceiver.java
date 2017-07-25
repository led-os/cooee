package com.cooee.app.cooeeweather.view;


import com.cooee.widget.samweatherclock.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class WeatherReceiver extends BroadcastReceiver
{
	
	private static String TAG = "com.cooee.weather.WeatherReceiver";
	public final static int MSG_REFRESH = 2;
	public final static int MSG_FAILED = 3;
	public final static int MSG_AVAILABLE = 4;
	public final static int MSG_INVILIDE = 5;
	// shanjie 20130131
	public final static int MSG_WEBSERVICE_ERROR = 6;
	private static Handler mHandler = null;
	private static Handler mEditHandler = null;
	// add weijie 0530
	public final static int MSG_SREACH_SUCCES = 7;
	public final static int MSG_SREACH_FAILED = 8;
	public final static int MSG_REF_SUCCES = 9;
	public final static int MSG_REF_FAILED = 10;
	
	// add end
	public static void setHandler(
			Handler h )
	{
		mHandler = h;
	}
	
	public static void setEditHandler(
			Handler h )
	{
		mEditHandler = h;
	}
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		Log.v( "shanjie" , "WeatherReceiver onReceive intent.getAction =" + intent.getAction() );
		if( intent.getAction().equals( MainActivity.BROATCAST_URI ) )
		{
			Bundle bundle = intent.getExtras();
			if( bundle != null )
			{
				String result = bundle.getString( "cooee.weather.updateResult" );
				Log.v( "shanjie" , "WeatherReceiver onReceive result =" + result );
				if( result != null )
				{
					if( result.equals( "UPDATE_SUCCESED" ) || result.equals( "AVAILABLE_DATA" ) )
					{
						Log.v( TAG , "receive " + result );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_REFRESH , null ).sendToTarget();
							// if (mEditHandler != null) {
							// mEditHandler.obtainMessage(MSG_REFRESH, null)
							// .sendToTarget();
							// }
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_REFRESH , null ).sendToTarget();
						}
						// else if (result.equals("UPDATE_FAILED")) {
						// Log.v("shanjie",
						// "receive UPDATE_FAILED mHandler="+mHandler+",mEditHandler="+mEditHandler);
						// if (mHandler != null) {
						// mHandler.obtainMessage(MSG_FAILED, null)
						// .sendToTarget();
						// }
						// if (mEditHandler != null) {
						// mEditHandler.obtainMessage(MSG_FAILED, null)
						// .sendToTarget();
						// }
						// }
						// //shanjie
						// else if(result.equals("WEBSERVICE_ERROR")){
						// Log.v("shanjie",
						// "receive WEBSERVICE_ERROR mHandler="+mHandler+",mEditHandler="+mEditHandler);
						// if (mHandler != null) {
						// mHandler.obtainMessage(MSG_WEBSERVICE_ERROR, null)
						// .sendToTarget();
						// }
						// if (mEditHandler != null) {
						// mEditHandler.obtainMessage(MSG_WEBSERVICE_ERROR,
						// null)
						// .sendToTarget();
						// }
						// }
						// else if (result.equals("INVILIDE_DATA")) {
						// Log.v("shanjie",
						// "receive INVILIDE_DATA mHandler="+mHandler+",mEditHandler="+mEditHandler);
						// if (mHandler != null) {
						// mHandler.obtainMessage(MSG_INVILIDE, null)
						// .sendToTarget();
						// }
						// if (mEditHandler != null) {
						// mEditHandler.obtainMessage(MSG_INVILIDE, null)
						// .sendToTarget();
						// }
						// }
					}
					else if( result.equals( "UPDATE_FAILED" ) )
					{
						Log.v( "shanjie" , "receive UPDATE_FAILED mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_FAILED , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_FAILED , null ).sendToTarget();
						}
					}
					// shanjie
					else if( result.equals( "WEBSERVICE_ERROR" ) )
					{
						Log.v( "shanjie" , "receive WEBSERVICE_ERROR mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_WEBSERVICE_ERROR , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_WEBSERVICE_ERROR , null ).sendToTarget();
						}
					}
					else if( result.equals( "INVILIDE_DATA" ) )
					{
						Log.v( "shanjie" , "receive INVILIDE_DATA mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_INVILIDE , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_INVILIDE , null ).sendToTarget();
						}
					}
					//add weijie 0530
					else if( result.equals( "UPDATE_SREACH_SUCCES" ) )
					{
						Log.v( "shanjie" , "receive INVILIDE_DATA mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_SREACH_SUCCES , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_SREACH_SUCCES , null ).sendToTarget();
						}
					}
					else if( result.equals( "UPDATE_SREACH_FAILED" ) )
					{
						Log.v( "shanjie" , "receive INVILIDE_DATA mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_SREACH_FAILED , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_SREACH_FAILED , null ).sendToTarget();
						}
					}
					else if( result.equals( "UPDATE_REF_SUCCES" ) )
					{
						Log.v( "shanjie" , "receive INVILIDE_DATA mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_REF_SUCCES , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_REF_SUCCES , null ).sendToTarget();
						}
					}
					else if( result.equals( "UPDATE_REF_FAILED" ) )
					{
						Log.v( "shanjie" , "receive INVILIDE_DATA mHandler=" + mHandler + ",mEditHandler=" + mEditHandler );
						if( mHandler != null )
						{
							mHandler.obtainMessage( MSG_REF_FAILED , null ).sendToTarget();
						}
						if( mEditHandler != null )
						{
							mEditHandler.obtainMessage( MSG_REF_FAILED , null ).sendToTarget();
						}
					}
					// add end
				}
			}
		}
	}
}
