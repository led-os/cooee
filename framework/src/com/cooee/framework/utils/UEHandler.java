// xiatian add whole file //添加uncaughtException保护类，捕捉uncaughtException
package com.cooee.framework.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;


public class UEHandler implements Thread.UncaughtExceptionHandler
{
	
	private static final String TAG = "UEHandler";
	private Application softApp;
	private File fileErrorLog;
	
	public UEHandler(
			Application app ,
			String path )
	{
		softApp = app;
		fileErrorLog = new File( path );
	}
	
	@Override
	public void uncaughtException(
			Thread thread ,
			Throwable ex )
	{
		String info = null;
		ByteArrayOutputStream baos = null;
		PrintStream printStream = null;
		try
		{
			baos = new ByteArrayOutputStream();
			printStream = new PrintStream( baos );
			ex.printStackTrace( printStream );
			byte[] data = baos.toByteArray();
			info = new String( data );
			data = null;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if( printStream != null )
				{
					printStream.close();
				}
				if( baos != null )
				{
					baos.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		// print
		long threadId = thread.getId();
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "ANDROID_LAB" , StringUtils.concat( "Thread.getName():" , thread.getName() , "-id:" , threadId , "-state:" + thread.getState() ) );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "ANDROID_LAB" , StringUtils.concat( "Error[" , info , "]" ) );
		if( info.contains( "createWindowSurface" ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "GL contain createWindowSurface,return" );
			return;
		}
		if( threadId != 1 )
		{
			recordErrorContinuousCount();
			write2ErrorLog( fileErrorLog , info );
			android.os.Process.killProcess( android.os.Process.myPid() );
			// 对于非UI线程可显示出提示界面，如果是UI线程抛的异常则界面卡死直到ANR。
			//			Intent intent = new Intent(softApp, ActErrorReport.class);
			//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//			intent.putExtra("error", info);
			//			intent.putExtra("by", "uehandler");
			//			softApp.startActivity(intent);
		}
		else
		{
			recordErrorContinuousCount();
			// write 2 /data/data/<app_package>/files/error.log
			write2ErrorLog( fileErrorLog , info );
			// kill App Progress
			android.os.Process.killProcess( android.os.Process.myPid() );
		}
	}
	
	private void write2ErrorLog(
			File file ,
			String content )
	{
		FileOutputStream fos = null;
		try
		{
			if( file.exists() )
			{
				// 清空之前的记录
				file.delete();
			}
			else
			{
				if( file.getParentFile() != null )
					file.getParentFile().mkdirs();
			}
			file.createNewFile();
			fos = new FileOutputStream( file );
			fos.write( content.getBytes() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if( fos != null )
				{
					fos.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	* 记录连续崩溃次数
	*/
	private void recordErrorContinuousCount()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( softApp.getApplicationContext() );
		long lastTime = prefs.getLong( "error_happen_time" , 0 );
		long time = System.currentTimeMillis() / 1000;//当前系统时间（单位为秒）
		//在一小时内连续崩溃
		if( time - lastTime < 3600 )
		{
			int count = prefs.getInt( "error_happen_continuous_count" , 0 );
			count += 1;
			prefs.edit().putLong( "error_happen_time" , time ).commit();
			prefs.edit().putInt( "error_happen_continuous_count" , count ).commit();
		}
		else
		{
			prefs.edit().putLong( "error_happen_time" , time ).commit();
			prefs.edit().putInt( "error_happen_continuous_count" , 1 ).commit();
		}
	}
}
