package com.cooee.framework.function.DynamicEntry.DLManager;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.manager.dl_info;


public class ReDownloadHelperHandle
{
	
	private static boolean ReDownloadFlag = false;
	CoolDLMgr dlMgr;
	private final String START_ICON_DPREFERENCE_KEY = "DynamicEntryConfig";
	public static String WIFI_DOWNLOAD_PREFIX = "WIFI_Continue_Download_";
	
	protected ReDownloadHelperHandle()
	{
	}
	
	//调用该函数获取用户非手动停止下载项列表并重新下载
	public void startImproperStopTasks(
			Context context )
	{
		new Thread() {
			
			@Override
			public void run()
			{
				if( BaseAppState.isWifiEnabled( BaseAppState.getActivityInstance() ) )
				{
					if( !getReDownloadFlag() )
					{
						setReDownloadFlag( true );
						dlMgr = DlManager.getInstance().getDlMgr();
						if( dlMgr == null )
						{
							return;
						}
						List<dl_info> continueDownloadAppList = dlMgr.ResGetTaskListNeedDownload();
						for( dl_info info : continueDownloadAppList )
						{
							String title = (String)info.getValue( "p101" );
							String pkgName = (String)info.getValue( "p2" );
							if( NeedDownload( pkgName ) )
							{
								DlManager.getInstance().downloadFile( BaseAppState.getActivityInstance() , title , pkgName , true );
							}
						}
						setReDownloadFlag( false );
					}
				}
			}
		}.start();
	}
	
	public boolean NeedDownload(
			String pkgName )
	{
		if( pkgName == null )
		{
			return true;
		}
		String value = getValue( StringUtils.concat( WIFI_DOWNLOAD_PREFIX , pkgName ) );
		if( value != null && value.equals( String.valueOf( false ) ) )
		{
			return false;
		}
		return true;
	}
	
	public void setReDownloadFlag(
			boolean value )
	{
		ReDownloadFlag = value;
	}
	
	public boolean getReDownloadFlag()
	{
		return ReDownloadFlag;
	}
	
	public String getTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyy年MM月dd日   HH:mm:ss" );
		Date curDate = new Date( System.currentTimeMillis() );//获取当前时间     
		String strtime = formatter.format( curDate );
		return strtime;
	}
	
	public String getValue(
			String key )
	{
		SharedPreferences preferences = getPreferences();
		if( key != null )
		{
			String value = preferences.getString( key , null );
			return value;
		}
		return null;
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
