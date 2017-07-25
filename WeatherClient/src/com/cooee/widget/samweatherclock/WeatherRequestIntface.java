package com.cooee.widget.samweatherclock;


import java.util.ArrayList;

import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.SettingEntity;
import com.cooee.app.cooeeweather.dataentity.WeatherCondition;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.filehelp.Log;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;


public class WeatherRequestIntface extends BroadcastReceiver
{
	
	public static final String REQUEST_REFRESH_ACTION = "com.cooee.weather.Weather.action.REQUEST_REFRESH_DATA";
	//public static final String SIMPLELAUNCHER2_REQUEST_ACTION = "com.cooee.weather.Weather.action.SIMPLELAUNCHER2_REQUEST_ACTION";
	private final String REQUEST_CHANGE_ACTION = "com.cooee.weather.Weather.action.REQUEST_CHANGE_CITY_DATA";
	private final String REQUEST_CITYLIST_ACTION = "com.cooee.weather.Weather.action.REQUEST_GET_CITYLIST_DATA";
	private final String REFRESH_ACTION = "com.cooee.weather.Weather.action.REFRESH_UPDATE_LAUNCHER";
	private final String CHANGE_ACTION = "com.cooee.weather.Weather.action.CHANGE_UPDATE_LAUNCHER";
	private final String CITYLIST_ACTION = "com.cooee.weather.Weather.action.GET_CITYLIST_LAUNCHER";
	private final String POSTALCODE_URI = "content://com.cooee.app.cooeeweather.dataprovider/postalCode";
	private final String WEATHER_URI = "content://com.cooee.app.cooeeweather.dataprovider/weather";
	private final String SETTING_URI = "content://com.cooee.app.cooeeweather.dataprovider/setting";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		if( intent == null )
			return;
		Log.d( "weijie" , "onReceive Action = " + intent.getAction() );
		if( intent.getAction().equals( REQUEST_REFRESH_ACTION ) )
		{
			String postalCode = null;
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = null;
			Uri CONTENT_URI;
			Intent mIntent = new Intent();
			CONTENT_URI = Uri.parse( SETTING_URI );
			cursor = resolver.query( CONTENT_URI , new String[]{ " * " } , null , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					postalCode = cursor.getString( cursor.getColumnIndex( "maincity" ) );
				}
				cursor.close();
			}
			if( postalCode == null || "".equals( postalCode ) || "null".equals( postalCode ) )
			{
				mIntent.setAction( REFRESH_ACTION );
				mIntent.putExtra( "result" , "ERROR" );
				mIntent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETCITY_DATABASE );
				context.sendBroadcast( mIntent );
				Log.e( "weijie" , "onReceive sendBroadcast error2!! act=" + REFRESH_ACTION );
				return;
			}
			else
			{
				weatherdataentity mweatherdataentity = readData( context , postalCode );
				Log.e( "weijie" , "onReceive mweatherdataentity = " + mweatherdataentity );
				try
				{
					if( mweatherdataentity == null || !responseInent( mweatherdataentity , mIntent ) )
					{
						mIntent.setAction( REFRESH_ACTION );
						mIntent.putExtra( "result" , "ERROR" );
						mIntent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETDATA_DATABASE );
						mIntent.putExtra( "postalCode" , postalCode );
						context.sendBroadcast( mIntent );
						Log.e( "weijie" , "onReceive sendBroadcast error3!! act=" + REFRESH_ACTION );
						return;
					}
					mIntent.setAction( REFRESH_ACTION );
					mIntent.putExtra( "postalCode" , postalCode );
					mIntent.putExtra( "postalListId" , 0 );
					context.sendBroadcast( mIntent );
				}
				catch( Exception eo )
				{
					Log.v( "" , "onReceive mweatherdataentity Exception " + eo.toString() );
				}
			}
		}
		else if( intent.getAction().equals( REQUEST_CHANGE_ACTION ) )
		{
			String currentCity = intent.getStringExtra( "postCode" );
			Log.e( "weijie" , "onReceive REQUEST_CHANGE_ACTION in postCode=" + currentCity );
			Intent mIntent = new Intent();
			if( currentCity == null || currentCity.equals( "" ) || currentCity.equals( "none" ) )
			{
				currentCity = getMainCity( context );
				if( currentCity.equals( "" ) )
				{
					mIntent.setAction( REFRESH_ACTION );
					mIntent.putExtra( "result" , "ERROR" );
					mIntent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETMAINCITY );
					context.sendBroadcast( mIntent );
					return;
				}
			}
			ArrayList<String> list = getPostalCodeList( context );
			int index = 0;
			boolean find = false;
			String next_postCode = "";
			for( int i = 0 ; i < list.size() ; i++ )
			{
				String city = list.get( i );
				Log.e( "weijie" , "onReceive city=" + city );
				if( currentCity.equals( city ) )
				{
					index = i;
					find = true;
					break;
				}
			}
			if( !find )
			{
				mIntent.setAction( CHANGE_ACTION );
				mIntent.putExtra( "result" , "ERROR" );
				mIntent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETCITY_DATABASE );
				context.sendBroadcast( mIntent );
				Log.e( "weijie" , "onReceive sendBroadcast error2!! act=" + CHANGE_ACTION );
				return;
			}
			Log.e( "weijie" , "onReceive index=" + index + ",list.size()=" + list.size() );
			if( index == list.size() - 1 )
				index = 0;
			else
				index += 1;
			next_postCode = list.get( index );
			Log.e( "weijie" , "onReceive next_postCode=" + next_postCode );
			setMainCity( context , next_postCode );
			weatherdataentity mweatherdataentity = readData( context , next_postCode );
			Log.e( "weijie" , "onReceive mweatherdataentity = " + mweatherdataentity );
			if( mweatherdataentity == null || !responseInent( mweatherdataentity , mIntent ) )
			{
				mIntent.setAction( CHANGE_ACTION );
				mIntent.putExtra( "result" , "ERROR" );
				mIntent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETDATA_DATABASE );
				mIntent.putExtra( "postalCode" , next_postCode );
				context.sendBroadcast( mIntent );
				Log.e( "weijie" , "onReceive sendBroadcast error3!! act=" + CHANGE_ACTION );
				return;
			}
			mIntent.setAction( CHANGE_ACTION );
			mIntent.putExtra( "postalCode" , next_postCode );
			mIntent.putExtra( "postalListId" , index );
			context.sendBroadcast( mIntent );
			Log.e( "weijie" , "onReceive sendBroadcast!! act=" + CHANGE_ACTION );
		}
		else if( intent.getAction().equals( REQUEST_CITYLIST_ACTION ) )
		{
			ArrayList<String> list = getPostalCodeList( context );
			Intent mIntent = new Intent();
			mIntent.setAction( CITYLIST_ACTION );
			String result = "";
			if( list != null )
			{
				mIntent.putExtra( "list_size" , list.size() );
				for( int i = 0 ; i < list.size() ; i++ )
				{
					String list_city_number = "list_city_" + String.valueOf( i );
					mIntent.putExtra( list_city_number , list.get( i ) );
				}
				result = "OK";
			}
			else
			{
				result = "ERROR";
				mIntent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETDATA_DATABASE );
			}
			mIntent.putExtra( "result" , result );
			context.sendBroadcast( mIntent );
			Log.e( "weijie" , "onReceive sendBroadcast!! act=" + CITYLIST_ACTION );
		}
		
	}
	
	public ArrayList<String> getPostalCodeList(
			Context context )
	{
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		Uri uri = Uri.parse( POSTALCODE_URI );
		ArrayList<String> CityList = new ArrayList<String>();
		String selection;
		selection = PostalCodeEntity.USER_ID + "=" + "'0'";
		cursor = resolver.query( uri , PostalCodeEntity.projection , selection , null , null );
		Log.e( "weijie" , "getPostalCodeList cursor = " + cursor );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				do
				{
					PostalCodeEntity mPostalCodeEntity;
					mPostalCodeEntity = new PostalCodeEntity();
					mPostalCodeEntity.setPostalCode( cursor.getString( 0 ) );
					mPostalCodeEntity.setUserId( cursor.getString( 1 ) );
					CityList.add( mPostalCodeEntity.getPostalCode() );
				}
				while( cursor.moveToNext() );
			}
			cursor.close();
		}
		return CityList;
	}
	
	private boolean responseInent(
			weatherdataentity mDataEntity ,
			Intent intent )
	{
		boolean ret = false;
		try
		{
			if( mDataEntity != null )
			{
				intent.putExtra( "T0_tempc_now" , mDataEntity.getTempC().intValue() );
				intent.putExtra( "T0_tempc_high" , mDataEntity.getDetails().get( 0 ).getHight().intValue() );
				intent.putExtra( "T0_tempc_low" , mDataEntity.getDetails().get( 0 ).getLow().intValue() );
				intent.putExtra( "T0_condition" , convertCondition( mDataEntity.getCondition() ) );
				intent.putExtra( "T0_windCondition" , mDataEntity.getWindCondition() );
				intent.putExtra( "T0_humidity" , mDataEntity.getHumidity() );
				intent.putExtra( "T0_lunarcalendar" , mDataEntity.getLunarcalendar() );
				intent.putExtra( "T0_ultravioletray" , mDataEntity.getUltravioletray() );
				intent.putExtra( "T0_weathertime" , mDataEntity.getWeathertime() );
				intent.putExtra( "T0_condition_index" , convertCondition( mDataEntity.getCondition() ) );
				intent.putExtra( "T0_condition_index2" , WeatherCondition.convertCondition( mDataEntity.getCondition() ).toString() );
				intent.putExtra( "T0_lastupdatetime" , mDataEntity.getLastUpdateTime().longValue() );
				intent.putExtra( "T1_tempc_high" , mDataEntity.getDetails().get( 1 ).getHight().intValue() );
				intent.putExtra( "T1_tempc_low" , mDataEntity.getDetails().get( 1 ).getLow().intValue() );
				intent.putExtra( "T1_condition" , convertCondition( mDataEntity.getDetails().get( 1 ).getCondition() ) );
				intent.putExtra( "T1_condition_index" , convertCondition( mDataEntity.getDetails().get( 1 ).getCondition() ) );
				intent.putExtra( "T2_tempc_high" , mDataEntity.getDetails().get( 2 ).getHight().intValue() );
				intent.putExtra( "T2_tempc_low" , mDataEntity.getDetails().get( 2 ).getLow().intValue() );
				intent.putExtra( "T2_condition" , convertCondition( mDataEntity.getDetails().get( 2 ).getCondition() ) );
				intent.putExtra( "T2_condition_index" , convertCondition( mDataEntity.getDetails().get( 2 ).getCondition() ) );
				intent.putExtra( "T3_tempc_high" , mDataEntity.getDetails().get( 3 ).getHight().intValue() );
				intent.putExtra( "T3_tempc_low" , mDataEntity.getDetails().get( 3 ).getLow().intValue() );
				intent.putExtra( "T3_condition" , convertCondition( mDataEntity.getDetails().get( 3 ).getCondition() ) );
				intent.putExtra( "T3_condition_index" , convertCondition( mDataEntity.getDetails().get( 3 ).getCondition() ) );
				intent.putExtra( "result" , "OK" );
				ret = true;
			}
			else
			{
				intent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETDATA_DATABASE );
				ret = true;
			}
		}
		catch( Exception ex )
		{
			ret = false;
			Log.e( "weijie" , "responseInent = Exception" );
			ex.printStackTrace();
		}
		return ret;
	}
	
	private final String[] w = { "雷阵雨" , "晴" , "多云" , "无数据" , "雾霾" , "雪" , "阴" , "雨" };
	
	public String convertCondition(
			String s )
	{
		int response = 3;
		System.out.println( "shlt convertCondition , s:" + s );
		int index = s.indexOf( "转" );
		String new_s = s;
		if( index != -1 )
		{
			new_s = s.substring( 0 , index );
			System.out.println( "shlt convertCondition , new_s:" + new_s );
		}
		for( int i = 0 ; i < WeatherCondition.des.length ; i++ )
		{
			for( int j = 0 ; j < WeatherCondition.des[i].length ; j++ )
			{
				if( WeatherCondition.des[i][j].equalsIgnoreCase( new_s ) )
				{
					response = i;
					break;
				}
			}
		}
		switch( response )
		{
			case 0:
				response = 1;
				break;
			case 1:
				response = 2;
				break;
			case 2:
				response = 4;
				break;
			case 3:
				response = 6;
				break;
			case 4:
			case 5:
				response = 5;
				break;
			case 6:
			case 7:
				response = 0;
				break;
			case 8:
			case 9:
			case 10:
			case 11:
				response = 7;
				break;
			case 12:
				response = 4;
				break;
			case 13:
				response = 1;
				break;
			default:
				response = 3;
				break;
		}
		System.out.println( "shlt convertCondition , response:" + response );
		return w[response];
	}
	
	public weatherdataentity readData(
			Context context ,
			String mCurrentPostalCode )
	{
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		Uri uri;
		String selection;
		uri = Uri.parse( WEATHER_URI + "/" + mCurrentPostalCode );
		selection = weatherdataentity.POSTALCODE + "=" + "'" + mCurrentPostalCode + "' or " + weatherdataentity.CITY + " = '" + mCurrentPostalCode + "'";
		cursor = resolver.query( uri , weatherdataentity.projection , selection , null , null );
		weatherdataentity mDataEntity = new weatherdataentity();
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				mDataEntity.setUpdateMilis( cursor.getInt( 0 ) );
				mDataEntity.setCity( cursor.getString( 1 ) );
				mDataEntity.setPostalCode( cursor.getString( 2 ) );
				mDataEntity.setForecastDate( cursor.getLong( 3 ) );
				//				mDataEntity.setCondition( cursor.getString( 4 ) );
				String language = context.getResources().getConfiguration().locale.getCountry();
				mDataEntity.setCondition( WeatherCondition.convertCondition( cursor.getString( 4 ) , language ) );
				mDataEntity.setTempF( cursor.getInt( 5 ) );
				mDataEntity.setTempC( cursor.getInt( 6 ) );
				mDataEntity.setHumidity( cursor.getString( 7 ) );
				mDataEntity.setIcon( cursor.getString( 8 ) );
				mDataEntity.setWindCondition( WeatherCondition.convertCondition( cursor.getString( 4 ) , language ) );
				language = null;
				//				mDataEntity.setWindCondition( cursor.getString( 9 ) );
				mDataEntity.setLastUpdateTime( cursor.getLong( 10 ) );
				mDataEntity.setIsConfigured( cursor.getInt( 11 ) );
				mDataEntity.setLunarcalendar( cursor.getString( 12 ) );
				mDataEntity.setUltravioletray( cursor.getString( 13 ) );
				mDataEntity.setWeathertime( cursor.getString( 14 ) );
			}
			else
			{
				mDataEntity = null;
			}
			cursor.close();
		}
		int details_count = 0;
		if( mDataEntity != null )
		{
			uri = Uri.parse( WEATHER_URI + "/" + mCurrentPostalCode + "/detail" );
			selection = weatherdataentity.POSTALCODE + "=" + "'" + mCurrentPostalCode + "' or " + weatherdataentity.CITY + " = '" + mCurrentPostalCode + "'";
			cursor = resolver.query( uri , weatherforecastentity.forecastProjection , selection , null , null );
			if( cursor != null )
			{
				weatherforecastentity forecast;
				while( cursor.moveToNext() )
				{
					forecast = new weatherforecastentity();
					forecast.setDayOfWeek( cursor.getInt( 2 ) );
					forecast.setLow( cursor.getInt( 3 ) );
					forecast.setHight( cursor.getInt( 4 ) );
					forecast.setIcon( cursor.getString( 5 ) );
					String language = context.getResources().getConfiguration().locale.getCountry();
					forecast.setCondition( WeatherCondition.convertCondition( cursor.getString( 6 ) , language ) );
					language = null;
					mDataEntity.getDetails().add( forecast );
					details_count = details_count + 1;
				}
				cursor.close();
			}
		}
		if( details_count < 4 )
			mDataEntity = null;
		return mDataEntity;
	}
	
	public String getMainCity(
			Context context )
	{
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		String ret = "";
		Uri uri = Uri.parse( MainActivity.SETTING_URI );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				ret = cursor.getString( 3 );
			}
			cursor.close();
			cursor = null;
		}
		return ret;
	}
	
	public String setMainCity(
			Context context ,
			String city )
	{
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		String ret = "";
		Uri uri = Uri.parse( MainActivity.SETTING_URI );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				ContentValues values = new ContentValues();
				values.put( SettingEntity.UPDATE_WHEN_OPEN , cursor.getInt( 0 ) );
				values.put( SettingEntity.UPDATE_REGULARLY , cursor.getInt( 1 ) );
				values.put( SettingEntity.UPDATE_INTERVAL , cursor.getInt( 2 ) );
				values.put( SettingEntity.SOUND_ENABLE , cursor.getInt( 4 ) );
				values.put( SettingEntity.MAINCITY , city );
				int updateRows;
				updateRows = resolver.update( uri , values , null , null );
			}
			cursor.close();
			cursor = null;
		}
		return ret;
	}
}
