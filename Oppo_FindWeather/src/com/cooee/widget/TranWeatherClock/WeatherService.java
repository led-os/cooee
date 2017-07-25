package com.cooee.widget.TranWeatherClock;


import com.cooee.app.Tranincooeeweather.filehelp.Log;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.weather.WeatherUtils;
import com.cooee.widget.FindWeatherClock.R;
import com.kpsh.sdk.KpshSdk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;


public class WeatherService extends Service
{
	
	private static final String TAG = "com.cooee.widget.samweatherclock";
	
	// private static final int RESTART_INTERVAL = 30 * 1000; // 每10秒重启一次服务，以防止被关闭
	@Override
	public IBinder onBind(
			Intent intent )
	{
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.v( TAG , "onCreate" );
		/**
		 * 初始化天气默认配置[必要配置项]即可
		 */
		WeatherUtils.initWeatherDefaultConfig( R.integer.weather_default_dock_mode , R.bool.weather_default_is_available , R.string.weather_default_packageName , -1 , -1 , -1 , -1 , -1 , -1 );
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( "android.intent.action.TIME_SET" );
		//天气相关
		filter = WeatherUtils.addWeatherIntentFilterAction( WeatherService.this , filter );
		this.registerReceiver( mIntentReceiver , filter );
		//yangmengchao add start   //push
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				KpshSdk.setAppKpshTag( WeatherService.this , WeatherService.this.getPackageName() );
				CooeeSdk.initCooeeSdk( WeatherService.this );
			}
		} ).start();
		//yangmengchao add end
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.v( TAG , "onDestroy" );
		Log.v( TAG , "unregisterReceiver action " + Intent.ACTION_TIME_TICK );
		this.unregisterReceiver( mIntentReceiver );
		// 非常奇怪的是，系统关闭服务竟然使用的是stopService而不是kill，导致服务没有自动重启
		// 所以我们再加个定时器1秒后把自己启动
		long now = System.currentTimeMillis();
		long updateMilis = 1000;
		PendingIntent pendingIntent = PendingIntent.getService( this , 0 , new Intent( this , WeatherService.class ) , 0 );
		// Schedule alarm, and force the device awake for this update
		AlarmManager alarmManager = (AlarmManager)getSystemService( Context.ALARM_SERVICE );
		alarmManager.set( AlarmManager.RTC_WAKEUP , now + updateMilis , pendingIntent );
	}
	
	/*   private void restartByAlarmManager(Intent intent) {
	       long now = System.currentTimeMillis();
	       long updateMilis = RESTART_INTERVAL;
	       Intent newIntent = null;
	
	       Log.v(TAG, "intent = " + intent);
	       if (intent != null) {
	           newIntent = intent;
	       } else {
	           newIntent = new Intent(this, WeatherService.class);
	       }
	       PendingIntent pendingIntent = PendingIntent.getService(this, 0,
	               newIntent, 0);
	       // Schedule alarm, and force the device awake for this update
	       AlarmManager alarmManager = (AlarmManager)
	               getSystemService(Context.ALARM_SERVICE);
	       alarmManager.set(AlarmManager.RTC_WAKEUP, now + updateMilis,
	               pendingIntent);
	   }*/
	@Override
	public int onStartCommand(
			Intent intent ,
			int flags ,
			int startId )
	{
		super.onStartCommand( intent , flags , startId );
		Log.v( TAG , "onStartCommand" );
		// 为防止用户使用内存整理，把widget和服务一并关掉，我们的服务还是永远开启吧
		//		restartByAlarmManager(intent);
		return START_REDELIVER_INTENT;
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String action = intent.getAction();
			Log.i( "test" , "flj  action===" + action );
			boolean updateWidget = false;
			if( ( action.equals( Intent.ACTION_TIMEZONE_CHANGED ) ) || ( action.equals( Intent.ACTION_TIME_TICK ) ) || ( action.equals( Intent.ACTION_DATE_CHANGED ) ) || ( action
					.equals( Intent.ACTION_TIME_CHANGED ) ) || ( action.equals( "android.intent.action.TIME_SET" ) ) )
			{
				System.out.println( "shlt , mIntentReceiver , shoudao" );
				updateWidget = true;
			}
			//天气相关
			else
			{
				WeatherUtils.onReceiveWeatherBroadcast( context , intent );
				updateWidget = true;
			}
			if( updateWidget )
			{
				WeatherProvider.setNeedUpdateWeather( updateWidget );
				updateWidget = false;
				WeatherProvider.updateAllWidget( context );
			}
		}
	};
}
