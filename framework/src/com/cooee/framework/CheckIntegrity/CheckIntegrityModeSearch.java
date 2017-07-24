// gaominghui add whole file //CheckIntegrity（添加：检查“酷搜模块”完整性）
package com.cooee.framework.CheckIntegrity;


import java.util.ArrayList;

import com.cooee.CheckIntegrity.CheckIntegrityModeBase;


public class CheckIntegrityModeSearch implements CheckIntegrityModeBase
{
	
	@Override
	public ArrayList<String> getNeed2CheckPermissionList()
	{
		ArrayList<String> mNeed2CheckPermissionList = new ArrayList<String>();
		mNeed2CheckPermissionList.add( "android.permission.GET_TASKS" );
		mNeed2CheckPermissionList.add( "android.permission.SYSTEM_ALERT_WINDOW" );
		mNeed2CheckPermissionList.add( "android.permission.RECEIVE_BOOT_COMPLETED" );
		mNeed2CheckPermissionList.add( "android.permission.INTERNET" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_WIFI_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_FINE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_MOCK_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_COARSE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" );
		mNeed2CheckPermissionList.add( "android.permission.WRITE_EXTERNAL_STORAGE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_EXTERNAL_STORAGE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_PHONE_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_CONTACTS" );
		mNeed2CheckPermissionList.add( "android.permission.WAKE_LOCK" );
		return mNeed2CheckPermissionList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckActivityList()
	{
		ArrayList<String> mNeed2CheckActivityList = new ArrayList<String>();
		//		mNeed2CheckActivityList.add( "com.solo.search.SearchActivity" );
		//		mNeed2CheckActivityList.add( "com.yahoo.mobile.client.share.search.ui.activity.SearchActivity" );
		mNeed2CheckActivityList.add( "com.search.kuso.SearchT9Main" );
		mNeed2CheckActivityList.add( "com.search.kuso.SearchSettingActivity" );
		//		mNeed2CheckActivityList.add( "com.solo.search.SearchActivity" );
		//		mNeed2CheckActivityList.add( "com.yahoo.mobile.client.share.search.ui.activity.SearchActivity" );
		//		mNeed2CheckActivityList.add( "com.solo.search.SearchActivity" );
		mNeed2CheckActivityList.add( "com.kmob.kmobsdk.InAppWebView" );
		mNeed2CheckActivityList.add( "com.kmob.kmobsdk.AdActivity" );
		return mNeed2CheckActivityList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckServiceList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckReceiverList()
	{
		ArrayList<String> mNeed2CheckReceiverList = new ArrayList<String>();
		mNeed2CheckReceiverList.add( "cool.sdk.update.manager.UpdateReceiver" );
		return mNeed2CheckReceiverList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckLibList()
	{
		ArrayList<String> mNeed2CheckLibList = new ArrayList<String>();
		mNeed2CheckLibList.add( "com.kmob.kmobsdk.KmobManager" );
		mNeed2CheckLibList.add( "cool.sdk.search.SearchActivityManager" );
		return mNeed2CheckLibList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAssetsList()
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckMipmapList()
	{
		// TODO Auto-generated method stub
		return null;
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
