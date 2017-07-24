/* 文件名: ThemeReceiver.java 2014年8月26日
 * 
 * 描述:主题广播接收器,主要接收主题相关操作的广播,例如切换主题
 * 
 * 作者: cooee */
package com.cooee.theme;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.cooee.center.pub.provider.PubProviderHelper;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.kmob.kmobsdk.KmobManager;


public class ThemeReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "ThemeReceiver";
	public static final String ACTION_DEFAULT_THEME_CHANGED = "com.coco.theme.action.DEFAULT_THEME_CHANGED";//add start for personal_center separate 2014.01.23 by hupeng
	public static final String ACTION_LAUNCHER_CLICK_THEME = "com.cooee.launcher.click_theme";
	public static final String ACTION_LAUNCHER_REQ_RESUME_TIME = "com.cooee.launcher.req_resume_time";
	public static final String ACTION_LAUNCHER_RSP_RESUME_TIME = "com.cooee.launcher.rsp_resume_time";
	public static final String ACTION_LAUNCHER_RESTART = "com.cooee.phenix.launcher.restart";
	public static final String ACTION_LAUNCHER_APPLY_THEME = "com.cooee.phenix.launcher.apply_theme";
	private static Launcher mLauncher;
	private Handler mHandler = new Handler();
	
	@Override
	public void onReceive(
			Context c ,
			Intent intent )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "intent:" , intent.getAction() ) );
		//<theme_issue> liuhailin@2014-09-03 del begin
		//if( !FeatureConfig.enable_themebox )
		//	return;
		//<theme_issue> liuhailin@2014-09-03 del end
		final String action = intent.getAction();
		if( action.equals( ACTION_LAUNCHER_RESTART ) )
		{
			restart( c , false );
		}
		else if( action.equals( ACTION_LAUNCHER_APPLY_THEME ) )
		{
			//UtilsShortcut.setWallpaperPositon( -1 );
			applyTheme( c , intent , false );
		}
		else if( action.equals( ACTION_LAUNCHER_CLICK_THEME ) )
		{
			String selectedLauncher = intent.getStringExtra( "selected_launcher" );
			String themePkgName = intent.getStringExtra( "theme_pkg_name" );
			if( selectedLauncher != null && selectedLauncher.equals( c.getPackageName() ) && themePkgName != null )
			{
				Intent intent2 = new Intent( ACTION_LAUNCHER_APPLY_THEME );//add start for personal_center separate 2014.01.23 by hupeng 
				intent2.putExtra( "theme_status" , 1 );
				intent2.putExtra( "theme" , themePkgName );
				//gaominghui add start //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_frome_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
				boolean apply_theme_from_beautycenter = intent.getBooleanExtra( "apply_theme_from_beautycenter" , false );
				intent2.putExtra( "apply_theme_from_beautycenter" , apply_theme_from_beautycenter );
				//gaominghui adde end //添加配置项“switch_enable_exit_overview_mode_when_apply_theme_frome_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。
				//gaominghui add start //美化中心应用主题，桌面会黑一下【i_0015112】
				if( apply_theme_from_beautycenter )
				{
					applyTheme( c , intent2 , false );
				}
				else
				{
					applyTheme( c , intent2 , true );
				}
				//gaominghui add end //美化中心应用主题，桌面会黑一下【i_0015112】
				//				restart( c , true );
				c.sendBroadcast( new Intent( ACTION_DEFAULT_THEME_CHANGED ) );//add start for personal_center separate 2014.01.23 by hupeng 
			}
		}
		else if( action.equals( ACTION_LAUNCHER_REQ_RESUME_TIME ) )
		{
			SharedPreferences prefs2 = c.getSharedPreferences( "launcher" , Context.MODE_WORLD_READABLE );
			Intent intent2 = new Intent( ACTION_LAUNCHER_RSP_RESUME_TIME );
			intent2.putExtra( "resume_time" , prefs2.getLong( "resume_time" , -1 ) );
			intent2.putExtra( "launcher_pkg_name" , c.getPackageName() );
			c.sendBroadcast( intent2 );
		}
	}
	
	//xiatian add start	//换主题不重启
	public static void initialize(
			Launcher launcher )
	{
		mLauncher = launcher;
	}
	
	public static void applyTheme(
			Context c ,
			Intent mIntent ,
			boolean fromThemeClick )
	{
		if( mLauncher != null )
		{
			Log.v( "TAG" , " mLauncher != null " );
			if( fromThemeClick )
			{
				restart( c , true );
			}
			// YANGTIANYU@2015/12/04 UPD START
			//mLauncher.applyTheme( mIntent , false );
			// 美化中心中点击主题应用，回到桌面后滑动桌面或点击menu键、返回键，桌面会重启 【i_0012966】
			mLauncher.applyTheme( mIntent , true );
			// YANGTIANYU@2015/12/04 UPD END
		}
		else
		{
			Log.v( "TAG" , " mLauncher == null " );
			saveThemeDataAndRestart( c , mIntent );
		}
	}
	
	private static void saveThemeDataAndRestart(
			Context c ,
			Intent mIntent )
	{
		PubProviderHelper.addOrUpdateValue( "theme" , "theme" , mIntent.getStringExtra( "theme" ) );
		PubProviderHelper.addOrUpdateValue( "theme" , "theme_status" , String.valueOf( mIntent.getIntExtra( "theme_status" , 1 ) ) );
		restart( c , false );
	}
	//xiatian add end
	;
	
	/**
	 * 桌面重新启动的接口
	 * @param c  上下文
	 * @param fromThemeClick  是否是从桌面直接点击主题的操作
	 */
	private static void restart(
			Context c ,
			boolean fromThemeClick )
	{
		//<theme_issue> liuhailin@2014-08-25 del begin
		//if( !iLoongApplication.init )
		//{
		//	final Intent intent2 = new Intent();
		//	intent2.setClass( c , iLoongLauncher.class );
		//	intent2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		//	c.startActivity( intent2 );
		//}
		//<theme_issue> liuhailin@2014-08-25 del end
		if( LauncherAppState.getInstance() == null )
		{
			KmobManager.clearAllDl();//cheyingkun add	//解决“点击文件夹下方推荐应用下载，下载过程中切换模式，此时在点击此应用下载，提示正在下载中。状态栏无下载显示。”的问题。【i_0013284】
			// wanghongjian@2015/04/27 UPD START 当切换桌面模式准备重启的时候，要将状态栏中launcher提示下载的提示给关闭 bug:0011115
			( (NotificationManager)( c.getSystemService( Context.NOTIFICATION_SERVICE ) ) ).cancelAll();
			// wanghongjian@2015/04/27 UPD END
			final Intent intent2 = new Intent();
			intent2.setClass( c , Launcher.class );
			intent2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			c.startActivity( intent2 );
			//cheyingkun del start	//解决“切换单双层反应过慢”的问题。【i_0012595】
			//			//cheyingkun add start	//添加友盟统计自定义事件(程序结束前保存友盟统计数据)
			//			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			//			{
			//				MobclickAgent.onKillProcess( c );
			//			}
			//			//cheyingkun add end
			//cheyingkun del end
			System.exit( 0 );
		}
		else if( fromThemeClick )
		{
			final Intent intent2 = new Intent();
			intent2.setClass( c , Launcher.class );
			intent2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			c.startActivity( intent2 );
		}
		else
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "Launcher" , "exit" );
			KmobManager.clearAllDl();//cheyingkun add	//解决“点击文件夹下方推荐应用下载，下载过程中切换模式，此时在点击此应用下载，提示正在下载中。状态栏无下载显示。”的问题。【i_0013284】
			// wanghongjian@2015/04/27 UPD START 当切换桌面模式准备重启的时候，要将状态栏中launcher提示下载的提示给关闭 bug:0011115
			( (NotificationManager)( c.getSystemService( Context.NOTIFICATION_SERVICE ) ) ).cancelAll();
			// wanghongjian@2015/04/27 UPD END
			final Intent intent2 = new Intent();
			intent2.setClass( c , Launcher.class );
			intent2.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			c.startActivity( intent2 );
			//cheyingkun del start	//解决“切换单双层反应过慢”的问题。【i_0012595】
			//			//cheyingkun add start	//添加友盟统计自定义事件(程序结束前保存友盟统计数据)
			//			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			//			{
			//				MobclickAgent.onKillProcess( c );
			//			}
			//			//cheyingkun add end
			//cheyingkun del end
			System.exit( 0 );
		}
	}
}
//enable_themebox
