package com.cooee.framework.function.Category;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cool.sdk.Category.CategoryConstant;


public class CategoryUninstallItem
{
	
	private static CategoryUninstallItem instance;
	private ArrayList<UninstallInfo> uninstallItems;
	
	private class UninstallInfo
	{
		
		String pkgName;
		long mRemoveTime;
		
		private UninstallInfo(
				String pkgName ,
				long mRemoveTime )
		{
			this.pkgName = pkgName;
			this.mRemoveTime = mRemoveTime;
		}
		
		private JSONObject toJSON()
		{
			JSONObject res = new JSONObject();
			try
			{
				res.put( "pkgName" , pkgName );
				res.put( "mRemoveTime" , mRemoveTime );
				return res;
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private CategoryUninstallItem()
	{
		uninstallItems = new ArrayList<UninstallInfo>();
		processData();
	}
	
	public static CategoryUninstallItem getInstance()
	{
		if( instance == null )
		{
			instance = new CategoryUninstallItem();
		}
		return instance;
	}
	
	public void addItem(
			String pkgName )
	{
		for( UninstallInfo info : uninstallItems )
		{
			if( pkgName.equals( info.pkgName ) )
			{
				return;
			}
		}
		uninstallItems.add( new UninstallInfo( pkgName , 0 ) );
		save();
	}
	
	public void addTime(
			String pkgName )
	{
		for( UninstallInfo info : uninstallItems )
		{
			if( pkgName.equals( info.pkgName ) )
			{
				info.mRemoveTime = System.currentTimeMillis();
				save();
				return;
			}
		}
	}
	
	public boolean findUninstallItem(
			String pkgName )
	{
		for( UninstallInfo info : uninstallItems )
		{
			if( pkgName.equals( info.pkgName ) )
			{
				if( Math.abs( System.currentTimeMillis() - info.mRemoveTime ) > CategoryConstant.ONE_DAY * 30 )
				{
					info.mRemoveTime = 0;
					save();
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	private JSONObject toJSON()
	{
		JSONArray jsonArray = new JSONArray();
		for( UninstallInfo info : uninstallItems )
		{
			jsonArray.put( info.toJSON() );
		}
		JSONObject res = new JSONObject();
		try
		{
			res.put( "uninstallItems" , jsonArray );
			return res;
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void processData()
	{
		String content = CategoryParse.mPreferences.getString( CategoryConstant.CATEGORYUNINSTALL , null );
		if( content != null )
		{
			try
			{
				JSONObject item = new JSONObject( content );
				JSONArray names = item.getJSONArray( "uninstallItems" );
				for( int j = 0 ; j < names.length() ; j++ )
				{
					JSONObject cateItem = names.getJSONObject( j );
					String pkgName = cateItem.optString( "pkgName" );
					long mRemoveTime = cateItem.optLong( "mRemoveTime" );
					uninstallItems.add( new UninstallInfo( pkgName , mRemoveTime ) );
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	private void save()
	{
		CategoryParse.mPreferences.edit().putString( CategoryConstant.CATEGORYUNINSTALL , toJSON().toString() ).commit();
	}
}
