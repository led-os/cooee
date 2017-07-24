package com.cooee.phenix.widget.timer;


// luomingjun add whole file //桌面时钟
/**
 * 时钟
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class TimeAppWidgetProvider extends AppWidgetProvider
{
	
	public static String TAG = "TimeAppWidgetProvider";
	public static TimeUpdateTask timer;
	public static boolean result = true;
	private static String default_clock_package = "";
	public static final String BT_REFRESH_ACTION = "com.android.timer.BT_REFRESH_ACTION";
	private List<String> pagList = new ArrayList<String>();
	private HashMap<String , Object> item = new HashMap<String , Object>();
	public static ScheduledThreadPoolExecutor stpe = null;
	
	// 每次删除该类型的窗口小部件(AppWidget)时都会触发 ，同时发送ACTION_APPWIDGET_DELETED广播，
	// 该广播可被onReceive()方法接受到.
	@Override
	public void onDeleted(
			Context context ,
			int[] appWidgetIds )
	{
		super.onDeleted( context , appWidgetIds );
	}
	
	// 最后删除该类型的窗口小部件(AppWidget)时触发
	@Override
	public void onDisabled(
			Context context )
	{
		if( stpe != null )
		{
			stpe.shutdown();
			stpe = null;
		}
		super.onDisabled( context );
	}
	
	// 第一次往桌面添加小部件是调用 启动服务，以后添加同类型的小部件时候不会调用
	@Override
	public void onEnabled(
			Context context )
	{
		result = false;
		if( stpe == null )
		{
			stpe = new ScheduledThreadPoolExecutor( 1 );
			timer = new TimeUpdateTask( context );
			stpe.scheduleWithFixedDelay( timer , 1 , 1 , TimeUnit.SECONDS );
		}
		super.onEnabled( context );
	}
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		String action = intent.getAction();
		if( stpe == null && action.equals( "android.appwidget.action.APPWIDGET_UPDATE" ) )
		{
			if( result )
			{
				stpe = new ScheduledThreadPoolExecutor( 1 );
				timer = new TimeUpdateTask( context );
				stpe.scheduleWithFixedDelay( timer , 1 , 1 , TimeUnit.SECONDS );
				result = false;
			}
		}
		else if( action.equals( BT_REFRESH_ACTION ) )
		{
			try
			{
				String packageName = null;
				SharedPreferences p = context.getSharedPreferences( "iLoong.Widget.Clock" , 0 );
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
						listPackages( context );
						if( pagList.size() != 0 )
						{
							packageName = pagList.get( 0 );
							editor.putString( "clock_package" , packageName );
						}
					}
					editor.commit();
				}
				PackageManager pm = context.getPackageManager();
				if( packageName != null )
				{
					Intent intent1 = pm.getLaunchIntentForPackage( packageName );
					if( intent1 != null )
					{
						intent1.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						context.startActivity( intent1 );
					}
					else
					{
						Intent i2 = new Intent( Settings.ACTION_DATE_SETTINGS );
						i2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						context.startActivity( i2 );
					}
				}
				else
				{
					Intent i2 = new Intent( Settings.ACTION_DATE_SETTINGS );
					i2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					context.startActivity( i2 );
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}// @gaominghui2015/07/02 ADD START
			//桌面清除数据后会收到这个广播,之前没有接这个广播，导致桌面重启后时间知道下一分钟才会更新
		else if( stpe == null && action.equals( "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS" ) )
		{
			stpe = new ScheduledThreadPoolExecutor( 1 );
			timer = new TimeUpdateTask( context );
			stpe.scheduleWithFixedDelay( timer , 1 , 1 , TimeUnit.SECONDS );
			result = false;
		}
		// @gaominghui2015/07/02 ADD END
		super.onReceive( context , intent );
	}
	
	// 每次添加一个该类型的窗口小部件窗口小部件(AppWidget)都会触发，同时发送ACTION_APPWIDGET_UPDATE广播
	// 一般在该函数为初始化添加的窗口小部件 , 即为它分配RemoteViews
	@Override
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
	}
	
	private void listPackages(
			Context context )
	{
		ArrayList<PInfo> apps = getInstalledApps( false , context );
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
							PackageInfo pInfo = context.getPackageManager().getPackageInfo( apps.get( i ).pname , 0 );
							if( isSystemApp( pInfo ) || isSystemUpdateApp( pInfo ) )
							{
								item.put( "pname" , apps.get( i ).pname );
								item.put( "appname" , apps.get( i ).appname );
								pagList.add( apps.get( i ).pname );
							}
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	class PInfo
	{
		
		private String appname = "";
		private String pname = "";
		private String versionName = "";
		private int versionCode = 0;
		
		private void prettyPrint()
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "taskmanger" , StringUtils.concat( appname , "\t" , pname , "\t" , versionName , "\t" , versionCode + "\t" ) );
		}
	}
	
	private ArrayList<PInfo> getInstalledApps(
			boolean getSysPackages ,
			Context context )
	{
		ArrayList<PInfo> res = new ArrayList<PInfo>();
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages( 0 );
		for( int i = 0 ; i < packs.size() ; i++ )
		{
			PackageInfo p = packs.get( i );
			if( ( !getSysPackages ) && ( p.versionName == null ) )
			{
				continue;
			}
			PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel( context.getPackageManager() ).toString();
			newInfo.pname = p.packageName;
			newInfo.versionName = p.versionName;
			newInfo.versionCode = p.versionCode;
			res.add( newInfo );
		}
		return res;
	}
	
	public boolean isSystemApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) != 0 );
	}
	
	public boolean isSystemUpdateApp(
			PackageInfo pInfo )
	{
		return( ( pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 );
	}
}
