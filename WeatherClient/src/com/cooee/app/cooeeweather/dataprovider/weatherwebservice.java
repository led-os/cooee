package com.cooee.app.cooeeweather.dataprovider;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import com.cooee.app.cooeeweather.dataentity.SettingEntity;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.MsnWeatherModel.MsnCurrentData;
import com.cooee.app.cooeeweather.dataprovider.MsnWeatherModel.MsnForwcastsData;
import com.cooee.app.cooeeweather.dataprovider.MsnWeatherModel.MsnXMLData;
import com.cooee.app.cooeeweather.dataprovider.MsnWeatherModel.MsnXMLDataField;
import com.cooee.app.cooeeweather.filehelp.Log;
import com.cooee.app.cooeeweather.lunarCalendar.DateFormatter;
import com.cooee.app.cooeeweather.lunarCalendar.LunarCalendar;
import com.cooee.widget.samweatherclock.AppConfig;
import com.cooee.widget.samweatherclock.MainActivity;


public class weatherwebservice
{
	
	private static final String TAG = "weatherwebservice";
	public static FLAG_UPDATE Update_Result_Flag = FLAG_UPDATE.INVILIDE_VALUE;
	// weijie_20130422
	public final static String UPDATE_SUCCES_LAUNCHER = "com.cooee.weather.Weather.action.UPDATE_SUCCES_LAUNCHER";
	public final static String UPDATE_SUCCES_SIMPLELAUNCHER2 = "com.cooee.weather.Weather.action.UPDATE_SUCCES_SIMPLELAUNCHER2";
	
	public static enum WeatherDataSource
	{
		GOOGLE , SINA , WEATHER_CN , COOEE , MSN
	};
	
	// 数据源来自COOEE
	public static WeatherDataSource dataSourceFlag = WeatherDataSource.MSN;// 此处含义发生变，表示先使用哪种数据源
	
	public static enum FLAG_UPDATE
	{
		UPDATE_SUCCES , WEBSERVICE_ERROR , DATAPROVIDER_ERROR , AVAILABLE_DATA , INVILIDE_VALUE ,
		// add weijie 0530
		UPDATE_SREACH_SUCCES ,
		UPDATE_SREACH_FAILED ,
		UPDATE_REF_SUCCES ,
		UPDATE_REF_FAILED
		// add end
	}
	
	// shlt , start
	private static final String SREACH_URI = "content://com.cooee.app.cooeeweather.dataprovider/sreach";
	private static Context mContext = null;
	
	//	private static boolean isLocation = false;
	public static void updateWeatherData(
			Context context ,
			Uri uri ,
			String PostMark ,
			int UseWC ,
			String wc ,
			boolean foreignCity )
	{
		Log.i( "TAG" , "updateWeatherData!!!" );
		mContext = context;
		Uri forecastUri = Uri.withAppendedPath( uri , "detail" );
		String postalCode = uri.getPathSegments().get( 1 );
		String postalName = uri.getPathSegments().get( 1 );
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		String selection = null;
		Log.v( TAG , "updateWeatherData context = " + context + "widgetUri = " + uri );
		weatherdataentity DataEntity = null;
		Log.v( TAG , "dataSourceFlag = " + dataSourceFlag );
		try
		{
			Properties pro = new Properties();
			pro.load( context.getAssets().open( "config.properties" ) );
			String flag = pro.getProperty( "dataSourceFlag" );
			if( WeatherDataSource.COOEE.toString().equals( flag ) )
			{
				dataSourceFlag = WeatherDataSource.COOEE;
			}
			else if( WeatherDataSource.MSN.toString().equals( flag ) )
			{
				dataSourceFlag = WeatherDataSource.MSN;
			}
			System.out.println( "shlt , load!!! dataSourceFlag : " + dataSourceFlag.toString() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		if( WeatherDataSource.COOEE == dataSourceFlag )
		{
			System.out.println( "shlt , first COOEE readdata" );
			if( AppConfig.getInstance( mContext ).isMerge() && foreignCity )
			{
				//Log.i( "TAG" , "AppConfig.getInstance( mContext ).isMerge()&&foreignCity !!!" );
				DataEntity = foreignCooeeServiceQuery.CooeeWeatherDataUpdate( mContext , postalCode );
			}
			else
			{
				Log.i( "TAG" , "inlandcity !!!" );
				DataEntity = cooeeServiceQuery.CooeeWeatherDataUpdate( postalCode );
			}
			if( DataEntity == null || Update_Result_Flag != FLAG_UPDATE.UPDATE_SUCCES )
			{
				System.out.println( "shlt , second MSN readdata" );
				MsnWeatherDataUpdate( uri , PostMark , UseWC , wc , forecastUri , postalCode , postalName , resolver );
			}
		}
		else if( WeatherDataSource.MSN == dataSourceFlag )
		{
			System.out.println( "shlt , first MSN readdata" );
			boolean b = MsnWeatherDataUpdate( uri , PostMark , UseWC , wc , forecastUri , postalCode , postalName , resolver );
			if( !b || Update_Result_Flag != FLAG_UPDATE.UPDATE_SREACH_SUCCES )
			{
				System.out.println( "shlt , second COOEE readdata" );
				DataEntity = cooeeServiceQuery.CooeeWeatherDataUpdate( postalCode );
			}
		}
		if( Update_Result_Flag == FLAG_UPDATE.UPDATE_SUCCES )
		{ // ���³ɹ����޸����
			Uri weather_uri = Uri.parse( MainActivity.WEATHER_URI + "/" + postalCode );
			Uri detail_uri = Uri.parse( MainActivity.WEATHER_URI + "/" + postalCode + "/detail" );
			/*Log.v( TAG , "delete details, detail_uri = " + detail_uri );
			Log.v( TAG , "delete details, weather_uri = " + weather_uri );*/
			resolver.delete( weather_uri , "city = '" + postalCode + "' or postalCode = '" + postalCode + "'" , null );
			resolver.delete( detail_uri , "city = '" + postalCode + "' or postalCode = '" + postalCode + "'" , null );
			resolver.delete( weather_uri , "city = '" + postalName + "' or postalCode = '" + postalName + "'" , null );
			resolver.delete( detail_uri , "city = '" + postalName + "' or postalCode = '" + postalName + "'" , null );
			ContentValues values = new ContentValues();
			values.clear();
			values.put( weatherdataentity.UPDATE_MILIS , DataEntity.getUpdateMilis() );
			values.put( weatherdataentity.CITY , DataEntity.getCity() );
			values.put( weatherdataentity.POSTALCODE , DataEntity.getPostalCode() );
			values.put( weatherdataentity.FORECASTDATE , DataEntity.getForecastDate() );
			values.put( weatherdataentity.CONDITION , DataEntity.getCondition() );
			values.put( weatherdataentity.HUMIDITY , DataEntity.getHumidity() );
			values.put( weatherdataentity.TEMPF , DataEntity.getTempF() );
			values.put( weatherdataentity.TEMPC , DataEntity.getTempC() );
			values.put( weatherdataentity.ICON , DataEntity.getIcon() );
			values.put( weatherdataentity.WINDCONDITION , DataEntity.getWindCondition() );
			values.put( weatherdataentity.LAST_UPDATE_TIME , DataEntity.getLastUpdateTime() );//谁之前改成系统时间啦？
			values.put( weatherdataentity.TEMPH , DataEntity.getDetails().get( 0 ).getHight() );
			values.put( weatherdataentity.TEMPL , DataEntity.getDetails().get( 0 ).getLow() );
			values.put( weatherdataentity.LUNARCALENDAR , DataEntity.getLunarcalendar() );
			values.put( weatherdataentity.ULTRAVIOLETRAY , DataEntity.getUltravioletray() );
			values.put( weatherdataentity.WEATHERTIME , DataEntity.getWeathertime() );
			selection = weatherdataentity.POSTALCODE + "=" + "'" + postalCode + "'";
			Log.v( TAG , "delete details, selection = " + selection );
			cursor = resolver.query( uri , null , selection , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					resolver.delete( uri , selection , null );
					Log.v( TAG , "delete details, uri = " + uri );
				}
				cursor.close();
			}
			// 保存数据
			resolver.insert( uri , values );
			selection = weatherforecastentity.CITY + "=" + "'" + postalCode + "'";
			Log.v( TAG , "delete details, uri = " + forecastUri + ", selection " + selection );
			cursor = resolver.query( forecastUri , null , selection , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					resolver.delete( forecastUri , selection , null );
				}
				cursor.close();
			}
			for( weatherforecastentity forecast : DataEntity.getDetails() )
			{
				values.clear();
				values.put( weatherforecastentity.CITY , postalCode );
				values.put( weatherforecastentity.DAYOFWEEK , forecast.getDayOfWeek() );
				values.put( weatherforecastentity.HIGHT , forecast.getHight() );
				values.put( weatherforecastentity.LOW , forecast.getLow() );
				values.put( weatherforecastentity.ICON , forecast.getIcon() );
				values.put( weatherforecastentity.CONDITION , forecast.getCondition() );
				resolver.insert( forecastUri , values );
			}
			try
			{
				// weijie_20130422
				Intent intent = new Intent();
				intent.setAction( UPDATE_SUCCES_LAUNCHER );
				intent.putExtra( "postalCode" , DataEntity.getPostalCode() );
				intent.putExtra( "T0_tempc_now" , DataEntity.getTempC() );
				intent.putExtra( "T0_tempc_high" , DataEntity.getDetails().get( 0 ).getHight() );
				intent.putExtra( "T0_tempc_low" , DataEntity.getDetails().get( 0 ).getLow() );
				intent.putExtra( "T0_condition" , DataEntity.getCondition() );
				intent.putExtra( "T0_windCondition" , DataEntity.getWindCondition() );
				intent.putExtra( "T0_humidity" , DataEntity.getHumidity() );
				intent.putExtra( "T0_lunarcalendar" , DataEntity.getLunarcalendar() );
				intent.putExtra( "T0_ultravioletray" , DataEntity.getUltravioletray() );
				intent.putExtra( "T0_weathertime" , DataEntity.getWeathertime() );
				intent.putExtra( "T1_tempc_high" , DataEntity.getDetails().get( 1 ).getHight() );
				intent.putExtra( "T1_tempc_low" , DataEntity.getDetails().get( 1 ).getLow() );
				intent.putExtra( "T1_condition" , DataEntity.getDetails().get( 1 ).getCondition() );
				intent.putExtra( "T2_tempc_high" , DataEntity.getDetails().get( 2 ).getHight() );
				intent.putExtra( "T2_tempc_low" , DataEntity.getDetails().get( 2 ).getLow() );
				intent.putExtra( "T2_condition" , DataEntity.getDetails().get( 2 ).getCondition() );
				intent.putExtra( "T3_tempc_high" , DataEntity.getDetails().get( 3 ).getHight() );
				intent.putExtra( "T3_tempc_low" , DataEntity.getDetails().get( 3 ).getLow() );
				intent.putExtra( "T3_condition" , DataEntity.getDetails().get( 3 ).getCondition() );
				intent.putExtra( "result" , "OK" );
				//	Log.d( TAG , "T0_condition =  " + DataEntity.getCondition() );
				Log.d( TAG , "updateWeatherData sendBroadcast UPDATE_SUCCES_LAUNCHER " );
				context.sendBroadcast( intent );
				String mainCity = getMainCity( context );
				if( mainCity != null && mainCity.equals( DataEntity.getPostalCode() ) )
				{
					intent.setAction( UPDATE_SUCCES_SIMPLELAUNCHER2 );
					context.sendBroadcast( intent );
					Log.d( TAG , "updateWeatherData sendBroadcast UPDATE_SUCCES_SIMPLELAUNCHER2 " );
				}
			}
			catch( Exception ex )
			{
				Log.d( TAG , "updateWeatherData sendBroadcast exception" );
			}
			System.out.println( "shlt , notifyChange api" );
			context.getContentResolver().notifyChange( Uri.parse( weatherdataprovider.DB_LISTENER_URI ) , null );
		}
	}
	
	private static String getMainCity(
			Context context )
	{
		// ��ȡ����
		String mainCity = null;
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		Uri uri = Uri.parse( weatherDataService.SETTING_URI );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				mainCity = cursor.getString( 3 );
			}
		}
		if( cursor != null )
		{
			cursor.close();
		}
		return mainCity;
	}
	
	private static boolean MsnWeatherDataUpdate(
			Uri uri ,
			String PostMark ,
			int UseWC ,
			String wc ,
			Uri forecastUri ,
			String postalCode ,
			String postalName ,
			ContentResolver resolver )
	{
		Cursor cursor;
		String selection;
		MsnWeatherModel Msn = new MsnWeatherModel( "C" );
		if( UseWC == 0 )
		{
			int result = Msn.MsnWeatherSreachDataByKey( postalCode );
			if( result == 0 && Update_Result_Flag == FLAG_UPDATE.UPDATE_SREACH_SUCCES )
			{
				Uri PostMark_uri = Uri.parse( SREACH_URI );
				selection = MsnXMLDataField.TIMEPOSTMARK + " = '" + PostMark + "'";
				cursor = resolver.query( PostMark_uri , MsnWeatherModel.projection , selection , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						resolver.delete( PostMark_uri , selection , null );
					}
					// liuhailin-20130716-<close the cursor of database>
					cursor.close();
					// liuhailin-20130716
				}
				for( int i = 0 ; i < Msn.mMsnXMLData_list.size() ; i++ )
				{
					final MsnXMLData mdata = Msn.mMsnXMLData_list.get( i );
					ContentValues values = new ContentValues();
					values.clear();
					values.put( MsnXMLDataField.TIMEPOSTMARK , PostMark );
					values.put( MsnXMLDataField.ENTITY_ID , mdata.getEntityID() );
					values.put( MsnXMLDataField.LONG , mdata.getLong() );
					values.put( MsnXMLDataField.LAT , mdata.getLat() );
					values.put( MsnXMLDataField.LOCATION_NAME , mdata.getLocationName() );
					postalCode = mdata.getLocationName();
					values.put( MsnXMLDataField.LOCATION_CODE , mdata.getLocationCode() );
					values.put( MsnXMLDataField.DEGREE_TYPE , mdata.getDegreetype() );
					values.put( MsnXMLDataField.TIMEZONE , mdata.getTimeZone() );
					values.put( MsnXMLDataField.ZIPCODE , mdata.getZipcode() );
					values.put( MsnXMLDataField.ALERT , mdata.getAlert() );
					final List<MsnCurrentData> CurrentData = mdata.MsnCurrentData_list;
					values.put( MsnXMLDataField.WINDSPEED , CurrentData.get( 0 ).getCurrentWindSpeed() );
					values.put( MsnXMLDataField.WINDDISPLAY , CurrentData.get( 0 ).getCurrentWindDisplay() );
					values.put( MsnXMLDataField.SHORTDAY , CurrentData.get( 0 ).getCurrentShortDay() );
					values.put( MsnXMLDataField.DAY , CurrentData.get( 0 ).getCurrentDay() );
					values.put( MsnXMLDataField.HUNIDITY , CurrentData.get( 0 ).getCurrentHumidity() );
					values.put( MsnXMLDataField.FEELSLIKE , CurrentData.get( 0 ).getCurrentFeelSlike() );
					values.put( MsnXMLDataField.OBSERVATION_POINT , CurrentData.get( 0 ).getCurrentObservationPoint() );
					values.put( MsnXMLDataField.OBSERVATION_TIME , CurrentData.get( 0 ).getCurrentObservationTime() );
					values.put( MsnXMLDataField.DATE , CurrentData.get( 0 ).getCurrentDate() );
					values.put( MsnXMLDataField.SKY_TEXT , CurrentData.get( 0 ).getCurrentSkyText() );
					values.put( MsnXMLDataField.TEMPERATURE_CURRENT , CurrentData.get( 0 ).getCurrentTemperature() );
					// values.put(MsnXMLDataField.USING, "using");
					final List<MsnForwcastsData> ForwcastsData = mdata.MsnForwcastsData_list;
					int iikk = mdata.MsnForwcastsData_list.size();
					for( int j = 0 ; j < mdata.MsnForwcastsData_list.size() ; j++ )
					{
						final MsnForwcastsData forwcast = ForwcastsData.get( j );
						values.put( MsnXMLDataField.PRECIP + String.valueOf( j ) , forwcast.getForwcastPrecip() );
						values.put( MsnXMLDataField.SKY_TEXT_DAY + String.valueOf( j ) , forwcast.getForwcastSkytextday() );
						values.put( MsnXMLDataField.TEMPERATURE_HIGH + String.valueOf( j ) , forwcast.getForwcastTemperatureHigh() );
						values.put( MsnXMLDataField.TEMPERATURE_LOW + String.valueOf( j ) , forwcast.getForwcastTemperatureLow() );
					}
					resolver.insert( PostMark_uri , values );
				}
			}
		}
		else
		{
			selection = weatherdataentity.POSTALCODE + "=" + "'" + postalCode + "'";
			cursor = resolver.query( uri , null , selection , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					String name = cursor.getString( 0 );
				}
				// liuhailin-20130716-<close the cursor of database>
				cursor.close();
				// liuhailin-20130716
			}
			int result = Msn.MsnWeatherGetDataByWoid( wc );
			for( int i = 0 ; i < Msn.mMsnXMLData_list.size() ; i++ )
			{
				final MsnXMLData mdata = Msn.mMsnXMLData_list.get( i );
				final List<MsnCurrentData> CurrentData = mdata.MsnCurrentData_list;
				ContentValues values = new ContentValues();
				values.clear();
				// 5天
				selection = weatherforecastentity.CITY + "=" + "'" + postalCode + "'";
				Log.v( TAG , "delete details, uri = " + forecastUri + ", selection " + selection );
				cursor = resolver.query( forecastUri , null , selection , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						resolver.delete( forecastUri , selection , null );
					}
					// liuhailin-20130716-<close the cursor of database>
					cursor.close();
					// liuhailin-20130716
				}
				// cursor.close();
				for( int j = 0 ; j <= 4 ; j++ )
				{
					values.clear();
					MsnForwcastsData data = mdata.MsnForwcastsData_list.get( j );
					values.put( weatherforecastentity.CITY , postalCode );
					values.put( weatherforecastentity.DAYOFWEEK , Msn.fomatDayofweek( data.getForwcastShortDay() ) );
					values.put( weatherforecastentity.HIGHT , data.getForwcastTemperatureHigh() );
					values.put( weatherforecastentity.LOW , data.getForwcastTemperatureLow() );
					values.put( weatherforecastentity.ICON , "" );
					values.put( weatherforecastentity.CONDITION , data.getForwcastSkytextday() );
					resolver.insert( forecastUri , values );
				}
				//
				selection = weatherdataentity.POSTALCODE + "=" + "'" + postalCode + "'";
				cursor = resolver.query( uri , null , selection , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						resolver.delete( uri , selection , null );
					}
					// liuhailin-20130716-<close the cursor of database>
					cursor.close();
					// liuhailin-20130716
				}
				// cursor.close();
				values.clear();
				values.put( weatherdataentity.TIMEPOSTMARK , PostMark );
				values.put( weatherdataentity.ENTITYID , mdata.getEntityID() );
				values.put( weatherdataentity.LOCATIONCODE , mdata.getLocationCode() );
				values.put( weatherdataentity.DEGREETYPE , mdata.getDegreetype() );
				values.put( weatherdataentity.CITY , mdata.getLocationName() );
				values.put( weatherdataentity.POSTALCODE , mdata.getLocationName() );
				values.put( weatherdataentity.FORECASTDATE , "" );
				values.put( weatherdataentity.CONDITION , CurrentData.get( 0 ).getCurrentSkyText() );
				values.put( weatherdataentity.HUMIDITY , CurrentData.get( 0 ).getCurrentHumidity() );
				values.put( weatherdataentity.TEMPF , CurrentData.get( 0 ).getCurrentTemperature() );
				values.put( weatherdataentity.TEMPC , CurrentData.get( 0 ).getCurrentTemperature() );
				values.put( weatherdataentity.WINDCONDITION , CurrentData.get( 0 ).getCurrentWindDisplay() );
				values.put( weatherdataentity.LAST_UPDATE_TIME , System.currentTimeMillis() );
				values.put( weatherdataentity.TEMPH , mdata.MsnForwcastsData_list.get( 0 ).getForwcastTemperatureHigh() );
				values.put( weatherdataentity.TEMPL , mdata.MsnForwcastsData_list.get( 0 ).getForwcastTemperatureLow() );
				// values.put(MsnXMLDataField.USING, "using");
				resolver.insert( uri , values );
			}
		}
		// 数据压入天气数据库中
		ContentValues values = new ContentValues();
		Uri sreach_uri = Uri.parse( SREACH_URI );
		String mem = MsnXMLDataField.LOCATION_NAME + " = '" + postalCode + "'";
		cursor = resolver.query( sreach_uri , MsnWeatherModel.sreach_projection , mem , null , null );
		if( cursor == null || ( cursor.getCount() != 1 ) )
		{
			cursor.close();
			return false;
		}
		Uri weather_uri = Uri.parse( MainActivity.WEATHER_URI + "/" + postalCode );
		Uri detail_uri = Uri.parse( MainActivity.WEATHER_URI + "/" + postalCode + "/detail" );
		resolver.delete( weather_uri , "city = '" + postalCode + "' or postalCode = '" + postalCode + "'" , null );
		resolver.delete( detail_uri , "city = '" + postalCode + "' or postalCode = '" + postalCode + "'" , null );
		resolver.delete( weather_uri , "city = '" + postalName + "' or postalCode = '" + postalName + "'" , null );
		resolver.delete( detail_uri , "city = '" + postalName + "' or postalCode = '" + postalName + "'" , null );
		cursor.moveToFirst();
		values.clear();
		Log.v( TAG , "now currentTimeMillis = " + System.currentTimeMillis() );
		values.put( weatherdataentity.UPDATE_MILIS , System.currentTimeMillis() );
		//	values.put( weatherdataentity.LAST_UPDATE_TIME , System.currentTimeMillis() );
		values.put( weatherdataentity.CITY , postalName );
		values.put( weatherdataentity.POSTALCODE , cursor.getString( 3 ) );
		values.put( weatherdataentity.FORECASTDATE , "" );
		values.put( weatherdataentity.CONDITION , cursor.getString( 10 ) );
		values.put( weatherdataentity.TEMPF , cursor.getString( 11 ) );
		values.put( weatherdataentity.TEMPC , cursor.getString( 11 ) );
		values.put( weatherdataentity.HUMIDITY , cursor.getString( 8 ) );
		values.put( weatherdataentity.ICON , "" );
		values.put( weatherdataentity.WINDCONDITION , formWindCondition( cursor.getString( 7 ) ) );
		String str = cursor.getString( 28 ) + " " + cursor.getString( 9 );
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );
		long millionSeconds = 0;
		try
		{
			millionSeconds = sdf.parse( str ).getTime();
		}
		catch( ParseException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//毫秒
		values.put( weatherdataentity.LAST_UPDATE_TIME , millionSeconds );
		values.put( weatherdataentity.IS_CONFIGURED , "" );
		values.put( weatherdataentity.TEMPH , cursor.getString( 13 ) );
		values.put( weatherdataentity.TEMPL , cursor.getString( 14 ) );
		values.put( weatherdataentity.ENTITYID , cursor.getString( 0 ) );
		values.put( weatherdataentity.LOCATIONCODE , cursor.getString( 4 ) );
		values.put( weatherdataentity.DEGREETYPE , cursor.getString( 5 ) );
		values.put( weatherdataentity.LUNARCALENDAR , getLunarDate( millionSeconds ) );
		resolver.insert( weather_uri , values );
		resolver.delete( sreach_uri , MsnXMLDataField.TIMEPOSTMARK + " = '" + cursor.getString( 6 ) + "'" , null );
		// 0
		values.clear();
		values.put( weatherforecastentity.CITY , postalCode );
		values.put( weatherforecastentity.POSTALCODE , postalName );
		values.put( weatherforecastentity.DAYOFWEEK , MsnWeatherModel.fomatDayofweek( cursor.getString( 27 ) ) );
		values.put( weatherforecastentity.HIGHT , cursor.getString( 13 ) );
		values.put( weatherforecastentity.LOW , cursor.getString( 14 ) );
		values.put( weatherforecastentity.ICON , "" );
		values.put( weatherforecastentity.CONDITION , cursor.getString( 12 ) );
		resolver.insert( detail_uri , values );
		// 1
		values.clear();
		values.put( weatherforecastentity.CITY , postalCode );
		values.put( weatherforecastentity.POSTALCODE , postalName );
		values.put( weatherforecastentity.DAYOFWEEK , 1 + MsnWeatherModel.fomatDayofweek( cursor.getString( 27 ) ) );
		values.put( weatherforecastentity.HIGHT , cursor.getString( 16 ) );
		values.put( weatherforecastentity.LOW , cursor.getString( 17 ) );
		values.put( weatherforecastentity.ICON , "" );
		values.put( weatherforecastentity.CONDITION , cursor.getString( 15 ) );
		resolver.insert( detail_uri , values );
		// 2
		values.clear();
		values.put( weatherforecastentity.CITY , postalCode );
		values.put( weatherforecastentity.POSTALCODE , postalName );
		values.put( weatherforecastentity.DAYOFWEEK , 2 + MsnWeatherModel.fomatDayofweek( cursor.getString( 27 ) ) );
		values.put( weatherforecastentity.HIGHT , cursor.getString( 20 ) );
		values.put( weatherforecastentity.LOW , cursor.getString( 19 ) );
		values.put( weatherforecastentity.ICON , "" );
		values.put( weatherforecastentity.CONDITION , cursor.getString( 18 ) );
		resolver.insert( detail_uri , values );
		// 3
		values.clear();
		values.put( weatherforecastentity.CITY , postalCode );
		values.put( weatherforecastentity.POSTALCODE , postalName );
		values.put( weatherforecastentity.DAYOFWEEK , 3 + MsnWeatherModel.fomatDayofweek( cursor.getString( 27 ) ) );
		values.put( weatherforecastentity.HIGHT , cursor.getString( 22 ) );
		values.put( weatherforecastentity.LOW , cursor.getString( 23 ) );
		values.put( weatherforecastentity.ICON , "" );
		values.put( weatherforecastentity.CONDITION , cursor.getString( 21 ) );
		resolver.insert( detail_uri , values );
		// 4
		values.clear();
		values.put( weatherforecastentity.CITY , postalCode );
		values.put( weatherforecastentity.POSTALCODE , postalName );
		values.put( weatherforecastentity.DAYOFWEEK , 4 + MsnWeatherModel.fomatDayofweek( cursor.getString( 27 ) ) );
		values.put( weatherforecastentity.HIGHT , cursor.getString( 25 ) );
		values.put( weatherforecastentity.LOW , cursor.getString( 26 ) );
		values.put( weatherforecastentity.ICON , "" );
		values.put( weatherforecastentity.CONDITION , cursor.getString( 24 ) );
		resolver.insert( detail_uri , values );
		cursor.close();
		return true;
	}
	
	// shlt , end
	public static Date convertStr2Date(
			String str )
	{
		Date d = null;
		try
		{
			SimpleDateFormat f = new SimpleDateFormat( "yyyy-MM-dd" );
			d = f.parse( str );
		}
		catch( ParseException e )
		{
			Log.d( TAG , "date format exception" );
		}
		return d;
	}
	
	/**
	 * 将风力风向数据更改为统一格式，“风向，风力km/h”
	 * @param windDisplay 获取到的风力风向数据
	 * @return 格式调整后的风力风向数据
	 */
	private static String formWindCondition(
			String windDisplay )
	{
		if( windDisplay.contains( "," ) )
			return windDisplay;
		String[] windTmp = windDisplay.split( "km/hr" );
		if( windTmp.length <= 0 )
			return ",";
		StringBuffer windCondition = new StringBuffer();
		if( windTmp.length > 1 )
		{
			windCondition.append( windTmp[1].trim().toLowerCase() );
		}
		else
		{
			windCondition.append( "无持续风向" );
		}
		windCondition.append( "," );
		windCondition.append( windTmp[0].trim() );
		windCondition.append( "km/h" );
		return windCondition.toString();
	}
	
	/**
	 * 根据系统时间获取农历日期
	 * @return 系统时间所对应的农历日期
	 */
	private static String getLunarDate(
			long millionSeconds )
	{
		LunarCalendar lunarCalendar = new LunarCalendar( millionSeconds );
		DateFormatter dateFormatter = new DateFormatter( mContext.getResources() );
		StringBuffer lunarDate = new StringBuffer();
		lunarDate.append( dateFormatter.getMonthName( lunarCalendar ) );
		lunarDate.append( dateFormatter.getDayName( lunarCalendar ) );
		return lunarDate.toString();
	}
}
