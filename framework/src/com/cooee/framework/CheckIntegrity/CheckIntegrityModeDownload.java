// xiatian add whole file //CheckIntegrity（添加：检查“下载模块”完整性）
package com.cooee.framework.CheckIntegrity;


import java.util.ArrayList;

import com.cooee.CheckIntegrity.CheckIntegrityModeBase;


public class CheckIntegrityModeDownload implements CheckIntegrityModeBase
{
	
	@Override
	public ArrayList<String> getNeed2CheckPermissionList()
	{
		ArrayList<String> mNeed2CheckPermissionList = new ArrayList<String>();
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_PHONE_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.INTERNET" );
		mNeed2CheckPermissionList.add( "android.permission.WRITE_EXTERNAL_STORAGE" );
		return mNeed2CheckPermissionList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckActivityList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckServiceList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckReceiverList()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckLibList()
	{
		// TODO Auto-generated method stub
		return null;
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
		ArrayList<String> mNeed2CheckDrawableList = new ArrayList<String>();
		mNeed2CheckDrawableList.add( "download_notification_icon" );
		return mNeed2CheckDrawableList;
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
		ArrayList<String> mNeed2CheckLayoutList = new ArrayList<String>();
		mNeed2CheckLayoutList.add( "download_notification_layout" );
		return mNeed2CheckLayoutList;
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
		ArrayList<String> mNeed2CheckStringList = new ArrayList<String>();
		mNeed2CheckStringList.add( "download_toast_tip_insert_SD" );
		mNeed2CheckStringList.add( "download_toast_tip_internet_err" );
		mNeed2CheckStringList.add( "download_toast_tip_downloading" );
		mNeed2CheckStringList.add( "download_notification_tip_downloading" );
		mNeed2CheckStringList.add( "download_notification_tip_download_finish" );
		mNeed2CheckStringList.add( "download_notification_tip_download_fail" );
		return mNeed2CheckStringList;
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
