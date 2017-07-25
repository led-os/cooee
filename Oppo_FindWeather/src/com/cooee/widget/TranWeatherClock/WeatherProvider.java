package com.cooee.widget.TranWeatherClock;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cooee.app.Tranincooeeweather.filehelp.FileService;
import com.cooee.app.Tranincooeeweather.filehelp.Log;
import com.cooee.utils.StatisticsUtils;
import com.cooee.weather.WeatherUtils;
import com.cooee.widget.FindWeatherClock.R;
import com.cooee.widget.Transkin.ClassicSkin;
import com.cooee.widget.Transkin.baseskin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.widget.RemoteViews;


public class WeatherProvider extends AppWidgetProvider
{
	
	//com.cooee.app.cooeeweather.dataprovider
	private static final String TAG = "oppofind_WeatherProvider";
	public final static String DATA_SERVICE_ACTION = "com.cooee.app.cooeeweather.dataprovider.weatherDataService";
	private static final String ON_QUARTER_HOUR = "com.cooee.findweather.ON_QUARTER_HOUR";
	private static final String LAUNCHER_TIME_TICK = "com.cooee.launcher.TIME_TICK";
	private static final String LAUNCHER_CHANGE_PAGE = "com.cool.launcher.workspace.finishAutoEffect";
	private static final String POSTAL_CODE = "postalCode";
	private static final String USER_ID = "userid";
	//	public static boolean isStartDownLoadClient = false;
	public static boolean hasClient = false;
	private FileService mservice;
	private static boolean f_defaultcity = false;
	private static boolean needUpdateWeather = true;
	private static boolean playRefreshAnim = false;
	private static int refreshWidgetId = -1;
	public static final String[] projection = new String[]{ POSTAL_CODE , USER_ID };
	public static final String BT_REFRESH_ACTION = "com.android.timer.BT_REFRESH_ACTION";
	private static String default_clock_package = "";
	private List<String> pagList = new ArrayList<String>();
	private HashMap<String , Object> item = new HashMap<String , Object>();
	
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
		Log.v( TAG , "onDeleted" );
		// 删除widget时应同时删除数据库中对应的城市
		//		if(hasClient)
		//		for (int i = 0; i < appWidgetIds.length; i++) {
		//			deletePostalCode(context, appWidgetIds[i]);
		//		}
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
		cancelAlarmOnQuarterHour( context );
		SharedPreferences preferences = context.getSharedPreferences( "find_weather" , Context.MODE_PRIVATE );
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
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
		context.startService( new Intent( context , WeatherService.class ) );
		StartAlarmManager( context );
		startAlarmOnQuarterHour( context );
	}
	
	static int ssss = 0;
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		final String action = intent.getAction();
		Log.v( TAG , "action = " + action );
		if( "com.cooee.widget.TranWeatherClock.WeatherProvider.activity".equals( action ) )
		{
			//是否有天气客户端安装包
			String weather_default_package_name = context.getResources().getString( R.string.weather_default_packageName );
			hasClient = WeatherUtils.isWeatherClientInstall( context , weather_default_package_name );
			if( hasClient )
			{
				Intent mIntent = new Intent();
				mIntent.setClassName( "com.cooee.widget.samweatherclock" , "com.cooee.widget.samweatherclock.MainActivity" );
				mIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				if( intent != null )
				{
					context.startActivity( mIntent );
				}
			}
			else
			{
				downLoadClient( context );
			}
		}
		else
			if( "com.cooee.widget.TranWeatherClock.WeatherProvider.refresh".equals( action ) )
		{
			Intent refreshIntent = new Intent( DATA_SERVICE_ACTION );
			refreshIntent.putExtra( "postalCode" , intent.getStringExtra( "postalCode" ) );
			refreshIntent.putExtra( "forcedUpdate" , 1 );
			context.startService( refreshIntent );
			refreshWidgetId = intent.getIntExtra( "widgetId" , -1 );
			/**
			 * 更新之前，读取一次ContentProvider
			 * fulijuan add 2017/5/4
			 */
			WeatherUtils.readAndSaveWeatherInfo( context );
			updateAllWidget( context );
		}
		else
				if( "android.intent.action.BOOT_COMPLETED".equals( action ) || "com.android.launcher.changed.resume".equals( action ) || "android.intent.action.USER_PRESENT".equals( action ) )
		{
			updateAllWidget( context );
		}
		else
					if( ON_QUARTER_HOUR.equals( action ) || LAUNCHER_TIME_TICK.equals( action ) )
		{
			if( !ON_QUARTER_HOUR.equals( action ) )
				cancelAlarmOnQuarterHour( context );
			startAlarmOnQuarterHour( context );
			updateAllWidget( context );
		}
		else
						if( LAUNCHER_CHANGE_PAGE.equals( action ) )
		{
			needUpdateWeather = true;
			updateAllWidget( context );
		}
		else
							if( action.equals( BT_REFRESH_ACTION ) )
		{
			try
			{
				String packageName = null;
				SharedPreferences p = context.getSharedPreferences( "iLoong.Widget.Clock" , 0 );
				packageName = p.getString( "clock_package" , null );
				if( packageName == null )
				{
					Editor editor = p.edit();
					if( null != default_clock_package && !"".equals( default_clock_package ) )
					{
						packageName = default_clock_package;
						editor.putString( "clock_package" , packageName );
					}
					else
					{
						listPackages( context );
						if( pagList.size() != 0 )
						{
							packageName = pagList.get( 0 );
							editor.putString( "clock_package" , packageName );
						}
					}
					editor.commit();
				}
				PackageManager pm = context.getPackageManager();
				int value = Integer.parseInt( android.os.Build.VERSION.SDK );
				String co_into = context.getResources().getString( R.string.enter_the_alarm_clock );
				Intent intent0 = new Intent( Intent.ACTION_MAIN );
				intent0.addCategory( Intent.CATEGORY_LAUNCHER );
				intent0.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				ComponentName cn = new ComponentName( "com.android.deskclock" , "com.android.deskclock.DeskClock" );
				intent0.setComponent( cn );
				if( packageName != null )
				{
					Intent intent1 = pm.getLaunchIntentForPackage( packageName );
					if( intent1 != null )
					{
						intent1.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						context.startActivity( intent1 );
					}
					else
					{
						if( value == 19 && co_into.equals( "0" ) )
						{
							context.startActivity( intent0 );
						}
						else
						{
							Intent i2 = new Intent( Settings.ACTION_DATE_SETTINGS );
							i2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
							context.startActivity( i2 );
						}
					}
				}
				else
				{
					if( value == 19 && co_into.equals( "0" ) )
					{
						context.startActivity( intent0 );
					}
					else
					{
						Intent i2 = new Intent( Settings.ACTION_DATE_SETTINGS );
						i2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						context.startActivity( i2 );
					}
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
		context.startService( new Intent( context , WeatherService.class ) );
		super.onReceive( context , intent );
	}
	
	private void StartAlarmManager(
			Context context )
	{
		String PostalCode = "StartAlarm";
		Intent intent = new Intent( DATA_SERVICE_ACTION );
		intent.putExtra( "postalCode" , PostalCode );
		intent.putExtra( "forcedUpdate" , 1 ); // 强制更新
		context.startService( intent );
	}
	
	@Override
	public void onUpdate(
			final Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		Log.v( TAG , "onUpdate" );
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
		//yangmengchao add start  //Statistics
		try
		{
			StatisticsUtils.getInstance( context ).olapStatistics();
		}
		catch( final Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//yangmengchao add end		
		/*
		ContentResolver cv = mycontext.getContentResolver();
		timeformat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		thead_timetick = true;
		RuntimeUpdatetime();
		*/
		needUpdateWeather = true;
		for( int i = 0 ; i < appWidgetIds.length ; i++ )
		{
			//updateWidget(context, appWidgetIds[i]);
			//		if(f_defaultcity)
			//		changePostalCode(context, appWidgetIds[i],
			//				context.getResources().getString(R.string.defaultcity));
			System.out.println( "shlt , WeatherProvider , onUpdate" );
			updateWidget( context , appWidgetIds[i] );
		}
		context.startService( new Intent( context , WeatherService.class ) );
		StartAlarmManager( context );
		startAlarmOnQuarterHour( context );
		/**
		 * 再次发送请求天气客户端刷新数据的广播
		 * 不发送广播的话 拖拽出第一个之后的插件时 获取不到最新的天气数据 一直显示第一次初始化的数据
		 * fulijuan add 2017/5/4
		 */
		WeatherUtils.sendRefreshWeatherBroadcast( context );
	}
	
	private void downLoadClient(
			final Context context )
	{
		//if(!isStartDownLoadClient){
		//			isStartDownLoadClient = true;
		//下载客户端
		Intent intent = new Intent( context , DownloadActivity.class );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity( intent );
		//		            isStartDownLoadClient = false;
		//}
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
		ComponentName componentName = new ComponentName( "com.cooee.widget.FindWeatherClock" , "com.cooee.widget.TranWeatherClock.WeatherProvider" );
		int widgetIds[] = manager.getAppWidgetIds( componentName );
		Log.v( TAG , "updateAllWidget widgetIds.length = " + widgetIds.length );
		for( int i = 0 ; i < widgetIds.length ; i++ )
		{
			System.out.println( "shlt , WeatherProvider , updateAllWidget" );
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
			System.out.println( "shlt , updateWidget , hasClient : " + hasClient );
			skinmap.get( skinstyle ).updateViews( mcontext , widgetId , rv );
			//WeatherProvider.updateViews(mcontext, mwidget, rv);
			manager.updateAppWidget( mwidget , rv );
			Log.v( TAG , "updateWidget updateAppWidget over " );
		}
	}
	
	public static boolean isNeedUpdateWeather()
	{
		return needUpdateWeather;
	}
	
	public static void setNeedUpdateWeather(
			boolean needUpdateWeather )
	{
		WeatherProvider.needUpdateWeather = needUpdateWeather;
	}
	
	public static boolean isPlayRefreshAnim()
	{
		return playRefreshAnim;
	}
	
	public static void setPlayRefreshAnim(
			boolean playRefreshAnim )
	{
		WeatherProvider.playRefreshAnim = playRefreshAnim;
	}
	
	/**
	 * 下一次整点、十五分、半点或四十五分的毫秒数
	 * @return
	 */
	public static long getAlarmOnQuarterHour()
	{
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.set( Calendar.SECOND , 1 );
		localCalendar.set( Calendar.MILLISECOND , 0 );
		localCalendar.add( Calendar.MINUTE , 15 - localCalendar.get( Calendar.MINUTE ) % 15 );
		long l1 = localCalendar.getTimeInMillis();
		long l2 = System.currentTimeMillis();
		long l3 = l1 - l2;
		if( ( 0L >= l3 ) || ( l3 > 901000L ) )
			l1 = l2 + 901000L;
		return l1;
	}
	
	/**
	 * 每过一段时间给自己发一个广播
	 * @param paramContext
	 */
	private void startAlarmOnQuarterHour(
			Context paramContext )
	{
		long l;
		PendingIntent localPendingIntent;
		AlarmManager localAlarmManager;
		if( paramContext != null )
		{
			l = getAlarmOnQuarterHour();
			//			l = System.currentTimeMillis() + 5000;
			localPendingIntent = PendingIntent.getBroadcast( paramContext , 0 , new Intent( ON_QUARTER_HOUR ) , PendingIntent.FLAG_CANCEL_CURRENT );
			localAlarmManager = (AlarmManager)paramContext.getSystemService( "alarm" );
			localAlarmManager.set( AlarmManager.RTC , l , localPendingIntent );
		}
	}
	
	/**
	 * 取消全局闹钟
	 * @param paramContext
	 */
	public void cancelAlarmOnQuarterHour(
			Context paramContext )
	{
		if( paramContext != null )
		{
			PendingIntent localPendingIntent = PendingIntent.getBroadcast( paramContext , 0 , new Intent( ON_QUARTER_HOUR ) , PendingIntent.FLAG_CANCEL_CURRENT );
			( (AlarmManager)paramContext.getSystemService( "alarm" ) ).cancel( localPendingIntent );
		}
	}
	
	public static int getRefreshWidgetId()
	{
		return refreshWidgetId;
	}
	
	private void listPackages(
			Context context )
	{
		ArrayList<PInfo> apps = getInstalledApps( false , context );
		final int max = apps.size();
		for( int i = 0 ; i < max ; i++ )
		{
			apps.get( i ).prettyPrint();
			item = new HashMap<String , Object>();
			int aa = apps.get( i ).pname.length();
			if( aa > 11 )
			{
				if( apps.get( i ).pname.indexOf( "clock" ) != -1 )
				{
					if( !( apps.get( i ).pname.indexOf( "widget" ) != -1 ) )
					{
						try
						{
							PackageInfo pInfo = context.getPackageManager().getPackageInfo( apps.get( i ).pname , 0 );
							if( isSystemApp( pInfo ) || isSystemUpdateApp( pInfo ) )
							{
								item.put( "pname" , apps.get( i ).pname );
								item.put( "appname" , apps.get( i ).appname );
								pagList.add( apps.get( i ).pname );
							}
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	class PInfo
	{
		
		private String appname = "";
		private String pname = "";
		private String versionName = "";
		private int versionCode = 0;
		
		private void prettyPrint()
		{
			Log.i( "taskmanger" , appname + "\t" + pname + "\t" + versionName + "\t" + versionCode + "\t" );
		}
	}
	
	private ArrayList<PInfo> getInstalledApps(
			boolean getSysPackages ,
			Context context )
	{
		ArrayList<PInfo> res = new ArrayList<PInfo>();
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages( 0 );
		for( int i = 0 ; i < packs.size() ; i++ )
		{
			PackageInfo p = packs.get( i );
			if( ( !getSysPackages ) && ( p.versionName == null ) )
			{
				continue;
			}
			PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel( context.getPackageManager() ).toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			res.add( newInfo );
		}
		return res;
	}
	
	public boolean isSystemApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) != 0 );
	}
	
	public boolean isSystemUpdateApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 );
	}
}
