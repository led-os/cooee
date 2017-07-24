package com.cooee.phenix;


import java.io.File;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;
import com.cooee.framework.utils.UEHandler;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.support.stub.MultiDexUtil;


public class LauncherApplication extends Application
//public class LauncherApplication extends Application
{
	
	//xiatian add start	//添加uncaughtException保护类，捕捉uncaughtException
	private UEHandler ueHandler;
	public static String PATH_ERROR_LOG = "error.log";
	//xiatian add end
	;
	private static final String TAG = "LauncherApplication";
	private static boolean isMultiDexInstall = false;
	//ME_RTFSC [start]
	String getCurProcessName(
			Context context )
	{
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		for( ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses() )
		{
			if( appProcess.pid == pid )
			{
				return appProcess.processName;
			}
		}
		return null;
	}
	
	@Override
	protected void attachBaseContext(
			Context base )
	{
		// TODO Auto-generated method stub
		super.attachBaseContext( base );
		// zhangjin@2016/10/18 ADD START
		long begin = -1;
		long end = -1;
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( TAG , "LauncherApplication attachBaseContext" );
			begin = System.currentTimeMillis();
		}
		if( !isMultiDexInstall )
		{
			MultiDexUtil.initMultiDex( getBaseContext() );
			isMultiDexInstall = true;
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			end = System.currentTimeMillis();
			Log.d( TAG , StringUtils.concat( "initMultiDex need time " , ( end - begin ) ) );
		}
		// zhangjin@2016/10/18 ADD END
	}
	
	//ME_RTFSC [end]
	@Override
	public void onCreate()
	{
		super.onCreate();
		//ME_RTFSC [start]
		//不是launcher 主进程，，不处理
		String strCurPidName = getCurProcessName( this );
		if( null == strCurPidName || !strCurPidName.equals( "com.cooee.phenix" ) )
		{
			return;
		}
		//ME_RTFSC [end]
		//<phenix modify> liuhailin@2015-01-23 modify begin
		LauncherDefaultConfig.setApplicationContext( this );
		//<phenix modify> liuhailin@2015-01-23 modify end
		LauncherAppState.setApplicationContext( this );
		LauncherAppState.getInstance();
		//xiatian add start	//需求：默认主题壁纸外置（使用包名为“config_custom_default_wallpaper_package_name”的res/drawable中的资源“config_custom_default_wallpaper_resource_name”），若“config_custom_default_wallpaper_package_name”和“config_custom_default_wallpaper_resource_name”其中有一个配置为空，则使用桌面默认主题的壁纸。
		// gaominghui@2017/01/09 UPD START
		//WallpaperManagerBase.getInstance( this );//壁纸模块初始化
		// gaominghui@2017/01/09 UPD END
		//xiatian add  end		
		//		Assets.initAssets( this );//cheyingkun add	//我们自己的统计Statistics	//cheyingkun del	//优化加载速度(统计放到桌面加载完再初始化)
		//xiatian add start	//添加uncaughtException保护类，捕捉uncaughtException
		String sdpath = getSDPath();
		if( sdpath != null )
			PATH_ERROR_LOG = StringUtils.concat( sdpath , File.separator , "launcher_error.log" );
		ueHandler = new UEHandler( this , PATH_ERROR_LOG );
		OperateDynamicProxy.initDynamicProxy( this );
		// 设置异常处理实例
		Thread.setDefaultUncaughtExceptionHandler( ueHandler );
		//xiatian add end
	}
	
	@Override
	public void onTerminate()
	{
		super.onTerminate();
		LauncherAppState.getInstance().onTerminate();
	}
	
	public static String getSDPath()
	{
		File SDdir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED );
		if( sdCardExist )
		{
			SDdir = Environment.getExternalStorageDirectory();
		}
		if( SDdir != null )
		{
			return SDdir.toString();
		}
		else
		{
			return null;
		}
	}
}
