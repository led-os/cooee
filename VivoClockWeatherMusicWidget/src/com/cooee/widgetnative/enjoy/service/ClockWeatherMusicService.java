package com.cooee.widgetnative.enjoy.service;


import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.weather.WeatherUtils;
import com.cooee.widgetnative.enjoy.manager.ClockManager;
import com.cooee.widgetnative.enjoy.manager.MusicManager;
import com.cooee.widgetnative.enjoy.manager.WeatherManager;
import com.cooee.widgetnative.enjoy.manager.WidgetViewManager;
import com.kpsh.sdk.KpshSdk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;


public class ClockWeatherMusicService extends Service
{
	
	private static final String TAG = "ClockService";
	View ForegroundView = null;
	protected long preParamId;
	
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
		Log.v( TAG , "registerReceiver " );
		IntentFilter filter = new IntentFilter();
		//时钟广播注册
		if( ClockManager.getInstance( getApplicationContext() ).isShowClockVeiw() )
		{
			Log.v( TAG , "registerReceiver clock " );
			filter.addAction( Intent.ACTION_DATE_CHANGED );
			filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
			filter.addAction( Intent.ACTION_TIME_CHANGED );
			filter.addAction( Intent.ACTION_TIME_TICK );
			filter.addAction( "android.intent.action.TIME_SET" );
		}
		//天气广播注册
		if( WeatherManager.getInstance( getApplicationContext() ).isShowWeatherVeiw() )
		{
			Log.v( TAG , "registerReceiver weather " );
			filter.addAction( WeatherUtils.WEATHER_CLIENT_CLOSED_UPDATE_LAUNCHER_ACTION );
			filter.addAction( WeatherUtils.WEATHER_CLIENT_UPDATE_RESULT_ACTION );
			filter.addAction( WeatherUtils.WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION );
		}
		//音乐广播注册
		if( MusicManager.getInstance( getApplicationContext() ).isShowMusicVeiw() )
		{
			filter.addAction( MusicManager.PLAYSTATE_CHANGED );
			filter.addAction( MusicManager.META_CHANGED );
		}
		this.registerReceiver( mIntentReceiver , filter );
		if( WeatherManager.getInstance( getApplicationContext() ).isShowWeatherVeiw() )
		{
			WeatherUtils.sendRefreshWeatherBroadcast( ClockWeatherMusicService.this );
		}
		//zhengkai  add statrt push SDK
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				KpshSdk.setAppKpshTag( ClockWeatherMusicService.this , ClockWeatherMusicService.this.getPackageName() );
				CooeeSdk.initCooeeSdk( ClockWeatherMusicService.this );
			}
		} ).start();
		//zhengkai  add end push SDK
		// YANGTIANYU@2016/05/11 ADD START
		// 6.0以上的手机,可能会提示关闭屏幕叠加层,6.0以上先不使用悬浮窗,有其他解决方案时可以修改【c_0004451】
		if( VERSION.SDK_INT < 23 )
			addForegroundView();
		// YANGTIANYU@2016/05/11 ADD END
	}
	
	/**
	 * 增加一个1X1的悬浮窗,提升服务的优先级
	 * @author yangtianyu 2016-5-11
	 */
	private void addForegroundView()
	{
		WindowManager wm = (WindowManager)getApplicationContext().getSystemService( Context.WINDOW_SERVICE );
		ForegroundView = new LinearLayout( this );
		WindowManager.LayoutParams ForegroundParams = new WindowManager.LayoutParams();
		ForegroundParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		ForegroundParams.format = PixelFormat.RGBA_8888;
		ForegroundParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		ForegroundParams.x = 0;
		ForegroundParams.y = 0;
		ForegroundParams.width = 1;
		ForegroundParams.height = 1;
		ForegroundParams.gravity = Gravity.BOTTOM;
		wm.addView( ForegroundView , ForegroundParams );
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
		PendingIntent pendingIntent = PendingIntent.getService( this , 0 , new Intent( this , ClockWeatherMusicService.class ) , 0 );
		// Schedule alarm, and force the device awake for this update
		AlarmManager alarmManager = (AlarmManager)getSystemService( Context.ALARM_SERVICE );
		alarmManager.set( AlarmManager.RTC_WAKEUP , now + updateMilis , pendingIntent );
		if( ForegroundView != null )
		{
			WindowManager wm = (WindowManager)getApplicationContext().getSystemService( Context.WINDOW_SERVICE );
			wm.removeView( ForegroundView );
		}
	}
	
	@Override
	public int onStartCommand(
			Intent intent ,
			int flags ,
			int startId )
	{
		super.onStartCommand( intent , flags , startId );
		Log.v( TAG , "onStartCommand" );
		// 为防止用户使用内存整理，把widget和服务一并关掉，我们的服务还是永远开启吧
		//restartByAlarmManager(intent);
		return START_STICKY;
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( intent == null )
				return;
			String action = intent.getAction();
			Log.d( TAG , "cyk mIntentReceiver ACTION :" + action );
			boolean updateWidget = false;
			//时钟
			if( action.equals( Intent.ACTION_TIMEZONE_CHANGED )// 
			|| action.equals( Intent.ACTION_TIME_TICK )//
			|| action.equals( Intent.ACTION_DATE_CHANGED )//
			|| action.equals( Intent.ACTION_TIME_CHANGED )//
			|| action.equals( "android.intent.action.TIME_SET" ) )
			{
				updateWidget = true;
			}
			//天气
			else
				if( WeatherUtils.WEATHER_CLIENT_UPDATE_RESULT_ACTION.equals( action ) )
			{
				Bundle bundle = intent.getExtras();
				if( bundle.getString( "cooee.weather.updateResult" ).equals( "UPDATE_SUCCESED" ) )
				{
					Log.v( "wangjing" , "UPDATA_WHEATER_ACTION !" );
					WeatherUtils.sendRefreshWeatherBroadcast( ClockWeatherMusicService.this );
				}
			}
			else
					if( WeatherUtils.WEATHER_CLIENT_CLOSED_UPDATE_LAUNCHER_ACTION.equals( action )//
					|| WeatherUtils.WEATHER_CLIENT_REFRESH_UPDATE_LAUNCHER_ACTION.equals( action ) )
			{
				WeatherManager.getInstance( context ).receiveWeatherInfo( context , intent );
				updateWidget = true;
			}
			//音乐
			else
						if( MusicManager.PLAYSTATE_CHANGED.equals( action ) )
			{
				MusicManager.getInstance( context ).setPlayingState( context , intent );
				updateWidget = true;
			}
			else
							if( MusicManager.META_CHANGED.equals( action ) )
			{
				String cmd = intent.getStringExtra( "command" );
				long paramId = intent.getLongExtra( "id" , -1 );
				boolean playing = intent.getBooleanExtra( "playing" , false );
				Log.v( TAG , " cyk onReceive() action:" + action + " cmd:" + cmd + " paramId:" + paramId + " playing:" + playing );
				if( paramId != preParamId )
				{
					MusicManager.getInstance( context ).setSongInfo( intent );
					MusicManager.getInstance( context ).changeMusicWidgetView( intent );
					preParamId = paramId;
				}
				updateWidget = true;
			}
			if( updateWidget )
			{
				updateWidget = false;
				WidgetViewManager.getWidgetViewManager( context ).updateAllWidget();
			}
		}
	};
}
