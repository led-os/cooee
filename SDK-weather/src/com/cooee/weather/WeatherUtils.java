package com.cooee.weather;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


/**
 * 天气客户端相关的工具类
 * @author fulijuan 2017-4-7
 */
public class WeatherUtils
{
	
	/**2345天气客户端包名*/
	public static final String WEATHER_CLIENT_PACKAGE_NAME_2345 = "com.tianqiwhite";
	/**我们自己的天气客户端包名*/
	public static final String WEATHER_CLIENT_PACKAGE_NAME_COOEE = "com.cooee.widget.samweatherclock";
	//客户端响应的action
	public static final String WEATHER_CLIENT_CLOSED_UPDATE_LAUNCHER_ACTION = "com.cooee.weather.Weather.action.CLOSED_UPDATE_LAUNCHER";
	public static final String WEATHER_CLIENT_UPDATE_RESULT_ACTION = "com.cooee.weather.data.action.UPDATE_RESULT";
	public static final String WEATHER_CLIENT_REQUEST_REFRESH_DATA_ACTION = "com.cooee.weather.Weather.action.REQUEST_REFRESH_DATA";
	public static final String WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION = "com.cooee.weather.Weather.action.REFRESH_UPDATE_LAUNCHER";
	//两张数据表
	private static final String WEATHER_URI = "content://com.cooee.app.cooeeweather.dataprovider/weather";
	public static final String TAG = "WeatherUtils";
	
	/**
	 * 保存最新的数据  
	 * 从最新的WeatherEntity中取出数据 put到SharedPreferences
	 * @param mContext 上下文
	 * @param mCurWeatherEntity 当前数据最新的WeatherEntity
	 */
	public static void saveWeatherData(
			Context mContext ,
			WeatherEntity mCurWeatherEntity )
	{
		if( mCurWeatherEntity != null )
		{
			int curTempC = mCurWeatherEntity.getTempC();
			String cityName = mCurWeatherEntity.getCity();
			String weather_index = mCurWeatherEntity.getWeather_index();
			Integer hTempC = mCurWeatherEntity.getTempH();
			Integer lTempC = mCurWeatherEntity.getTempL();
			String weather = mCurWeatherEntity.getCondition();
			//MODE_MULTI_PROCESS  MODE_PRIVATE
			SharedPreferences preferences = mContext.getSharedPreferences( "widgetnative" , Context.MODE_PRIVATE );
			Editor editor = preferences.edit();
			editor.putString( "currentCity" , cityName );
			editor.putString( "currentWeatherIndex" , weather_index );
			editor.putString( "currentWeather" , weather );
			editor.putInt( "currentTemp" , curTempC );
			editor.putInt( "hTempC" , hTempC );
			editor.putInt( "lTempC" , lTempC );
			editor.commit();
		}
	}
	
	/**
	 * 获取天气数据  并且同时将数据set到WeatherEntity
	 * 根据SharedPreferences初始化默认天气信息，如果没有，就使用默认值
	 * @param mContext 上下文
	 * @param defaultCityName 默认的城市名
	 * @param defaultWeatherIndex 默认的天气状况对应的图片index
	 * @param defaultWeather 默认的天气状况
	 * @param defaultTempC 默认的当前温度
	 * @param hTempC 默认的最高温度
	 * @param lTempC 默认的最低温度
	 * @return
	 */
	public static WeatherEntity getWeatherInfo(
			Context mContext ,
			String defaultCityName ,
			String defaultWeatherIndex ,
			String defaultWeather ,
			int defaultTempC ,
			int hTempC ,
			int lTempC )
	{
		SharedPreferences preferences = mContext.getSharedPreferences( "widgetnative" , Context.MODE_PRIVATE );
		defaultCityName = preferences.getString( "currentCity" , defaultCityName );
		defaultWeatherIndex = preferences.getString( "currentWeatherIndex" , defaultWeatherIndex );
		defaultWeather = preferences.getString( "currentWeather" , defaultWeather );
		defaultTempC = preferences.getInt( "currentTemp" , defaultTempC );
		hTempC = preferences.getInt( "hTempC" , hTempC );
		lTempC = preferences.getInt( "lTempC" , lTempC );
		//
		WeatherEntity mCurWeatherEntity = new WeatherEntity();
		mCurWeatherEntity.setCity( defaultCityName );
		mCurWeatherEntity.setWeather_index( defaultWeatherIndex );
		mCurWeatherEntity.setCondition( defaultWeather );
		mCurWeatherEntity.setTempC( defaultTempC );
		mCurWeatherEntity.setTempH( hTempC );
		mCurWeatherEntity.setTempL( lTempC );
		return mCurWeatherEntity;
	}
	
	/**
	 * 收到天气信息后,根据收取到的信息进行展示或抛弃
	 * 将天气信息set到WeatherEntity 并且返回出去
	 * @param context 上下文
	 * @param intent 包含天气信息的intent
	 */
	public static WeatherEntity receiveWeatherInfo(
			Context context ,
			Intent intent ,
			String default_weather_package )
	{
		String action = intent.getAction();
		String cityName = intent.getExtras().getString( "postalCode" );
		Integer curTempC = intent.getExtras().getInt( "T0_tempc_now" );
		Integer curHTempC = intent.getExtras().getInt( "T0_tempc_high" );
		Integer curLTempC = intent.getExtras().getInt( "T0_tempc_low" );
		String condition = intent.getExtras().getString( "T0_condition" );
		String condition_index = null;
		// 我们的客户端中发送的WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION取值比较特殊,所以先尝试一下能否取到值,取不到值则为其他情况
		// CLOSED_UPDATE_LAUNCHER广播中的值跟2345客户端的WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION广播中的值字段统一,可以用同样逻辑
		if( WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION.equals( action ) )
			condition_index = intent.getExtras().getString( "T0_condition_index2" );
		if( condition_index == null )
			condition_index = intent.getExtras().getString( "T0_condition_index" );
		// 把客户端当前选中的城市取出来对比一下,广播过来的城市名字如果不与其相等,就抛弃这条广播信息
		String clientDefaultCity = null;
		if( WEATHER_CLIENT_PACKAGE_NAME_2345.equals( default_weather_package ) )
			clientDefaultCity = ContentProviderUtil2345.getSelectedCityName( context );
		else
			if( WEATHER_CLIENT_PACKAGE_NAME_COOEE.equals( default_weather_package ) )
			clientDefaultCity = ContentProviderUtilCooee.getSelectedCity( context );
		Log.i( TAG , "receiveWeatherInfo" );
		if( cityName != null && clientDefaultCity != null && cityName.equals( clientDefaultCity ) )
		{
			WeatherEntity newWeatherEntity = new WeatherEntity();
			newWeatherEntity.setCity( cityName );
			if( curTempC != null )
				newWeatherEntity.setTempC( curTempC );
			if( curHTempC != null )
				newWeatherEntity.setTempH( curHTempC );
			if( curLTempC != null )
				newWeatherEntity.setTempL( curLTempC );
			if( condition != null )
				newWeatherEntity.setCondition( condition );
			if( condition_index != null )
				newWeatherEntity.setWeather_index( condition_index );
			Log.i( TAG , "cyk newWeatherEntity = " + newWeatherEntity.toString() );
			return newWeatherEntity;
		}
		return null;
	}
	
	/**
	 * 检测是否有default_package_name对应的天气客户端
	 * @param mContext 上下文
	 * @param default_package_name 天气客户端包名
	 * @return
	 */
	public static boolean isWeatherAtapterInstall(
			Context mContext ,
			String default_package_name )
	{
		PackageInfo packageInfo;
		try
		{
			packageInfo = mContext.getPackageManager().getPackageInfo( default_package_name , 0 );
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
	
	/**
	 * 如果获取到我们自己的客户端的天气数据：城市名字，通过以下两个uri读取天气客户端数据
	 * ① WEATHER_URI + "/" + postalCode     ——————》  WeatherEntity.projection
	 * ② WEATHER_URI + "/" + postalCode + "/detail"    ——————》  WeatherEntity.detailProjection
	 * @param context
	 * @return
	 * @author fulijuan 2017-4-12
	 */
	public static WeatherEntity readData(
			Context context )
	{
		WeatherEntity dataEntity = null;
		String postalCode = ContentProviderUtilCooee.getSelectedCity( context );
		if( postalCode == null || "".equals( postalCode ) || "null".equals( postalCode ) )
		{
		}
		else
		{
			ContentResolver resolver = context.getContentResolver();
			Uri CONTENT_URI = Uri.parse( WEATHER_URI + "/" + postalCode );
			String selection = WeatherEntity.POSTALCODE + "=" + "'" + postalCode + "' or " + WeatherEntity.CITY + " = '" + postalCode + "'";
			Cursor cursor = resolver.query( CONTENT_URI , WeatherEntity.projection , selection , null , null );
			if( cursor != null )
			{
				dataEntity = new WeatherEntity();
				if( cursor.moveToFirst() )
				{
					dataEntity.setUpdateMilis( cursor.getInt( 0 ) );
					dataEntity.setCity( cursor.getString( 1 ) );
					dataEntity.setPostalCode( cursor.getString( 2 ) );
					dataEntity.setForecastDate( cursor.getLong( 3 ) );
					dataEntity.setCondition( cursor.getString( 4 ) );
					dataEntity.setTempF( cursor.getInt( 5 ) );
					dataEntity.setTempC( cursor.getInt( 6 ) );
					dataEntity.setHumidity( cursor.getString( 7 ) );
					dataEntity.setIcon( cursor.getString( 8 ) );
					dataEntity.setWindCondition( cursor.getString( 9 ) );
					dataEntity.setLastUpdateTime( cursor.getLong( 10 ) );
					dataEntity.setIsConfigured( cursor.getInt( 11 ) );
				}
				cursor.close();
			}
			int details_count = 0;
			if( dataEntity != null )
			{
				CONTENT_URI = Uri.parse( WEATHER_URI + "/" + postalCode + "/detail" );
				selection = WeatherEntity.POSTALCODE + "=" + "'" + postalCode + "' or " + WeatherEntity.CITY + " = '" + postalCode + "'";
				cursor = resolver.query( CONTENT_URI , WeatherEntity.detailProjection , selection , null , null );
				if( cursor != null )
				{
					WeatherEntity forecast;
					while( cursor.moveToNext() )
					{
						forecast = new WeatherEntity();
						forecast.setDayOfWeek( cursor.getInt( 2 ) );
						forecast.setLow( cursor.getInt( 3 ) );
						forecast.setHight( cursor.getInt( 4 ) );
						forecast.setIcon( cursor.getString( 5 ) );
						forecast.setCondition( cursor.getString( 6 ) );
						// forecast.setWidgetId(cursor.getInt(6));
						dataEntity.getDetails().add( forecast );
						details_count = details_count + 1;
					}
					cursor.close();
				}
			}
			if( details_count < 4 )
			{
				dataEntity = null;
			}
		}
		return dataEntity;
	}
	
	/**
	 * 向天气客户端发送请求数据刷新广播
	 */
	public static void sendRefreshWeatherBroadcast(
			Context context )
	{
		Intent intent = new Intent();
		intent.setAction( WEATHER_CLIENT_REQUEST_REFRESH_DATA_ACTION );
		context.sendBroadcast( intent );
	}
}
