package com.cooee.widgetnative.CW3in1.ClockWeather;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import com.cooee.widgetnative.CW3in1.R;
import com.cooee.widgetnative.CW3in1.base.WidgetManager;


public class ClockManager
{
	
	private Context mContext = null;
	private int mHour = -1;
	private int mCurrentHour = 0;
	private int mMinute = -1;
	private int mCurrentMinute = 0;
	private int mYear = -1;
	private int mCurrentYear = 0;
	private int mMonth = -1;
	private int mCurrentMonth = 0;
	private int mDay = -1;
	private int mCurrentDay = 0;
	private boolean isChinese = true;
	/***
	 * 是否截取week
	 * ***/
	private boolean isSubWeek = false;
	/**日期字符串*/
	String dateNumber;
	private String default_clock_package = null;
	private static ClockManager mClockManager;
	private static final String TAG = "ClockManager";
	private int[] timeNumbers = {
			R.drawable.clock_time_0 ,
			R.drawable.clock_time_1 ,
			R.drawable.clock_time_2 ,
			R.drawable.clock_time_3 ,
			R.drawable.clock_time_4 ,
			R.drawable.clock_time_5 ,
			R.drawable.clock_time_6 ,
			R.drawable.clock_time_7 ,
			R.drawable.clock_time_8 ,
			R.drawable.clock_time_9 };
	
	private ClockManager(
			Context context )
	{
		this.mContext = context;
		initConfigData();
	}
	
	public static ClockManager getInstance(
			Context context )
	{
		if( mClockManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mClockManager == null && context != null )
				{
					mClockManager = new ClockManager( context );
				}
			}
		}
		return mClockManager;
	}
	
	private void initConfigData()
	{
		/**
		 * 是否截取日期
		 * */
		isSubWeek = mContext.getResources().getBoolean( R.bool.sub_week );
		default_clock_package = mContext.getResources().getString( R.string.default_clock_package ).trim();
	}
	
	private void UpdateHourView()
	{
		WidgetManager mWidgetManager = WidgetManager.getInstance( mContext );
		RemoteViews mRemoteViews = mWidgetManager.getRemoteViews();
		if( mWidgetManager.showClockVeiw )
		{
			mRemoteViews.setImageViewResource( R.id.clock_hour_tens , timeNumbers[mHour / 10] );
			mRemoteViews.setImageViewResource( R.id.clock_hour_ones , timeNumbers[mHour % 10] );
		}
	}
	
	private void UpdateMinuteView()
	{
		WidgetManager mWidgetManager = WidgetManager.getInstance( mContext );
		RemoteViews mRemoteViews = mWidgetManager.getRemoteViews();
		if( mWidgetManager.showClockVeiw )
		{
			mRemoteViews.setImageViewResource( R.id.clock_minute_tens , timeNumbers[mMinute / 10] );
			mRemoteViews.setImageViewResource( R.id.clock_minute_ones , timeNumbers[mMinute % 10] );
		}
	}
	
	/**
	 * @see com.cooee.widgetnative.CW3in1.timer.ClockTimerListener#clockTimeChanged()
	 * @auther gaominghui  2014年12月31日
	 */
	public void clockTimeChanged()
	{
		// TODO Auto-generated method stub
		Calendar mCalendar = Calendar.getInstance();
		long milliseconds = System.currentTimeMillis();
		mCalendar.setTimeInMillis( milliseconds );
		if( DateFormat.is24HourFormat( mContext ) )
		{
			mCurrentHour = mCalendar.get( Calendar.HOUR_OF_DAY );
		}
		else
		{
			mCurrentHour = mCalendar.get( Calendar.HOUR );
			mCurrentHour = mCurrentHour == 0 ? 12 : mCurrentHour;
		}
		mCurrentMinute = mCalendar.get( Calendar.MINUTE );
		mCurrentYear = mCalendar.get( Calendar.YEAR );
		mCurrentMonth = mCalendar.get( Calendar.MONTH ) + 1;
		mCurrentDay = mCalendar.get( Calendar.DAY_OF_MONTH );
	}
	
	public void updateClockView()
	{
		mYear = mCurrentYear;
		mMonth = mCurrentMonth;
		mDay = mCurrentDay;
		UpdateDateView();
		mHour = mCurrentHour;
		UpdateHourView();
		mMinute = mCurrentMinute;
		UpdateMinuteView();
		Log.d( TAG , "cyk clockTimeChanged mHour: " + mHour + " mMinute: " + mMinute );
	}
	
	private void UpdateDateView()
	{
		if( isChinese )
		{
			if( mDay < 10 )
			{
				dateNumber = StringMonth( mContext ) + "0" + mDay + "日  ";
			}
			else
			{
				dateNumber = StringMonth( mContext ) + mDay + "日";
			}
		}
		else
		{
			/* 日期/月份格式  mMonth 月*/
			if( isSubWeek )
			{
				if( mDay < 10 )
				{
					dateNumber = "0" + mDay + "/" + mMonth;
				}
				else
				{
					dateNumber = mDay + "/" + mMonth;
				}
			}
			else
			{
				dateNumber = StringMonth( mContext ) + mDay;
			}
		}
		Log.d( TAG , "cyk UpdateDateView dateNumber: " + dateNumber );
		WidgetManager mWidgetManager = WidgetManager.getInstance( mContext );
		RemoteViews mRemoteViews = mWidgetManager.getRemoteViews();
		if( mWidgetManager.showClockVeiw )
		{
			mRemoteViews.setTextViewText( R.id.date_textview , dateNumber );
		}
	}
	
	/**
	 * 
	 *转换日期显示，主要为了切换语言时使用
	 * @param appContext
	 * @return
	 * @author gaominghui 2015年1月7日
	 */
	private String StringMonth(
			Context appContext )
	{
		String month = " ";
		switch( mMonth )
		{
			case 1:
				month = appContext.getResources().getString( R.string.clock_month_January );
				break;
			case 2:
				month = appContext.getResources().getString( R.string.clock_month_February );
				break;
			case 3:
				month = appContext.getResources().getString( R.string.clock_month_March );
				break;
			case 4:
				month = appContext.getResources().getString( R.string.clock_month_April );
				break;
			case 5:
				month = appContext.getResources().getString( R.string.clock_month_May );
				break;
			case 6:
				month = appContext.getResources().getString( R.string.clock_month_June );
				break;
			case 7:
				month = appContext.getResources().getString( R.string.clock_month_July );
				break;
			case 8:
				month = appContext.getResources().getString( R.string.clock_month_August );
				break;
			case 9:
				month = appContext.getResources().getString( R.string.clock_month_September );
				break;
			case 10:
				month = appContext.getResources().getString( R.string.clock_month_October );
				break;
			case 11:
				month = appContext.getResources().getString( R.string.clock_month_November );
				break;
			case 12:
				month = appContext.getResources().getString( R.string.clock_month_December );
				break;
		}
		return month;
	}
	
	/**
	 * 判断当前系统语言
	 *
	 * @return true中午，false其他语言，默认统一做英文处理
	 * @author gaominghui 2015年1月7日
	 */
	public boolean isLanguage()
	{
		String country = mContext.getResources().getConfiguration().locale.getCountry();
		if( country.equals( "CN" ) || country.equals( "TW" ) || country.equals( "HK" ) )
		{
			isChinese = true;
		}
		else
		{
			isChinese = false;
		}
		return isChinese;
	}
	
	private HashMap<String , Object> item = new HashMap<String , Object>();
	private List<String> pagList = new ArrayList<String>();
	
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
			boolean getSysPackages )
	{
		ArrayList<PInfo> res = new ArrayList<PInfo>();
		List<PackageInfo> packs = mContext.getPackageManager().getInstalledPackages( 0 );
		for( int i = 0 ; i < packs.size() ; i++ )
		{
			PackageInfo p = packs.get( i );
			if( ( !getSysPackages ) && ( p.versionName == null ) )
			{
				continue;
			}
			PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel( mContext.getPackageManager() ).toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			res.add( newInfo );
		}
		return res;
	}
	
	private boolean isSystemApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) != 0 );
	}
	
	private boolean isSystemUpdateApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 );
	}
	
	private void listPackages()
	{
		ArrayList<PInfo> apps = getInstalledApps( false );
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
							PackageInfo pInfo = mContext.getPackageManager().getPackageInfo( apps.get( i ).pname , 0 );
							if( isSystemApp( pInfo ) || isSystemUpdateApp( pInfo ) )
							{
								item.put( "pname" , apps.get( i ).pname );
								item.put( "appname" , apps.get( i ).appname );
								pagList.add( apps.get( i ).pname );
							}
						}
						catch( Exception e )
						{
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void onClickClock()
	{
		Log.d( TAG , "cyk 点击时钟布局" );
		try
		{
			String packageName = null;
			SharedPreferences p = mContext.getSharedPreferences( "iLoong.Widget.Clock" , 0 );
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
					listPackages();
					if( pagList.size() != 0 )
					{
						packageName = pagList.get( 0 );
						editor.putString( "clock_package" , packageName );
					}
				}
				editor.commit();
			}
			PackageManager pm = mContext.getPackageManager();
			if( packageName != null )
			{
				Intent intent = pm.getLaunchIntentForPackage( packageName );
				if( intent != null )
				{
					intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					mContext.startActivity( intent );
				}
				else
				{
					Intent i2 = new Intent( Settings.ACTION_DATE_SETTINGS );
					i2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					mContext.startActivity( i2 );
				}
			}
			else
			{
				Intent i2 = new Intent( Settings.ACTION_DATE_SETTINGS );
				i2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				mContext.startActivity( i2 );
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings( "deprecation" )
	public void onclickDate()
	{
		Log.d( TAG , "cyk 点击日期" );
		try
		{
			Intent i = new Intent();
			ComponentName cn = null;
			if( Integer.parseInt( Build.VERSION.SDK ) >= 8 )
			{
				cn = new ComponentName( "com.android.calendar" , "com.android.calendar.LaunchActivity" );
			}
			else
			{
				cn = new ComponentName( "com.google.android.calendar" , "com.android.calendar.LaunchActivity" );
			}
			i.setComponent( cn );
			i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
			mContext.startActivity( i );
		}
		catch( ActivityNotFoundException e )
		{
			// TODO: handle exception
			Log.e( "ActivityNotFoundException" , e.toString() );
		}
	}
}
