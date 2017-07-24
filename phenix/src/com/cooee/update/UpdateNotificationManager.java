package com.cooee.update;


import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.UnreadHelper;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * 自更新提示管理类
 * @author hp
 *
 */
public class UpdateNotificationManager
{
	
	private static String TAG = "UpdateUi.UpdateNotificationManager";
	private final int RUN_COUNT_MAX = 10; //提示次数的最大值;
	private final long UPDATE_DEFAULT_INTERVAL_TIME = 24 * 3600 * 1000; //ms 为1天。
	//
	private static UpdateNotificationManager mInstance = null;
	private Context mGlobalContext = null; //全局Context	
	private Timer mUpdateTimer = null;
	private TimerTask mUpdateTask = null;
	private long mUpdateVersion; //最新版本;
	private int mDisplay; //更新升级显示菜单开关;
	private int runCount = 0; //运行了多少次;
	
	public static UpdateNotificationManager getInstance()
	{
		if( mInstance == null )
		{
			synchronized( UpdateNotificationManager.class )
			{
				if( mInstance == null )
				{
					mInstance = new UpdateNotificationManager();
				}
			}
		}
		return mInstance;
	}
	
	public boolean isShowUpdateIcon()
	{
		SharedPreferences pref = getGlobalContext().getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
		{
			return pref.getBoolean( UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP , false );
		}
		else
		{
			return pref.getBoolean( UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP_DOUBLE , false );
		}
	}
	
	/**
	 * 开始版本更新提示。
	 * @param updateVersion 最新版本号
	 * @param display 显示菜单, 1： 显示更新升级菜单,0： 关闭更新升级菜单。
	 */
	public void startUpdatePrompt(
			long updateVersion ,
			int display )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "startUpdatePrompt " );
		SharedPreferences pref = getGlobalContext().getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		// zhangjin@2016/01/06 bug c_0013236 ADD START
		String iconExistKey = UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP;
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		{
			iconExistKey = UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP_DOUBLE;
		}
		// zhangjin@2016/01/06 ADD END
		// zhangjin@2015/12/14 UPD START
		//if( pref.getBoolean( UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP , false ) == false )
		if( pref.getBoolean( iconExistKey , false ) == false && ( display == 1 || UpdateUiManager.getInstance().getUpdateConfig() ) )
		// zhangjin@2015/12/14 UPD END
		{
			pref.edit().putBoolean( iconExistKey , true ).commit();
			LauncherAppState.getInstance().getModel().onAddUpdateIcon();
		}
		// zhangjin@2015/12/14 ADD START
		if( display == 0 && UpdateUiManager.getInstance().getUpdateConfig() == false )
		{
			LauncherAppState.getInstance().getModel().onRemoveUpdateIcon();
			pref.edit().putBoolean( iconExistKey , false ).commit();
		}
		// zhangjin@2015/12/14 ADD END
		startUpdatePrompt( 0 , updateVersion , display );
	}
	
	/**
	 * 开始版本更新提示
	 * @param startCount 已经执行了几次
	 * @param updateVersion 最新版本号
	 * @param display 显示菜单, 1： 显示更新升级菜单,0： 关闭更新升级菜单。
	 */
	public void startUpdatePrompt(
			int startCount ,
			long updateVersion ,
			int display )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "startUpdatePrompt startCount:" , startCount , "-updateVersion:" , updateVersion , "-display:" , display ) );
		runCount = startCount;
		mUpdateVersion = updateVersion;
		mDisplay = display;
		//取消后重新开始
		// zhangjin@2015/12/14 UPD START
		//cancelUpdatePrompt();
		cancelUpdatePrompt( runCount );
		// zhangjin@2015/12/14 UPD END
		//更新图标更新
		long curVersion = Long.parseLong( UpdateUiManager.getInstance().getVersionCode() );
		UpdateIconManager.getInstance().setHasUpdate( updateVersion > curVersion );
		//保存更新信息
		SharedPreferences pref = getGlobalContext().getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		pref.edit().putLong( UpdateUiManager.KEY_UPDATE_VERSION_IN_SP , updateVersion ).commit();
		pref.edit().putInt( UpdateUiManager.KEY_UPDATE_DISPLAY_IN_SP , display ).commit();
		//更新图标信息
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			mUnreadHelper.changeUpdateIcon();
		}
		//开始
		if( mUpdateTask == null )
		{
			mUpdateTask = new TimerTask() {
				
				@Override
				public void run()
				{
					excuteUpdatePromptTask();
				}
			};
		}
		if( mUpdateTimer == null )
		{
			mUpdateTimer = new Timer();
			mUpdateTimer.schedule( mUpdateTask , UPDATE_DEFAULT_INTERVAL_TIME , UPDATE_DEFAULT_INTERVAL_TIME );
		}
	}
	
	public void cancelUpdatePrompt()
	{
		cancelUpdatePrompt( -1 );
	}
	
	/**
	 * 取消更新提示
	 */
	public void cancelUpdatePrompt(
			int defCount )
	{
		//清除记录
		SharedPreferences pref = getGlobalContext().getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		// zhangjin@2015/12/14 UPD START
		//pref.edit().remove( UpdateUiManager.KEY_UPDATE_PROMPT_COUNT_IN_SP ).commit();		
		pref.edit().putInt( UpdateUiManager.KEY_UPDATE_PROMPT_COUNT_IN_SP , defCount ).commit();
		// zhangjin@2015/12/14 UPD END
		//清除定时器
		if( mUpdateTask != null )
		{
			mUpdateTask.cancel();
			mUpdateTask = null;
		}
		if( mUpdateTimer != null )
		{
			mUpdateTimer.cancel();
			mUpdateTimer = null;
		}
	}
	
	/**
	 * 执行更新提示任务
	 */
	private void excuteUpdatePromptTask()
	{
		if( mUpdateVersion > getCurVersionCode() && runCount < RUN_COUNT_MAX )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "excuteUpdatePromptTask runCount:" , runCount ) );
			UpdateUiManager.getInstance().notifyLauncher( mDisplay );
			runCount++;
			SharedPreferences pref = getGlobalContext().getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
			pref.edit().putInt( UpdateUiManager.KEY_UPDATE_PROMPT_COUNT_IN_SP , runCount ).commit();
		}
		else
		{
			cancelUpdatePrompt();
		}
	}
	
	private Context getGlobalContext()
	{
		if( mGlobalContext == null )
		{
			mGlobalContext = UpdateUiManager.getInstance().getGlobalContext();
		}
		return mGlobalContext;
	}
	
	/**
	 * 当前版本号
	 * @return
	 */
	private long getCurVersionCode()
	{
		return Long.parseLong( UpdateUiManager.getInstance().getVersionCode() );
	}
	
	public void resetAllPrompt()
	{
		cancelUpdatePrompt();
		UpdateIconManager.getInstance().setHasUpdate( false );
		SharedPreferences pref = getGlobalContext().getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		SharedPreferences.Editor editor = pref.edit();
		editor.remove( UpdateUiManager.KEY_UPDATE_PROMPT_COUNT_IN_SP );
		editor.remove( UpdateUiManager.KEY_UPDATE_VERSION_IN_SP );
		editor.remove( UpdateUiManager.KEY_UPDATE_DISPLAY_IN_SP );
		editor.remove( UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP );
		// zhangjin@2016/01/06 bug c_0013236 ADD START
		editor.remove( UpdateUiManager.KEY_UPDATE_ICON_EXIST_IN_SP_DOUBLE );
		// zhangjin@2016/01/06 ADD END
		editor.commit();
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			mUnreadHelper.changeUpdateIcon();
		}
	}
}
