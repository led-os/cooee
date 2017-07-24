package com.cooee.favorites.utils;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


public class RunningAppHelper
{
	
	private static ActivityManager manager = null;
	private static List<ResolveInfo> resolveInfosList;
	
	public static ComponentName getTopAppPckageName(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		ComponentName mComponentName = null;
		try
		{
			if( manager == null )
			{
				manager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
			}
			if( manager == null )
			{
				return null;
			}
			//			String[] activePackages;
			//			if( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH )
			//			{
			//				activePackages = getActivePackages();
			//			}
			//			else
			//			{
			//				activePackages = getActivePackagesCompat();
			//			}
			mComponentName = new ComponentName( manager.getRunningTasks( 1 ).get( 0 ).topActivity.getPackageName() , manager.getRunningTasks( 1 ).get( 0 ).topActivity.getClassName() );
			// if (Build.VERSION.SDK_INT >= 21) {
			// // 取出第一个正在运行的进程
			// RunningAppProcessInfo runningAppProcessInfo =
			// manager.getRunningAppProcesses().get(0);
			// // TODO 目前仅通过importance确定该进程是否正在前台运行，后期寻找被隐藏的flags字段的获取方法，进一步确定
			// if (runningAppProcessInfo.importance ==
			// RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
			// // 获取该进程包含的第一个包名
			// mComponentName = new
			// ComponentName(runningAppProcessInfo.pkgList[0], "");
			// }
			// } else {
			// mComponentName = new
			// ComponentName(manager.getRunningTasks(1).get(0).topActivity.getPackageName(),
			// manager.getRunningTasks(1).get(0).topActivity.getClassName());
			// }
			if( isHomeScreenAtMySelf( context , mComponentName ) )
			{
				// Log.w(TAG, mComponentName + " is home app by myself");
				return mComponentName;
			}
			if( !isHaveCategoryLauncher( mComponentName ) )
			{
				return null;
			}
		}
		catch( Exception e )
		{
			mComponentName = null;
		}
		//		Log.v( "lvjiangbin" , "有图标在桌面上 = " + mComponentName );
		return mComponentName;
	}
	
	/**
	 * 判断是否在自身桌面
	 * 
	 * @param context
	 * @param topApp
	 * @return
	 */
	private static boolean isHomeScreenAtMySelf(
			Context context ,
			ComponentName topApp )
	{
		if( topApp != null )
		{
			String packageName = context.getApplicationContext().getPackageName();
			String topAppName = topApp.getPackageName();
			if( topAppName == null )
			{
				return false;
			}
			return packageName.equals( topAppName );
		}
		return false;
	}
	
	/**
	 * 该componentName的APP是否具有launcher属性
	 * 
	 * @param componentName
	 * @return
	 */
	private static boolean isHaveCategoryLauncher(
			ComponentName componentName )
	{
		if( resolveInfosList != null && resolveInfosList.size() > 0 )
		{
			for( int i = 0 ; i < resolveInfosList.size() ; i++ )
			{
				String packageName = resolveInfosList.get( i ).activityInfo.packageName;
				String className = resolveInfosList.get( i ).activityInfo.name;
				//				Log.v( "lvjiangbin" , "-----------------------------------------------------------" );
				//				Log.v( "lvjiangbin" , "packageName" + resolveInfosList.get( i ).activityInfo.packageName );
				//				Log.v( "lvjiangbin" , "className" + resolveInfosList.get( i ).activityInfo.name );
				//				Log.v( "lvjiangbin" , "componentName=" + componentName );
				if( packageName.equals( componentName.getPackageName() ) && className.equals( componentName.getClassName() ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	// 获得所有启动Activity的信息，类似于Launch界面 应在Ui线程 尽早初始化
	/**
	 * @param context
	 */
	public static void queryAppInfo(
			Context context )
	{
		PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
		Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		// 通过查询，获得所有ResolveInfo对象.
		resolveInfosList = pm.queryIntentActivities( mainIntent , 0 );
		//		Log.v( "lvjiangbin" , "-----------------------------------------------------------resolveInfos = " + resolveInfosList.size() );
		//		if( resolveInfosList.size() > 0 )
		//		{
		//			for( int i = 0 ; i < resolveInfosList.size() ; i++ )
		//			{
		//				Log.v( "lvjiangbin" , "-----------------------------------------------------------" );
		//				Log.v( "lvjiangbin" , "packageName" + resolveInfosList.get( i ).activityInfo.packageName );
		//				Log.v( "lvjiangbin" , "className" + resolveInfosList.get( i ).activityInfo.name );
		//			}
		//		}
	}
	
	static String[] getActivePackagesCompat()
	{
		final List<ActivityManager.RunningTaskInfo> taskInfo = manager.getRunningTasks( 1 );
		final ComponentName componentName = taskInfo.get( 0 ).topActivity;
		final String[] activePackages = new String[1];
		activePackages[0] = componentName.getPackageName();
		return activePackages;
	}
	
	static String[] getActivePackages()
	{
		final Set<String> activePackages = new HashSet<String>();
		final List<ActivityManager.RunningAppProcessInfo> processInfos = manager.getRunningAppProcesses();
		for( ActivityManager.RunningAppProcessInfo processInfo : processInfos )
		{
			if( processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND )
			{
				activePackages.addAll( Arrays.asList( processInfo.pkgList ) );
			}
		}
		return activePackages.toArray( new String[activePackages.size()] );
	}
}
