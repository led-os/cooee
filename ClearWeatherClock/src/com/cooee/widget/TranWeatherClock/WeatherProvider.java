package com.cooee.widget.TranWeatherClock;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.StatisticsBase.Assets;
import com.cooee.StatisticsBase.StatisticsMainBase;
import com.cooee.app.Tranincooeeweather.filehelp.FileService;
import com.cooee.app.Tranincooeeweather.filehelp.Log;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.weather.WeatherEntity;
import com.cooee.weather.WeatherUtils;
import com.cooee.widget.Transkin.BaseSkin;
import com.cooee.widget.Transkin.ClassicSkin;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.RemoteViews;


public class WeatherProvider extends AppWidgetProvider
{
	
	private static final String TAG = "WeatherProvider";
	private static final String POSTAL_CODE = "postalCode";
	private static final String USER_ID = "userid";
	public static boolean hasClient = false;
	private FileService mservice;
	private static boolean f_defaultcity = false;
	public static final String[] projection = new String[]{ POSTAL_CODE , USER_ID };
	private static WeatherEntity mCurWeatherEntity = null;
	public static String defaultPackage = "com.tianqiwhite";//默认使用2345天气客户端（客户端包名）
	public static String DEFAULT_CLIENT = "com.cooee.widget.samweatherclock";//我们的天气客户端包名
	public static String CLINT_2345 = "com.tianqiwhite";//2345天气客户端
	
	public enum SKINTABLE
	{
		FASHION_STYLE , CLASSIC_STYLE
	}
	
	private static SKINTABLE skinstyle = SKINTABLE.CLASSIC_STYLE;
	private static Map<SKINTABLE , BaseSkin> skinmap = null;
	
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		super.onDeleted( context , appWidgetIds );
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
		Log.v( TAG , "onEnabled" );
		skinstyle = SKINTABLE.CLASSIC_STYLE;
		skinmap = new HashMap<SKINTABLE , BaseSkin>();
		//kj 定制
		/*skinmap.put(SKINTABLE.FASHION_STYLE, new FashionSkin());
		skinmap.put(SKINTABLE.CLASSIC_STYLE, new ClassicSkin());*/
		skinmap.put( SKINTABLE.CLASSIC_STYLE , new ClassicSkin() );
		//weijie20121228
		// gaominghui@2016/08/01 ADD START
		//第一次加载插件需要请求天气客户端数据
		sendRefreshWeatherBroadcast( context );
		// gaominghui@2016/08/01 ADD END
	}
	
	public void sendRefreshWeatherBroadcast(
			Context context )
	{
		Intent intent = new Intent();
		intent.setAction( WeatherUtils.WEATHER_CLIENT_REQUEST_REFRESH_DATA_ACTION );
		context.sendBroadcast( intent );
	}
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		final String action = intent.getAction();
		Log.v( TAG , "action = " + action );
		final Context mcontext = context;
		if( action.equals( WeatherUtils.WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION ) || action.equals( WeatherUtils.WEATHER_CLIENT_CLOSED_UPDATE_LAUNCHER_ACTION ) )
		{ // 天气数据更新完成的广播
			if( intent != null )
			{
				receiveWeatherInfo( mcontext , intent );
				// 即使是更新失败，也要updateWidget
				String cityName = intent.getExtras().getString( "postalCode" );
				if( cityName != null )
				{
					// 通过城市来判断是否更新
					AppWidgetManager wm;
					int widgetIds[];
					wm = AppWidgetManager.getInstance( mcontext );
					widgetIds = wm.getAppWidgetIds( new ComponentName( "com.cooee.widget.ClearWeatherClock" , "com.cooee.widget.TranWeatherClock.WeatherProvider" ) );
					for( int i = 0 ; i < widgetIds.length ; i++ )
					{
						updateWidget( mcontext , widgetIds[i] );
					}
				}
			}
		}
		else
			if( "com.cooee.widget.TranWeatherClock.WeatherProvider.activity".equals( action ) )
		{
			/*try
			{
				final Intent intent = new Intent();
				PackageManager pm = context.getPackageManager();
				Intent mIntent = pm.getLaunchIntentForPackage( defaultPackage );
				if( intent != null )
				{
					if( defaultPackage.equals( CLINT_2345 ) )
					{
						olapStatistics( context );
					}
					context.startActivity( mIntent );
				}
			}
			catch( final Exception e )
			{
				// TODO: handle exception
			}*/
			checkClient( context );
			Log.i( TAG , "hasClient =  " + hasClient );
			if( hasClient )
			{
				PackageManager pm = context.getPackageManager();
				defaultPackage = AppConfig.getInstance( context ).getDefaultPackage();
				Intent mIntent = pm.getLaunchIntentForPackage( defaultPackage );
				if( mIntent != null )
				{
					context.startActivity( mIntent );
				}
				try
				{
					if( defaultPackage.equals( CLINT_2345 ) )
					{
						olapStatistics( context );
					}
				}
				catch( NameNotFoundException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e( TAG , "NameNotFoundException e = " + e );
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e( TAG , "JSONException e = " + e );
				}
			}
			else
			{
				downLoadClient( context );
			}
		}
		// YANGTIANYU@2016/05/11 ADD START
		// 剩下的广播是为了保证时间刷新服务的存在
		else
		{
			context.startService( new Intent( context , WeatherService.class ) );
		}
		// YANGTIANYU@2016/05/11 ADD END
		super.onReceive( context , intent );
	}
	
	/*private void StartAlarmManager(
			Context context )
	{
		String PostalCode = "StartAlarm";
		Intent intent = new Intent( DATA_SERVICE_ACTION );
		intent.putExtra( "postalCode" , PostalCode );
		intent.putExtra( "forcedUpdate" , 1 ); // 强制更新
		context.startService( intent );
	}*/
	/**
	 *
	 * @throws JSONException
	 * @throws NameNotFoundException
	 * @author gaominghui 2016年4月8日
	 */
	private void olapStatistics(
			Context context ) throws JSONException , NameNotFoundException
	{
		Log.i( TAG , "olapStatistics!!" );
		SharedPreferences prefs = context.getSharedPreferences( "weather" , Activity.MODE_PRIVATE );
		JSONObject config = Assets.getConfig( Assets.CONFIG_FILE_NAME , context );
		String appid = null;
		String sn = null;
		if( config != null )
		{
			JSONObject tmp = config.getJSONObject( "config" );
			appid = tmp.optString( "app_id" );
			sn = tmp.optString( "serialno" );
		}
		//StatisticsExpandNew.setStatiisticsLogEnable( true );
		//StatisticsExpandNew.setTestURL();
		int versionCode = context.getPackageManager().getPackageInfo( context.getPackageName() , 0 ).versionCode;
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( context.getApplicationContext() , sn , appid , CooeeSdk.cooeeGetCooeeId( context ) , 4 , defaultPackage , "" + versionCode );
			prefs.edit().putBoolean( "first_run" , false ).apply();
		}
		else
		{
			StatisticsExpandNew.use( context.getApplicationContext() , sn , appid , CooeeSdk.cooeeGetCooeeId( context ) , 4 , defaultPackage , "" + versionCode );
		}
	}
	
	@Override
	public void onUpdate(
			final Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		System.out.println( "shlt , onUpdate()" );
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
			skinmap = new HashMap<SKINTABLE , BaseSkin>();
			//kj 定制
			/*skinmap.put(SKINTABLE.FASHION_STYLE, new FashionSkin());
			skinmap.put(SKINTABLE.CLASSIC_STYLE, new ClassicSkin());*/
			skinmap.put( SKINTABLE.CLASSIC_STYLE , new ClassicSkin() );
		}
		Log.v( TAG , "f_defaultcity = " + f_defaultcity );
		/*
		 * wanghongjian add 统计
		 */
		StatisticsMainBase statisticsMainBase = new StatisticsMainBase( context );
		statisticsMainBase.doneStatistic();
		//weijie20121228
		// gaominghui@2016/08/01 ADD START
		//获取天气客户端包名
		defaultPackage = AppConfig.getInstance( context ).getDefaultPackage();
		Log.i( flag , "defaultPackage = " + defaultPackage );
		// gaominghui@2016/08/01 ADD END
		for( int i = 0 ; i < appWidgetIds.length ; i++ )
		{
			updateWidget( context , appWidgetIds[i] );
		}
		context.startService( new Intent( context , WeatherService.class ) );
		//StartAlarmManager( context );
		//		checkClient( context );
		//		downLoadClient( context );
	}
	
	private void downLoadClient(
			Context context )
	{
		if( !hasClient )
		{
			//下载客户端
			Intent intent = new Intent( context , DownloadActivity.class );
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			context.startActivity( intent );
		}
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
		ComponentName componentName = new ComponentName( "com.cooee.widget.ClearWeatherClock" , "com.cooee.widget.TranWeatherClock.WeatherProvider" );
		int widgetIds[] = manager.getAppWidgetIds( componentName );
		for( int i = 0 ; i < widgetIds.length ; i++ )
		{
			updateWidget( context , widgetIds[i] );
		}
	}
	
	private static void updateWidget(
			Context context ,
			int widgetId )
	{
		final int mwidget = widgetId;
		final Context mcontext = context;
		AppWidgetManager manager = AppWidgetManager.getInstance( mcontext );
		//weijie  20130218
		if( mwidget > 0 )
		{
			if( skinmap == null )
			{
				skinmap = new HashMap<SKINTABLE , BaseSkin>();
				skinmap.put( SKINTABLE.CLASSIC_STYLE , new ClassicSkin() );
			}
			initWeatherInfo( mcontext );
			RemoteViews rv = new RemoteViews( mcontext.getPackageName() , skinmap.get( skinstyle ).getLayout() );
			skinmap.get( skinstyle ).updateViews( mcontext , widgetId , rv , mCurWeatherEntity );
			manager.updateAppWidget( mwidget , rv );
		}
	}
	
	// fulijuan@2017/4/7 ADD START weather.jar中的调用
	/**
	 * 获取天气数据  并且同时将数据set到WeatherEntity
	 * 根据SharedPreferences初始化默认天气信息，如果没有，就使用默认值
	 */
	private static void initWeatherInfo(
			Context context )
	{
		mCurWeatherEntity = WeatherUtils.getWeatherInfo( context , "" , "" , "" , -256 , -256 , -256 );
	}
	
	/**
	 * 接收天气信息，并且set到WeatherEntity中
	 * 如果当前显示的天气信息和接收到的不一致，则更新，并且从最新的WeatherEntity中put进SharedPreferences
	 * @param mContext
	 * @param intent
	 * @author fulijuan 2017-4-7
	 */
	private void receiveWeatherInfo(
			Context mContext ,
			Intent intent )
	{
		WeatherEntity newWeatherEntity = WeatherUtils.receiveWeatherInfo( mContext , intent , defaultPackage );
		if( newWeatherEntity != null && !newWeatherEntity.equals( mCurWeatherEntity ) )
		{
			mCurWeatherEntity = newWeatherEntity;
			WeatherUtils.saveWeatherData( mContext , mCurWeatherEntity );
		}
	}
	
	/**
	 * 检测是否有对应包名的客户端
	 */
	private void checkClient(
			Context context )
	{
		hasClient = WeatherUtils.isWeatherAtapterInstall( context , AppConfig.getInstance( context ).getDefaultPackage() );
	}
	// fulijuan@2017/4/7 ADD END weather.jar中的调用
}
