package com.cooee.framework.function.DynamicEntry.DLManager;


import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.DynamicEntry.DynamicEntry;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class SystemBroadcastReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(
			Context arg0 ,
			Intent arg1 )
	{
		// TODO Auto-generated method stub
		//处理下载中，使用T卡存储状态，返回LAUNCHER，LAUNCHER重启，存储状态，把下载的应用全部STOP
		if( arg1.getAction().equals( Intent.ACTION_MEDIA_EJECT ) )
		{
			if( BaseAppState.getActivityInstance() == null )
			{
				return;
			}
			CoolDLMgr mDlMgr = DynamicEntry.CoolDLMgr( BaseAppState.getActivityInstance() , "DAPP" );
			if( mDlMgr != null )
			{
				List<dl_info> ApkTaskList = mDlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
				if( ApkTaskList != null )
				{
					for( dl_info info : ApkTaskList )
					{
						if( !info.IsDownloadSuccess() && 0 != info.getDownloadState() )
						{
							int state = info.getDownloadState();
							String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
							if( pkgName != null )
							{
								DlManager.getInstance().pauseAppDownload( pkgName );
							}
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "COOL" , StringUtils.concat( "pakName:" , pkgName , "-state:" , state ) );
						}
					}
				}
			}
		}
		else if( arg1.getAction().equals( Intent.ACTION_MEDIA_MOUNTED ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "COOL" , "COOL mounted" );
		}
	}
}
