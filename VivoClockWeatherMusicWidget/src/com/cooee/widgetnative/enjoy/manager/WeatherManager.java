package com.cooee.widgetnative.enjoy.manager;


import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.download.Assets;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.weather.WeatherEntity;
import com.cooee.weather.WeatherIMG;
import com.cooee.weather.WeatherUtils;
import com.cooee.widgetnative.enjoy.DownloadActivity;
import com.cooee.widgetnative.enjoy.R;
import com.cooee.widgetnative.enjoy.WidgetProvider;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.RemoteViews;


public class WeatherManager
{
	
	/** 初始化天气图片资源id */
	static
	{
		WeatherIMG.initResId(
				R.drawable.weather_data_fog ,
				R.drawable.weather_data_mostcloudy ,
				R.drawable.weather_data_overcast ,
				R.drawable.weather_data_rain ,
				R.drawable.weather_data_snow ,
				R.drawable.weather_data_sunny ,
				R.drawable.weather_data_thunderstorm ,
				R.drawable.weather_data_unknow );
	}
	
	private static WeatherManager mWeatherManager;
	public static final String TAG = "WeatherManager";
	private Context mContext;
	/**天气配置*/
	private boolean showWeatherVeiw;
	/**获取天气来源的天气客户端的包名*/
	private String default_weather_package = null;
	private String defaultCityName = null;
	private String defaultWeatherIndex = "UNKONWN";
	private int defaultTempC = 0;
	private WeatherEntity mCurWeatherEntity = null;
	private int[] weatherNumbers = {
			R.drawable.weather_number_0 ,
			R.drawable.weather_number_1 ,
			R.drawable.weather_number_2 ,
			R.drawable.weather_number_3 ,
			R.drawable.weather_number_4 ,
			R.drawable.weather_number_5 ,
			R.drawable.weather_number_6 ,
			R.drawable.weather_number_7 ,
			R.drawable.weather_number_8 ,
			R.drawable.weather_number_9 };
			
	private WeatherManager(
			Context context )
	{
		mContext = context;
		initConfig();
	}
	
	public static WeatherManager getInstance(
			Context context )
	{
		if( mWeatherManager == null )
		{
			synchronized( TAG )
			{
				if( mWeatherManager == null )
				{
					mWeatherManager = new WeatherManager( context );
				}
			}
		}
		return mWeatherManager;
	}
	
	/**初始化配置*/
	private void initConfig()
	{
		//天气配置
		showWeatherVeiw = mContext.getResources().getBoolean( R.bool.show_weatherView );
		if( showWeatherVeiw )
		{
			defaultCityName = mContext.getResources().getString( R.string.city_name );
			defaultTempC = mContext.getResources().getInteger( R.integer.default_tempC );
			// YANGTIANYU@2016/08/29 ADD START
			// 对接2345
			default_weather_package = mContext.getResources().getString( R.string.default_weather_package );
			// YANGTIANYU@2016/08/29 ADD END
			initWeatherInfo();
		}
	}
	
	/**初始化view点击事件和是否可见*/
	public void initClickView(
			RemoteViews remoteview )
	{
		if( remoteview != null )
		{
			int visibility = View.GONE;
			if( showWeatherVeiw )
			{
				visibility = View.VISIBLE;
				Intent intentClockClick = new Intent( WidgetProvider.CLICK_WEATHER_LAYOUT );
				PendingIntent pendingClockIntent = PendingIntent.getBroadcast( mContext , 0 , intentClockClick , 0 );
				remoteview.setOnClickPendingIntent( R.id.weather_layout , pendingClockIntent );
			}
			remoteview.setViewVisibility( R.id.weather_layout , visibility );
		}
	}
	
	/**点击时钟*/
	public void onClick()
	{
		try
		{
			PackageManager pm = mContext.getPackageManager();
			Intent mIntent = pm.getLaunchIntentForPackage( default_weather_package );
			mContext.startActivity( mIntent );
			if( default_weather_package.equals( WeatherUtils.WEATHER_CLIENT_PACKAGE_NAME_2345 ) )
			{
				olapStatistics();
			}
		}
		catch( final Exception e )
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
	
	// gaominghui@2016/09/27 ADD START
	/**
	 *
	 * @throws JSONException
	 * @throws NameNotFoundException
	 * @author gaominghui 2016年4月8日
	 */
	private void olapStatistics() throws JSONException , NameNotFoundException
	{
		SharedPreferences prefs = mContext.getSharedPreferences( "weather" , Activity.MODE_PRIVATE );
		JSONObject tmp = Assets.getConfig( mContext );
		String appid = null;
		String sn = null;
		if( tmp != null )
		{
			appid = tmp.optString( "app_id" );
			sn = tmp.optString( "serialno" );
		}
		int versionCode = mContext.getPackageManager().getPackageInfo( mContext.getPackageName() , 0 ).versionCode;
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 4 , default_weather_package , "" + versionCode );
			prefs.edit().putBoolean( "first_run" , false ).apply();
		}
		else
		{
			StatisticsExpandNew.use( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 4 , default_weather_package , "" + versionCode );
		}
	}
	
	// gaominghui@2016/09/27 ADD END
	public void updateAllWidget(
			RemoteViews remoteview )
	{
		if( remoteview != null && showWeatherVeiw )
		{
			//城市
			remoteview.setTextViewText( R.id.weather_city , mCurWeatherEntity.getCity() );
			//天气图片
			int drawable = WeatherIMG.getWeatherDataImageIdByIndex( mCurWeatherEntity.getWeather_index() );
			remoteview.setImageViewResource( R.id.weather_img , drawable );
			//温度图片
			int visibility = View.INVISIBLE;
			if( mCurWeatherEntity.getTempC() < 0 )
			{
				visibility = View.VISIBLE;
			}
			//-号
			remoteview.setViewVisibility( R.id.weather_f , visibility );
			//数字
			remoteview.setImageViewResource( R.id.weather_tens , weatherNumbers[mCurWeatherEntity.getTempC() / 10] );
			remoteview.setImageViewResource( R.id.weather_ones , weatherNumbers[mCurWeatherEntity.getTempC() % 10] );
		}
	}
	
	public boolean isShowWeatherVeiw()
	{
		return showWeatherVeiw;
	}
	
	// fulijuan@2017/4/7 ADD START weather.jar中的调用
	/**
	 * 获取天气数据  并且同时将数据set到WeatherEntity
	 * 根据SharedPreferences初始化默认天气信息，如果没有，就使用默认值
	 */
	private void initWeatherInfo()
	{
		mCurWeatherEntity = WeatherUtils.getWeatherInfo( mContext , defaultCityName , defaultWeatherIndex , "" , defaultTempC , 0 , 0 );
	}
	
	/**
	 * 接收天气信息，并且set到WeatherEntity中
	 * 如果当前显示的天气信息和接收到的不一致，则更新，并且从最新的WeatherEntity中put进SharedPreferences
	 * @param mContext
	 * @param intent
	 * @author fulijuan 2017-4-7
	 */
	public void receiveWeatherInfo(
			Context mContext ,
			Intent intent )
	{
		WeatherEntity newWeatherEntity = WeatherUtils.receiveWeatherInfo( mContext , intent , default_weather_package );
		if( newWeatherEntity != null && !newWeatherEntity.equals( mCurWeatherEntity ) )
		{
			mCurWeatherEntity = newWeatherEntity;
			WeatherUtils.saveWeatherData( mContext , mCurWeatherEntity );
		}
	}
	// fulijuan@2017/4/7 ADD END weather.jar中的调用
}
