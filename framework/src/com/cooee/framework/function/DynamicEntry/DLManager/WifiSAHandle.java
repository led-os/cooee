package com.cooee.framework.function.DynamicEntry.DLManager;


import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.SADownload.SAMutipleNotification;
import com.cooee.framework.function.DynamicEntry.SADownload.SANotification;
import com.cooee.framework.function.DynamicEntry.SADownload.SASingleNotification;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.DynamicEntry.DynamicEntry;
import cool.sdk.SAManager.SACoolDLMgr.NotifyType;
import cool.sdk.SAManager.SAHelper;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class WifiSAHandle
{
	
	private int G_index = 0;
	private static List<dl_info> g_dl_ino_list_mutiple = new ArrayList<dl_info>();
	
	protected WifiSAHandle()
	{
	}
	
	public void showWifiSANotify(
			NotifyType type ,
			List<dl_info> dl_info_list )
	{
		int count = dl_info_list.size();
		if( count == 0 )
		{
			return;
		}
		if( count > 0 && count <= 2 )
		{
			showSingNotify( type , dl_info_list );
		}
		else if( count > 2 )
		{
			showMutiplenotify( type , dl_info_list );
		}
	}
	
	private void showSingNotify(
			NotifyType type ,
			List<dl_info> dl_info_list )
	{
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		SASingleNotification singleNotify = new SASingleNotification( BaseAppState.getActivityInstance() , mNotificationManager );
		for( int i = 0 ; i < dl_info_list.size() ; i++ )
		{
			dl_info info = dl_info_list.get( i );
			if( info != null )
			{
				addInfoToDownloadList( info );
				DeleteLocalDownload( info );
				singleNotify.showSASingleNotification( type , info );
			}
		}
	}
	
	private void showMutiplenotify(
			NotifyType type ,
			List<dl_info> dl_info_list )
	{
		List<dl_info> dl_ino_list_mutiple = new ArrayList<dl_info>();
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		SASingleNotification singleNotify = new SASingleNotification( BaseAppState.getActivityInstance() , mNotificationManager );
		SAMutipleNotification mutiplenNotify = new SAMutipleNotification( BaseAppState.getActivityInstance() , mNotificationManager );
		dl_info info = dl_info_list.get( 0 );
		if( info != null )
		{
			addInfoToDownloadList( info );
			DeleteLocalDownload( info );
			singleNotify.showSASingleNotification( type , info );
		}
		else
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "COOL" , "can't get appname" );
		}
		for( int i = 1 ; i < dl_info_list.size() ; i++ )
		{
			info = dl_info_list.get( i );
			if( info != null )
			{
				addInfoToDownloadList( info );
				DeleteLocalDownload( info );
				dl_ino_list_mutiple.add( info );
			}
		}
		if( dl_ino_list_mutiple.size() != 0 )
		{
			mutiplenNotify.showSAMutipleNotification( type , dl_ino_list_mutiple );
		}
	}
	
	public void addInfoToDownloadList(
			dl_info info )
	{
		if( info == null )
		{
			return;
		}
		DlManager
				.getInstance()
				.getSharedPreferenceHandle()
				.saveValue(
						StringUtils.concat( SharedPreferenceHandle.SILENTDOWNLOAD_PREFIX , (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY ) ) ,
						String.valueOf( SharedPreferenceHandle.SIENT_SHOW ) );
		String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
		String title = getTitleName( info );
		if( pkgName != null && title != null )
		{
			DlNotifyManager mDlNotifyManager = DlManager.getInstance().getDlNotifyManager();
			mDlNotifyManager.addToDownloadList( pkgName , title , SANotification.Single_Notification_Id_Base + info.getID() , Constants.DL_STATUS_SUCCESS , 100 , null );
		}
	}
	
	public dl_info getDlInfo(
			String packageName )
	{
		List<dl_info> dl_info_list = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getSuccessButNotInstallList();
		for( dl_info info : dl_info_list )
		{
			String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
			if( pkgName.equals( packageName ) )
			{
				return info;
			}
		}
		return null;
	}
	
	public List<dl_info> getWifiDownloadAllState()
	{
		List<dl_info> ret_dl_info_list = new ArrayList<dl_info>();
		List<dl_info> dl_info_list = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getSuccessButNotInstallList();
		if( dl_info_list != null )
		{
			for( dl_info info : dl_info_list )
			{
				String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
				if( pkgName != null )
				{
					String value = DlManager.getInstance().getSharedPreferenceHandle().getValue( StringUtils.concat( SharedPreferenceHandle.SILENTDOWNLOAD_PREFIX , pkgName ) );
					if( value != null && value.equals( String.valueOf( SharedPreferenceHandle.SIENT_SHOW ) ) )
					{
						ret_dl_info_list.add( info );
					}
				}
			}
		}
		return ret_dl_info_list;
	}
	
	public String getTitleName(
			dl_info info )
	{
		String appName;
		int lan = OperateDynamicUtils.getCurLanguage();
		if( lan == OperateDynamicUtils.CHINESE )
		{
			appName = (String)info.getValue( Constants.LANGUAGE_CH );
		}
		else if( lan == OperateDynamicUtils.CHINESE_TW )
		{
			appName = (String)info.getValue( Constants.LANGUAGE_TW );
		}
		else
		{
			appName = (String)info.getValue( Constants.LANGUAGE_ENGLIST );
		}
		return appName;
	}
	
	public void setUninstallToDB(
			String packageName )
	{
		dl_info info = getDlInfo( packageName );
		if( info != null )
		{
			info.setValue( SAHelper.WIFISA_INSTALL_STATE , SAHelper.WIFISA_UNSTALL );
			CoolDLMgr wifidlMgr = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getCoolDLMgrApk();
			wifidlMgr.dl_mgr.updateTaskInfo( info );
		}
	}
	
	//通知下来了。就清除掉原来此应用的NOTIFY信息，避免显示重复。SDK的数据也消除，以免此应用加入了继续下载。
	public void DeleteLocalDownload(
			dl_info info )
	{
		List<dl_info> dl_info_list = getLocalList();
		if( info == null && dl_info_list.size() == 0 )
		{
			return;
		}
		String packName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
		CoolDLMgr mDlMgr = DynamicEntry.CoolDLMgr( BaseAppState.getActivityInstance() , "DAPP" );
		NotificationManager mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		for( dl_info tempinfo : dl_info_list )
		{
			String TempPackName = (String)tempinfo.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
			if( packName.equals( TempPackName ) )
			{
				mDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , TempPackName , true );
				mNotificationManager.cancel( tempinfo.getID() );
				break;
			}
		}
	}
	
	//获取用户点击下载的列表
	public List<dl_info> getLocalList()
	{
		CoolDLMgr mDlMgr;
		mDlMgr = DynamicEntry.CoolDLMgr( BaseAppState.getActivityInstance() , "DAPP" );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "DlManager" , "Dl Manager getAllState begin" );
		try
		{
			return mDlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
		}
		catch( Exception e )
		{
			return new ArrayList<dl_info>();
		}
	}
	
	//此函数不可以改名字。底层会调用此函数访问用户是否设置了可下载。
	public static boolean allowSADownload()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( BaseAppState.getActivityInstance() );
		boolean dynamicShare = sharedPreferences.getBoolean( BaseDefaultConfig.getString( R.string.settings_key_smart_download ) , true );
		if( !dynamicShare )
		{
			return false;
		}
		return true;
	}
	
	//点击多个Notify时要获取的信息
	public List<dl_info> getDisplayWifiDownloadInto()
	{
		List<dl_info> dl_infos = getWifiDownloadAllState();
		return dl_infos;
	}
}
