// xiatian add whole file //CheckIntegrity（添加：检查“酷生活模块”完整性）
package com.cooee.framework.CheckIntegrity;


import java.util.ArrayList;

import com.cooee.CheckIntegrity.CheckIntegrityModeBase;


public class CheckIntegrityModeFavorites implements CheckIntegrityModeBase
{
	
	boolean mIsCooeeLauncher = true;
	
	public CheckIntegrityModeFavorites(
			boolean isCooeeLauncher )
	{
		mIsCooeeLauncher = isCooeeLauncher;
	}
	
	public CheckIntegrityModeFavorites()
	{
	}
	
	@Override
	public ArrayList<String> getNeed2CheckActivityList()
	{
		ArrayList<String> mNeed2CheckActivityList = new ArrayList<String>();
		mNeed2CheckActivityList.add( "com.kmob.kmobsdk.InAppWebView" );
		mNeed2CheckActivityList.add( "com.kmob.kmobsdk.AdActivity" );
		mNeed2CheckActivityList.add( "com.cooee.dynamicload.DLProxyActivity" );
		mNeed2CheckActivityList.add( "com.cooee.favorites.AdvanceActivity" );
		mNeed2CheckActivityList.add( "com.cooee.favorites.AdvanceActivitySecond" );
		mNeed2CheckActivityList.add( "com.cooee.favorites.AdvanceActivityThird" );
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
		mNeed2CheckAssetsList.add( "com.cooee.favorites.data" );
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
	public ArrayList<String> getNeed2CheckLibList()
	{
		// TODO Auto-generated method stub
		ArrayList<String> mNeed2CheckLibList = new ArrayList<String>();
		mNeed2CheckLibList.add( "com.kmob.kmobsdk.KmobManager" );
		return mNeed2CheckLibList;
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
		// TODO Auto-generated method stub
		ArrayList<String> mNeed2CheckPermissionList = new ArrayList<String>();
		mNeed2CheckPermissionList.add( "android.permission.WRITE_EXTERNAL_STORAGE" );
		mNeed2CheckPermissionList.add( "android.permission.GET_TASKS" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" );
		mNeed2CheckPermissionList.add( "android.permission.WAKE_LOCK" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_FINE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.INTERNET" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_WIFI_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.CHANGE_WIFI_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.CHANGE_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_PHONE_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_COARSE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.READ_CONTACTS" );
		mNeed2CheckPermissionList.add( "android.permission.REAL_GET_TASKS" );
		return mNeed2CheckPermissionList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckReceiverList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckServiceList()
	{
		// TODO Auto-generated method stub
		ArrayList<String> mNeed2CheckServiceList = new ArrayList<String>();
		mNeed2CheckServiceList.add( "com.cooee.dynamicload.DLProxyService" );
		mNeed2CheckServiceList.add( "com.cooee.favorites.AdvanceService" );
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
