package com.cooee.weather;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


/**
 * 2345天气客户端的ContentResolver
 * @author yangtianyu  2016-8-29
 */
public class ContentProviderUtil2345
{
	
	/**uri公共部分*/
	private static final String AUTHORITY = "content://com.tianqiyubao2345.weatherprovider";
	/**城市ID,用于更换城市时使用*/
	private static final String AREA_ID = "areaId";
	/**城市名称*/
	private static final String AREA_NAME = "areaName";
	/**是否为国外城市*/
	private static final String INTERNATIONAL = "international";
	/**当前选中的城市*/
	private static final String DEFAULT_CITY = "default_city";
	
	/**
	 * 获取天气客户端中的城市列表
	 * @param context
	 * @return
	 * @author yangtianyu 2016-8-29
	 */
	private static ArrayList<CityEntity2345> getCityList(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		try
		{
			ArrayList<CityEntity2345> cities = new ArrayList<CityEntity2345>();
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.parse( AUTHORITY + "/menu_citys" );
			// 查询数据
			Cursor cursor = null;
			try
			{
				cursor = resolver.query( uri , new String[]{ AREA_ID , AREA_NAME , INTERNATIONAL } , null , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						// 循环遍历所有保存的城市名
						while( !cursor.isAfterLast() )
						{
							CityEntity2345 cityEntity = new CityEntity2345();
							int areaId = cursor.getInt( cursor.getColumnIndex( AREA_ID ) );
							String areaName = cursor.getString( cursor.getColumnIndex( AREA_NAME ) );
							String international = cursor.getString( cursor.getColumnIndex( INTERNATIONAL ) );
							cityEntity.setAreaId( areaId );
							cityEntity.setAreaName( areaName );
							cityEntity.setInternational( international );
							cities.add( cityEntity );
							cursor.moveToNext();
						}
					}
				}
			}
			finally
			{
				if( cursor != null && !cursor.isClosed() )
				{
					cursor.close();
				}
			}
			return cities;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获得当前选中的城市的ID
	 * 
	 * @param context
	 *            上下文
	 * @return areaId
	 */
	private static int getSelectedCityId(
			Context context )
	{
		if( context == null )
		{
			return -1;
		}
		try
		{
			int areaId = -1;
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.parse( AUTHORITY + "/default_city" );
			Cursor cursor = null;
			try
			{
				cursor = resolver.query( uri , new String[]{ DEFAULT_CITY } , null , null , null );
				if( cursor != null && cursor.moveToFirst() )
				{
					areaId = cursor.getInt( cursor.getColumnIndex( DEFAULT_CITY ) );
				}
			}
			finally
			{
				if( cursor != null )
				{
					cursor.close();
				}
			}
			return areaId;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * 获取当前选中的城市名称
	 * @param context 上下文
	 * @return 城市名称,出错时返回null
	 * @author yangtianyu 2016-9-1
	 */
	public static String getSelectedCityName(
			Context context )
	{
		if( context == null )
			return null;
		List<CityEntity2345> cityList = getCityList( context );
		int cityId = getSelectedCityId( context );
		CityEntity2345 cityEntity = null;
		if( cityList != null && cityId > 0 )
		{
			for( int i = 0 ; i < cityList.size() ; i++ )
			{
				cityEntity = cityList.get( i );
				if( cityEntity != null && cityEntity.getAreaId() == cityId )
					return cityEntity.getAreaName();
			}
		}
		return null;
	}
	
	/**
	 * 城市信息内部类
	 * @author yangtianyu  2016-8-29
	 */
	private static class CityEntity2345
	{
		
		private int areaId = -1;
		private String areaName = null;
		private String international = null;
		
		public CityEntity2345()
		{
		}
		
		public int getAreaId()
		{
			return areaId;
		}
		
		public void setAreaId(
				int areaId )
		{
			this.areaId = areaId;
		}
		
		public String getAreaName()
		{
			return areaName;
		}
		
		public void setAreaName(
				String areaName )
		{
			this.areaName = areaName;
		}
		
		public void setInternational(
				String international )
		{
			this.international = international;
		}
		
		@Override
		public boolean equals(
				java.lang.Object o )
		{
			if( o != null && o instanceof CityEntity2345 )
				return this.areaId == ( (CityEntity2345)o ).getAreaId();
			return false;
		}
		
		@Override
		public String toString()
		{
			return "areaId : " + areaId + ", areaName : " + areaName + ", international = " + international;
		}
	}
}
