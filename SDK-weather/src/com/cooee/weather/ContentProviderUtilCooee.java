package com.cooee.weather;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


/**
 * 我们自己天气客户端的ContentResolver
 * @author yangtianyu  2016-8-29
 */
public class ContentProviderUtilCooee
{
	
	// 天气组建的配置uri
	public final static String SETTING_URI = "content://com.cooee.app.cooeeweather.dataprovider/setting";
	public static final String MAINCITY = "maincity";
	
	/**
	 * 获得当前选中的城市
	 * 
	 * @param context
	 *            上下文
	 * @return 城市名字
	 */
	public static String getSelectedCity(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		try
		{
			String postalCode = null;
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.parse( SETTING_URI );
			Cursor cursor = null;
			try
			{
				cursor = resolver.query( uri , new String[]{ MAINCITY } , null , null , null );
				if( cursor != null && cursor.moveToFirst() )
				{
					postalCode = cursor.getString( cursor.getColumnIndex( MAINCITY ) );
				}
			}
			finally
			{
				if( cursor != null )
				{
					cursor.close();
				}
			}
			return postalCode;
		}
		catch( Exception e )
		{
			// TODO: handle exception
			return null;
		}
	}
}
