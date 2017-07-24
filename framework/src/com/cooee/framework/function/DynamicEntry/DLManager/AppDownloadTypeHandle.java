package com.cooee.framework.function.DynamicEntry.DLManager;


import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;


public class AppDownloadTypeHandle
{
	
	public static final String APPSTORE_PAKAGENAME_PREFIX = "market://details?id=";
	
	public static boolean startAppStoreDownload(
			Intent intent ,
			String pkgName )
	{
		if( isAppStoreDownloadItem( intent ) )
		{
			try
			{
				BaseAppState.getActivityInstance().startActivity( new Intent( Intent.ACTION_VIEW ).setData( Uri.parse( StringUtils.concat( APPSTORE_PAKAGENAME_PREFIX , pkgName ) ) ) );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( "apk" , "appstore start" );
				return true;
			}
			catch( Exception e2 )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( "apk" , StringUtils.concat( "start appstore Activity error:" , e2.getMessage() ) );
				return false;
				// TODO: handle exception
			}
		}
		return false;
	}
	
	public static boolean isAppStoreDownloadItem(
			Intent intent )
	{
		int downloadType = intent.getIntExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , OperateDynamicUtils.NORMAL_DOWNLOAD );
		if( downloadType == OperateDynamicUtils.WIFI_APPSTORE_DOWNLOAD || downloadType == OperateDynamicUtils.APPSTORE_DOWNLOAD )
		{
			return true;
		}
		return false;
	}
	
	public static boolean isMeEntryVirtualApp(
			Intent intent )
	{
		int downloadType = intent.getIntExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , OperateDynamicUtils.NORMAL_DOWNLOAD );
		if( ( downloadType & OperateDynamicUtils.ME_ENTRY_FLAG ) == OperateDynamicUtils.ME_ENTRY_FLAG )
		{
			return true;
		}
		return false;
	}
}
