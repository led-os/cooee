package com.cooee.StatisticsBase;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cooee.statistics.StatisticsBaseNew;
import com.cooee.statistics.StatisticsExpandNew;


public class StatisticsMainBase
{
	
	private Context mContext = null;
	private String default_package = null;
	private final String LOG_TAG = "StatisticsMainBase";
	private String appid = null;
	private String sn = null;
	private int launcherVersion = -1;
	
	public StatisticsMainBase(
			Context mContext )
	{
		this.mContext = mContext;
		this.default_package = mContext.getPackageName();
		Log.v( LOG_TAG , "default_package is " + default_package );
	}
	
	public void oncreate()
	{
		Log.v( LOG_TAG , "set context" );
		StatisticsBaseNew.setApplicationContext( mContext );
		StatisticsExpandNew.setStatiisticsLogEnable( true );
		Assets.initAssets( mContext );
		JSONObject tmp = Assets.config;
		PackageManager mPackageManager = mContext.getPackageManager();
		try
		{
			JSONObject config = tmp.getJSONObject( "config" );
			appid = config.getString( "app_id" );
			sn = config.getString( "serialno" );
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		try
		{
			launcherVersion = mPackageManager.getPackageInfo( default_package , 0 ).versionCode;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		Log.d( LOG_TAG , "before first run" );
		Log.d( LOG_TAG , "in first run new dbhelp " );
		Dbhelp dbhelp = new Dbhelp( mContext.getApplicationContext() , "SamWeather.db" );
		SQLiteDatabase sqliteDatabase = dbhelp.getWritableDatabase();
		Log.d( LOG_TAG , "in first run sqliteDatabase = " + sqliteDatabase );
		if( !dbhelp.onSerch( sqliteDatabase , "SamWeatherClock" ) )
		{
			// dbhelp.onCreateTable(sqliteDatabase, "locktable");
			ContentValues values = new ContentValues();
			values.put( "_id" , 1 );
			values.put( "num" , 1 );
			sqliteDatabase.insert( "SamWeatherClock" , null , values );
			Log.d( LOG_TAG , "is first run" );
			// xiatian add start //StatisticsNew
			StatisticsExpandNew.register( mContext.getApplicationContext() , sn , appid , "" , 1 , default_package , "" + launcherVersion );
		}
		sqliteDatabase.close();
	}
	
	public void onResume()
	{
		Log.d( LOG_TAG , "not first run" );
		StatisticsExpandNew.use( mContext.getApplicationContext() , sn , appid , "" , 1 , default_package , "" + launcherVersion );
	}
}
