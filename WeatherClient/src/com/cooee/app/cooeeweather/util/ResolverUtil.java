package com.cooee.app.cooeeweather.util;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.cooee.app.cooeeweather.dataentity.ForeignCitysEntity;
import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.InlandCitysEntity;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.filehelp.Log;
import com.cooee.widget.samweatherclock.AppConfig;


public class ResolverUtil
{
	
	private final static String CITY_CONTENT_URI = "content://com.cooee.app.cooeeweather.dataprovider/citys";
	private final static String POSTALCODE_URI = "content://com.cooee.app.cooeeweather.dataprovider/postalCode";
	
	/**
	 * 向已选中的城市数据库中添加通过定位获取的城市
	 * 
	 */
	public static void addLocatedCity(
			Context context )
	{
		String defaultCity = AppConfig.getInstance( context ).getDefaultCity();
		defaultCity = getParsedCity( context , defaultCity );
		Log.i( "MainActivity" , "addLocatedCity ---defaultCity = " + defaultCity );
		if( defaultCity != null && !defaultCity.equalsIgnoreCase( "" ) && !defaultCity.equalsIgnoreCase( "none" ) && !defaultCity.equalsIgnoreCase( "null" ) )
		{
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.parse( POSTALCODE_URI );
			ContentValues values = new ContentValues();
			values.put( PostalCodeEntity.POSTAL_CODE , defaultCity );
			values.put( PostalCodeEntity.USER_ID , 0 );
			values.put( PostalCodeEntity.AUTO_LOCATE , "true" );
			resolver.insert( uri , values );
		}
	}
	
	// gaominghui@2016/03/22 ADD START
	public static String addDefaultCity(
			Context context )
	{
		String defaultCity = AppConfig.getInstance( context ).getDefaultCity();
		defaultCity = getParsedCity( context , defaultCity );
		//Log.i( "MainActivity" , "addDefaultCity ---defaultCity = " + defaultCity );
		if( defaultCity != null && !defaultCity.equalsIgnoreCase( "" ) && !defaultCity.equalsIgnoreCase( "none" ) && !defaultCity.equalsIgnoreCase( "null" ) )
		{
			boolean isContainsDefaultCity = false;
			Cursor cursor = null;
			Uri uri = Uri.parse( POSTALCODE_URI );
			ContentResolver resolver = context.getContentResolver();
			cursor = resolver.query( uri , PostalCodeEntity.projection , PostalCodeEntity.POSTAL_CODE + " = '" + defaultCity + "'" + " and " + PostalCodeEntity.USER_ID + " = '0'" , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					isContainsDefaultCity = true;
				}
				cursor.close();
			}
			if( !isContainsDefaultCity )
			{
				ContentValues values = new ContentValues();
				values.put( PostalCodeEntity.POSTAL_CODE , defaultCity );
				values.put( PostalCodeEntity.USER_ID , 0 );
				resolver.insert( uri , values );
			}
		}
		return defaultCity;
	}
	
	// gaominghui@2016/03/22 ADD END
	/**
	 * 向已选中的城市数据库中更新新定位获取的城市
	 * 
	 */
	public static String updataLocatedCity(
			String lastLocateCity ,
			Context context )
	{
		String city = null;
		if( lastLocateCity != null )
		{
			ContentResolver res = context.getContentResolver();
			Cursor cur = null;
			Uri uri = null;
			String auto_locate = null;
			String selection = weatherdataentity.POSTALCODE + "=" + "'" + lastLocateCity + "'" + " and auto_locate =" + "'true'";
			uri = Uri.parse( POSTALCODE_URI );
			cur = res.query( uri , PostalCodeEntity.projection , selection , null , null );
			if( cur != null )
			{
				if( cur.moveToFirst() )
				{
					auto_locate = cur.getString( 8 );
					//Log.i( "weatherDataService" , "auto_locate = " + auto_locate );
				}
				cur.close();
			}
			Log.i( "weatherDataService" , "lastLocateCity = " + lastLocateCity + "; auto_locate = " + auto_locate );
			if( auto_locate != null && auto_locate.equals( "true" ) && lastLocateCity != null )
			{
				city = getUpdateStringLoactionCity( lastLocateCity , context , city , uri );
			}
		}
		return city;
	}
	
	/**
	 *
	 * @param lastLocateCity
	 * @param context
	 * @param city
	 * @param uri
	 * @return
	 * @author gaominghui 2016年3月22日
	 */
	private static String getUpdateStringLoactionCity(
			String lastLocateCity ,
			Context context ,
			String city ,
			Uri uri )
	{
		String defaultCity = AppConfig.getInstance( context ).getDefaultCity();
		defaultCity = getParsedCity( context , defaultCity );
		if( defaultCity != null && !defaultCity.equalsIgnoreCase( "" ) && !defaultCity.equalsIgnoreCase( "none" ) && !defaultCity.equalsIgnoreCase( "null" ) )
		{
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			String selection1 = PostalCodeEntity.AUTO_LOCATE + "=" + "'true'";
			values.put( PostalCodeEntity.POSTAL_CODE , defaultCity );
			int temp = resolver.update( uri , values , selection1 , null );
			if( temp != -1 )
			{
				//Log.i( "weatherDataService" , "temp != -1 !!!!" );
				city = defaultCity;
			}
			else
			{
				//Log.i( "weatherDataService" , "temp == -1 !!!!" );
				city = lastLocateCity;
			}
			//Log.i( "weatherDataService" , "city = " + city );
		}
		return city;
	}
	
	/**
	 * 判断postalCode表是否为空
	 * @return true 为空 , false不为空
	 */
	public static boolean IsPostalCityEmpty(
			Context context )
	{
		boolean isPostalCityEmpty = false;
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		Uri uri = Uri.parse( POSTALCODE_URI );
		cursor = resolver.query( uri , PostalCodeEntity.projection , PostalCodeEntity.USER_ID + " = '0'" , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
			}
			else
			{
				isPostalCityEmpty = true;
			}
			cursor.close();
		}
		else
		{
			isPostalCityEmpty = true;
		}
		return isPostalCityEmpty;
	}
	
	/**
	 * 获取数据库中选为定位城市的城市名
	 * @return 存在返回城市名 不存在返回null
	 */
	public static String getLocatedCity(
			Context context )
	{
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		String cityName = null;
		Uri uri = Uri.parse( POSTALCODE_URI );
		cursor = resolver.query( uri , PostalCodeEntity.projection , PostalCodeEntity.AUTO_LOCATE + " = 'true'" , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				cityName = cursor.getString( 0 );
			}
			cursor.close();
		}
		return cityName;
	}
	
	/**
	 * 更新数据库中被选为定位城市的位置
	 * @param context
	 * @param postalCode
	 */
	public static boolean updateLocatedCity(
			Context context ,
			String postalCode )
	{
		boolean success = false;
		if( postalCode != null )
		{
			postalCode = getParsedCity( context , postalCode );
			if( postalCode != null && !postalCode.equalsIgnoreCase( "" ) && !postalCode.equalsIgnoreCase( "none" ) && !postalCode.equalsIgnoreCase( "null" ) )
			{
				ContentResolver resolver = context.getContentResolver();
				Uri uri = Uri.parse( POSTALCODE_URI );
				ContentValues values = new ContentValues();
				values.put( PostalCodeEntity.POSTAL_CODE , postalCode );
				resolver.update( uri , values , PostalCodeEntity.AUTO_LOCATE + " = 'true'" , null );
				success = true;
			}
		}
		return success;
	}
	
	/**
	 * 根据定位到的城市名和城市列表返回服务器可辨认的城市名称
	 * @param context
	 * @param cityName
	 * @return
	 */
	public static String getParsedCity(
			Context context ,
			String cityName )
	{
		if( cityName == null )
			return null;
		String resultCity = cityName;
		Uri uri = Uri.parse( CITY_CONTENT_URI );
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		int cityIndex = 0;
		if( ( cityIndex = resultCity.lastIndexOf( "市" ) ) == resultCity.length() - 1 )
		{
			resultCity = resultCity.substring( 0 , cityIndex );
		}
		cursor = resolver.query( uri , InlandCitysEntity.projection , InlandCitysEntity.NAME + " = '" + resultCity + "'" , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				resultCity = cursor.getString( 0 );
			}
			else if( AppConfig.getInstance( context ).isMerge() )
			{
				cursor = resolver.query( uri , ForeignCitysEntity.projection_abroad_en , ForeignCitysEntity.CITY_EN + " = '" + resultCity + "' COLLATE NOCASE" , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						resultCity = cursor.getString( ForeignCitysEntity.CITY_EN_INDEX );
					}
					else
					{
						resultCity = null;
					}
				}
				else
				{
					resultCity = null;
				}
			}
			cursor.close();
		}
		else
		{
			resultCity = null;
		}
		//Log.i( "andy" , "resultCity = " + resultCity );
		if( resultCity == null )
		{
			//数据库中的城市名一般不以“市”结尾，但是有少数几个不同，所以还需要以原名字查找一次
			cursor = resolver.query( uri , InlandCitysEntity.projection , InlandCitysEntity.NAME + " = '" + cityName + "'" , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
					resultCity = cursor.getString( 0 );
				cursor.close();
			}
		}
		return resultCity;
	}
	
	/**
	 *
	 * @param postCode
	 * @return
	 * @author gaomignhui 2015年10月20日
	 */
	public static  boolean checkCityIsForeignCity(
			Context mContext ,
			String postCode )
	{
		boolean result = false;
		ContentResolver resolver = mContext.getContentResolver();
		Uri uri = Uri.parse( CITY_CONTENT_URI );
		String selection = ForeignCitysEntity.CITY_EN + " = '" + postCode + "' COLLATE NOCASE";
		//Log.i( "andy" , "selection = " + selection );
		Cursor cursor = resolver.query( uri , ForeignCitysEntity.projection_abroad_en , selection , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				//Log.i( "andy" , "true!!!" );
				cursor.close();
				return true;
			}
			else
			{
				//Log.i( "andy" , "false!!!" );
				cursor.close();
				return false;
			}
		}
		else
		{
			cursor.close();
			return false;
		}
	};
}
