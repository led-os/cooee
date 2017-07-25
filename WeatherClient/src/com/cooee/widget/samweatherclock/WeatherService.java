package com.cooee.widget.samweatherclock;


import com.cooee.app.cooeeweather.filehelp.Log;

import android.app.AlarmManager;
import android.app.Notification;
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
		Log.v( TAG , "registerReceiver action " + Intent.ACTION_TIME_TICK );
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( "android.intent.action.TIME_SET" );
		this.registerReceiver( mIntentReceiver , filter );
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
		//restartByAlarmManager(intent);
		return START_REDELIVER_INTENT;
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			Log.v( TAG , "action = " + intent.getAction() );
			if( ( intent.getAction().equals( Intent.ACTION_TIMEZONE_CHANGED ) ) || ( intent.getAction().equals( Intent.ACTION_TIME_TICK ) ) || ( intent.getAction().equals( Intent.ACTION_DATE_CHANGED ) ) || ( intent
					.getAction().equals( Intent.ACTION_TIME_CHANGED ) ) || ( intent.getAction().equals( "android.intent.action.TIME_SET" ) ) )
			{
				WeatherProvider.updateAllWidget( context );
			}
		}
	};
}
