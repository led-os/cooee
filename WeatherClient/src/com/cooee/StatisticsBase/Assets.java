package com.cooee.StatisticsBase;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.util.Log;


public class Assets
{
	
	public static final String CONFIG_FILE_NAME = "config.ini";
	public static final String PREFERENCE_KEY_CONFIG = "config";
	public static final String PREFERENCE_KEY_CONFIG_DOMAIN = "domain";
	public static final String PREFERENCE_KEY_CONFIG_SERIALNO = "serialno";
	public static final String PREFERENCE_KEY_CONFIG_APPID = "app_id";
	public static final String PREFERENCE_KEY_CONFIG_TEMPLATEID = "template_id";
	public static final String PREFERENCE_KEY_CONFIG_CHANNELID = "channel_id";
	private static Context mContext;
	public static JSONObject config;
	
	public static void initAssets(
			Context context )
	{
		mContext = context;
		config = getConfig( CONFIG_FILE_NAME );
		if( config != null )
		{
			SharedPreferences prefs = mContext.getSharedPreferences( PREFERENCE_KEY_CONFIG , mContext.MODE_WORLD_READABLE );
			try
			{
				JSONObject tmp = config.getJSONObject( "config" );
				String serialno = tmp.getString( "serialno" );
				String domain = tmp.getString( "domain" );
				String app_id = tmp.getString( "app_id" );
				String template_id = tmp.getString( "template_id" );
				String channel_id = tmp.getString( "channel_id" );
				Editor edit = prefs.edit();
				edit.putString( PREFERENCE_KEY_CONFIG_DOMAIN , domain );
				edit.putString( PREFERENCE_KEY_CONFIG_SERIALNO , serialno );
				edit.putString( PREFERENCE_KEY_CONFIG_APPID , app_id );
				edit.putString( PREFERENCE_KEY_CONFIG_TEMPLATEID , template_id );
				edit.putString( PREFERENCE_KEY_CONFIG_CHANNELID , channel_id );
				edit.commit();
			}
			catch( JSONException e1 )
			{
				e1.printStackTrace();
			}
		}
	}
	
	public static JSONObject getConfig(
			String fileName )
	{
		AssetManager assetManager = mContext.getAssets();
		InputStream inputStream = null;
		try
		{
			inputStream = assetManager.open( fileName );
			String config = readTextFile( inputStream );
			JSONObject jObject;
			try
			{
				jObject = new JSONObject( config );
				// JSONObject jRes = new
				// JSONObject(jObject.getString("config"));
				return jObject;
			}
			catch( JSONException e1 )
			{
				e1.printStackTrace();
			}
		}
		catch( IOException e )
		{
			Log.e( "tag" , e.getMessage() );
		}
		return null;
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
