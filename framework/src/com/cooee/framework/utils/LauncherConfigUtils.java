package com.cooee.framework.utils;


import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.util.Log;

import com.cooee.shell.sdk.CooeeSdk;


public class LauncherConfigUtils
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
	
	private static void initConfig(
			Context context )
	{
		config = getAssetFileJSON( context , CONFIG_FILE_NAME );
		if( config != null )
		{
			SharedPreferences prefs = context.getSharedPreferences( PREFERENCE_KEY_CONFIG , Activity.MODE_WORLD_READABLE );
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
	
	public static String getAppID(
			Context context )
	{
		if( appid == null )
		{
			initConfig( context );
		}
		return appid;
	}
	
	public static String getSN(
			Context context )
	{
		if( sn == null )
		{
			initConfig( context );
		}
		return sn;
	}
	
	public static int getLauncherVersion(
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
	
	public static String cooeeGetCooeeId(
			Context context )
	{
		return CooeeSdk.cooeeGetCooeeId( context );
	}
	
	public static JSONObject getAssetFileJSON(
			Context context ,
			String fileName )
	{
		AssetManager assetManager = context.getAssets();
		InputStream inputStream = null;
		try
		{
			inputStream = assetManager.open( fileName );
			String config = Utils.readTextFile( inputStream );
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
}
