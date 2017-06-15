package com.iLoong.launcher.MList;


import java.util.List;

import cool.sdk.MicroEntry.MicroEntry;
import cool.sdk.download.CoolDLMgr;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;


public class MeGeneralMethod
{
	
	public static boolean IsForegroundRunning(
			Context context )
	{
		ActivityManager mActivityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		List<ActivityManager.RunningAppProcessInfo> mRunningService = mActivityManager.getRunningAppProcesses();
		for( ActivityManager.RunningAppProcessInfo amService : mRunningService )
		{
			if( amService.pid == android.os.Process.myPid() )
			{
				return amService.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
			}
		}
		return false;
	}
	
	public static boolean IsDownloadTaskRunning(
			Context context )
	{
		MeApkDownloadManager MeApkDlMgr = MeApkDlMgrBuilder.Build( context.getApplicationContext() , "M" , 0 );
		if( MeApkDlMgr.GetDownLoadingApkCount() > 0 )
		{
			return true;
		}
		return false;
	}
}
