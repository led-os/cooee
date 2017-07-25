package com.cooee.widget.samweatherclock;


import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.SettingEntity;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.weatherwebservice;
import com.cooee.app.cooeeweather.filehelp.Log;


public class BootCompletedReceiver extends BroadcastReceiver
{
	
	public static final String GET_CURIENTE_CITY = "com.cooee.weather.data.action.GET_CURIENTE_CITY";
	public static final String BOOTUP_ACTION = "android.intent.action.BOOT_COMPLETED";
	private Context mContext;
	private ArrayList<String> mPoscalCodList;
	private String TAG = "BootCompletedReceiver";
	private SettingEntity mSettingEntity;
	private String mCurrentPostalCode;
	private int mCurrentIndex;
	private weatherdataentity mDataEntity;
	private static Boolean isBootUpComplete = false;
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		//从sharedPreferences中读取是否需要显示联网提醒，需要联网提醒时为true，即表示用户未对联网进行授权，开机刷新天气功能不启用
		if( context.getSharedPreferences( "weacherClient" , Activity.MODE_PRIVATE ).getBoolean( "notice" , true ) )
		{
			return;
		}
		this.mContext = context;
		mSettingEntity = new SettingEntity();
		mPoscalCodList = new ArrayList<String>();
		readSetting();
		mCurrentPostalCode = mSettingEntity.getMainCity();
		if( intent.getAction().endsWith( BOOTUP_ACTION ) )
		{
			isBootUpComplete = true;
			readPostalCodeList();
			if( mCurrentPostalCode == null || mCurrentPostalCode.equals( "" ) || mCurrentPostalCode.equals( "none" ) )
			{
				if( !AppConfig.getInstance( context ).isPosition() )
				{
					mCurrentPostalCode = AppConfig.getInstance( context ).getDefaultCity();
					Log.i( "weatherDataService" , "onReceive ---defaultCity = " + mCurrentPostalCode );
					saveSetting(); //将当前的城市保存到设置里
				}
			}
			requestData();
		}
		else if( isBootUpComplete && ( intent.getAction().endsWith( weatherwebservice.UPDATE_SUCCES_LAUNCHER ) ) )
		{
			isBootUpComplete = false;
			SendBroadcastToLauncher();
		}
	}
	
	public void readSetting()
	{
		// ��ȡ����
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = null;
		boolean found = false;
		Uri uri = Uri.parse( MainActivity.SETTING_URI );
		Log.v( TAG , "readSetting uri = " + uri );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				found = true;
			}
		}
		// ���û�ҵ����ã��趨Ĭ��ֵ
		if( !found )
		{
			// ��������Ĭ��ֵ
			ContentValues values = new ContentValues();
			// YANGTIANYU@2016/11/25 UPD START
			//mSettingEntity.setUpdateWhenOpen( 0 );
			mSettingEntity.setUpdateWhenOpen( AppConfig.getInstance( mContext ).isUpdateWhenOpen() );
			// YANGTIANYU@2016/11/25 UPD END
			mSettingEntity.setUpdateRegularly( 1 );
			mSettingEntity.setUpdateInterval( 1 );
			mSettingEntity.setSoundEnable( 0 );
			mSettingEntity.setMainCity( "" );
			values.put( SettingEntity.UPDATE_WHEN_OPEN , mSettingEntity.getUpdateWhenOpen() );
			values.put( SettingEntity.UPDATE_REGULARLY , mSettingEntity.getUpdateRegularly() );
			values.put( SettingEntity.UPDATE_INTERVAL , mSettingEntity.getUpdateInterval() );
			values.put( SettingEntity.SOUND_ENABLE , mSettingEntity.getSoundEnable() );
			values.put( SettingEntity.MAINCITY , mSettingEntity.getMainCity() );
			resolver.insert( uri , values );
		}
		else
		{
			mSettingEntity.setUpdateWhenOpen( cursor.getInt( 0 ) );
			mSettingEntity.setUpdateRegularly( cursor.getInt( 1 ) );
			mSettingEntity.setUpdateInterval( cursor.getInt( 2 ) );
			mSettingEntity.setMainCity( cursor.getString( 3 ) );
			mSettingEntity.setSoundEnable( cursor.getInt( 4 ) );
		}
		if( cursor != null )
		{
			cursor.close();
		}
	}
	
	public void saveSetting()
	{
		// ��������
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = null;
		boolean found = false;
		Uri uri = Uri.parse( MainActivity.SETTING_URI );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				found = true;
			}
		}
		Log.v( TAG , "found = " + found );
		if( found )
		{
			ContentValues values = new ContentValues();
			values.put( SettingEntity.UPDATE_WHEN_OPEN , mSettingEntity.getUpdateWhenOpen() );
			values.put( SettingEntity.UPDATE_REGULARLY , mSettingEntity.getUpdateRegularly() );
			values.put( SettingEntity.UPDATE_INTERVAL , mSettingEntity.getUpdateInterval() );
			values.put( SettingEntity.SOUND_ENABLE , mSettingEntity.getSoundEnable() );
			values.put( SettingEntity.MAINCITY , mCurrentPostalCode );
			int updateRows;
			updateRows = resolver.update( uri , values , null , null );
			Log.v( TAG , "update setting rows = " + updateRows );
		}
		else
		{
			if( cursor != null )
			{
				cursor.close();
			}
			throw new UnsupportedOperationException();
		}
		if( cursor != null )
		{
			cursor.close();
		}
	}
	
	public void readPostalCodeList()
	{
		boolean isContainsDefaultCity = false;
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = null;
		Uri uri = Uri.parse( MainActivity.POSTALCODE_URI );
		// �����mPoscalCodList
		mPoscalCodList.clear();
		String selection;
		selection = PostalCodeEntity.USER_ID + "=" + "'0'";
		cursor = resolver.query( uri , PostalCodeEntity.projection , selection , null , null );
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
					mPoscalCodList.add( mPostalCodeEntity.getPostalCode() );
					Log.v( TAG , "mPostalCodeEntity.getPostalCode() = " + mPostalCodeEntity.getPostalCode() + ",userID=" + mPostalCodeEntity.getUserId() );
				}
				while( cursor.moveToNext() );
			}
			cursor.close();
		}
		if( !AppConfig.getInstance( mContext ).isPosition() )
		{
			String defaultCity = AppConfig.getInstance( mContext ).getDefaultCity();
			Log.i( "weatherDataService" , "readPostalCodeList ---defaultCity = " + defaultCity );
			for( int i = 0 ; i < mPoscalCodList.size() ; i++ )
			{
				String a = mPoscalCodList.get( i );
				if( a.equals( defaultCity ) )
				{
					isContainsDefaultCity = true;
					break;
				}
			}
			if( !isContainsDefaultCity )
			{
				ContentValues values = new ContentValues();
				values.put( PostalCodeEntity.POSTAL_CODE , AppConfig.getInstance( mContext ).getDefaultCity() );
				values.put( PostalCodeEntity.USER_ID , 0 );
				resolver.insert( uri , values );
			}
		}
	}
	
	public void requestData()
	{
		Intent intent = new Intent( mContext , com.cooee.app.cooeeweather.dataprovider.weatherDataService.class );
		intent.setAction( "com.cooee.app.cooeeweather.dataprovider.weatherDataService" );
		intent.putExtra( "postalCode" , mCurrentPostalCode );
		intent.putExtra( "forcedUpdate" , 1 ); // ǿ�Ƹ���
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		mContext.startService( intent );
	}
	
	public void readData()
	{
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = null;
		Uri uri;
		String selection;
		uri = Uri.parse( MainActivity.WEATHER_URI + "/" + mCurrentPostalCode );
		selection = weatherdataentity.POSTALCODE + "=" + "'" + mCurrentPostalCode + "'";
		cursor = resolver.query( uri , weatherdataentity.projection , selection , null , null );
		Log.d( TAG , "selection = " + selection );
		if( cursor != null )
		{
			mDataEntity = new weatherdataentity();
			Log.d( TAG , "cursor moveToFirst" );
			if( cursor.moveToFirst() )
			{
				Log.d( TAG , "cursor moveToFirst begion" );
				mDataEntity.setUpdateMilis( cursor.getInt( 0 ) );
				mDataEntity.setCity( cursor.getString( 1 ) );
				mDataEntity.setPostalCode( cursor.getString( 2 ) );
				mDataEntity.setForecastDate( cursor.getLong( 3 ) );
				mDataEntity.setCondition( cursor.getString( 4 ) );
				mDataEntity.setTempF( cursor.getInt( 5 ) );
				mDataEntity.setTempC( cursor.getInt( 6 ) );
				mDataEntity.setHumidity( cursor.getString( 7 ) );
				mDataEntity.setIcon( cursor.getString( 8 ) );
				mDataEntity.setWindCondition( cursor.getString( 9 ) );
				mDataEntity.setLastUpdateTime( cursor.getLong( 10 ) );
				mDataEntity.setIsConfigured( cursor.getInt( 11 ) );
				mDataEntity.setLunarcalendar( cursor.getString( 12 ) );
				mDataEntity.setUltravioletray( cursor.getString( 13 ) );
				mDataEntity.setWeathertime( cursor.getString( 14 ) );
			}
			int count = 0;
			while( cursor.moveToNext() )
			{
				Log.v( TAG , "updateMilis[" + count + "] = " + cursor.getInt( 0 ) );
				Log.v( TAG , "city[" + count + "] = " + cursor.getString( 1 ) );
				Log.v( TAG , "postcalCode[" + count + "] = " + cursor.getString( 2 ) );
				count++;
			}
			cursor.close();
		}
		int details_count = 0;
		if( mDataEntity != null )
		{
			uri = Uri.parse( MainActivity.WEATHER_URI + "/" + mCurrentPostalCode + "/detail" );
			selection = weatherforecastentity.CITY + "=" + "'" + mCurrentPostalCode + "'";
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
					forecast.setCondition( cursor.getString( 6 ) );
					// forecast.setWidgetId(cursor.getInt(6));
					mDataEntity.getDetails().add( forecast );
					details_count = details_count + 1;
				}
				cursor.close();
			}
		}
	}
	
	public void SendBroadcastToLauncher()
	{
		try
		{
			Intent intent = new Intent();
			intent.setAction( MainActivity.CLOSED_UPDATE_LAUNCHER );
			intent.putExtra( "postalCode" , mCurrentPostalCode );
			intent.putExtra( "postalListId" , mCurrentIndex );
			Log.d( TAG , "SendBroadcastToLauncherByClosed readData" );
			readData();
			Log.d( TAG , "SendBroadcastToLauncherByClosed readData end" );
			if( mDataEntity != null )
			{
				intent.putExtra( "T0_tempc_now" , mDataEntity.getTempC() );
				//	Toast.makeText(mContext, "T0_tempc_now="+mDataEntity.getTempC(), Toast.LENGTH_LONG).show();	
				intent.putExtra( "T0_tempc_high" , mDataEntity.getDetails().get( 0 ).getHight() );
				intent.putExtra( "T0_tempc_low" , mDataEntity.getDetails().get( 0 ).getLow() );
				intent.putExtra( "T0_condition" , mDataEntity.getCondition() );
				intent.putExtra( "T0_lastupdatetime" , mDataEntity.getLastUpdateTime() );
				intent.putExtra( "T0_windCondition" , mDataEntity.getWindCondition() );
				intent.putExtra( "T0_humidity" , mDataEntity.getHumidity() );
				intent.putExtra( "T0_lunarcalendar" , mDataEntity.getLunarcalendar() );
				intent.putExtra( "T0_ultravioletray" , mDataEntity.getUltravioletray() );
				intent.putExtra( "T0_weathertime" , mDataEntity.getWeathertime() );
				intent.putExtra( "T1_tempc_high" , mDataEntity.getDetails().get( 1 ).getHight() );
				intent.putExtra( "T1_tempc_low" , mDataEntity.getDetails().get( 1 ).getLow() );
				intent.putExtra( "T1_condition" , mDataEntity.getDetails().get( 1 ).getCondition() );
				intent.putExtra( "T2_tempc_high" , mDataEntity.getDetails().get( 2 ).getHight() );
				intent.putExtra( "T2_tempc_low" , mDataEntity.getDetails().get( 2 ).getLow() );
				intent.putExtra( "T2_condition" , mDataEntity.getDetails().get( 2 ).getCondition() );
				intent.putExtra( "T3_tempc_high" , mDataEntity.getDetails().get( 3 ).getHight() );
				intent.putExtra( "T3_tempc_low" , mDataEntity.getDetails().get( 3 ).getLow() );
				intent.putExtra( "T3_condition" , mDataEntity.getDetails().get( 3 ).getCondition() );
				intent.putExtra( "result" , "OK" );
				Log.e( "T0_condition" , "getDetails condition = " + mDataEntity.getDetails().get( 0 ).getCondition() );
				Log.e( "T0_condition" , "condition = " + mDataEntity.getCondition() );
			}
			else
			{
				Log.d( TAG , "SendBroadcastToLauncherByClosed 0" );
				intent.putExtra( "errorcode" , MainActivity.ERROR_BAD_GETDATA_DATABASE );
				intent.putExtra( "result" , "ERROR" );
			}
			Log.d( TAG , "SendBroadcastToLauncherByClosed 1" );
			mContext.sendBroadcast( intent );
			Log.d( TAG , "SendBroadcastToLauncherByClosed 2" );
		}
		catch( Exception ex )
		{
			Log.d( TAG , "SendBroadcastToLauncherByClosed exception = " + ex.getMessage() );
		}
	}
}
