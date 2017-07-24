// xiatian add whole file //CheckIntegrity（添加：检查“酷生活模块”完整性）
package com.cooee.framework.CheckIntegrity;


import java.util.ArrayList;

import com.cooee.CheckIntegrity.CheckIntegrityModeBase;


public class CheckIntegrityModeKpsh implements CheckIntegrityModeBase
{
	
	@Override
	public ArrayList<String> getNeed2CheckActivityList()
	{
		// TODO Auto-generated method stub
		ArrayList<String> mNeed2CheckActivityList = new ArrayList<String>();
		mNeed2CheckActivityList.add( "com.kpsh.sdk.KpshActivity" );
		mNeed2CheckActivityList.add( "com.cooee.shell.download.DownloaderActivityHullV5" );
		mNeed2CheckActivityList.add( "com.cooee.shell.shell.SdkActivityHullV5" );
		mNeed2CheckActivityList.add( "com.cooee.shell.pay.PayActivityHullV5" );
		return mNeed2CheckActivityList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAnimList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckArrayList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAssetsList()
	{
		// TODO Auto-generated method stub
		ArrayList<String> mNeed2CheckAssetsList = new ArrayList<String>();
		/*mNeed2CheckAssetsList.add( "KpshPlatform.jar" );*/
		return mNeed2CheckAssetsList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAttrList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckBoolList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckColorList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckDimenList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckDrawableList()
	{
		ArrayList<String> drawables = new ArrayList<String>();
		/*drawables.add( "kpsh_left" );
		drawables.add( "kpsh_more" );
		drawables.add( "kpsh_notify_icon_0" );
		drawables.add( "kpsh_notify_icon_1" );
		drawables.add( "kpsh_notify_icon_2" );
		drawables.add( "kpsh_notify_icon_3" );
		drawables.add( "kpsh_notify_icon_4" );
		drawables.add( "kpsh_notify_icon_5" );
		drawables.add( "kpsh_notify_icon_6" );
		drawables.add( "kpsh_notify_icon_7" );
		drawables.add( "kpsh_right" );*/
		return drawables;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckIdList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckIntegerList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckLayoutList()
	{
		ArrayList<String> layouts = new ArrayList<String>();
		/*layouts.add( "download_layout" );
		layouts.add( "mnm_layout" );
		layouts.add( "notify_layout" );*/
		return layouts;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckLibList()
	{
		// TODO Auto-generated method stub  
		ArrayList<String> lib = new ArrayList<String>();
		lib.add( "com.kpsh.sdk.KpshSdk" );
		return lib;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckMipmapList()
	{
		// TODO Auto-generated method stub 
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckPermissionList()
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
	
	@Override
	public ArrayList<String> getNeed2CheckReceiverList()
	{
		ArrayList<String> mNeed2CheckReceiverList = new ArrayList<String>();
		mNeed2CheckReceiverList.add( "com.kpsh.sdk.KpshReceiver" );
		mNeed2CheckReceiverList.add( "com.cooee.shell.shell.SdkReceiver" );
		return mNeed2CheckReceiverList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckServiceList()
	{
		ArrayList<String> mNeed2CheckServiceList = new ArrayList<String>();
		mNeed2CheckServiceList.add( "com.cooee.shell.pay.PayServiceHullV5" );
		mNeed2CheckServiceList.add( "com.cooee.shell.shell.SdkServiceHullV5" );
		return mNeed2CheckServiceList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckStringList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckStyleList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckStyleableList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckXmlList()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
