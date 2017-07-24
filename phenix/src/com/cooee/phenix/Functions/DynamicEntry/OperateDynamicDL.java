package com.cooee.phenix.Functions.DynamicEntry;


import java.io.File;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.NotificationUtils;
import com.cooee.util.NotificationUtils.NotificationInfo;

import cool.sdk.Category.CategoryHelper;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class OperateDynamicDL
{
	
	private Activity mActivity = null;
	public final static String OPERATE_FOLDER_DOWNLOAD_DONE_ACTION = "operateFolderLoadDone";//添加在应用已经下载了100%的时候发送广播使用 wanghongjian add
	public final static String OPERATE_FOLDER_PKG_NAME_KEY = "operateFolderPkgNameKey";
	
	public OperateDynamicDL(
			Activity activity )
	{
		mActivity = activity;
	}
	
	/**
	 * 下载app应用
	 * @param apkInfo
	 */
	public void downloadApp(
			final Context context ,
			final String packName ,
			final String title )
	{
		if( LauncherAppState.getInstance().isSDCardExist() == false )
		{
			Toast.makeText( context , LauncherDefaultConfig.getString( R.string.category_download_fail ) , Toast.LENGTH_LONG ).show();//cheyingkun add
			return;
		}
		CoolDLMgr dlMgr = CategoryHelper.getInstance( context ).getCoolDLMgrApk();
		dl_info dlInfo = dlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , packName );
		final NotificationInfo notificationInfo = NotificationUtils.getNotification( context , title.toString() );
		if( dlInfo != null )
		{
			if( dlInfo.IsDownloadSuccess() )
			{
				installApk( dlInfo.getFilePath() , mActivity );
				return;
			}
			else if( dlInfo.getDownloadState() != 0 )
			{
				Toast.makeText( context , R.string.downloading_toast , Toast.LENGTH_SHORT ).show();
				return;
			}
		}
		final NotificationManager mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.notify( notificationInfo.getNotifyID() , notificationInfo.getNotification() );
		dlMgr.ResDownloadStart( CoolDLResType.RES_TYPE_APK , packName , new CoolDLCallback() {
			
			@Override
			public void onSuccess(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				installApk( arg2.getFilePath() , mActivity );
				mNotificationManager.cancel( notificationInfo.getNotifyID() );
				sendBroadcastTolauncher( packName );
			}
			
			@Override
			public void onFail(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				NotificationUtils.updateNotificationByDownloadFail( context , title , notificationInfo );
				mNotificationManager.notify( notificationInfo.getNotifyID() , notificationInfo.getNotification() );
			}
			
			@Override
			public void onDoing(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				int progress = (int)( 100 * arg2.getCurBytes() / arg2.getTotalBytes() );
				NotificationUtils.updateNotificationByDownloading( context , progress , notificationInfo );
				mNotificationManager.notify( notificationInfo.getNotifyID() , notificationInfo.getNotification() );
			}
		} );
	}
	
	private void installApk(
			String mFilePath ,
			Activity activity )
	{
		Intent intent = new Intent( Intent.ACTION_VIEW );
		intent.setDataAndType( Uri.fromFile( new File( mFilePath ) ) , "application/vnd.android.package-archive" );
		activity.startActivity( intent );
	}
	
	/**
	 * 当下载完成以后发送广播给launcher
	 * @param packName
	 */
	private void sendBroadcastTolauncher(
			String packName )
	{
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance( mActivity );
		Intent intent = new Intent();
		intent.setAction( OPERATE_FOLDER_DOWNLOAD_DONE_ACTION );
		intent.putExtra( OPERATE_FOLDER_PKG_NAME_KEY , packName );
		localBroadcastManager.sendBroadcast( intent );
	}
}
