package com.cooee.widgetnative.tango.manager;


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

import com.cooee.widgetnative.tango.R;


public class ClockCalendarManager
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
	private int mCurrentWeekDay = 0;
	private String mWeekDay = "";
	private boolean isChinese = true;
	/***
	 * 是否截取week
	 * ***/
	private boolean isSubWeek = false;
	/**日期字符串*/
	String dateNumber;
	private String default_clock_package = null;
	private static ClockCalendarManager mClockCalendarManager;
	private static final String TAG = "TwinkleClockwidgetManager";
	private int[] timeNumbers = {
			R.drawable.time_0 ,
			R.drawable.time_1 ,
			R.drawable.time_2 ,
			R.drawable.time_3 ,
			R.drawable.time_4 ,
			R.drawable.time_5 ,
			R.drawable.time_6 ,
			R.drawable.time_7 ,
			R.drawable.time_8 ,
			R.drawable.time_9 };
	
	private ClockCalendarManager(
			Context context )
	{
		this.mContext = context;
		initConfigData();
	}
	
	public static ClockCalendarManager getInstance(
			Context context )
	{
		if( mClockCalendarManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mClockCalendarManager == null && context != null )
				{
					mClockCalendarManager = new ClockCalendarManager( context );
				}
			}
		}
		return mClockCalendarManager;
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
		TwinkleClockwidgetManager instance = TwinkleClockwidgetManager.getInstance( mContext );
		RemoteViews rv = instance.getRv();
		if( instance.showClockVeiw )
		{
			rv.setImageViewResource( R.id.clock_hour_tens , timeNumbers[mHour / 10] );
			rv.setImageViewResource( R.id.clock_hour_ones , timeNumbers[mHour % 10] );
		}
	}
	
	private void UpdateMinuteView()
	{
		TwinkleClockwidgetManager instance = TwinkleClockwidgetManager.getInstance( mContext );
		RemoteViews rv = instance.getRv();
		if( instance.showClockVeiw )
		{
			rv.setImageViewResource( R.id.clock_minute_tens , timeNumbers[mMinute / 10] );
			rv.setImageViewResource( R.id.clock_minute_ones , timeNumbers[mMinute % 10] );
		}
	}
	
	/**
	 * @see com.cooee.phenix.widget.weather_clock.timer.ClockTimerListener#clockTimeChanged()
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
		mCurrentWeekDay = mCalendar.get( Calendar.DAY_OF_WEEK );
	}
	
	public void updateClockView()
	{
		mYear = mCurrentYear;
		mMonth = mCurrentMonth;
		mDay = mCurrentDay;
		UpdateDateView();
		updateWeekDay();
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
				dateNumber = mDay + StringMonth( mContext );
			}
		}
		Log.d( TAG , "cyk UpdateDateView dateNumber: " + dateNumber );
		TwinkleClockwidgetManager instance = TwinkleClockwidgetManager.getInstance( mContext );
		RemoteViews rv = instance.getRv();
		if( instance.showClockVeiw )
		{
			rv.setTextViewText( R.id.date_textview , dateNumber );
		}
	}
	
	private void updateWeekDay()
	{
		if( isChinese )
		{
			switch( mCurrentWeekDay )
			{
				case 1:
					mWeekDay = "周日";
					break;
				case 2:
					mWeekDay = "周一";
					break;
				case 3:
					mWeekDay = "周二";
					break;
				case 4:
					mWeekDay = "周三";
					break;
				case 5:
					mWeekDay = "周四";
					break;
				case 6:
					mWeekDay = "周五";
					break;
				case 7:
					mWeekDay = "周六";
					break;
				default:
					break;
			}
		}
		else
		{
			switch( mCurrentWeekDay )
			{
				case 1:
					mWeekDay = "Sunday";
					break;
				case 2:
					mWeekDay = "Monday";
					break;
				case 3:
					mWeekDay = "Tuesday";
					break;
				case 4:
					mWeekDay = "Wednesday";
					break;
				case 5:
					mWeekDay = "Thursday";
					break;
				case 6:
					mWeekDay = "Friday";
					break;
				case 7:
					mWeekDay = "Saturday";
					break;
				default:
					break;
			}
		}
		Log.d( TAG , "cyk mcurrentweekday = " + mCurrentWeekDay + "mweekday =" + mWeekDay );
		TwinkleClockwidgetManager instance = TwinkleClockwidgetManager.getInstance( mContext );
		RemoteViews rv = instance.getRv();
		if( instance.showClockVeiw )
		{
			rv.setTextViewText( R.id.weekday_textview , mWeekDay );
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
				month = appContext.getResources().getString( R.string.month_January );
				break;
			case 2:
				month = appContext.getResources().getString( R.string.month_February );
				break;
			case 3:
				month = appContext.getResources().getString( R.string.month_March );
				break;
			case 4:
				month = appContext.getResources().getString( R.string.month_April );
				break;
			case 5:
				month = appContext.getResources().getString( R.string.month_May );
				break;
			case 6:
				month = appContext.getResources().getString( R.string.month_June );
				break;
			case 7:
				month = appContext.getResources().getString( R.string.month_July );
				break;
			case 8:
				month = appContext.getResources().getString( R.string.month_August );
				break;
			case 9:
				month = appContext.getResources().getString( R.string.month_September );
				break;
			case 10:
				month = appContext.getResources().getString( R.string.month_October );
				break;
			case 11:
				month = appContext.getResources().getString( R.string.month_November );
				break;
			case 12:
				month = appContext.getResources().getString( R.string.month_December );
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
