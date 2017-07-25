package com.cooee.widgetnative.M3in1.base;


import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.widgetnative.M3in1.R;
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
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RemoteViews;


public class WidgetService extends Service
{
	
	private static final String TAG = "WidgetService";
	View ForegroundView = null;
	protected long preParamId;
	
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
		filter.addAction( WidgetProvider.PLAYSTATE_CHANGED );
		filter.addAction( WidgetProvider.META_CHANGED );
		this.registerReceiver( mIntentReceiver , filter );
		// YANGTIANYU@2016/05/11 ADD START
		// 6.0以上的手机,可能会提示关闭屏幕叠加层,6.0以上先不使用悬浮窗,有其他解决方案时可以修改【c_0004451】
		//yangmengchao add start   //push
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				KpshSdk.setAppKpshTag( WidgetService.this , WidgetService.this.getPackageName() );
				CooeeSdk.initCooeeSdk( WidgetService.this );
			}
		} ).start();
		//yangmengchao add end
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
		WindowManager.LayoutParams mForegroundParams = new WindowManager.LayoutParams();
		mForegroundParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		mForegroundParams.format = PixelFormat.RGBA_8888;
		mForegroundParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		mForegroundParams.x = 0;
		mForegroundParams.y = 0;
		mForegroundParams.width = 1;
		mForegroundParams.height = 1;
		mForegroundParams.gravity = Gravity.BOTTOM;
		wm.addView( ForegroundView , mForegroundParams );
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
		PendingIntent pendingIntent = PendingIntent.getService( this , 0 , new Intent( this , WidgetService.class ) , 0 );
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
	
	private static RemoteViews mRemoteViews;
	
	public static RemoteViews getRemoteViews()
	{
		return mRemoteViews;
	}
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			WidgetManager mWidgetManager = WidgetManager.getInstance( context );
			mRemoteViews = new RemoteViews( context.getPackageName() , R.layout.widget_layout );
			String action = intent.getAction();
			Log.d( "" , "cyk MusicWidgetService action: " + action );
			mWidgetManager.initView( mRemoteViews );
			String cmd = intent.getStringExtra( "command" );
			long paramId = intent.getLongExtra( "id" , -1 );
			boolean playing = intent.getBooleanExtra( "playing" , false );
			Log.v( "cyk" , TAG + " onReceive() action:" + action + " cmd:" + cmd + " paramId:" + paramId + " playing:" + playing );
			if( action.equals( WidgetProvider.PLAYSTATE_CHANGED ) )
			{
				mWidgetManager.setPlayState( intent , mRemoteViews );
				mWidgetManager.changeAlbumArtClickState( context , intent , mRemoteViews );
			}
			else if( action.equals( WidgetProvider.META_CHANGED ) && paramId != preParamId )
			{
				mWidgetManager.setSongInfo( intent , mRemoteViews );
				mWidgetManager.changeMusicWidgetView( intent , mRemoteViews );
				makeAlbumArtClickStateBetrue( context );
				preParamId = paramId;
			}
			mWidgetManager.updateAppWidget( mRemoteViews );
		}
		
		private void makeAlbumArtClickStateBetrue(
				Context context )
		{
			Intent intentTrue = new Intent();
			intentTrue.putExtra( "broacastIsMetaChanged" , true );
			WidgetManager.getInstance( context ).changeAlbumArtClickState( context , intentTrue , mRemoteViews );
		}
	};
}
