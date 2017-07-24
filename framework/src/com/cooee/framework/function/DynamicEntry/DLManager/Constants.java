package com.cooee.framework.function.DynamicEntry.DLManager;


public class Constants
{
	
	// -1:未在下载列表中;0:正在下载状态;1:暂停下载状态;2:下载完成;3:安装完成
	public static final int DL_STATUS_NOTDOWN = -1;
	public static final int DL_STATUS_ING = 0;
	public static final int DL_STATUS_PAUSE = 1;
	public static final int DL_STATUS_SUCCESS = 2;
	//public static final int DL_STATUS_INSTALL = 3;
	public static final int DL_STATUS_FAIL = 4;
	//这两项仅仅在下载管理器中使用的常量，不是下载的状态
	public static final int DL_STATUS_REMOVED = 5;
	public static final int DL_STATUS_PATCH = 6;
	public static final int DL_INVALID_NOTIFYID = -1;
	public static final String DL_INFO_GET_PKGNAME_KEY = "p2";
	public static final String NOTIFY_ID = "notifyId";
	public static final String PKG_NAME = "pkgId";
	public static final String MSG = "msg";
	public static final String SILENT_TYPE = "silent_type";
	public static final int SILENT_SINGLE = 0;
	public static final int SILENT_MUTIPLE = 1;
	public static final String SINGLE_OR_MUTIPLE = "single_or_mutiple";
	public static final String SHOW_WHICH_VIEW = "showWhichView";
	public static final int SHOW_DOWNLOAD_VIEW = 0;
	public static final int SHOW_INSTALL_VIEW = 1;
	public static final String FILEPATH_FLAG = "filepath";
	//wifi1118 START 下载应该直接显示安装的List
	//wifi1118 END 
	public static final String NOTIFY = "notify";
	public static final String APK_TITLE = "title";
	public static final String APK_PATH = "filePath";
	public static final String APK_INSTALLED = "installed";
	public static final String APK_CLASSNAME = "apkClsName";
	public static final String NOTIFY_ACTIVITY_CLASS_NAME = "com.cooee.framework.function.DynamicEntry.DLManager.DlNotifyActivity";
	public static final String DLLIST_ACTIVITY_CLASS_NAME = "com.cooee.framework.function.DynamicEntry.DLManager.DlApkMangerActivity";
	public static final String MSG_SUCCESS = "msgSuccess";
	public static final String MSG_DOING = "msgDoing";
	public static final String MSG_FAILURE = "msgFail";
	public static final String MSG_PAUSE = "msgPause";
	public static final String MSG_WIFI_SA = "msgWifiSA";//WIFI静默下载
	public static final String MSG_DL_INSTALL = "msgDLinstall";//下载后提示未安装
	public static final String MSG_ALL_DLING = "msgAllDLing";//所有正在下载
	public static final String DL_MGR_ACTION_PAUSE = "com.iLoong.Download.Manager.action.pause";
	public static final String DL_MGR_ACTION_DOWNING = "com.iLoong.Download.Manager.action.downing";
	public static final String DL_MGR_ACTION_REMOVED = "com.iLoong.Download.Manager.action.removed";
	public static final String DL_MGR_ACTION_SUCCESS = "com.iLoong.Download.Manager.action.success";
	public static final String DL_MGR_ACTION_FAILURE = "com.iLoong.Download.Manager.action.failure";
	public static final String LANGUAGE_ENGLIST = "ENName";
	public static final String LANGUAGE_CH = "CNName";
	public static final String LANGUAGE_TW = "TWName";
	public static final boolean DownloadSuccessItemNoDelete = true;
	public static boolean SUPPORT_CLICK_CHANGEICON_ICON_TO_DESK = true;//是否支持点击图标移至桌面
	public static final boolean NEW_ICON_DISPLAY_NUM = true;//文件夹新增内容的时候。是否显示数字。还是直接显示N标
	public static final String NEW_ICON_COUNT = "new_icon_count";
	public static final boolean DELETE_ICON_SHOW_INSTALL = true; //删除下载好的应用，显示对话框是否安装
}
