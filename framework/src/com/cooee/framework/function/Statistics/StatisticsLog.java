package com.cooee.framework.function.Statistics;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.log.LogHelper;


public class StatisticsLog
{
	
	private static final String ACTION_CONFIG_BILLING = "3707";
	private static final String FEATURE_COMMA = ",";
	public static final int h12 = 1;
	
	public synchronized static void LogBilling(
			Context context ,
			String requestPkgArrayString )
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put( "Action" , ACTION_CONFIG_BILLING );
			json.put( "h12" , h12 );
			json.put( "h13" , context.getPackageName() );
			json.put( "p1" , getInnerEntryID( context ) );
			json.put( "p2" , getInstalledApk( context , requestPkgArrayString ) );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , StringUtils.concat( "StatisticsLog LogBX str=" , json.toString() ) );
		}
		catch( JSONException e )
		{
		}
		LogHelper.Item( context , json , null );
	}
	
	private static String getInnerEntryID(
			Context context )
	{
		try
		{
			return DynamicEntryHelper.getInstance( context ).getEntryID();
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private static int checkAppType(
			Context context ,
			String pname )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "StatisticsLog LogBX pname=" , pname ) );
		try
		{
			PackageInfo pInfo = context.getPackageManager().getPackageInfo( pname , 0 );
			// 是系统软件或者是系统软件更新
			if( isSystemApp( pInfo ) || isSystemUpdateApp( pInfo ) )
			{
				return 1; /* system app */
			}
			else
			{
				return 0;/* user app */
			}
		}
		catch( NameNotFoundException e )
		{
			//e.printStackTrace();
		}
		return -1;
	}
	
	private static boolean isSystemApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0 );
	}
	
	private static boolean isSystemUpdateApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 );
	}
	
	private static String getInstalledApk(
			Context context ,
			String requestPkgArrayString )
	{
		JSONArray list;
		String retString;
		try
		{
			list = new JSONArray( requestPkgArrayString );
			retString = "";
			for( int j = 0 ; j < list.length() ; j++ )
			{
				String pkgnameString = list.getString( j );
				if( checkAppType( context , pkgnameString ) == 1 )
				{
					retString += pkgnameString;
					retString += FEATURE_COMMA;
				}
			}
			if( retString != "" )
			{
				int commaIndex = retString.lastIndexOf( FEATURE_COMMA );
				if( commaIndex == -1 )
				{
					return retString;
				}
				retString = retString.substring( 0 , commaIndex );
				return retString;
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
