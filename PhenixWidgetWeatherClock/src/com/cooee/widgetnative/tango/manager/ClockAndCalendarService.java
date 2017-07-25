package com.cooee.widgetnative.tango.manager;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.cooee.widgetnative.tango.utils.ConfigUtils;


public class ClockAndCalendarService extends Service
{
	
	private static final String TAG = "ClockAndCalendarService";
	View ForegroundView = null;
	
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
		Log.v( TAG , "cyk registerReceiver action " + Intent.ACTION_TIME_TICK );
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( "android.intent.action.TIME_SET" );
		this.registerReceiver( mIntentReceiver , filter );
		// YANGTIANYU@2016/05/11 ADD START
		// 6.0以上的手机,可能会提示关闭屏幕叠加层,6.0以上先不使用悬浮窗,有其他解决方案时可以修改【c_0004451】
		if( VERSION.SDK_INT < 23 )
			addForegroundView();
		// YANGTIANYU@2016/05/11 ADD END
		ConfigUtils.getInstance( getApplicationContext() ).initStatistics();
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
		PendingIntent pendingIntent = PendingIntent.getService( this , 0 , new Intent( this , ClockAndCalendarService.class ) , 0 );
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
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			Log.v( TAG , "action = " + intent.getAction() );
			if( intent.getAction().equals( Intent.ACTION_TIMEZONE_CHANGED )//
					|| intent.getAction().equals( Intent.ACTION_TIME_TICK )//
					|| intent.getAction().equals( Intent.ACTION_DATE_CHANGED )//
					|| intent.getAction().equals( Intent.ACTION_TIME_CHANGED )//
					|| intent.getAction().equals( "android.intent.action.TIME_SET" )//
			)
			{
				ClockCalendarManager.getInstance( context ).clockTimeChanged();
				TwinkleClockwidgetManager.getInstance( context ).updateAppWidget();
			}
		}
	};
}
