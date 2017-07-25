package com.cooee.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.shell.sdk.CooeeSdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.util.Log;


public class ConfigUtils
{
	
	public static final String CONFIG_FILE_NAME = "config.ini";
	public static final String PREFERENCE_KEY_CONFIG = "config";
	public static final String PREFERENCE_KEY_CONFIG_DOMAIN = "domain";
	public static final String PREFERENCE_KEY_CONFIG_SERIALNO = "serialno";
	public static final String PREFERENCE_KEY_CONFIG_APPID = "app_id";
	public static final String PREFERENCE_KEY_CONFIG_TEMPLATEID = "template_id";
	public static final String PREFERENCE_KEY_CONFIG_CHANNELID = "channel_id";
	private static JSONObject config;
	private static String appid = null;
	private static String sn = null;
	private static int launcherVersion = -1;
	private static ConfigUtils mWidgetConfigUtils;
	private static String TAG = "WidgetConfigUtils";
	private static SharedPreferences prefs = null;
	private Context mContext;
	private String cooeeId = null;
	
	private ConfigUtils(
			Context context )
	{
		// TODO Auto-generated constructor stub
		mContext = context;
		initConfig();
		cooeeId = CooeeSdk.cooeeGetCooeeId( context );
	}
	
	private void initConfig()
	{
		config = getAssetFileJSON( mContext , CONFIG_FILE_NAME );
		if( config != null )
		{
			prefs = mContext.getSharedPreferences( PREFERENCE_KEY_CONFIG , Activity.MODE_PRIVATE );
			try
			{
				JSONObject tmp = config.getJSONObject( "config" );
				sn = tmp.getString( "serialno" );
				appid = tmp.getString( "app_id" );
				final String domain = tmp.getString( "domain" );
				final String template_id = tmp.getString( "template_id" );
				final String channel_id = tmp.getString( "channel_id" );
				Editor edit = prefs.edit();
				edit.putString( PREFERENCE_KEY_CONFIG_DOMAIN , domain );
				edit.putString( PREFERENCE_KEY_CONFIG_SERIALNO , sn );
				edit.putString( PREFERENCE_KEY_CONFIG_APPID , appid );
				edit.putString( PREFERENCE_KEY_CONFIG_TEMPLATEID , template_id );
				edit.putString( PREFERENCE_KEY_CONFIG_CHANNELID , channel_id );
				edit.commit();
			}
			catch( JSONException e1 )
			{
				e1.printStackTrace();
				sn = "";
				appid = "";
			}
		}
	}
	
	public static ConfigUtils getInstace(
			Context context )
	{
		if( mWidgetConfigUtils == null && context != null )
		{
			synchronized( TAG )
			{
				if( mWidgetConfigUtils == null && context != null )
				{
					mWidgetConfigUtils = new ConfigUtils( context );
				}
			}
		}
		return mWidgetConfigUtils;
	}
	
	public String getAppID()
	{
		if( appid == null )
		{
			if( prefs == null )
			{
				initConfig();
			}
			else
			{
				appid = prefs.getString( PREFERENCE_KEY_CONFIG_APPID , "" );
			}
			return appid;
		}
		return appid;
	}
	
	public String getSN()
	{
		if( sn == null )
		{
			if( prefs == null )
			{
				initConfig();
			}
			else
			{
				sn = prefs.getString( PREFERENCE_KEY_CONFIG_SERIALNO , "" );
			}
			return sn;
		}
		return sn;
	}
	
	public int getLauncherVersion(
			Context context )
	{
		if( launcherVersion == -1 )
		{
			PackageManager mPackageManager = context.getPackageManager();
			try
			{
				launcherVersion = mPackageManager.getPackageInfo( context.getPackageName() , 0 ).versionCode;
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				launcherVersion = 0;
				e.printStackTrace();
			}
		}
		return launcherVersion;
	}
	
	public String cooeeGetCooeeId()
	{
		if( cooeeId == null )
		{
			return CooeeSdk.cooeeGetCooeeId( mContext );
		}
		return cooeeId;
	}
	
	private JSONObject getAssetFileJSON(
			Context context ,
			String fileName )
	{
		AssetManager assetManager = context.getAssets();
		InputStream inputStream = null;
		try
		{
			inputStream = assetManager.open( fileName );
			String config = readTextFile( inputStream );
			JSONObject jObject;
			try
			{
				jObject = new JSONObject( config );
				//				JSONObject jRes = new JSONObject(jObject.getString("config"));
				return jObject;
			}
			catch( JSONException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		catch( IOException e )
		{
			Log.e( "tag" , e.getMessage() );
		}
		return null;
	}
	
	private String readTextFile(
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
