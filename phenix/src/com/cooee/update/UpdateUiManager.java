package com.cooee.update;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * @author zhangjin
 *升级管理类
 */
public class UpdateUiManager
{
	
	private static String TAG = "UpdateUi";
	public static final String SP_UPDATE_PATH_NAME = "Update";
	public static final String KEY_UPDATE_MENU_IN_SP = "UpdateMenu";
	public static final String KEY_UPDATE_PROMPT_COUNT_IN_SP = "update_notification_count";
	public static final String KEY_UPDATE_VERSION_IN_SP = "update_version";//服务器的传递
	public static final String KEY_UPDATE_DISPLAY_IN_SP = "update_display";//服务器的传递
	public static final String KEY_UPDATE_ICON_EXIST_IN_SP = "update_icon_exist";//更新图标是否存在
	public static final String KEY_UPDATE_ICON_EXIST_IN_SP_DOUBLE = "update_icon_exist_double";//双层更新图标是否存在
	protected static UpdateUiManager instance;
	public static int NOFITY_ID = 20150701;
	private static Context mContext = null;
	
	public static UpdateUiManager getInstance()
	{
		if( instance == null && mContext != null )
		{
			synchronized( UpdateUiManager.class )
			{
				if( instance == null )
				{
					instance = new UpdateUiManager();
				}
			}
		}
		return instance;
	}
	
	/**
	 * 主动开启自更新菜单
	 * @param show
	 * true开启更新菜单，false关闭更新菜单
	 */
	public void updateMenu(
			boolean show )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "showUpdateMenu:" , show ) );
		//打开更新菜单
		//保存菜单状态
		saveMenuState( show );
	}
	
	/**
	 * 保存自更新菜单状态
	 * @param show
	 */
	public void saveMenuState(
			boolean show )
	{
		if( mContext == null )
		{
			return;
		}
		SharedPreferences sharedPreferences = mContext.getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.putBoolean( UpdateUiManager.KEY_UPDATE_MENU_IN_SP , show );
		editor.commit();//提交修改
	}
	
	/**
	 * 读取自更新菜单状态
	 * @return
	 */
	public boolean getMenuState()
	{
		if( mContext == null )
		{
			return false;
		}
		boolean show = false;
		SharedPreferences sharedPreferences = mContext.getSharedPreferences( UpdateUiManager.SP_UPDATE_PATH_NAME , Context.MODE_PRIVATE );
		show = sharedPreferences.getBoolean( UpdateUiManager.KEY_UPDATE_MENU_IN_SP , false );
		return show;
	}
	
	/**
	 * 获取默认配置，是否显示更新菜单
	 * @return
	 * 
	 */
	public boolean getUpdateConfig()
	{
		return LauncherDefaultConfig.LAUNCHER_UPDATE;
	}
	
	//这个是luancher配置的值，是否有显性菜单
	public int isDisplay()
	{
		if( getUpdateConfig() )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public void notifyLauncher(
			int disPlay )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "notifyLauncher disPlay:" , disPlay ) );
		//launcher没有配置显性菜单
		if( isDisplay() != 1 )
		{
			//这里通知launcher处理
			//1、更新菜单状态
			updateMenu( disPlay == 1 );
			//2、弹出对应的通知栏(下载，安装选项)
			if( disPlay == 1 )
			{
				showUpdateNotify();
				// zhangjin@2015/12/01 ADD START
				UpdateDownloadManager.getInstance( getGlobalContext() ).showUpdateDialog();
				// zhangjin@2015/12/01 ADD END
			}
		}
		else
		{
			//launcher有配置显性菜单
			showUpdateNotify();
			// zhangjin@2015/12/01 ADD START
			UpdateDownloadManager.getInstance( getGlobalContext() ).showUpdateDialog();
			// zhangjin@2015/12/01 ADD END
		}
	}
	
	// zhangjin@2015/12/02 ADD START
	public Context getGlobalContext()
	{
		return LauncherAppState.getActivityInstance();
	}
	
	public static void setGlobalContext(
			Context context )
	{
		mContext = context;
	}
	
	// zhangjin@2015/12/02 ADD END
	/**
	 * 有版本更新时，显示通知栏
	 */
	public void showUpdateNotify()
	{
		Notification notification = new Notification();
		notification.icon = R.mipmap.ic_launcher_home;
		notification.when = System.currentTimeMillis();
		notification.tickerText = LauncherDefaultConfig.getString( R.string.updateNotifyTicker );
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		RemoteViews contentView = new RemoteViews( mContext.getPackageName() , R.layout.uiupdate_notify );
		contentView.setTextViewText( R.id.notification_downloading_titlev , LauncherDefaultConfig.getString( R.string.updateNotifyTitle ) );
		contentView.setViewVisibility( R.id.notification_pb , View.GONE );
		notification.contentView = contentView;
		Intent intent = new Intent();
		intent.setComponent( new ComponentName( mContext , UpdateActivity.class ) );
		intent.putExtra( LauncherUpdateFragment.KEY_FIND_NEW , true );
		intent.putExtra( "notifyID" , NOFITY_ID );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		PendingIntent contentIntent = PendingIntent.getActivity( mContext , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.contentIntent = contentIntent;
		//
		NotificationManager manager = (NotificationManager)mContext.getSystemService( Context.NOTIFICATION_SERVICE );
		manager.notify( NOFITY_ID , notification );
	}
	
	public String getVersionCode()
	{
		PackageInfo info;
		try
		{
			info = mContext.getPackageManager().getPackageInfo( mContext.getPackageName() , 0 );
			return String.valueOf( info.versionCode );
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return "0";
	}
}
