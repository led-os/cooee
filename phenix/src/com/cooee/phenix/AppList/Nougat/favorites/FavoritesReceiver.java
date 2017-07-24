package com.cooee.phenix.AppList.Nougat.favorites;


import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;



public class FavoritesReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "FavoritesReceiver";
	private Context mContext;
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
	private static int mFlag = -1;
	private Handler mTipsHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			super.handleMessage( msg );
			//cheyingkun add start	//解决“通话界面手机电流增高”的问题【c_0004610】
			//【问题原因】获取顶应用导致手机电流增高200mA
			//【解决方案】用常用应用开关把相关代码包起来(之前的开关只控制在了界面显示部分,后续优化功能相关代码全部用开关包起来)
			//cheyingkun add end
			switch( msg.what )
			{
				case MESSAGE_SERVICE_START:
					//cheyingkun add start	//解决“通话界面手机电流增高”的问题【c_0004610】
					if( LauncherDefaultConfig.SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS )
					//cheyingkun add end
					{
						if( ( Build.VERSION.SDK_INT < 21 || isSystemApp( mContext ) ) )
						{
							Log.d( TAG , "handle start MonitorThread" );
							// ，5.0以下通过服务，每隔一秒获取最上层activity，来更新常用应用，但5.0以上需要系统权限，才能获取到系统最上层activity，因此通过点击来更新常用应用
							new MonitorThread( mContext ).start();
						}
					}
					break;
				case MESSAGE_SERVICE_STOP:
					if( LauncherDefaultConfig.SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS )
					{
						if( mContext != null )
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
			Context mApp )
	{
		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get( Calendar.YEAR );
		mMonth = calendar.get( Calendar.MONTH );
		mWeek = calendar.get( Calendar.WEEK_OF_YEAR );
		mDay = calendar.get( Calendar.DAY_OF_MONTH );
		this.mContext = mApp;
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
			FavoritesAppManager.getInstance().dayDecrease();
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
			timeChanged();
		}
		if( Intent.ACTION_SCREEN_ON.equals( action ) )
		{
			isScreenOff = false;
			if( mContext != null )
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
			if( mContext != null )
			{
				mTipsHandler.removeMessages( MESSAGE_SERVICE_START );
				mTipsHandler.removeMessages( MESSAGE_SERVICE_STOP );
				mTipsHandler.sendEmptyMessageDelayed( MESSAGE_SERVICE_STOP , 3000 );
			}
		}

	}
	
	public static boolean isSystemApp(
			Context context )
	{
		if( mFlag == -1 )
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = null;
			try
			{
				packageInfo = packageManager.getPackageInfo( context.getPackageName() , 0 );
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( packageInfo != null )
			{
				mFlag = packageInfo.applicationInfo.flags;
			}
		}
		if( ( mFlag & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0 || ( mFlag & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
		{
			return true;
		}
		return false;
	}
	
}
