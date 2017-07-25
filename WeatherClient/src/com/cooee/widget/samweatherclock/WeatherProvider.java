package com.cooee.widget.samweatherclock;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cooee.StatisticsBase.StatisticsMainBase;
import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.filehelp.FileService;
import com.cooee.app.cooeeweather.filehelp.Log;

import com.cooee.widget.samskin.ClassicSkin;
import com.cooee.widget.samskin.baseskin;


public class WeatherProvider extends AppWidgetProvider
{
	
	private static final String TAG = "com.cooee.widget.samweatherclock.WeatherProvider";
	public final static String DATA_SERVICE_ACTION = "com.cooee.app.cooeeweather.dataprovider.weatherDataService";
	private static final String WEATHER_URI = "content://com.cooee.app.cooeeweather.dataprovider/weather";
	private static final String UPDATA_WHEATER_ACTION = "com.cooee.weather.data.action.UPDATE_RESULT";
	private static final String POSTALCODE_URI = "content://com.cooee.app.cooeeweather.dataprovider/postalCode";
	private static final String DEL_DAFAULT_CITY_ACTION = "com.cooee.weather.data.action.DEL_DAFAULT_CITY";
	private static final String ADD_DAFAULT_CITY_ACTION = "com.cooee.weather.data.action.ADD_DAFAULT_CITY";
	private static final String POSTAL_CODE = "postalCode";
	private static final String USER_ID = "userid";
	private FileService mservice;
	private static boolean f_defaultcity = false;
	public static final String[] projection = new String[]{ POSTAL_CODE , USER_ID };
	
	//weijie20121228
	//public static boolean thead_timetick = true;
	// private static IntentFilter filter = null;
	/*
	 //weijie20121228
	private static int mlasttimeminute = 0;
	private static int mlasttimehoure = 0;
	private static Context mycontext = null;
	private static String timeformat = "";
	private static Thread mThread = null;
		
	*/
	public enum SKINTABLE
	{
		FASHION_STYLE , CLASSIC_STYLE
	}
	
	private static SKINTABLE skinstyle = SKINTABLE.CLASSIC_STYLE;
	private static Map<SKINTABLE , baseskin> skinmap = null;
	
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		super.onDeleted( context , appWidgetIds );
		// 删除widget时应同时删除数据库中对应的城市
		for( int i = 0 ; i < appWidgetIds.length ; i++ )
		{
			deletePostalCode( context , appWidgetIds[i] );
		}
		//weijie 20121228
		//thead_timetick = false;
		/*
		 * if(WeatherClockService.mthread != null) {
		 * WeatherClockService.mthread.stop(); }
		 */
	}
	
	@Override
	public void onDisabled(
			Context context )
	{
		super.onDisabled( context );
		Log.v( TAG , "onDisabled" );
		// 如果所有的天气时钟wigdet都被移除，停止服务
		context.stopService( new Intent( context , WeatherService.class ) );
		//weijie20121228
		//thead_timetick = false;
	}
	
	@Override
	public void onEnabled(
			Context context )
	{
		super.onEnabled( context );
		skinstyle = SKINTABLE.CLASSIC_STYLE;
		skinmap = new HashMap<SKINTABLE , baseskin>();
		//kj 定制
		/*skinmap.put(SKINTABLE.FASHION_STYLE, new FashionSkin());
		skinmap.put(SKINTABLE.CLASSIC_STYLE, new ClassicSkin());*/
		skinmap.put( SKINTABLE.CLASSIC_STYLE , new ClassicSkin() );
		//weijie20121228
		//thead_timetick = true;
		Log.v( TAG , "onEnabled" );
	}
	
	static int ssss = 0;
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		final String action = intent.getAction();
		Log.v( TAG , "action = " + action );
		final Context mcontext = context;
		final Intent mintent = intent;
		if( action.equals( UPDATA_WHEATER_ACTION ) )
		{ // 天气数据更新完成的广播
			Log.v( TAG , "广播到：" + UPDATA_WHEATER_ACTION );
			// Toast.makeText(context.getApplicationContext(), (ssss++)+ "", Toast.LENGTH_SHORT).show();
			new Thread( new Runnable() {
				
				public void run()
				{
					Bundle bun = mintent.getExtras();
					//String str = bun.getString("cooee.weather.updateResult");
					// if (str.equals("UPDATE_SUCCESED") ||
					// str.equals("AVAILABLE_DATA")) {
					if( true )
					{ // 即使是更新失败，也要updateWidget
						String postalCode = bun.getString( "cooee.weather.updateResult.postalcode" );
						if( postalCode != null )
						{
							if( true )
							{ // 通过城市来判断是否更新
								AppWidgetManager wm;
								int widgetIds[];
								String widgetPostalCode = null;
								wm = AppWidgetManager.getInstance( mcontext );
								widgetIds = wm.getAppWidgetIds( new ComponentName( "com.cooee.widget.samweatherclock" , "com.cooee.widget.samweatherclock.WeatherProvider" ) );
								for( int i = 0 ; i < widgetIds.length ; i++ )
								{
									widgetPostalCode = getPostalCode( mcontext , widgetIds[i] );
									if( postalCode.equals( widgetPostalCode ) )
									{
										updateWidget( mcontext , widgetIds[i] );
									}
								}
							}
						}
					}
				}
			} ).start();
		}
		else if( action.equals( DEL_DAFAULT_CITY_ACTION ) )
		{
			f_defaultcity = false;
			mservice = new FileService( context.getApplicationContext() );
			try
			{
				mservice.save( "setting" , "false" );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if( action.equals( ADD_DAFAULT_CITY_ACTION ) )
		{
			f_defaultcity = false;
			mservice = new FileService( context.getApplicationContext() );
			try
			{
				mservice.save( "setting" , "true" );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else if( intent.getAction().equals( localConfigureActivity.CHANGE_POSTALCODE ) )
		{ // 更换城市
			new Thread( new Runnable() {
				
				public void run()
				{
					Bundle bundle = mintent.getExtras();
					if( bundle != null )
					{
						String postalCode = bundle.getString( "com.cooee.weather.Weather.postalCode" );
						int widgetId = bundle.getInt( "com.cooee.weather.Weather.userId" , 0 );
						/*skinstyle =  bundle.getInt(
								"com.cooee.weather.Weather.skin", 0);*/
						switch( bundle.getInt( "com.cooee.weather.Weather.skin" , 0 ) )
						{
							case 0:
								skinstyle = SKINTABLE.CLASSIC_STYLE;
								break;
							case 1:
								skinstyle = SKINTABLE.FASHION_STYLE;
								break;
							default:
								skinstyle = SKINTABLE.FASHION_STYLE;
								break;
						}
						AppWidgetManager wm;
						int widgetIds[];
						AppWidgetProviderInfo info;
						wm = AppWidgetManager.getInstance( mcontext );
						info = wm.getAppWidgetInfo( widgetId );
						widgetIds = wm.getAppWidgetIds( new ComponentName( "com.cooee.widget.samweatherclock" , "com.cooee.widget.samweatherclock.WeatherProvider" ) );
						Log.v( TAG , "widgetId = " + widgetId + ", info = " + info );
						if( info != null )
						{
							Log.v( TAG , "info.provider.getClassName() = " + info.provider.getClassName() );
							if( info.provider.getClassName().equals( "com.cooee.widget.samweatherclock.WeatherProvider" ) )
							{
								if( postalCode != null && widgetId != 0 )
								{
									Log.v( TAG , "changePostalCode postalCode = " + postalCode + ", widgetId = " + widgetId );
									changePostalCode( mcontext , widgetId , postalCode );
									for( int i = 0 ; i < widgetIds.length ; i++ )
									{
										updateWidget( mcontext , widgetIds[i] );
									}
								}
							}
						}
					}
				}
			} ).start();
		}
		super.onReceive( context , intent );
	}
	
	public void changePostalCode(
			Context context ,
			int widgetId ,
			String postalCode )
	{
		// 鏇存敼widgetId鍜宲ostalCode鐨勮〃
		deletePostalCode( context , widgetId );
		addPostalCode( context , postalCode , widgetId );
		// updateWidget(context, widgetId, postalCode);
	}
	
	public static void deletePostalCode(
			Context context ,
			int widgetId )
	{
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse( localConfigureActivity.POSTALCODE_URI );
		String selection;
		selection = PostalCodeEntity.USER_ID + "=" + "'" + widgetId + "'";
		resolver.delete( uri , selection , null );
	}
	
	public static void addPostalCode(
			Context context ,
			String postalCode ,
			int widgetId )
	{
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse( localConfigureActivity.POSTALCODE_URI );
		ContentValues values = new ContentValues();
		values.put( PostalCodeEntity.POSTAL_CODE , postalCode );
		values.put( PostalCodeEntity.USER_ID , widgetId );
		resolver.insert( uri , values );
	}
	
	private void StartAlarmManager(
			Context context )
	{
		String PostalCode = "StartAlarm";
		Intent intent = new Intent( context , com.cooee.app.cooeeweather.dataprovider.weatherDataService.class );
		intent.setAction( "com.cooee.app.cooeeweather.dataprovider.weatherDataService" );
		intent.putExtra( "postalCode" , PostalCode );
		intent.putExtra( "forcedUpdate" , 1 ); // 强制更新
		context.startService( intent );
	}
	
	@Override
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		Log.v( "weijie" , "onupdatedata!!!appWidgetIds=" + appWidgetIds );
		String flag = "";
		//weijie20121228
		//   mycontext = context;
		mservice = new FileService( context.getApplicationContext() );
		try
		{
			//mservice.save("setting", "true");
			flag = mservice.read( "setting" );
		}
		catch( Exception e )
		{
			try
			{
				mservice.save( "setting" , "true" );
			}
			catch( Exception e1 )
			{
				e1.printStackTrace();
			}
		}
		try
		{
			flag = mservice.read( "setting" );
		}
		catch( Exception e1 )
		{
			e1.printStackTrace();
		}
		//	   if(flag!=null)
		//		   f_defaultcity = (flag.equals("true"));
		f_defaultcity = false;
		if( skinmap == null )
		{
			skinmap = new HashMap<SKINTABLE , baseskin>();
			//kj 定制
			/*skinmap.put(SKINTABLE.FASHION_STYLE, new FashionSkin());
			skinmap.put(SKINTABLE.CLASSIC_STYLE, new ClassicSkin());*/
			skinmap.put( SKINTABLE.CLASSIC_STYLE , new ClassicSkin() );
		}
		Log.v( "sxddd" , "f_defaultcity = " + f_defaultcity );
		for( int i = 0 ; i < appWidgetIds.length ; i++ )
		{
			//updateWidget(context, appWidgetIds[i]);
			//		if(f_defaultcity)
			//		changePostalCode(context, appWidgetIds[i],
			//				context.getResources().getString(R.string.defaultcity));
			updateWidget( context , appWidgetIds[i] );
		}
		context.startService( new Intent( context , WeatherService.class ) );
		StartAlarmManager( context );
		//weijie20121228
		/*
		ContentResolver cv = mycontext.getContentResolver();
		timeformat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		thead_timetick = true;
		RuntimeUpdatetime();
		*/
	}
	
	public static String getPostalCode(
			Context context ,
			int widgetId )
	{
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse( POSTALCODE_URI );
		String selection;
		Cursor cursor = null;
		String postalCode = null;
		selection = USER_ID + "=" + "'" + widgetId + "'";
		cursor = resolver.query( uri , projection , selection , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				postalCode = cursor.getString( 0 );
			}
			cursor.close();
		}
		return postalCode;
	}
	
	/**
	 * 更新所有的天气时钟小部件
	 * 
	 * @param context
	 */
	public static void updateAllWidget(
			Context context )
	{
		AppWidgetManager manager = AppWidgetManager.getInstance( context );
		ComponentName componentName = new ComponentName( "com.cooee.widget.samweatherclock" , "com.cooee.widget.samweatherclock.WeatherProvider" );
		int widgetIds[] = manager.getAppWidgetIds( componentName );
		Log.v( TAG , "updateAllWidget widgetIds.length = " + widgetIds.length );
		for( int i = 0 ; i < widgetIds.length ; i++ )
		{
			updateWidget( context , widgetIds[i] );
		}
	}
	
	public static void updateWidget(
			Context context ,
			int widgetId )
	{
		final int mwidget = widgetId;
		final Context mcontext = context;
		Log.v( TAG , "updateWidget widgetId = " + mwidget );
		AppWidgetManager manager = AppWidgetManager.getInstance( mcontext );
		/*RemoteViews rv = new RemoteViews(mcontext.getPackageName(),
				R.layout.weatherlayout);*/
		Log.v( TAG , "updateWidget skinmap 1= " + skinmap );
		//weijie  20130218
		if( true )
		{
			if( mwidget > 0 )
			{
				if( skinmap == null )
				{
					skinmap = new HashMap<SKINTABLE , baseskin>();
					//kj 定制
					/*skinmap.put(SKINTABLE.FASHION_STYLE, new FashionSkin());
					skinmap.put(SKINTABLE.CLASSIC_STYLE, new ClassicSkin());*/
					skinmap.put( SKINTABLE.CLASSIC_STYLE , new ClassicSkin() );
				}
				RemoteViews rv = new RemoteViews( mcontext.getPackageName() , skinmap.get( skinstyle ).getLayout() );
				skinmap.get( skinstyle ).updateViews( mcontext , widgetId , rv );
				//WeatherProvider.updateViews(mcontext, mwidget, rv);
				manager.updateAppWidget( mwidget , rv );
				Log.v( TAG , "updateWidget updateAppWidget over " );
			}
		}
		else
		{
			if( skinmap != null )
			{
				RemoteViews rv = new RemoteViews( mcontext.getPackageName() , skinmap.get( skinstyle ).getLayout() );
				skinmap.get( skinstyle ).updateViews( mcontext , widgetId , rv );
				//WeatherProvider.updateViews(mcontext, mwidget, rv);
				manager.updateAppWidget( mwidget , rv );
			}
		}
		/*new Thread(new Runnable() {
			public void run() {
				Log.v(TAG, "updateWidget widgetId = " + mwidget);
				AppWidgetManager manager = AppWidgetManager
						.getInstance(mcontext);
				RemoteViews rv = new RemoteViews(mcontext.getPackageName(),
						R.layout.weatherlayout);
				WeatherProvider.updateViews(mcontext, mwidget, rv);
				manager.updateAppWidget(mwidget, rv);
			}
		}).start();*/
	}
	
	public static void updateViews(
			Context context ,
			int widgetId ,
			RemoteViews rv )
	{
		updateTime( context , widgetId , rv );
		String postalCode = WeatherProvider.getPostalCode( context , widgetId );
		weatherdataentity dataEntity = WeatherProvider.readData( context , rv , widgetId , postalCode );
		updateWeather( context , rv , widgetId , postalCode , dataEntity );
		Log.v( TAG , "updateViews widgetId = " + widgetId + ", postalCode = " + postalCode );
	}
	
	public static void updateTime(
			Context context ,
			int widgetId ,
			RemoteViews rv )
	{
		// com.android.settings.DateTimeSettingsSetupWizard
		Intent clockIntent = new Intent();
		/*
		 * clockIntent.setClassName("com.android.settings",
		 * "com.android.settings.DateTimeSettings");
		 */
		String release = android.os.Build.VERSION.RELEASE;
		String version = "2.4";
		if( release.compareTo( version ) > 0 )
		{
			//weijie popup the datetime's activity by android 4.0 OS
			clockIntent.setAction( "android.settings.DATE_SETTINGS" );
			clockIntent.addCategory( "android.intent.category.VOICE_LAUNCH" );
			clockIntent.addCategory( "android.intent.category.DEFAULT" );
			//clockIntent.setClassName("com.android.settings",
			//		"com.android.settings.DateTimeSettingsSetupWizard");
		}
		else
		{
			clockIntent.setClassName( "com.android.settings" , "com.android.settings.DateTimeSettings" );
		}
		PendingIntent pendintent = PendingIntent.getActivity( context , widgetId , clockIntent , PendingIntent.FLAG_UPDATE_CURRENT );
		rv.setOnClickPendingIntent( R.id.weLayout , pendintent );
		// com.cooee.weather.Weather
		Intent weatherActivity = new Intent();
		weatherActivity.setClassName( "com.cooee.widget.samweatherclock" , "com.cooee.widget.samweatherclock.MainActivity" );
		//		weatherActivity.setClassName("com.cooee.weather",
		//				"com.cooee.weather.Weather");
		weatherActivity.putExtra( "userId" , widgetId );
		//		if(f_defaultcity)
		//			weatherActivity.putExtra("defaultcity", context.getResources().getString(R.string.defaultcity));
		//		else
		weatherActivity.putExtra( "defaultcity" , "none" );
		PendingIntent weatherintent = PendingIntent.getActivity( context , widgetId , weatherActivity , PendingIntent.FLAG_UPDATE_CURRENT );
		rv.setOnClickPendingIntent( R.id.curweatherImage , weatherintent );
		// get calendar
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get( Calendar.HOUR_OF_DAY );
		int min = calendar.get( Calendar.MINUTE );
		ContentResolver cv = context.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString( cv , android.provider.Settings.System.TIME_12_24 );
		if( strTimeFormat != null && strTimeFormat.equals( "12" ) )
		{
			rv.setViewVisibility( R.id.AM_PM , View.VISIBLE );
			if( hour > 11 )
			{
				if( hour != 12 )
					hour -= 12;
				rv.setTextViewText( R.id.AM_PM , "PM" );
			}
			else
			{
				if( hour == 0 )
					hour = 12;
				rv.setTextViewText( R.id.AM_PM , "AM" );
			}
		}
		else
		{
			rv.setViewVisibility( R.id.AM_PM , View.INVISIBLE );
		}
		/*
				if (strTimeFormat != null && strTimeFormat.equals("24")) {
					rv.setViewVisibility(R.id.AM_PM, View.INVISIBLE);
				} else {
					rv.setViewVisibility(R.id.AM_PM, View.VISIBLE);
					if (hour > 11) {
						if (hour != 12)
							hour -= 12;
						rv.setTextViewText(R.id.AM_PM, "PM");
					} else {
						if (hour == 0) hour = 12;
						rv.setTextViewText(R.id.AM_PM, "AM");
					}
				}
		*/
		rv.setImageViewResource( R.id.dateLeft1 , R.drawable.time_0 + hour / 10 );
		rv.setImageViewResource( R.id.dateLeft2 , R.drawable.time_0 + hour % 10 );
		//weijie_20121122_01 再开机初始化的时候，在时间没有显示的时候，不让时间中间的":"显示出来
		//这里将每一次刷新的时候，将":"一起刷新出来
		rv.setImageViewResource( R.id.timedot , R.drawable.time_dot );
		rv.setImageViewResource( R.id.dateRigth1 , R.drawable.time_0 + min / 10 );
		rv.setImageViewResource( R.id.dateRigth2 , R.drawable.time_0 + min % 10 );
		int month = 0 , day = 0 , week = 0;
		month = calendar.get( Calendar.MONTH ) + 1;
		day = calendar.get( Calendar.DAY_OF_MONTH );
		week = calendar.get( Calendar.DAY_OF_WEEK ) - 1;
		//weijie20121228
		/*
		mlasttimeminute = min;
		mlasttimehoure = hour;
		*/
		String str = context.getResources().getString( R.string.date_widget_format_string );
		String sTextshow = String.format( str , context.getString( R.string.monthA + month - 1 ) , day , context.getString( R.string.week0 + week ) );
		System.out.println( sTextshow );
		rv.setTextViewText( R.id.tadayDate , sTextshow );
	}
	
	public static weatherdataentity readData(
			Context context ,
			RemoteViews updateViews ,
			int widgetId ,
			String postalCode )
	{
		weatherdataentity dataEntity = null;
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		Uri CONTENT_URI;
		CONTENT_URI = Uri.parse( WEATHER_URI + "/" + postalCode );
		String selection = weatherdataentity.POSTALCODE + "=" + "'" + postalCode + "'";
		cursor = resolver.query( CONTENT_URI , weatherdataentity.projection , selection , null , null );
		if( cursor != null )
		{
			dataEntity = new weatherdataentity();
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
				dataEntity.setLunarcalendar( cursor.getString( 12 ) );
				dataEntity.setUltravioletray( cursor.getString( 13 ) );
				dataEntity.setWeathertime( cursor.getString( 14 ) );
			}
			cursor.close();
		}
		int details_count = 0;
		if( dataEntity != null )
		{
			CONTENT_URI = Uri.parse( WEATHER_URI + "/" + postalCode + "/detail" );
			selection = weatherforecastentity.CITY + "=" + "'" + postalCode + "'";
			cursor = resolver.query( CONTENT_URI , weatherforecastentity.forecastProjection , selection , null , null );
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
		return dataEntity;
	}
	
	/**
	 * 给RemoteViews设置上天气数据相关显示
	 * 
	 * @param context
	 * @param updateViews
	 * @param widgetId
	 * @param postalCode
	 * @param dataEntity
	 */
	private static void updateWeather(
			Context context ,
			RemoteViews updateViews ,
			int widgetId ,
			String postalCode ,
			weatherdataentity dataEntity )
	{
		if( dataEntity != null )
		{
			// String strTempc = mDataEntity.getTempC().toString();
			updateViews.setTextViewText( R.id.curadress , dataEntity.getPostalCode() );
			updateViews.setTextViewText( R.id.curweather , dataEntity.getCondition() );
			updateViews.setTextViewText( R.id.curTemperature , dataEntity.getTempC() + "℃" );
			//weijie20121228
			//Log.e("weijie", "!!!"+thead_timetick+"1111");
			if( dataEntity.getDetails().get( 0 ) != null )
			{
				updateViews.setViewVisibility( R.id.degreescelsius1 , View.VISIBLE );
				updateViews.setViewVisibility( R.id.degreescelsius2 , View.VISIBLE );
				updateViews.setViewVisibility( R.id.tempc_dot1 , View.VISIBLE );
				updateViews.setTextViewText( R.id.TemperatureRange , dataEntity.getDetails().get( 0 ).getHight().toString() );
				updateViews.setTextViewText( R.id.TemperatureLower , dataEntity.getDetails().get( 0 ).getLow().toString() );
			}
			int imageid = WeatherConditionImage.getwidgetConditionImage( dataEntity.getCondition() , true );
			updateViews.setImageViewResource( R.id.land_bg , imageid );
		}
		else
		{
			if( ( postalCode != null ) && ( !( postalCode.equals( "none" ) ) ) )
			{
				updateViews.setTextViewText( R.id.curadress , postalCode );
				// updateViews.setTextViewText(R.id.curweather, "无可用数据");
				updateViews.setTextViewText( R.id.curweather , context.getString( R.string.nodate ) );
			}
			else
			{
				updateViews.setTextViewText( R.id.curadress , context.getString( R.string.selectpostakcode ) + " " );
				updateViews.setTextViewText( R.id.curweather , "" );
			}
			updateViews.setTextViewText( R.id.TemperatureRange , "N/A " );
			updateViews.setViewVisibility( R.id.degreescelsius1 , View.INVISIBLE );
			updateViews.setViewVisibility( R.id.degreescelsius2 , View.INVISIBLE );
			updateViews.setViewVisibility( R.id.tempc_dot1 , View.INVISIBLE );
			updateViews.setTextViewText( R.id.TemperatureLower , "" );
			updateViews.setImageViewResource( R.id.land_bg , R.drawable.weather_default );
		}
	}
	/*//weijie20121228
	public static void RuntimeUpdatetime() {
		long now = System.currentTimeMillis();
		Time mCalendar = new Time();
		mCalendar.setToNow();
		Log.v("weijie",
		 "!!!now="+now+",,mCalendar.hour"+mCalendar.hour+",,mCalendar.minute"+mCalendar.minute);
		if (!thead_timetick) {
			return;
		}
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					// get calendar
					Log.v("weijie", "!!!"+thead_timetick+"vvv");
					try {
						ContentResolver cv = mycontext.getContentResolver();
						String strTimeFormat = android.provider.Settings.System
								.getString(
										cv,
										android.provider.Settings.System.TIME_12_24);
						Time mCalendar = new Time();
						mCalendar.setToNow();

						int hour = mCalendar.hour;
						int min = mCalendar.minute;
						
						if (min > mlasttimeminute) {
							if (true) {

								updateAllWidget(mycontext);
							}
							mlasttimeminute = min;
							mlasttimehoure = hour;
							
						} else if ((hour * 60 + min) != (mlasttimehoure * 60 + mlasttimeminute)) {
							if (true) {

								updateAllWidget(mycontext);
							}
							mlasttimeminute = min;
							mlasttimehoure = hour;
						} else if (!strTimeFormat.equals(timeformat)) {

							if (true) {

								updateAllWidget(mycontext);
							}
							mlasttimeminute = min;
							mlasttimehoure = hour;
							timeformat = strTimeFormat;
						} else {

						}

						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.v("weijie", "!!!"+thead_timetick+"aaaaa");
					if (!thead_timetick)
						break;
					Log.v("weijie", "!!!"+thead_timetick+"bbbb");
				}
				// RuntimeUpdatetime();
			}
		}).start();
	}
	*/
}
