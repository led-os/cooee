package com.cooee.framework.function.DynamicEntry.DLManager;


import java.io.File;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.SAManager.SAHelper;


public class DlNotifyActivity extends Activity
{
	
	private String mPackageName;
	private String mTitle;
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		openWindowView();
	}
	
	private void openWindowView()
	{
		try
		{
			Bundle bundle = getIntent().getExtras();
			setVisible( false );
			String Msg = bundle.getString( Constants.MSG );
			if( Msg.equals( Constants.MSG_DOING ) )
			{
				doMsgDoing( bundle );
			}
			else if( Msg.equals( Constants.MSG_SUCCESS ) )
			{
				doMsgSuccess( bundle );
				//doMsgDoing( bundle );
			}
			else if( Msg.equals( Constants.MSG_FAILURE ) )
			{
				doMsgFailure( bundle );
			}
			else if( Msg.equals( Constants.MSG_PAUSE ) )
			{
				doMsgPause( bundle );
			}
			else if( Msg.equals( Constants.MSG_WIFI_SA ) )
			{
				doMsgWifiSA( bundle );
			}
			else if( Msg.equals( Constants.MSG_DL_INSTALL ) )
			{
				DlManager.getInstance().getDialogHandle().doMsgDLinstall( this.getApplication() , bundle );
			}
			else if( Msg.equals( Constants.MSG_ALL_DLING ) )
			{
				DlManager.getInstance().getDialogHandle().doMsgAllDLing( this.getApplication() , bundle );
			}
		}
		finally
		{
			finish();
		}
	}
	
	private void doMsgFailure(
			Bundle bundle )
	{
		mPackageName = bundle.getString( Constants.PKG_NAME );
		mTitle = bundle.getString( Constants.APK_TITLE );
		DlManager.getInstance().downloadFile( BaseAppState.getActivityInstance() , mTitle , mPackageName , false );
		final Notification notification = (Notification)bundle.get( Constants.NOTIFY );
		RemoteViews contentView1 = notification.contentView;
		contentView1.setViewVisibility( R.id.notificationProgress , View.VISIBLE );
		contentView1.setViewVisibility( R.id.notificationPercent , View.VISIBLE );
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		NotificationManager mNotificationManager = (NotificationManager)this.getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.notify( bundle.getInt( Constants.NOTIFY_ID ) , notification );
		startDlManageActivity( bundle.getInt( Constants.NOTIFY_ID ) );
	}
	
	//���ý������ع�����ֱ�ӽ��밲װ�б�
	private void doMsgWifiSA(
			Bundle bundle )
	{
		int SA_type = bundle.getInt( Constants.SINGLE_OR_MUTIPLE );
		SAHelper.getInstance( BaseAppState.getActivityInstance() ).clickNotify( bundle.getInt( Constants.SILENT_TYPE ) );
		if( SA_type == Constants.SILENT_SINGLE )
		{
			Intent intentTemp = new Intent();
			String pkgName = bundle.getString( Constants.PKG_NAME );
			String filePath = bundle.getString( Constants.FILEPATH_FLAG );
			intentTemp.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			intentTemp.setAction( android.content.Intent.ACTION_VIEW );
			intentTemp.setDataAndType( Uri.fromFile( new File( filePath ) ) , "application/vnd.android.package-archive" );
			BaseAppState.getActivityInstance().startActivity( intentTemp );
			//	DynamicEntryHandle.getWifiSilentHandle().addInfoToDownloadList();
		}
		else
		{
			int notifyID = bundle.getInt( Constants.NOTIFY_ID );
			Intent intentTemp = new Intent();
			intentTemp.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
			intentTemp.putExtra( "OperateFolderNotifyID" , notifyID );
			intentTemp.putExtra( "msg" , "msgWifiSA" );
			intentTemp.putExtra( "moudleName" , "DAPP" );
			intentTemp.putExtra( Constants.SHOW_WHICH_VIEW , Constants.SHOW_INSTALL_VIEW );
			intentTemp.setClassName( getApplicationContext() , Constants.DLLIST_ACTIVITY_CLASS_NAME );
			//	DynamicEntryHandle.getWifiSilentHandle().addMutipleInfoToDownloadList();
			BaseAppState.getActivityInstance().startActivity( intentTemp );
		}
	}
	
	private void doMsgPause(
			Bundle bundle )
	{
		mPackageName = bundle.getString( Constants.PKG_NAME );
		mTitle = bundle.getString( Constants.APK_TITLE );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "DlManager" , StringUtils.concat( "doMsgPause myPID:" , Process.myPid() , "-mPackageName:" , mPackageName ) );
		//		DlManager.getInstance().downloadFile( BaseAppState.getActivityInstance() , mTitle , mPackageName , true );
		//		final Notification notification = (Notification)bundle.get( Constants.NOTIFY );
		//		RemoteViews contentView1 = notification.contentView;
		//		contentView1.setViewVisibility( R.id.notificationProgress , View.VISIBLE );
		//		contentView1.setViewVisibility( R.id.notificationPercent , View.VISIBLE );
		//		notification.flags = Notification.FLAG_ONGOING_EVENT;
		//		NotificationManager mNotificationManager = (NotificationManager)this.getSystemService( Context.NOTIFICATION_SERVICE );
		//		mNotificationManager.notify( bundle.getInt( Constants.NOTIFY_ID ) , notification );
		startDlManageActivity( bundle.getInt( Constants.NOTIFY_ID ) );
	}
	
	private void startDlManageActivity(
			int notifyID )
	{
		Intent intent = new Intent();
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		//intent.setClassName( context , "com.iLoong.launcher.desktop.iLoongLauncher" );
		intent.putExtra( "OperateFolderNotifyID" , notifyID );
		intent.putExtra( "packageName" , mPackageName );
		intent.putExtra( "moudleName" , "DAPP" );
		intent.setClassName( getApplicationContext() , Constants.DLLIST_ACTIVITY_CLASS_NAME );
		startActivity( intent );
	}
	
	private void doMsgDoing(
			Bundle bundle )
	{
		mPackageName = bundle.getString( Constants.PKG_NAME );
		mTitle = bundle.getString( Constants.APK_TITLE );
		startDlManageActivity( bundle.getInt( Constants.NOTIFY_ID ) );
	}
	
	private void doMsgSuccess(
			Bundle bundle )
	{
		mPackageName = bundle.getString( Constants.PKG_NAME );
		String filePath = bundle.getString( Constants.APK_PATH );
		boolean isInstalled = bundle.getBoolean( Constants.APK_INSTALLED );
		Intent intent = new Intent();
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.addFlags( Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
		intent.setAction( android.content.Intent.ACTION_VIEW );
		if( isInstalled )
		{
			String className = bundle.getString( Constants.APK_CLASSNAME );
			intent.setClassName( mPackageName , className );
		}
		else
		{
			intent.setDataAndType( Uri.fromFile( new File( filePath ) ) , "application/vnd.android.package-archive" );
		}
		if( !isInstalled && OperateDynamicUtils.checkApkExist( this , mPackageName ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "NotifyActivity" , StringUtils.concat( "doMsgSuccess return directly [isInstalled==false] pkgName:" , mPackageName ) );
			return;
		}
		startActivity( intent );
		//		final Notification notification = (Notification)bundle.get( Constants.NOTIFY );
		//		RemoteViews contentView1 = notification.contentView;
		//		contentView1.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
		//		contentView1.setViewVisibility( R.id.notificationPercent , View.INVISIBLE );
		//		notification.flags = 0;
		NotificationManager mNotificationManager = (NotificationManager)this.getSystemService( Context.NOTIFICATION_SERVICE );
		//		mNotificationManager.notify( bundle.getInt( Constants.NOTIFY_ID ) , notification );
		mNotificationManager.cancel( bundle.getInt( Constants.NOTIFY_ID ) );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "NotifyActivity" , StringUtils.concat( "doMsgSuccess notifyid:" , bundle.getInt( Constants.NOTIFY_ID ) , "-mPackageName:" , mPackageName ) );
	}
}
