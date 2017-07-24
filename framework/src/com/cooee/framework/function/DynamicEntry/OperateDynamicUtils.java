package com.cooee.framework.function.DynamicEntry;


import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.LauncherConfigUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.shell.sdk.CooeeSdk;

import cool.sdk.common.UrlUtil;
import cool.sdk.download.manager.DlMethod;


public class OperateDynamicUtils
{
	
	public final static int FOLDER = 1;
	public final static int VIRTUAL_APP = 2;
	public final static int VIRTUAL_LINK = 3;
	public final static int INSTALL_APP = 999;//用来一个应用两个图标的情况
	public static final int STATE_FILE_NOTEXIST = 0;
	public static final int STATE_FILE_EXIST_NOT_ALL = 1;
	public static final int STATE_FILE_EXIST_ALL = 2;
	public final static String TOPWISE_SN = "dz28678";
	public static final String DOWNLOAD_APK_FOLDER = "cooee/launcher/operate_folder";
	public static long EXPIRED_MS_TIME;
	public static int curLanguage = 0;
	public static final int ENGLISH = 0;
	public static final int CHINESE = 1;
	public static final int CHINESE_TW = 2;
	public static final String INVALID_FOLDERID = "emptyID";
	public static final String DYNAMIC_ID_KEY = "dynamicID";
	public static final String BITMAP_PATH_KEY = "BitmapPath";
	public static final String TITLE_KEY = "Title";
	public static final String FROM_KEY = "from";
	public static final String INSTALLED_KEY = "isInstallItem";
	public static final String UN_INSTALLED_KEY = "isUninstallItem";
	public static final String DYNAMIC_TYPE_KEY = "dynamicType";
	public static final String MENU_POS_KEY = "MenuPos";
	public static final String DYNAMIC_DATA_INDESK = "1";
	public static final String DYNAMIC_DATA_INMENU = "2";
	public static final String DYNAMIC_DATA_INALL = "3";
	public static final String DYNAMIC_DATA_CAN_DELETE = "canDelete";
	public static final String DYNAMIC_DATA_KEEP_ITEM = "keepItem";
	public static final int DYNAMIC_INSTALLED = 1;
	public static final int DYNAMIC_UNINSTALLED = 0;
	public static final String DYNAMIC_COMMA = ",";
	public static final String DYNAMIC_SEMICOLON = ";";
	//如果数据层给出的提示语存在（长度不为空，且长度大于等于6）， 那么使用数据层给出的提示语
	public static final int TIP_SHOW_MIN_LENGTH = 6;
	//对于合一桌面，统一为3
	//dynamicEntry1010 start
	public static final String DYNAMIC_APP_DOWNLOAD_TYPE = "appdownloadtype";
	public static final String DYNAMIC_APP_SIZE = "appsize";
	public static final int NORMAL_DOWNLOAD = 0;
	public static final int WIFI_DOWNLOAD = 1;
	public static final int APPSTORE_DOWNLOAD = 2;
	public static final int WIFI_APPSTORE_DOWNLOAD = 3;
	public static final int ME_ENTRY_FLAG = 4;
	//dynamicEntry1010 end
	public static final String DYNAMIC_HOT = "dynamichot";
	static
	{
		String lan = Locale.getDefault().getLanguage();
		if( lan.equals( "zh" ) )
		{
			lan = Locale.getDefault().toString();
			if( lan.equals( "zh_TW" ) )
			{
				curLanguage = 2;
			}
			else
			{
				curLanguage = 1;
			}
		}
		else
		{
			curLanguage = 0;
		}
	}
	
	public static String getShellID(
			Context context )
	{
		String id = LauncherConfigUtils.cooeeGetCooeeId( context );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "OperateDynamicUtils" , StringUtils.concat( "shell id=" , id ) );
		return id;
	}
	
	public static int getCurLanguage()
	{
		return curLanguage;
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
	
	public static String getDownloadFilePath()
	{
		String sdDir = getSDPath();
		if( sdDir == null )
		{
			return null;
		}
		return( StringUtils.concat( sdDir , File.separator , DOWNLOAD_APK_FOLDER ) );
	}
	
	//0:文件不存在，1：文件存在但不完整，2：文件完整
	public static int verifyAPKFile(
			Context context ,
			String path )
	{
		try
		{
			File packageFile = new File( path );
			if( packageFile.exists() )
			{
				PackageManager pm = context.getPackageManager();
				PackageInfo info = pm.getPackageArchiveInfo( path , PackageManager.GET_ACTIVITIES );
				if( info != null )
				{
					return STATE_FILE_EXIST_ALL;
				}
				else
				{
					return STATE_FILE_EXIST_NOT_ALL;
				}
			}
			else
			{
				return STATE_FILE_NOTEXIST;
			}
		}
		catch( Exception e )
		{
			return STATE_FILE_NOTEXIST;
		}
	}
	
	public static int verifyAPKFile(
			Context context ,
			String path ,
			String packageName )
	{
		try
		{
			File packageFile = new File( path );
			if( packageFile.exists() )
			{
				PackageManager pm = context.getPackageManager();
				PackageInfo info = pm.getPackageArchiveInfo( path , PackageManager.GET_ACTIVITIES );
				if( info != null )
				{
					if( packageName == null )
					{
						return STATE_FILE_EXIST_ALL;
					}
					else
					{
						if( info.packageName.equals( packageName ) )
						{
							return STATE_FILE_EXIST_ALL;
						}
						else
						{
							return STATE_FILE_EXIST_NOT_ALL;
						}
					}
				}
				else
				{
					return STATE_FILE_EXIST_NOT_ALL;
				}
			}
			else
			{
				return STATE_FILE_NOTEXIST;
			}
		}
		catch( Exception e )
		{
			return STATE_FILE_NOTEXIST;
		}
	}
	
	//安装APK文件
	public static void installAPKFile(
			Context context ,
			String path )
	{
		Intent intent = new Intent();
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.addFlags( Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
		intent.setAction( android.content.Intent.ACTION_VIEW );
		intent.setDataAndType( Uri.fromFile( new File( path ) ) , "application/vnd.android.package-archive" );
		context.startActivity( intent );
	}
	
	public static String getClassName(
			Context context ,
			String packageName )
	{
		PackageInfo pi;
		try
		{
			pi = context.getPackageManager().getPackageInfo( packageName , 0 );
			Intent resolveIntent = new Intent( Intent.ACTION_MAIN , null );
			resolveIntent.setPackage( pi.packageName );
			PackageManager pManager = context.getPackageManager();
			List<ResolveInfo> apps = pManager.queryIntentActivities( resolveIntent , 0 );
			ResolveInfo ri = apps.iterator().next();
			if( ri != null )
			{
				return ri.activityInfo.name;
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public static boolean isNetworkAvailable(
			Context context )
	{
		try
		{
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo info = cm.getActiveNetworkInfo();
			return( info != null && info.isConnected() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return false;
		}
	}
	
	//删除一项
	public static void removeOneItem(
			List<OperateDynamicData> dataList ,
			String pkgName )
	{
		if( dataList != null && dataList.size() > 0 )
		{
			Iterator<OperateDynamicData> ite = dataList.iterator();
			while( ite.hasNext() )
			{
				OperateDynamicData data = ite.next();
				if( data.dynamicType == VIRTUAL_APP || data.dynamicType == VIRTUAL_LINK )
				{
					if( pkgName.equals( data.mPkgnameOrAddr ) )
					{
						ite.remove();
						break;
					}
				}
				else
				{
					Iterator<OperateDynamicItem> iteItem = data.mDynamicItems.iterator();
					while( iteItem.hasNext() )
					{
						OperateDynamicItem item = iteItem.next();
						if( pkgName.equals( item.mPackageName ) )
						{
							iteItem.remove();
							break;
						}
					}
					if( data.mDynamicItems.size() == 0 )
					{
						ite.remove();
					}
				}
			}
		}
	}
	
	//去重，虚入口优先级大于文件夹内的icon
	public static void removeDuplicate(
			List<OperateDynamicData> dataList )
	{
		if( dataList != null && dataList.size() != 0 )
		{
			List<String> idList = new ArrayList<String>();
			List<String> nameList = new ArrayList<String>();
			List<String> folderList = new ArrayList<String>();
			Iterator<OperateDynamicData> ite = dataList.iterator();
			while( ite.hasNext() )
			{
				OperateDynamicData data = ite.next();
				//去掉空dynamicID和重复dynamicID，去掉重复虚应用或者虚链接
				if( data.dynamicID == null || data.dynamicID.equals( "" ) || idList.contains( data.dynamicID ) )
				{
					ite.remove();
				}
				else
				{
					if( data.dynamicType == VIRTUAL_APP || data.dynamicType == VIRTUAL_LINK )
					{
						if( nameList.contains( data.mPkgnameOrAddr ) )
						{
							ite.remove();
						}
						else
						{
							idList.add( data.dynamicID );
							nameList.add( data.mPkgnameOrAddr );
						}
					}
				}
			}
			idList.clear();
			if( dataList.size() > 0 )
			{
				//虚应用和虚链接和文件夹内的icon去重
				ite = dataList.iterator();
				while( ite.hasNext() )
				{
					OperateDynamicData data = ite.next();
					if( data.dynamicType == FOLDER && data.mDynamicItems.size() > 0 )
					{
						Iterator<OperateDynamicItem> iteItem = data.mDynamicItems.iterator();
						while( iteItem.hasNext() )
						{
							OperateDynamicItem item = iteItem.next();
							if( nameList.contains( item.mPackageName ) )
							{
								iteItem.remove();
							}
							else
							{
								nameList.add( item.mPackageName );
							}
						}
						if( data.mDynamicItems.size() == 0 )
						{
							//ite.remove();
						}
					}
				}
			}
			nameList.clear();
			if( dataList.size() > 0 )
			{
				//文件夹去重
				ite = dataList.iterator();
				while( ite.hasNext() )
				{
					OperateDynamicData data = ite.next();
					if( data.dynamicType == FOLDER )
					{
						if( folderList.contains( data.dynamicID ) )
						{
							ite.remove();
						}
						else
						{
							folderList.add( data.dynamicID );
						}
					}
				}
			}
			folderList.clear();
		}
	}
	
	public static List<ResolveInfo> findActivitiesForPackage(
			Context context ,
			String packageName )
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		mainIntent.setPackage( packageName );
		final List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		return apps != null ? apps : new ArrayList<ResolveInfo>();
	}
	
	public static boolean checkApkExist(
			Context context ,
			String packageName )
	{
		List<ResolveInfo> apps = findActivitiesForPackage( context , packageName );
		if( apps.size() > 0 )
		{
			return true;
		}
		return false;
	}
	
	public static long getServerCurTime(
			Context context )
	{
		// TODO Auto-generated method stub
		long CurTime = 0L;
		//CurTime = System.currentTimeMillis() / 1000;
		try
		{
			HttpURLConnection conn = DlMethod.HttpGet( context , UrlUtil.urlGetTime );
			CurTime = Long.parseLong( new String( DlMethod.bytesFromStream( conn.getInputStream() ) ) );
			conn.disconnect();
		}
		catch( Exception e )
		{
			// TODO: handle exception
			CurTime = 0L;
		}
		return CurTime;
	}
}
