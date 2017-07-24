/* 文件名: PubProviderHelper.java 2014年8月26日
 * 
 * 描述: PubContentProvider相关操作的辅助类
 * 
 * 作者: cooee */
package com.cooee.center.pub.provider;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.cooee.framework.utils.StringUtils;


public class PubProviderHelper
{
	
	static Context mContext = null;
	
	public static void SetContext(
			Context context )
	{
		mContext = context;
	}
	
	public static long insertValue(
			String table ,
			String name ,
			String value )
	{
		try
		{
			ContentResolver resolver = mContext.getContentResolver();
			Uri uri = Uri.parse( StringUtils.concat( "content://" , PubContentProvider.LAUNCHER_AUTHORITY , "/" , table ) );
			ContentValues values = new ContentValues();
			values.put( "propertyName" , name );
			values.put( "propertyValue" , value );
			Uri result = resolver.insert( uri , values );
			if( result != null )
			{
				return ContentUris.parseId( result );
			}
			else
			{
				return 0;
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return 0;
	}
	
	public static int updateValue(
			String table ,
			String name ,
			String value )
	{
		try
		{
			ContentResolver resolver = mContext.getContentResolver();
			Uri uri = Uri.parse( StringUtils.concat( "content://" , PubContentProvider.LAUNCHER_AUTHORITY , "/" , table ) );
			String where = " propertyName=? ";
			String[] args = new String[]{ name };
			ContentValues values = new ContentValues();
			values.put( "propertyName" , name );
			values.put( "propertyValue" , value );
			return resolver.update( uri , values , where , args );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			return 0;
		}
	}
	
	public static void addOrUpdateValue(
			String table ,
			String name ,
			String value )
	{
		try
		{
			ContentResolver resolver = mContext.getContentResolver();
			Uri uri = Uri.parse( StringUtils.concat( "content://" , PubContentProvider.LAUNCHER_AUTHORITY , "/" , table ) );
			String where = " propertyName=? ";
			String[] args = new String[]{ name };
			Cursor cursor = resolver.query( uri , null , where , args , null );
			if( cursor != null )
			{
				int cursorCount = cursor.getCount();
				cursor.close();
				if( cursorCount > 0 )
				{
					updateValue( table , name , value );
				}
				else
				{
					insertValue( table , name , value );
				}
			}
			else
			{
				insertValue( table , name , value );
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}
	
	public static String queryValue(
			String table ,
			String name )
	{
		ContentResolver resolver = mContext.getContentResolver();
		Uri uri = Uri.parse( StringUtils.concat( "content://" , PubContentProvider.LAUNCHER_AUTHORITY , "/" , table ) );
		String where = " propertyName=? ";
		String[] args = new String[]{ name };
		Cursor cursor = resolver.query( uri , null , where , args , null );
		String result = null;
		if( cursor != null )
		{
			if( cursor.getCount() > 0 )
			{
				cursor.moveToFirst();
				int index = cursor.getColumnIndex( "propertyValue" );
				if( index != -1 )
				{
					result = cursor.getString( index );
				}
				else
				{
					result = null;
				}
			}
			else
			{
				result = null;
			}
			cursor.close();
		}
		else
		{
			result = null;
		}
		return result;
	}
}
