package com.cooee.widgetnative.ALL3in1.ClockWeather;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.widgetnative.ALL3in1.R;
import com.cooee.widgetnative.ALL3in1.ClockWeather.activity.DownloadActivity;
import com.cooee.widgetnative.ALL3in1.ClockWeather.common.WeatherCondition;
import com.cooee.widgetnative.ALL3in1.ClockWeather.common.WeatherEntity;
import com.cooee.widgetnative.ALL3in1.ClockWeather.common.WeatherIMG;
import com.cooee.widgetnative.ALL3in1.base.WidgetManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class WeatherManager
{
	
	//private final static String DATA_SERVICE_ACTION = "com.cooee.app.cooeeweather.dataprovider.weatherDataService";
	private final static String CLOSED_UPDATE_LAUNCHER = "com.cooee.weather.Weather.action.CLOSED_UPDATE_LAUNCHER";
	private final static String UPDATA_WHEATER_ACTION = "com.cooee.weather.data.action.UPDATE_RESULT";
	private static final String REQUEST_REFRESH_ACTION = "com.cooee.weather.Weather.action.REQUEST_REFRESH_DATA";
	private static final String REFRESH_ACTION = "com.cooee.weather.Weather.action.REFRESH_UPDATE_LAUNCHER";
	private Context mContext = null;
	private WeatherEntity weatherEntity = null;
	private String defaultCityName = null;
	private String defaultWeatherName = null;
	private String defaultWeatherIndex = "WEATHER_FINE";
	private int defaultTempC;
	private int defaultHTempC;
	private int defaultLTempC;
	private boolean isFirst = false;
	private boolean showDefaultData = false;
	// gaominghui@2016/04/11 ADD START 获取天气来源的天气客户端 的包名
	private String default_weather_package = null;
	// gaominghui@2016/04/11 ADD END 获取天气来源的天气客户端 的包名
	private static WeatherManager mWeatherManager = null;
	private static final String TAG = "WeatherManager";
	
	//
	//
	//
	private WeatherManager(
			Context mContext )
	{
		this.mContext = mContext;
		//注册广播
		mContext.getApplicationContext().registerReceiver( mWeatherReceiver , new IntentFilter( CLOSED_UPDATE_LAUNCHER ) );
		mContext.getApplicationContext().registerReceiver( mWeatherReceiver , new IntentFilter( UPDATA_WHEATER_ACTION ) );
		mContext.getApplicationContext().registerReceiver( mWeatherReceiver , new IntentFilter( REFRESH_ACTION ) );
		weatherEntity = new WeatherEntity();
		sendRefreshWeatherBroadcast();
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
	
	public void initConfig()
	{
		//默认天气，城市，温度
		defaultCityName = mContext.getResources().getString( R.string.weather_city_name_default );
		defaultWeatherName = mContext.getResources().getString( R.string.weather_data_name_default );
		defaultTempC = Integer.valueOf( mContext.getResources().getString( R.string.weather_tempC_default ) );
		showDefaultData = mContext.getResources().getBoolean( R.bool.show_default_data );
		default_weather_package = mContext.getResources().getString( R.string.default_weather_package );
		initWeatherInfo();
	}
	
	/**
	 * 根据SharedPreferences初始化默认天气信息
	 */
	private void initWeatherInfo()
	{
		SharedPreferences preferences = mContext.getSharedPreferences( "twinkle_clock_widget" , Context.MODE_PRIVATE );
		isFirst = preferences.getBoolean( "isFirst" , false );
		defaultCityName = preferences.getString( "currentCity" , defaultCityName );
		defaultWeatherName = preferences.getString( "currentWeather" , defaultWeatherName );
		String language = mContext.getResources().getConfiguration().locale.getCountry();
		defaultWeatherName = WeatherCondition.convertCondition( defaultWeatherName , language );
		defaultWeatherIndex = preferences.getString( "currentWeatherIndex" , defaultWeatherIndex );
		defaultHTempC = preferences.getInt( "highTemp" , defaultHTempC );
		defaultTempC = preferences.getInt( "currentTemp" , defaultTempC );
		defaultLTempC = preferences.getInt( "lowTemp" , defaultLTempC );
	}
	
	public void initView()
	{
		/**获取语言*/
		String language = mContext.getResources().getConfiguration().locale.getCountry();
		/**初始化一个默认城市*/
		defaultWeatherName = WeatherCondition.convertCondition( "晴" , language );
		Log.d( TAG , "isWeatherAtapterInstall() = " + isWeatherAtapterInstall() );
		isFirst = true;
		if( isWeatherAtapterInstall() )
		{
			if( isFirst )
			{
				updateData( null );
			}
			else
			{
				if( !showDefaultData )
					ClockWeatherManager.getInstance( mContext ).setWeatherVisibility( View.GONE );
			}
		}
		else
		{
			if( !showDefaultData )
				ClockWeatherManager.getInstance( mContext ).setWeatherVisibility( View.GONE );
		}
	}
	
	public boolean onClick()
	{
		Log.d( TAG , "cyk 点击天气 default_weather_package: " + default_weather_package );
		try
		{
			final Intent intent = new Intent();
			PackageManager pm = mContext.getPackageManager();
			Intent mIntent = pm.getLaunchIntentForPackage( default_weather_package );
			if( intent != null )
			{
				olapStatistics();
				mContext.startActivity( mIntent );
			}
		}
		catch( Exception e )
		{
			//下载天气客户端
			downLoadClient( mContext );
		}
		return true;
	}
	
	private void downLoadClient(
			Context context )
	{
		Log.i( TAG , "cyk startDownloadActivity " );
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
			appid = tmp.getString( "app_id" );
			sn = tmp.getString( "serialno" );
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
	/**
	 * 天气广播处理
	 */
	private BroadcastReceiver mWeatherReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( intent == null )
				return;
			Log.v( "wangjing" , "ACTION :" + intent.getAction() );
			if( intent.getAction().equals( UPDATA_WHEATER_ACTION ) )
			{
				Bundle bundle = intent.getExtras();
				if( bundle.getString( "cooee.weather.updateResult" ).equals( "UPDATE_SUCCESED" ) )
				{
					Log.v( "wangjing" , "UPDATA_WHEATER_ACTION !" );
					sendRefreshWeatherBroadcast();
				}
			}
			if( intent.getAction().equals( CLOSED_UPDATE_LAUNCHER ) )
			{
				String cityName = intent.getExtras().getString( "postalCode" );
				Integer curTempC = intent.getExtras().getInt( "T0_tempc_now" );
				Integer curHTempC = intent.getExtras().getInt( "T0_tempc_high" );
				Integer curLTempC = intent.getExtras().getInt( "T0_tempc_low" );
				String condition = intent.getExtras().getString( "T0_condition" );
				String condition_index = intent.getExtras().getString( "T0_condition_index" );
				if( cityName != null )
					weatherEntity.setCityName( cityName );
				if( curTempC != null )
					weatherEntity.setCurTempC( curTempC );
				if( curHTempC != null )
					weatherEntity.setHTempC( curHTempC );
				if( curLTempC != null )
					weatherEntity.setLTempC( curLTempC );
				if( condition != null )
					weatherEntity.setWeather( condition );
				if( condition_index != null )
					weatherEntity.setWeather_index( condition_index );
				updateData( weatherEntity );
			}
			else
				if( intent.getAction().equals( REFRESH_ACTION ) )
			{
				Log.v( "wangjing" , "REFRESH_ACTION !!!" );
				try
				{
					String cityName = intent.getExtras().getString( "postalCode" );
					//Log.v( "wangjing" , "cityName is :" + cityName );
					Integer curTempC = intent.getExtras().getInt( "T0_tempc_now" );
					//Log.v( "wangjing" , "curTempC is :" + curTempC );
					Integer curHTempC = intent.getExtras().getInt( "T0_tempc_high" );
					//Log.v( "wangjing" , "curHTempC is :" + curHTempC );
					Integer curLTempC = intent.getExtras().getInt( "T0_tempc_low" );
					//Log.v( "wangjing" , "curLTempC is :" + curLTempC );
					String condition = intent.getExtras().getString( "T0_condition" );
					//Log.v( "condition" , "condition is :" + condition );
					String condition_index = intent.getExtras().getString( "T0_condition_index2" );
					//Log.v( "wangjing" , "condition_index is :" + condition_index );
					if( cityName != null )
					{
						weatherEntity.setCityName( cityName );
						if( curTempC != null )
							weatherEntity.setCurTempC( curTempC );
						if( curHTempC != null )
							weatherEntity.setHTempC( curHTempC );
						if( curLTempC != null )
							weatherEntity.setLTempC( curLTempC );
						if( condition != null )
							weatherEntity.setWeather( condition );
						if( condition_index != null )
							weatherEntity.setWeather_index( condition_index );
						Log.i( "tianyu" , "curTempC = " + curTempC + "  curHTempC = " + curHTempC + "  curLTempC = " + curLTempC );
						updateData( weatherEntity );
					}
					else
					{
						updateData( null );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			WidgetManager.getInstance( mContext ).updateAppWidget();
		}
	};
	
	private void sendRefreshWeatherBroadcast()
	{
		Intent intent = new Intent();
		intent.setAction( REQUEST_REFRESH_ACTION );
		mContext.sendBroadcast( intent );
	}
	
	// 更新数据
	private void updateData(
			WeatherEntity weatherEntity )
	{
		if( weatherEntity == null || weatherEntity.getCurTempC() == 0 )
		{
			weatherEntity = new WeatherEntity();
			weatherEntity.setCityName( defaultCityName );
			weatherEntity.setHTempC( defaultHTempC );
			weatherEntity.setCurTempC( defaultTempC );
			weatherEntity.setLTempC( defaultLTempC );
			weatherEntity.setWeather( defaultWeatherName );
			weatherEntity.setWeather_index( defaultWeatherIndex );
		}
		else
			if( weatherEntity != null )
		{
			//测试数据
			/*weatherEntity.setHTempC( 20 );
			weatherEntity.setCurTempC( 20 );
			weatherEntity.setLTempC( 20 );*/
			ClockWeatherManager.getInstance( mContext ).setWeatherVisibility( View.VISIBLE );
			SharedPreferences preferences = mContext.getSharedPreferences( "twinkle_clock_widget" , Context.MODE_PRIVATE );
			Editor editor = preferences.edit();
			editor.putString( "currentCity" , weatherEntity.getCityName() );
			editor.putString( "currentWeather" , weatherEntity.getWeather() );
			editor.putString( "currentWeatherIndex" , weatherEntity.getWeather_index() );
			editor.putInt( "highTemp" , weatherEntity.getHTempC() );
			editor.putInt( "currentTemp" , weatherEntity.getCurTempC() );
			editor.putInt( "lowTemp" , weatherEntity.getLTempC() );
			if( !isFirst )
			{
				editor.putBoolean( "isFirst" , true );
			}
			editor.commit();
		}
		if( "none".equals( weatherEntity.getCityName() ) )
		{
			return;
		}
		this.weatherEntity = weatherEntity;
	}
	
	public void updateWeatherView()
	{
		//温度
		final int curTempC = weatherEntity.getCurTempC();
		final int highTempC = weatherEntity.getHTempC();
		final int lowTempC = weatherEntity.getLTempC();
		//城市
		final String cityName = weatherEntity.getCityName();
		//
		ClockWeatherManager instance = ClockWeatherManager.getInstance( mContext );
		RemoteViews rv = WidgetManager.getInstance( mContext ).getRemoteViews();
		if( instance.showWeatherVeiw )
		{
			String highLowTemp = highTempC + "°/" + lowTempC + "°";
			rv.setTextViewText( R.id.temperature_range , highLowTemp );
			String temp = curTempC + "°c";
			rv.setTextViewText( R.id.temperature_current , temp );
			rv.setTextViewText( R.id.city_textview , cityName );
		}
		//天气
		final String weather = weatherEntity.getWeather();
		final String weather_index = weatherEntity.getWeather_index();
		if( weather_index != null && weather != null )
		{
			int matchingWeatherId = matchingWeather( weather_index );
			rv.setImageViewResource( R.id.weather_city , matchingWeatherId );
		}
	}
	
	// 匹配天气 ，获得天气图片路径
	private int matchingWeather(
			String weather_index )
	{
		Log.v( "wangjing" , "weather_index is : " + weather_index );
		if( weather_index.equals( "WEATHER_OVERCAST" ) )
			return WeatherIMG.OVERCASTS;
		if( weather_index.equals( "WEATHER_FOG" ) || weather_index.equals( "WEATHER_HAZE" ) )
			return WeatherIMG.FOGS;
		if( weather_index.equals( "WEATHER_CLOUDY" ) )
			return WeatherIMG.MOSTCLOUDYS;
		if( weather_index.equals( "WEATHER_RAIN" ) || weather_index.equals( "WEATHER_LIGHTRAIN" ) || weather_index.equals( "WEATHER_RAINSTORM" ) || weather_index.equals( "WEATHER_STORM" ) )
			return WeatherIMG.RAINS;
		if( weather_index.equals( "WEATHER_SNOW" ) || weather_index.equals( "WEATHER_SLEET" ) )
			return WeatherIMG.SNOWS;
		if( weather_index.equals( "WEATHER_FINE" ) || weather_index.equals( "WEATHER_HOT" ) )
			return WeatherIMG.SUNNYS;
		if( weather_index.equals( "WEATHER_THUNDERSTORM" ) )
			return WeatherIMG.THUNDERSTORMS;
		else
			return WeatherIMG.UNKONWN;
	}
	
	private boolean isWeatherAtapterInstall()
	{
		PackageInfo packageInfo;
		try
		{
			packageInfo = mContext.getPackageManager().getPackageInfo( "com.cooee.widget.samweatherclock" , 0 );
		}
		catch( NameNotFoundException e )
		{
			packageInfo = null;
			e.printStackTrace();
		}
		if( packageInfo == null )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
