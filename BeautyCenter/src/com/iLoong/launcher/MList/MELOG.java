package com.iLoong.launcher.MList;


import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;

import cool.sdk.log.CoolLog;

import android.os.Environment;
import android.util.Log;


public class MELOG
{
	
	private static boolean bEnableLog = false;
	//是否已经查询过LOG开关配置文件
	private static boolean bIsCheckOpenLog = false;
	
	public static void setEnableLog(
			boolean bEnable )
	{
		if( bEnable )
		{
			bEnableLog = bEnable;
		}
	}
	
	private static boolean isOpenLog()
	{
		if( !bIsCheckOpenLog )
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					try
					{
						File file = new File( Environment.getExternalStorageDirectory() + "/cooee/ME_CFG" );
						if( file.exists() )
						{
							FileInputStream inStream = new FileInputStream( file );//读文件
							byte[] buffer = new byte[inStream.available()];
							inStream.read( buffer );
							String json = new String( buffer , "utf-8" );
							android.util.Log.v( "ME_RTFSC" , "isOpenLog json:" + json );
							JSONObject jsonObject = new JSONObject( json.substring( json.indexOf( "{" ) , json.lastIndexOf( "}" ) + 1 ) );
							String ME_LOG = jsonObject.getString( "ME_LOG" );
							if( ME_LOG.equals( "true" ) )
							{
								MELOG.setEnableLog( true );
								android.util.Log.v( "ME_RTFSC " , "===============   ME_LOG is OPEN!!!  =============" );
							}
							inStream.close();
						}
					}
					catch( Exception e )
					{
						// TODO: handle exception
						android.util.Log.v( "ME_RTFSC" , "isOpenLog ERROR: " + e.toString() );
					}
				}
			} ).start();
		}
		//已查询过LOG开关配置文件
		bIsCheckOpenLog = true;
		return bEnableLog;
	}
	
	public static final int v(
			String tag ,
			String msg )
	{
		int result = 0;
		if( bEnableLog || isOpenLog() )
		{
			result = android.util.Log.v( tag , msg );
		}
		return result;
	}
	
	//	public static final int v(
	//			String tag ,
	//			String msg ,
	//			Throwable tr )
	//	{
	//		int result = 0;
	//		if( bEnableLog )
	//		{
	//			result = android.util.Log.v( tag , msg , tr );
	//		}
	//		return result;
	//	}
	public static final int d(
			String tag ,
			String msg )
	{
		int result = 0;
		if( bEnableLog || isOpenLog() )
		{
			result = android.util.Log.d( tag , msg );
		}
		return result;
	}
	
	//	public static final int d(
	//			String tag ,
	//			String msg ,
	//			Throwable tr )
	//	{
	//		int result = 0;
	//		if( bEnableLog )
	//		{
	//			result = android.util.Log.d( tag , msg , tr );
	//		}
	//		return result;
	//	}
	public static final int i(
			String tag ,
			String msg )
	{
		int result = 0;
		if( bEnableLog || isOpenLog() )
		{
			result = android.util.Log.i( tag , msg );
		}
		return result;
	}
	
	//	public static final int i(
	//			String tag ,
	//			String msg ,
	//			Throwable tr )
	//	{
	//		int result = 0;
	//		if( bEnableLog )
	//		{
	//			result = android.util.Log.i( tag , msg , tr );
	//		}
	//		return result;
	//	}
	public static final int w(
			String tag ,
			String msg )
	{
		int result = 0;
		if( bEnableLog || isOpenLog() )
		{
			result = android.util.Log.w( tag , msg );
		}
		return result;
	}
	
	//	public static final int w(
	//			String tag ,
	//			String msg ,
	//			Throwable tr )
	//	{
	//		int result = 0;
	//		if( bEnableLog )
	//		{
	//			result = android.util.Log.w( tag , msg , tr );
	//		}
	//		return result;
	//	}
	public static final int e(
			String tag ,
			String msg )
	{
		int result = 0;
		if( bEnableLog || isOpenLog() )
		{
			result = android.util.Log.e( tag , msg );
		}
		return result;
	}
	//	public static final int e(
	//			String tag ,
	//			String msg ,
	//			Throwable tr )
	//	{
	//		int result = 0;
	//		if( bEnableLog )
	//		{
	//			result = android.util.Log.e( tag , msg , tr );
	//		}
	//		return result;
	//	}
}
