package com.cooee.widgetnative.CW3in1.ClockWeather;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.weather.WeatherEntity;
import com.cooee.weather.WeatherIMG;
import com.cooee.weather.WeatherUtils;
import com.cooee.widgetnative.CW3in1.R;
import com.cooee.widgetnative.CW3in1.ClockWeather.activity.DownloadActivity;
import com.cooee.widgetnative.CW3in1.base.WidgetManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class WeatherManager
{
	
	private Context mContext = null;
	/**是否默认显示数据*/
	private boolean showDefaultData = false;
	private String default_weather_package = null;
	// gaominghui@2016/04/11 ADD END 获取天气来源的天气客户端 的包名
	private static WeatherManager mWeatherManager = null;
	private static final String TAG = "WeatherManager";
	
	/** 初始化天气图片资源id */
	static
	{
		WeatherIMG.initWeatherDataImageId(
				R.drawable.weather_data_fog ,
				R.drawable.weather_data_mostcloudy ,
				R.drawable.weather_data_overcast ,
				R.drawable.weather_data_rain ,
				R.drawable.weather_data_snow ,
				R.drawable.weather_data_sunny ,
				R.drawable.weather_data_thunderstorm ,
				R.drawable.weather_data_unknow ,
				-1 ,
				-1 );
	}
	
	/**
	 * 初始化天气默认配置
	 */
	static
	{
		WeatherUtils.initWeatherDefaultConfig(
				R.integer.weather_default_dock_mode ,
				R.bool.weather_default_is_available ,
				R.string.weather_default_packageName ,
				R.string.weather_default_cityName ,
				R.string.weather_default_condition ,
				R.string.weather_default_weatherIndex ,
				R.integer.weather_default_tempC ,
				R.integer.weather_default_HtempC ,
				R.integer.weather_default_LtempC );
	}
	
	private WeatherManager(
			Context mContext )
	{
		this.mContext = mContext;
		initConfig();
	}
	
	public static WeatherManager getInstance(
			Context context )
	{
		if( mWeatherManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mWeatherManager == null && context != null )
				{
					mWeatherManager = new WeatherManager( context );
				}
			}
		}
		return mWeatherManager;
	}
	
	/**
	 * 初始化默认配置
	 */
	public void initConfig()
	{
		showDefaultData = mContext.getResources().getBoolean( R.bool.show_default_data );
		default_weather_package = mContext.getResources().getString( R.string.weather_default_packageName );
		if( showDefaultData )
		{ //默认显示数据
			// 使用默认配置，初始化天气数据
			WeatherUtils.initWeatherInfoUseDefaultConfig( mContext );
			WidgetManager.getInstance( mContext ).setWeatherVisibility( View.VISIBLE );
		}
		else
		{
			WidgetManager.getInstance( mContext ).setWeatherVisibility( View.GONE );
		}
	}
	
	/**
	 * 响应点击事件
	 */
	public void onClick()
	{
		Log.d( TAG , "cyk 点击天气" );
		boolean hasClient = WeatherUtils.isWeatherClientInstall( mContext , default_weather_package );
		if( hasClient )
		{
			PackageManager pm = mContext.getPackageManager();
			Intent mIntent = pm.getLaunchIntentForPackage( default_weather_package );
			if( null != mIntent )
			{
				mContext.startActivity( mIntent );
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
		}
		else
		{
			downLoadClient( mContext );
		}
	}
	
	private void downLoadClient(
			Context context )
	{
		//下载客户端
		Intent intent = new Intent( context , DownloadActivity.class );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity( intent );
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
		SharedPreferences prefs = mContext.getSharedPreferences( "weather" , Activity.MODE_PRIVATE );
		JSONObject tmp = getAssets();
		String appid = null;
		String sn = null;
		if( tmp != null )
		{
			appid = tmp.optString( "app_id" );
			sn = tmp.optString( "serialno" );
		}
		StatisticsExpandNew.setStatiisticsLogEnable( true );
		int versionCode = mContext.getPackageManager().getPackageInfo( mContext.getPackageName() , 0 ).versionCode;
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 4 , default_weather_package , "" + versionCode );//添加参数，将插件自己包名作为参数上传
			if( prefs != null && !prefs.contains( "first_run" ) )
				prefs.edit().putBoolean( "first_run" , false ).apply();
		}
		else
		{
			//添加参数，将插件自己包名作为参数上传
			StatisticsExpandNew.use( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 4 , default_weather_package , "" + versionCode );
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
	
	// gaominghui@2016/04/11 ADD END
	public void updateWeatherView()
	{
		//获取当前天气数据对象
		WeatherEntity mCurWeatherEntity = WeatherUtils.getCurWeatherEntity();
		if( null != mCurWeatherEntity )
		{
			//温度
			final int curTempC = mCurWeatherEntity.getTempC();
			final int highTempC = mCurWeatherEntity.getTempH();
			final int lowTempC = mCurWeatherEntity.getTempL();
			//城市
			final String cityName = mCurWeatherEntity.getCity();
			//
			WidgetManager mWidgetManager = WidgetManager.getInstance( mContext );
			RemoteViews mRemoteViews = mWidgetManager.getRemoteViews();
			if( mWidgetManager.showWeatherVeiw )
			{
				String highLowTemp = highTempC + "°/" + lowTempC + "°";
				mRemoteViews.setTextViewText( R.id.temperature_range , highLowTemp );
				String temp = curTempC + "°c";
				mRemoteViews.setTextViewText( R.id.temperature_current , temp );
				mRemoteViews.setTextViewText( R.id.city_textview , cityName );
			}
			//天气
			final String condition = mCurWeatherEntity.getCondition();
			final String weather_index = mCurWeatherEntity.getWeatherIndex();
			if( weather_index != null && condition != null )
			{
				int matchingWeatherId = WeatherIMG.getWeatherDataImageId( mContext , condition , false );
				mRemoteViews.setImageViewResource( R.id.weather_city , matchingWeatherId );
			}
		}
	}
}
