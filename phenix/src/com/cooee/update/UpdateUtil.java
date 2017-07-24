package com.cooee.update;


import java.io.File;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.ShellUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.umeng.analytics.MobclickAgent;


public class UpdateUtil
{
	
	/**
	 * 是否可以更新此Fragment的UI
	 * @return
	 */
	public static boolean FragUpdatable(
			Fragment frag )
	{
		if( frag.isRemoving() || frag.isDetached() || !frag.isAdded() )
		{
			return false;
		}
		return true;
	}
	
	/**
	* install package normal by system intent
	* 
	* @param context
	* @param filePath file path of package
	* @return whether apk exist
	*/
	public static boolean InstallNormalApk(
			Context context ,
			String filePath )
	{
		File file = new File( filePath );
		if( file == null || !file.exists() || !file.isFile() || file.length() <= 0 )
		{
			return false;
		}
		//cheyingkun add start	//自更新完善友盟统计
		//进入安装界面
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( context , UmengStatistics.UPDATE_BY_SELF_INSTALL );
		}
		//cheyingkun add end
		Intent i = new Intent( Intent.ACTION_VIEW );
		i.setDataAndType( Uri.parse( "file://" + filePath ) , "application/vnd.android.package-archive" );
		i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity( i );
		return true;
	}
	
	/**
	* install package normal by system intent
	* 
	* @param context
	* @param filePath file path of package
	* @return whether apk exist
	*/
	public static void InstallPmApk(
			Context context ,
			String filePath )
	{
		File file = new File( filePath );
		if( file == null || !file.exists() || !file.isFile() || file.length() <= 0 )
		{
			return;
		}
		//		filePath = "/storage/sdcard0/dpcheck.apk";
		String cmd = "pm install -r " + filePath;
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "MM" , StringUtils.concat( "InstallPmApk cmd:" , cmd ) );
		String ret = ShellUtils.sync_do_exec( cmd );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( "MM" , StringUtils.concat( "InstallPmApk ret:" , ret ) );
	}
	
	/**
	 * uninstall package error by system intent
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean uninstallErrorApk(
			Context context ,
			String packageName )
	{
		if( context == null || TextUtils.isEmpty( packageName ) )
		{
			return false;
		}
		Uri packageUri = Uri.parse( "package:" + packageName );
		Intent intent = new Intent( Intent.ACTION_DELETE , packageUri );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		context.startActivity( intent );
		return true;
	}
}
