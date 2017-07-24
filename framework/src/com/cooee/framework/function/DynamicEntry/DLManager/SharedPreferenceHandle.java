package com.cooee.framework.function.DynamicEntry.DLManager;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cooee.framework.app.BaseAppState;


public class SharedPreferenceHandle
{
	
	public static int APK_STOP = 0;
	public static int APK_DOWNLOAD = 1;
	public static int unNeedStart = 0;
	public static int needStart = 1;
	public static int unNeedHideHot = 0;
	public static int needHideHot = 1;
	public static int SIENT_HIDE = 0;
	public static int SIENT_SHOW = 1;
	public static int appUnShowHot = -1;
	public static int appShowHot = 1;
	public static String APP_HOT = "app_hot";
	public static String DESKTOP_HOT = "deskTop_hot";
	public static String MAINMEUN_HOT = "mainMeun_hot";
	public static String DOWNLOAD_APK_PREFIX = "prefix_download_";
	public static String START_APK_PREFIX = "prefix_start_";
	public static String HIDE_VIRTUAL_APK_HOT_PREFIX = "prefix_hide_virtual_apk_hot_";
	private final String START_ICON_DPREFERENCE_KEY = "DynamicEntryConfig";
	public static String WIFI_DOWNLOAD_PREFIX = "WIFI_Continue_Download_";
	public static String HIDE_HOT_PREFIX = "Need_Hide_Hot_";
	private final String VALUE_NULL = null;
	public static final String UNINSTALL_FOLDER_ITEM_PREFIX = "prefix_uninstall_";
	public static final String UNINSTALL_FOLDER_VALUE = "uninstallSuccess";
	public static final String SILENTDOWNLOAD_PREFIX = "prefix_silent";
	public static final int DEFAULT_VALUE = 0;
	// N 标动画显示否的控制开关
	public static String NFLAG_DISPLAY_PREFIX = "NFLAG_DISPLAY_PREFIX_";
	public static int FolderShowNAnim = 1;
	public static int FolderNotShowNAnim = -1;
	// 显示拉手逻辑
	// 配置enable_icon_category=1的时候，如果没有执行，就要显示出来
	//配置enable_icon_category=2的时候,收到服务器要求显示的通知，同时，将拉手已经执行开关复位，就要显示出来
	// 服务器配置为取消智能分类开关的时候，就不能显示出来，拉手已经执行开关复位，服务器要求显示的开关复位
	// 拉手出来后，只要执行开关被设定为已经执行，就不再显示
	public static final String SHAKEHAND_EXECUTE = "shakehand_execute";
	public static final String SERVICE_PERMIT_SHAKEHAND_SHOW = "service_permit_shakehand_show";
	public static final int SHAKEHAND_EXECUTE_VALUE = 100;
	public static final int SHAKEHAND_NOTEXECUTE_VALUE = 1;
	public static final int PERMIT_SHAKEHAND_SHOW_VALUE = 100;
	public static final int DISABLE_SHAKEHAND_SHOW_VALUE = 1;
	public static String REMOVED_NOEMPTY_CSFOLDER_PREFIX = "prefix_removed_notempty_csfolder_";
	
	protected SharedPreferenceHandle()
	{
	}
	
	//gzh1011 start
	//本身的值 就作为 KEY值去取
	public String getValue(
			String key )
	{
		SharedPreferences preferences = getPreferences();
		if( key != null )
		{
			String value = preferences.getString( key , VALUE_NULL );
			return value;
		}
		return VALUE_NULL;
	}
	
	public void removeValue(
			String key )
	{
		if( key == null )
		{
			return;
		}
		SharedPreferences preferences = getPreferences();
		Editor editor = preferences.edit();
		editor.remove( key );
		editor.commit();
	}
	
	public void saveValue(
			String key ,
			String value )
	{
		if( value == null )
		{
			return;
		}
		if( key != null )
		{
			SharedPreferences preferences = getPreferences();
			Editor editor = preferences.edit();
			editor.putString( key , value );
			editor.commit();
		}
	}
	
	private SharedPreferences getPreferences()
	{
		SharedPreferences preferences = BaseAppState.getActivityInstance().getSharedPreferences( START_ICON_DPREFERENCE_KEY , Context.MODE_PRIVATE );
		return preferences;
	}
}
