package com.cooee.phenix.Functions.Category;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.EnhanceItemInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.data.VirtualInfo;


// 添加智能分类功能 , change by shlt@2015/02/27 UPD START
public class ActivityUtils
{
	
	private static String TAG = "ActivityUtils";
	
	// update form launcher.java method startActivity() 
	public static boolean startActivitySafely(
			Launcher launcher ,
			View v ,
			Intent intent ,
			Object tag )
	{
		boolean success = false;
		boolean isVirtualItem = false;//xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//<数据库字段更新> liuhailin@2015-03-24 modify begin
		EnhanceItemInfo info = null;
		boolean isOperateVirtualItem = false;
		boolean isOperateVirtualMoreAppItem = false;
		if( tag instanceof EnhanceItemInfo )
		{
			info = (EnhanceItemInfo)tag;
		}
		if( info != null && info.getOperateIntent() != null )
		{
			isOperateVirtualItem = info.isOperateVirtualItem();
		}
		//xiatian add start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		if( info != null && info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )
		{
			isVirtualItem = true;
		}
		//xiatian add end
		if( isOperateVirtualItem )
		//<数据库字段更新> liuhailin@2015-03-24 modify end
		{
			isOperateVirtualMoreAppItem = info.isOperateVirtualMoreAppItem();
			if( isOperateVirtualMoreAppItem )
			{
				OperateHelp.getInstance( launcher ).enterMoreAppUI( (ShortcutInfo)tag );
			}
			else
			{
				if( v instanceof BubbleTextView && tag instanceof ShortcutInfo )
					DlManager.getInstance().getDownloadHandle().DownloadApkBegin( launcher , info.getIntent() , ( (TextView)v ).getText().toString() , ( (ShortcutInfo)tag ).getIcon() );
				else
					OperateHelp.getInstance( launcher ).downloadApp( (ShortcutInfo)tag );
			}
			success = true;
		}
		//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//		else//xiatian del
		//xiatian add start
		else if( isVirtualItem )
		{//虚图标的onClick，自己统一处理。
			if( info instanceof ShortcutInfo )
			{
				VirtualInfo mVirtualInfo = ( (ShortcutInfo)info ).makeVirtual();
				success = mVirtualInfo.onClick( launcher );
			}
		}
		else if( info instanceof ShortcutInfo )
		{
			ShortcutInfo si = (ShortcutInfo)info;
			if( si.isOperateIconItem() && v instanceof BubbleTextView )
			{
				DlManager.getInstance().getDownloadHandle().DownloadApkBegin( launcher , si.getIntent() , ( (BubbleTextView)v ).getText().toString() , si.getIcon() );
				success = true;
			}
		}
		if( success == true )
		{//虚图标的onClick返回false，则走正常打开activity的流程
			return success;
		}
		//xiatian add end
		//xiatian end
		{
			if( intent == null )
			{
				return false;
			}
			try
			{
				success = launcher.startActivity( v , intent , tag );
			}
			catch( ActivityNotFoundException e )
			{
				Toast.makeText( launcher , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "Unable to launch. tag=" , tag , " intent=" , intent.toUri( 0 ) , e ) );
			}
		}
		return success;
	}
}
