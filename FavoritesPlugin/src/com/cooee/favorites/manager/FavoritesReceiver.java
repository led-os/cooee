package com.cooee.favorites.manager;


import java.lang.reflect.Method;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.ad.nearby.KmobAdMessage;
import com.cooee.favorites.apps.FavoritesAppData;
import com.cooee.favorites.apps.MonitorThread;
import com.cooee.favorites.news.NewsView;
import com.cooee.favorites.utils.NetworkAvailableUtils;
import com.cooee.uniex.wrap.FavoritesConfig;

import cool.sdk.FavoriteControl.FavoriteControlHelper;


public class FavoritesReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "FavoritesReceiver";
	private Context mApp;
	//常用应用和常用联系人（新功能） hp@2015/10/08 ADD START
	private int mYear;
	private int mMonth;
	private int mWeek;
	private int mDay;
	public static boolean isScreenOff = false;
	//常用应用和常用联系人（新功能） hp@2015/10/08 ADD END
	//常用应用和常用联系人（新功能） hp@2015/10/08 ADD START
	private static final int MESSAGE_SERVICE_START = 0;
	private static final int MESSAGE_SERVICE_STOP = 1;
	private static final int MESSAGE_DELAY_TIME = 30000;//30s
	public static long AD_GET_DELAY_TIME = 24 * 60 * 60 * 1000;//24h  毫秒单位 
	private Handler mTipsHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			super.handleMessage( msg );
			//cheyingkun add start	//解决“通话界面手机电流增高”的问题【c_0004610】
			//【问题原因】获取顶应用导致手机电流增高200mA
			//【解决方案】用常用应用开关把相关代码包起来(之前的开关只控制在了界面显示部分,后续优化功能相关代码全部用开关包起来)
			FavoritesConfig config = FavoritesManager.getInstance().getConfig();
			//cheyingkun add end
			switch( msg.what )
			{
				case MESSAGE_SERVICE_START:
					//cheyingkun add start	//解决“通话界面手机电流增高”的问题【c_0004610】
					if( config != null && config.getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
					//cheyingkun add end
					{
						if( mApp != null && ( Build.VERSION.SDK_INT < 21 || FavoritesManager.getInstance().isSystemApp( mApp ) ) )
						{
							Log.d( TAG , "handle start MonitorThread" );
							// ，5.0以下通过服务，每隔一秒获取最上层activity，来更新常用应用，但5.0以上需要系统权限，才能获取到系统最上层activity，因此通过点击来更新常用应用
							new MonitorThread( mApp ).start();
						}
					}
					break;
				case MESSAGE_SERVICE_STOP:
					//cheyingkun add start	//解决“通话界面手机电流增高”的问题【c_0004610】
					if( config != null && config.getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
					//cheyingkun add end
					{
						if( mApp != null )
						{
							Log.d( TAG , "handle stop MonitorThread" );
							MonitorThread.setRun( false );
						}
					}
					break;
			}
		}
	};
	
	public FavoritesReceiver(
			Context mApp ,
			NewsView newsView )
	{
		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get( Calendar.YEAR );
		mMonth = calendar.get( Calendar.MONTH );
		mWeek = calendar.get( Calendar.WEEK_OF_YEAR );
		mDay = calendar.get( Calendar.DAY_OF_MONTH );
		this.mApp = mApp;
	}
	
	/**
	 * 时间改变
	 * @author 常用应用和常用联系人（新功能） hp@2015/10/08 ADD START
	 */
	private void timeChanged()
	{
		boolean isChanged = false;
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get( Calendar.YEAR );
		int month = calendar.get( Calendar.MONTH );
		int week = calendar.get( Calendar.WEEK_OF_YEAR );
		int day = calendar.get( Calendar.DAY_OF_MONTH );
		if( mYear != year || mMonth != month || mWeek != week || mDay != day )
		{
			//一周一周的衰减
			if( mWeek != week )
			{
			}
			mYear = year;
			mMonth = month;
			mWeek = week;
			mDay = day;
			isChanged = true;
			MonitorThread.isDataChanged = true;
		}
		if( isChanged )
		{
			FavoritesAppData.dayDecrease();
			if( !isScreenOff )
			{
				mTipsHandler.removeMessages( MESSAGE_SERVICE_START );
				mTipsHandler.removeMessages( MESSAGE_SERVICE_STOP );
				mTipsHandler.sendEmptyMessageDelayed( MESSAGE_SERVICE_STOP , 0 );
				mTipsHandler.sendEmptyMessageDelayed( MESSAGE_SERVICE_START , MESSAGE_DELAY_TIME );
			}
		}
	}
	
	@Override
	public void onReceive(
			final Context context ,
			Intent intent )
	{
		String action = intent.getAction();
		if( action.equals( Intent.ACTION_DATE_CHANGED ) || Intent.ACTION_TIME_TICK.equals( action ) || Intent.ACTION_TIME_CHANGED.equals( action ) || Intent.ACTION_TIMEZONE_CHANGED.equals( action ) )
		{
			if( mApp != null )
			{
				timeChanged();
			}
			if( checkAdUpdate() )
			{
				Log.v( "lvjiangbin" , "reloadAndBindFavoritesAd" );
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( FavoritesManager.getInstance().getContainerContext() );
				Editor editor = mSharedPreferences.edit();
				editor.putLong( KmobAdMessage.AD_GET_LAST_TIME_KEY , System.currentTimeMillis() );
				editor.commit();
				FavoritesManager.getInstance().reloadAndBindFavoritesAd();
			}
		}
		else if( Intent.ACTION_SCREEN_ON.equals( action ) )
		{
			isScreenOff = false;
			if( mApp != null )
			{
				mTipsHandler.removeMessages( MESSAGE_SERVICE_START );
				mTipsHandler.removeMessages( MESSAGE_SERVICE_STOP );
				mTipsHandler.sendEmptyMessageDelayed( MESSAGE_SERVICE_STOP , 0 );
				mTipsHandler.sendEmptyMessageDelayed( MESSAGE_SERVICE_START , MESSAGE_DELAY_TIME );
			}
		}
		else if( Intent.ACTION_SCREEN_OFF.equals( action ) )
		{
			isScreenOff = true;
			if( mApp != null )
			{
				mTipsHandler.removeMessages( MESSAGE_SERVICE_START );
				mTipsHandler.removeMessages( MESSAGE_SERVICE_STOP );
				mTipsHandler.sendEmptyMessageDelayed( MESSAGE_SERVICE_STOP , 3000 );
			}
		}
		else if( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) || action.equals( WifiManager.NETWORK_STATE_CHANGED_ACTION ) || action.equals( WifiManager.WIFI_STATE_CHANGED_ACTION ) || action
				.equals( Intent.ACTION_TIMEZONE_CHANGED ) )
		{
			Log.v( "news" , "action = " + action );
			AsyncTask.execute( new Runnable() {
				
				@Override
				public void run()
				{
					int type = NetworkAvailableUtils.getNetworkType( context );
					FavoritesManager.getInstance().newsNetworkChanged( type );
				}
			} );
		}
		updateFavoriteSwitch( context , intent );
	}
	
	public void updateFavoriteSwitch(
			final Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		try
		{
			String action = intent.getAction();
			if( action.equals( Intent.ACTION_DATE_CHANGED ) || action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) || action.equals( "android.intent.action.PHONE_STATE" ) || action
					.equals( "android.intent.action.USER_PRESENT" ) || action.equals( "android.provider.Telephony.SMS_RECEIVED" ) )
			{
				Log.v( "COOL" , "" + action );
				if( !allowUpdate( context ) )
				{
					Log.v( "COOL" , "can't allow update" + action );
					return;
				}
				new Thread() {
					
					public void run()
					{
						try
						{
							FavoriteControlHelper helper = FavoriteControlHelper.getInstance( context );
							helper.UpdateSync( false );
						}
						catch( Exception e )
						{
						}
					};
				}.start();
			}
		}
		catch( Exception e )
		{
		}
	}
	
	public static boolean allowUpdate(
			Context context )
	{
		boolean isNeedShowDisclaimer = false;
		try
		{
			Class<?> cls = Class.forName( "com.iLoong.launcher.desktop.Disclaimer" );
			Method method = cls.getMethod( "isNeedShowDisclaimer" );
			isNeedShowDisclaimer = (Boolean)method.invoke( cls );
		}
		catch( Throwable t )
		{
			t.getStackTrace();
			isNeedShowDisclaimer = false;
			Log.v( "COOL" , "allowUpdate class or method NotFoundException" );
		}
		return !isNeedShowDisclaimer;
	}
	
	private boolean checkAdUpdate()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( FavoritesManager.getInstance().getContainerContext() );
		if( mSharedPreferences.getBoolean( KmobAdMessage.AD_GET_FIRST , true ) )
		{
			return false;
		}
		long delayTime = mSharedPreferences.getLong( KmobAdMessage.AD_GET_DELAY_TIME_KEY , AD_GET_DELAY_TIME );
		long time = System.currentTimeMillis() - mSharedPreferences.getLong( KmobAdMessage.AD_GET_LAST_TIME_KEY , System.currentTimeMillis() );
		return time > delayTime;
	}
}
