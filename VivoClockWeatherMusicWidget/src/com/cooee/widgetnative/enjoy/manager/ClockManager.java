package com.cooee.widgetnative.enjoy.manager;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.cooee.widgetnative.enjoy.R;
import com.cooee.widgetnative.enjoy.WidgetProvider;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class ClockManager
{
	
	private static ClockManager mClockManager;
	public static final String TAG = "ClockManager";
	private Context mContext;
	/**时钟配置*/
	private boolean showClockVeiw;
	private String default_clock_package = null;
	private HashMap<String , Object> item = new HashMap<String , Object>();
	private List<String> pagList = new ArrayList<String>();
	private int mCurrentHour;
	private int mCurrentMinute;
	
	private ClockManager(
			Context context )
	{
		mContext = context;
		initConfig();
	}
	
	public static ClockManager getInstance(
			Context mContext )
	{
		if( mClockManager == null )
		{
			synchronized( TAG )
			{
				if( mClockManager == null )
				{
					mClockManager = new ClockManager( mContext );
				}
			}
		}
		return mClockManager;
	}
	
	private void initConfig()
	{
		//时钟配置
		showClockVeiw = mContext.getResources().getBoolean( R.bool.show_clockView );
		if( showClockVeiw )
		{
			default_clock_package = mContext.getResources().getString( R.string.default_clock_package );
		}
	}
	
	public void initClickView(
			RemoteViews remoteview )
	{
		if( remoteview != null )
		{
			int visibility = View.GONE;
			if( showClockVeiw )
			{
				visibility = View.VISIBLE;
				Intent intentClockClick = new Intent( WidgetProvider.CLICK_CLOCK_LAYOUT );
				PendingIntent pendingClockIntent = PendingIntent.getBroadcast( mContext , 0 , intentClockClick , 0 );
				remoteview.setOnClickPendingIntent( R.id.clock_layout , pendingClockIntent );
			}
			remoteview.setViewVisibility( R.id.clock_layout , visibility );
		}
	}
	
	private int[] timeNumbers = {
			R.drawable.clock_number_0 ,
			R.drawable.clock_number_1 ,
			R.drawable.clock_number_2 ,
			R.drawable.clock_number_3 ,
			R.drawable.clock_number_4 ,
			R.drawable.clock_number_5 ,
			R.drawable.clock_number_6 ,
			R.drawable.clock_number_7 ,
			R.drawable.clock_number_8 ,
			R.drawable.clock_number_9 ,
			R.drawable.clock_number_10 };
			
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
	
	/**点击时钟*/
	public void onClick()
	{
		Log.d( TAG , "cyk onClickClockLayout " );
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
			Log.d( TAG , "cyk onClickClockLayout packageName: " + packageName );
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
	
	private void clockTimeChanged()
	{
		Calendar mCalendar = Calendar.getInstance();
		long milliseconds = System.currentTimeMillis();
		mCalendar.setTimeInMillis( milliseconds );
		/**
		 * @author WangJing
		 */
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
	}
	
	private void updateClockView(
			RemoteViews remoteview )
	{
		//timeNumbers
		if( remoteview != null )
		{
			int horuTensId = 0;
			if( mCurrentHour / 10 == 1 )
			{
				horuTensId = timeNumbers[10];
			}
			else
			{
				horuTensId = timeNumbers[mCurrentHour / 10];
			}
			remoteview.setImageViewResource( R.id.clock_hour_tens , horuTensId );
			remoteview.setImageViewResource( R.id.clock_hour_ones , timeNumbers[mCurrentHour % 10] );
			remoteview.setImageViewResource( R.id.clock_minute_tens , timeNumbers[mCurrentMinute / 10] );
			remoteview.setImageViewResource( R.id.clock_minute_ones , timeNumbers[mCurrentMinute % 10] );
		}
	}
	
	public void updateAllWidget(
			RemoteViews remoteview )
	{
		if( showClockVeiw )
		{
			Log.d( TAG , "cyk updateAllWidget " );
			clockTimeChanged();
			Log.d( TAG , "cyk updateAllWidget mCurrentHour: " + mCurrentHour + " mCurrentMinute: " + mCurrentMinute );
			updateClockView( remoteview );
		}
	}
	
	public boolean isShowClockVeiw()
	{
		return showClockVeiw;
	}
}
