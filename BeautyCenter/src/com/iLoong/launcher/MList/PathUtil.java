package com.iLoong.launcher.MList;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;


public class PathUtil
{
	
	private static final String myPath = "/Cooee/JSCallTest";
	private static final String myUrl = "com.mas.wawagame.Kuwalord";
	
	public static String getPathHtmlSdcard()
	{
		return Environment.getExternalStorageDirectory() + myPath + "/Html/";
	}
	
	public static String getPathDBSdcard(
			Context context )
	{
		//return Environment.getExternalStorageDirectory() + myPath + "/DB/";
		return context.getFilesDir().getAbsolutePath() + myPath + "/DB/";
	}
	
	public static String getPathHtmlDownloadUrl()
	{
		return myUrl;
	}
	
	public static String getPathDownloadFolder()
	{
		return Environment.getExternalStorageDirectory() + myPath + "/Download/";
	}
	
	public static String getCurProcessName(
			Context context )
	{
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		for( ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses() )
		{
			if( appProcess.pid == pid )
			{
				return appProcess.processName;
			}
		}
		return null;
	}
}
