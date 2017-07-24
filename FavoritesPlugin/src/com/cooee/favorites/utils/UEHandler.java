// xiatian add whole file //添加uncaughtException保护类，捕捉uncaughtException
package com.cooee.favorites.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import android.app.Application;
import android.util.Log;


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
		Log.d( "ANDROID_LAB" , "Thread.getName()=" + thread.getName() + " id=" + threadId + " state=" + thread.getState() );
		Log.d( "ANDROID_LAB" , "Error[" + info + "]" );
		if( info.contains( "createWindowSurface" ) )
		{
			Log.e( TAG , "GL contain createWindowSurface,return" );
			return;
		}
		if( threadId != 1 )
		{
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
}
