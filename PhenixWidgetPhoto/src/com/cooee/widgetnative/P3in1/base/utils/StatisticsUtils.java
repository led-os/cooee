package com.cooee.widgetnative.P3in1.base.utils;


import org.json.JSONException;

import com.cooee.statistics.StatisticsExpandNew;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;


public class StatisticsUtils
{
	
	private static final String TAG = "StatisticsUtils";
	public static StatisticsUtils mStatisticsUtils;
	private String default_clock_package = null;
	private Context mContext;
	private final int PRODUCTTYPE = 4;
	
	private StatisticsUtils(
			Context mContext )
	{
		this.mContext = mContext;
		default_clock_package = mContext.getPackageName();
	}
	
	public static StatisticsUtils getInstance(
			Context context )
	{
		if( mStatisticsUtils == null && context != null )
		{
			synchronized( TAG )
			{
				if( mStatisticsUtils == null && context != null )
				{
					mStatisticsUtils = new StatisticsUtils( context );
				}
			}
		}
		return mStatisticsUtils;
	}
	
	// gaominghui@2016/04/11 ADD START
	/**
	 *
	 * @throws JSONException
	 * @throws NameNotFoundException
	 * @author gaominghui 2016年4月8日
	 */
	public void olapStatistics() throws JSONException , NameNotFoundException
	{
		Log.d( TAG , "开始统计" );
		SharedPreferences prefs = mContext.getSharedPreferences( "tangoClock" , Activity.MODE_PRIVATE );
		String appid = ConfigUtils.getInstace( mContext ).getAppID();
		String sn = ConfigUtils.getInstace( mContext ).getSN();
		String cooeeId = ConfigUtils.getInstace( mContext ).cooeeGetCooeeId();
		int versionCode = mContext.getPackageManager().getPackageInfo( mContext.getPackageName() , 0 ).versionCode;
		StatisticsExpandNew.setStatiisticsLogEnable( true );
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( mContext , sn , appid , cooeeId , PRODUCTTYPE , default_clock_package , "" + versionCode );//添加参数，将插件自己包名作为参数上传
			if( prefs != null && !prefs.contains( "first_run" ) )
				prefs.edit().putBoolean( "first_run" , false ).apply();
		}
		else
		{
			//添加参数，将插件自己包名作为参数上传
			StatisticsExpandNew.use( mContext , sn , appid , cooeeId , PRODUCTTYPE , default_clock_package , "" + versionCode );
		}
	}
	
	//自定义统计
	public void customStatistics(
			int productType ,
			String evnet ,
			String productName )
	{
	}
}
