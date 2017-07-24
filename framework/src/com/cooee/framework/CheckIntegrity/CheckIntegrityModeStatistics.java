// xiatian add whole file //CheckIntegrity（添加：检查“统计模块”完整性）
package com.cooee.framework.CheckIntegrity;


import java.util.ArrayList;

import com.cooee.CheckIntegrity.CheckIntegrityModeBase;


public class CheckIntegrityModeStatistics implements CheckIntegrityModeBase
{
	
	@Override
	public ArrayList<String> getNeed2CheckPermissionList()
	{
		ArrayList<String> mNeed2CheckPermissionList = new ArrayList<String>();
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_PHONE_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.INTERNET" );
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
		ArrayList<String> mNeed2CheckServiceList = new ArrayList<String>();
		mNeed2CheckServiceList.add( "com.cooee.statistics.StatisticsServiceNew" );
		return mNeed2CheckServiceList;
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
