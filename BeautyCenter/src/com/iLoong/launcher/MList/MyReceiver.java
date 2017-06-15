package com.iLoong.launcher.MList;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cool.sdk.MicroEntry.MicroEntryHelper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Message;
import android.webkit.WebView;
import android.widget.Toast;


public class MyReceiver extends BroadcastReceiver
{
	
	Context mContent = null;
	String pkgName = null;
	
	//	public boolean IsMeForeground(
	//			Context context )
	//	{
	//		ActivityManager mActivityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
	//		List<ActivityManager.RunningAppProcessInfo> mRunningService = mActivityManager.getRunningAppProcesses();
	//		for( ActivityManager.RunningAppProcessInfo amService : mRunningService )
	//		{
	//			if( "com.iLoong.Second".equals( amService.processName ) )
	//			{
	//				return amService.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
	//			}
	//		}
	//		return false;
	//	}
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		mContent = context;
		pkgName = intent.getDataString().substring( 8 );
		MELOG.v( "ME_RTFSC" , intent.getAction() + "====MyReceiver onReceive =====  pkgName:" + pkgName );
		//MELOG.v( "ME_RTFSC" , "IsMeForeground = " + IsMeForeground( context ) );
		if( intent.getAction().equals( Intent.ACTION_PACKAGE_ADDED ) || intent.getAction().equals( Intent.ACTION_PACKAGE_REPLACED ) )
		{
			//判断是不是微入口的APK安装成功
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					MicroEntryHelper microEntryHelper = MicroEntryHelper.getInstance( mContent );
					ArrayList<Integer> PkgAddedEntryIDList = new ArrayList<Integer>();
					//微入口从1开始到4
					int EntryId = microEntryHelper.getInt( pkgName + MeServiceType.MEApkOnSucess + 0 , -1 );
					if( -1 != EntryId )
					{
						PkgAddedEntryIDList.add( EntryId );
						microEntryHelper.setValue( pkgName + MeServiceType.MEApkOnSucess + 0 , -1 );
					}
					MELOG.v( "ME_RTFSC" , "PkgAddedEntryIDList:" + PkgAddedEntryIDList );
					if( !PkgAddedEntryIDList.isEmpty() || MeGeneralMethod.IsForegroundRunning( mContent ) )
					{
						//通过MeServiceType 处理安装成功事件
						microEntryHelper.setValue( pkgName + MeServiceType.MeApkOnInstalled , "TRUE" );
						Intent MePkgAddedIntent = new Intent( mContent , MEServiceActivity.class );
						MePkgAddedIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						MePkgAddedIntent.putExtra( "MeServiceType" , MeServiceType.MeApkOnPkgInstalled );
						MePkgAddedIntent.putIntegerArrayListExtra( "PkgAddedEntryIDList" , PkgAddedEntryIDList );
						MePkgAddedIntent.putExtra( "PkgName" , pkgName );
						mContent.startActivity( MePkgAddedIntent );
					}
				}
			} ).start();
		}
		if( intent.getAction().equals( Intent.ACTION_PACKAGE_REMOVED ) )
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					MicroEntryHelper microEntryHelper = MicroEntryHelper.getInstance( mContent );
					if( "TRUE".equals( microEntryHelper.getString( pkgName + MeServiceType.MeApkOnInstalled ) ) || MeGeneralMethod.IsForegroundRunning( mContent ) )
					{
						microEntryHelper.setValue( pkgName + MeServiceType.MeApkOnInstalled , "FALSE" );
						Intent MePkgRemoveIntent = new Intent( mContent , MEServiceActivity.class );
						MePkgRemoveIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						MePkgRemoveIntent.putExtra( "MeServiceType" , MeServiceType.MeApkOnPkgUninstall );
						MePkgRemoveIntent.putExtra( "PkgName" , pkgName );
						mContent.startActivity( MePkgRemoveIntent );
					}
				}
			} ).start();
		}
	}
}
