package com.cooee.kpsh;


import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


public class KpshStatistics
{
	
	//	public static final String event = "kpsh_function";
	//	public static final String sensitive_event = "sensitive_premisson";
	//	public static final String productName = "KPSH";
	public static void kpshFunctionStatistics(
			final Context context )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				checkAndroidManifest( context );//检查manifest中权限、service等注册情况
				checkResource( context );//检查资源，包括图片、layout以及assets中的jar包
			}
		} ).start();
	}
	
	private static void checkAndroidManifest(
			Context context )
	{
		PackageInfo pi = null;
		try
		{
			pi = context.getPackageManager().getPackageInfo(
					context.getPackageName() ,
					PackageManager.GET_PERMISSIONS | PackageManager.GET_ACTIVITIES | PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		if( pi == null )
		{
			return;
		}
		CheckUitls.checkPermissionInAndroidManifest( getNeed2CheckPermissionList() , pi.requestedPermissions , true );
		CheckUitls.checkActivityInAndroidManifest( getNeed2CheckActivityList() , pi.activities , true );
		CheckUitls.checkServiceInAndroidManifest( getNeed2CheckServiceList() , pi.services , true );
		CheckUitls.checkReceiverInAndroidManifest( getNeed2CheckReceiverList() , pi.receivers , true );
	}
	
	private static void checkResource(
			Context context )
	{
		String drawables[] = new String[]{
				"kpsh_left" ,
				"kpsh_more" ,
				"kpsh_notify_icon_0" ,
				"kpsh_notify_icon_1" ,
				"kpsh_notify_icon_2" ,
				"kpsh_notify_icon_3" ,
				"kpsh_notify_icon_4" ,
				"kpsh_notify_icon_5" ,
				"kpsh_notify_icon_6" ,
				"kpsh_notify_icon_7" ,
				"kpsh_right" };
		CheckUitls.checkResource( drawables , "drawable" , context , true );
		String layouts[] = new String[]{ "download_layout" , "mnm_layout" , "notify_layout" };
		CheckUitls.checkResource( layouts , "layout" , context , true );
		CheckUitls.checkAssetsResource( context , new String[]{ "KpshPlatform.jar" } , true );
		try
		{
			Class<?> c = Class.forName( "com.kpsh.sdk.KpshSdk" );
		}
		catch( ClassNotFoundException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new RuntimeException( "AM lost KpshSdk.Jar " );
		}
	}
	
	private static ArrayList<String> getNeed2CheckServiceList()
	{
		ArrayList<String> mNeed2CheckServiceList = new ArrayList<String>();
		mNeed2CheckServiceList.add( "com.kpsh.sdk.KpshService" );
		mNeed2CheckServiceList.add( "com.cooee.shell.pay.PayServiceHullV5" );
		mNeed2CheckServiceList.add( "com.cooee.shell.shell.SdkServiceHullV5" );
		return mNeed2CheckServiceList;
	}
	
	private static ArrayList<String> getNeed2CheckActivityList()
	{
		ArrayList<String> mNeed2CheckActivityList = new ArrayList<String>();
		mNeed2CheckActivityList.add( "com.kpsh.sdk.KpshActivity" );
		mNeed2CheckActivityList.add( "com.cooee.shell.download.DownloaderActivityHullV5" );
		mNeed2CheckActivityList.add( "com.cooee.shell.shell.SdkActivityHullV5" );
		mNeed2CheckActivityList.add( "com.cooee.shell.pay.PayActivityHullV5" );
		return mNeed2CheckActivityList;
	}
	
	private static ArrayList<String> getNeed2CheckReceiverList()
	{
		ArrayList<String> mNeed2CheckReceiverList = new ArrayList<String>();
		mNeed2CheckReceiverList.add( "com.kpsh.sdk.KpshReceiver" );
		mNeed2CheckReceiverList.add( "com.cooee.shell.shell.SdkReceiver" );
		return mNeed2CheckReceiverList;
	}
	
	private static ArrayList<String> getNeed2CheckPermissionList()
	{
		ArrayList<String> mNeed2CheckPermissionList = new ArrayList<String>();
		mNeed2CheckPermissionList.add( "android.permission.RECEIVE_BOOT_COMPLETED" );
		mNeed2CheckPermissionList.add( "com.android.launcher.permission.UNINSTALL_SHORTCUT" );
		mNeed2CheckPermissionList.add( "com.android.launcher.permission.CREATE_SHORTCUT" );
		mNeed2CheckPermissionList.add( "com.android.launcher.permission.INSTALL_SHORTCUT" );
		mNeed2CheckPermissionList.add( "android.permission.VIBRATE" );
		//push的权限这些都要有
		mNeed2CheckPermissionList.add( "android.permission.READ_PHONE_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.INTERNET" );
		mNeed2CheckPermissionList.add( "android.permission.SEND_SMS" );
		mNeed2CheckPermissionList.add( "android.permission.WRITE_EXTERNAL_STORAGE" );
		mNeed2CheckPermissionList.add( "android.permission.GET_TASKS" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_WIFI_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_COARSE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_FINE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" );
		mNeed2CheckPermissionList.add( "android.permission.WAKE_LOCK" );
		mNeed2CheckPermissionList.add( "android.permission.INSTALL_PACKAGES" );
		mNeed2CheckPermissionList.add( "android.permission.SYSTEM_ALERT_WINDOW" );
		mNeed2CheckPermissionList.add( "android.permission.READ_CONTACTS" );
		mNeed2CheckPermissionList.add( "android.permission.READ_SMS" );
		return mNeed2CheckPermissionList;
	}
}
