package com.cooee.widgetnative.tango.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;

import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;


public class ConfigUtils
{
	
	private static final String TAG = "ConfigUtils";
	public static ConfigUtils mConfigUtils;
	private String default_clock_package = null;
	private Context mContext;
	
	private void initConfig()
	{
		// TODO Auto-generated method stub
		getPgName();
	}
	
	private void getPgName()
	{
		// TODO Auto-generated method stub
		default_clock_package = mContext.getPackageName();
	}
	
	private ConfigUtils(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
	}
	
	public static ConfigUtils getInstance(
			Context context )
	{
		if( mConfigUtils == null && context != null )
		{
			synchronized( TAG )
			{
				if( mConfigUtils == null && context != null )
				{
					mConfigUtils = new ConfigUtils( context );
				}
			}
		}
		return mConfigUtils;
	}
	
	public void initStatistics()
	{
		try
		{
			olapStatistics();
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// gaominghui@2016/04/11 ADD START
	/**
	 *
	 * @throws JSONException
	 * @throws NameNotFoundException
	 * @author gaominghui 2016年4月8日
	 */
	private void olapStatistics() throws JSONException , NameNotFoundException
	{
		SharedPreferences prefs = mContext.getSharedPreferences( "tangoClock" , Activity.MODE_PRIVATE );
		JSONObject tmp = getAssets();
		String appid = null;
		String sn = null;
		if( tmp != null )
		{
			appid = tmp.getString( "app_id" );
			sn = tmp.getString( "serialno" );
		}
		StatisticsExpandNew.setStatiisticsLogEnable( true );
		int versionCode = mContext.getPackageManager().getPackageInfo( mContext.getPackageName() , 0 ).versionCode;
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 4 , default_clock_package , "" + versionCode );//添加参数，将插件自己包名作为参数上传
			if( prefs != null && !prefs.contains( "first_run" ) )
				prefs.edit().putBoolean( "first_run" , false ).apply();
		}
		else
		{
			//添加参数，将插件自己包名作为参数上传
			StatisticsExpandNew.use( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 4 , default_clock_package , "" + versionCode );
		}
	}
	
	private static final String CONFIG_FILE_NAME = "config.ini";
	
	private JSONObject getAssets()
	{
		Context remoteContext;
		JSONObject config = null;
		try
		{
			remoteContext = mContext.createPackageContext( mContext.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
			AssetManager assetManager = remoteContext.getAssets();
			InputStream inputStream = assetManager.open( CONFIG_FILE_NAME );
			String text = readTextFile( inputStream );
			JSONObject jObject = new JSONObject( text );
			config = new JSONObject( jObject.getString( "config" ) );
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
	
	private static String readTextFile(
			InputStream inputStream )
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		int len;
		try
		{
			while( ( len = inputStream.read( buf ) ) != -1 )
			{
				outputStream.write( buf , 0 , len );
			}
			outputStream.close();
			inputStream.close();
		}
		catch( IOException e )
		{
		}
		return outputStream.toString();
	}
}
