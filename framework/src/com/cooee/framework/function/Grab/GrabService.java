package com.cooee.framework.function.Grab;


import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DefaultLauncher.DefaultLauncherActivity;
import com.cooee.framework.utils.StringUtils;
import com.cooee.shell.sdk.CooeeSdk;

import cool.sdk.statistics.StatisticsUpdate;


public class GrabService extends Service
{
	
	private static final String TAG = "GrabLauncher";
	private static boolean isStart = false;
	public static final int maxRunDayBuindIn = 90;//内置多少天内抢占
	public static final int maxRunDayNormal = 10;//非内置多少天内抢占
	public static final int oncePerHours = 3;//多少小时抢占一次
	public static final int grabSecondsOnBoot = 90;//开机多少秒内抢占
	public static final int sleepGapTicks = 100;//sleep多少毫秒
	public static final int maxGapTicks = 3000;//总多少毫秒
	//zhangzhiyuan add for [grab] start
	public static boolean switch_enable_grab_service_on_bootup = true;
	public static boolean switch_enable_grab_service_on_unlock = true;
	
	//zhangzhiyuan add for [grab] end
	@Override
	public synchronized int onStartCommand(
			Intent intent ,
			int flags ,
			int startId )
	{
		// TODO Auto-generated method stub
		int flag;
		if( SystemClock.elapsedRealtime() < GrabService.grabSecondsOnBoot * 1000 )
		{
			flag = 1;
		}
		else
		{
			flag = 0;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "flag:" , flag ) );
		final Context context = this;
		switch( flag )
		{
			case 0://正常触发
				if( !switch_enable_grab_service_on_unlock )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "flag=0--return 1" );
					break;
				}
				if( isStart )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "flag=0--return 2" );
					break;
				}
				isStart = true;
				new Thread() {
					
					public void run()
					{
						try
						{
							long totalRunDay = CooeeSdk.cooeeGetTotalRunTime() / 1000 / 60 / 60 / 24;
							long maxRunDay;
							if( ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM ) != 0 )
							{
								maxRunDay = maxRunDayBuindIn;
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.i( TAG , StringUtils.concat( "is buildin:" , maxRunDay ) );
							}
							else
							{
								maxRunDay = maxRunDayNormal;
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.i( TAG , StringUtils.concat( "not buildin:" , maxRunDay ) );
							}
							if( totalRunDay > maxRunDay )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.i( TAG , StringUtils.concat( "totalRunDay > " , maxRunDay ) );
								return;
							}
							if( !GrabService.isMylauncherNeedShow( context ) )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( TAG , "flag=0--return 3" );
								return;
							}
							ComponentName defaultLauncher = getDefaultLauncher( context );
							if( defaultLauncher != null )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.i( TAG , "defaultLauncher is not null !" );
								clearPreDefaultSetting( context , defaultLauncher.getPackageName() );
								//return;
							}
							StatisticsUpdate db = StatisticsUpdate.getInstance( context );
							Long lastShowMinutes = db.getLong( "SetMyLauncher_lastShowMinutes" , 0L );
							Long curMinutes = System.currentTimeMillis() / ( 60 * 1000 );
							Long gapMinutes = Math.abs( curMinutes - lastShowMinutes );
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , StringUtils.concat( "curMinutes:" , curMinutes , "-lastShowMinutes:" , lastShowMinutes , "-gapMinutes:" , gapMinutes ) );
							if( gapMinutes < oncePerHours * 60 )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.i( TAG , "gapMinutes < 3 hours" );
								return;
							}
							db.setValue( "SetMyLauncher_lastShowMinutes" , curMinutes );
							//		ComponentName defaultLauncher = BootProtectionService.getDefaultLauncher( context );
							//		if( defaultLauncher == null )
							//		{
							//			Log.i( TAG , "defaultLauncher=" + defaultLauncher );
							//			return;
							//		}
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , "start set Mylauncher" );
							//抢过来
							int counterMax = maxGapTicks / sleepGapTicks;
							int counterCur = 0;
							while( true )
							{
								ComponentName topActivity = getTopActivity( context );
								if( isLauncher( context , topActivity.getPackageName() ) )
								{
									//是launcher就抢
									if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
										Log.i( TAG , "BootProtectionService isLauncher:" + topActivity );
									if( isMyProduct( topActivity.getPackageName() ) )
									{
										counterCur++;
										if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
											Log.i( TAG , "BootProtectionService counterCur=" + counterCur );
										if( counterCur >= counterMax )
										{
											if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
												Log.i( TAG , "BootProtectionService success" );
											break;
										}
									}
									else
									{
										counterCur = 0;
										if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
											Log.i( TAG , "BootProtectionService start Mylauncher" );
										startLauncher( context );
									}
									Thread.sleep( sleepGapTicks );
								}
							}
						}
						catch( Exception e )
						{
							// TODO: handle exception
							e.printStackTrace();
						}
						finally
						{
							//Process.killProcess( Process.myPid() )
							stopSelf();
							isStart = false;
						}
					};
				}.start();
				break;
			case 1://开机触发
				if( !switch_enable_grab_service_on_bootup )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , "flag=1--return 1" );
					break;
				}
				isStart = true;
				new Thread() {
					
					public void run()
					{
						startGrab( context );
						//Process.killProcess( Process.myPid() )
						stopSelf();
						isStart = false;
					};
				}.start();
				break;
		}
		return super.onStartCommand( intent , flags , startId );
	}
	
	public synchronized static void startGrab(
			final Context context )
	{
		int counterMax = maxGapTicks / sleepGapTicks;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "BootProtectionService counterMax=" + counterMax );
		int counterCur = 0;
		int grabResolverActivity = 0;
		boolean hasGrab = false;
		try
		{
			ComponentName defaultLauncher = getDefaultLauncher( context );
			if(
			//
			defaultLauncher != null
			//
			&& ( isMyProduct( defaultLauncher.getPackageName() ) == false/* //xiatian add	//解决“手机重启后，在出现锁屏界面时快速解锁进入桌面并打开任一应用（确保已经从桌面打开了其他界面后，才收到底层发过来的开机广播android.intent.action.BOOT_COMPLETED），等一段时间，手机自动跳到桌面界面”的问题。 */)
			//
			)
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "startGrab defaultLauncher is not null !" );
				clearPreDefaultSetting( context , defaultLauncher.getPackageName() );
				//return;
				//xiatian add start	//解决“手机重启后，在出现锁屏界面时快速解锁进入桌面并打开任一应用（确保已经从桌面打开了其他界面后，才收到底层发过来的开机广播android.intent.action.BOOT_COMPLETED），等一段时间，手机自动跳到桌面界面”的问题。
				ComponentName topActivity = getTopActivity( context );
				if(
				//
				isLauncher( context , topActivity.getPackageName() )
				//
				&& isMyProduct( topActivity.getPackageName() ) == false
				//
				)
				{
					startLauncher( context );
				}
				//xiatian add end
			}
			//xiatian del start	//解决“手机重启后，在出现锁屏界面时快速解锁进入桌面并打开任一应用（确保已经从桌面打开了其他界面后，才收到底层发过来的开机广播android.intent.action.BOOT_COMPLETED），等一段时间，手机自动跳到桌面界面”的问题。
			//			//开机start一次
			//			startLauncher( context );
			//xiatian del end
			//			if( defaultLauncher != null && !isMyProduct( defaultLauncher.getPackageName() ) )
			//			{
			//				context.getPackageManager().clearPackagePreferredActivities( defaultLauncher.getPackageName() );
			//			}
			while( true )
			{
				long elapsedRealtime = SystemClock.elapsedRealtime();
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "elapsedRealtime:" + elapsedRealtime );
				//超过90秒就退
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "BootProtectionService success - hasGrab:" + hasGrab );
				if( elapsedRealtime > ( grabSecondsOnBoot ) * 1000 && hasGrab )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , StringUtils.concat( "elapsedRealtime:" , elapsedRealtime ) );
					break;
				}
				ComponentName topActivity = getTopActivity( context );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , StringUtils.concat( "topActivity:" , topActivity.toString() ) );
				boolean grab = false;
				if( grabResolverActivity < 1 && "com.android.internal.app.ResolverActivity".equals( topActivity.getClassName() ) )
				{
					grabResolverActivity++;
					grab = true;
				}
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "grab:" + grab );
				if( grab || isLauncher( context , topActivity.getPackageName() ) )
				{
					//是launcher就抢
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , "BootProtectionService isLauncher:" + topActivity );
					if( isMyProduct( topActivity.getPackageName() ) )
					{
						counterCur++;
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "BootProtectionService counterCur=" + counterCur );
						if( counterCur >= counterMax )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , "BootProtectionService success - hasGrab=true" );
							//Process.killProcess( Process.myPid() );
							//break;
							//Log.i( TAG , "hasGrab success" );
							hasGrab = true;
						}
					}
					else
					{
						counterCur = 0;
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "BootProtectionService start Mylauncher" );
						startLauncher( context );
					}
					Thread.sleep( sleepGapTicks );
				}
			}
		}
		catch( Exception e )
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Override
	public IBinder onBind(
			Intent intent )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void startLauncher(
			Context context )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "clearPreDefaultSetting - startLauncher" );
		Intent intent = new Intent();
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );//| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
		intent.setClassName( context , "com.cooee.phenix.Launcher" );
		context.startActivity( intent );
	}
	
	static final String[] productList = new String[]{ "com.cooee.uniex" ,//UNI桌面
			"com.cooee.phenix" ,//phenix桌面
			"com.cooee.unilauncher" ,//uni桌面
			"com.cool.launcher" ,//桌面
			"com.cooee.Mylauncher" ,//小米桌面
			"com.coco.launcher" ,//coco桌面
			"com.cooee.launcherS5" ,//S5桌面
			"com.cooee.ios8launcher" ,//IOS桌面
			"com.cooee.oilauncher" ,//OI桌面
			"com.cooee.launcherHS" ,//HTC桌面
			"com.cooee.launcherHW3X" ,//华为桌面
			"com.cooee.launcherS4" ,//S4桌面
			"com.cooee.launcher8" ,//win8桌面
			"com.coco.launcher.client" ,//基伍客户版本
			"com.cooee.launcherS3" ,//S3桌面
			"com.cooeeui.brand.turbolauncher" ,//turbo桌面
	};
	
	public static boolean isMyProduct(
			String packageName )
	{
		for( int i = 0 ; i < productList.length ; i++ )
		{
			if( productList[i].equals( packageName ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "isMyProduct:true--packageName:" + packageName );
				return true;
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "isMyProduct:false--packageName:" + packageName );
		return false;
	}
	
	/**
	 * 是否是桌面应用
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isLauncher(
			Context context ,
			String packageName )
	{
		//获取所有launcher
		Intent intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_HOME );
		intent.addCategory( Intent.CATEGORY_DEFAULT );
		List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities( intent , 0 );
		for( ResolveInfo info : infoList )
		{
			if( packageName.equals( info.activityInfo.packageName ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 当前在前台的activity
	 * @param context
	 * @return
	 */
	public static ComponentName getTopActivity(
			Context context )
	{
		ActivityManager manager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks( 1 );
		RunningTaskInfo task = runningTaskInfos.get( 0 );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( task.topActivity.getPackageName() , "/" , task.topActivity.getClassName() ) );
		return task.topActivity;
	}
	
	/**
	 * 是否要显示我们的桌面
	 * @param context
	 * @return
	 */
	public static boolean isMylauncherNeedShow(
			Context context )
	{
		try
		{
			String curPackageName = getTopActivity( context ).getPackageName();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "curPackageName:" , curPackageName ) );
			if( context.getPackageName().equals( curPackageName ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "this launcher is top." );
				return false;
			}
			if( isMyProduct( curPackageName ) )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "our product is top." );
				return false;
			}
			if( isLauncher( context , curPackageName ) )
			{
				//当前launcher不是我们的，并且在前台
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "top is launcher, it is not this." );
				return true;
			}
		}
		catch( Exception e )
		{
			// TODO: handle exception
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "catch:" , e.toString() ) );
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "top is not launcher." );
		return false;
	}
	
	/**
	 * 获取默认启动launcher
	 * @param context
	 * @return
	 */
	public static ComponentName getDefaultLauncher(
			Context context )
	{
		//获取不正确，不再使用
		// hp@2015/08/06 DEL START
		//IntentFilter intentFilter = new IntentFilter( Intent.ACTION_MAIN );
		//intentFilter.addCategory( Intent.CATEGORY_HOME );
		//intentFilter.addCategory( Intent.CATEGORY_DEFAULT );
		//List<IntentFilter> filters = new ArrayList<IntentFilter>();
		//List<ComponentName> names = new ArrayList<ComponentName>();
		//filters.add( intentFilter );
		//context.getPackageManager().getPreferredActivities( filters , names , null );
		//Intent intent = new Intent( Intent.ACTION_MAIN );
		//intent.addCategory( Intent.CATEGORY_HOME );
		//intent.addCategory( Intent.CATEGORY_DEFAULT );
		//List<ResolveInfo> infoList = context.getPackageManager().queryIntentActivities( intent , 0 );
		//for( ComponentName name : names )
		//{
		//	for( ResolveInfo info : infoList )
		//	{
		//		if( name.getPackageName().equals( info.activityInfo.packageName ) && name.getClassName().equals( info.activityInfo.name ) )
		//		{
		//			return name;
		//		}
		//	}
		//}
		//return null;
		// hp@2015/08/06 DEL END
		//获取不正确，不再使用
		final Intent intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_HOME );
		intent.addCategory( Intent.CATEGORY_DEFAULT );
		final ResolveInfo res = context.getPackageManager().resolveActivity( intent , 0 );
		if( res.activityInfo == null )
		{
			// should not happen. A home is always installed, isn't it?
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "get default launcher is null" );
			return null;
		}
		if( "android".equals( res.activityInfo.packageName ) )
		{
			// No default selected
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "get default launcher is null for no default selected !" );
			return null;
		}
		else
		{
			//
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "get default launcher is :" + res.activityInfo.packageName + "/" + res.activityInfo.name );
			return new ComponentName( res.activityInfo.packageName , res.activityInfo.name );
		}
	}
	
	private static void clearPreDefaultSetting(
			Context context ,
			String packageName )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "clearPreDefaultSetting - packageName:" + packageName );
		if( TextUtils.isEmpty( packageName ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "clearPreDefaultSetting - packageName isEmpty" );
			return;
		}
		if( !packageName.equals( context.getPackageName() ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "clearPreDefaultSetting ok" );
			PackageManager p = context.getPackageManager();
			ComponentName cn = new ComponentName( context.getPackageName() , DefaultLauncherActivity.class.getName() );
			p.setComponentEnabledSetting( cn , PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP );
			Intent selector = new Intent( Intent.ACTION_MAIN );
			selector.addCategory( Intent.CATEGORY_HOME );
			selector.addCategory( Intent.CATEGORY_DEFAULT );
			p.resolveActivity( selector , PackageManager.GET_RESOLVED_FILTER );
			p.setComponentEnabledSetting( cn , PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP );
		}
	}
}
