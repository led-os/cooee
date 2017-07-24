package com.cooee.update;


import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.update.taskManager.Listener;
import com.cooee.update.taskManager.TaskManager;
import com.cooee.update.taskManager.TaskResult;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.Uiupdate.UiupdateHelper;
import cool.sdk.download.manager.DlMethod;


/**
 * @author zhangjin
 *此类负责下载管理，和下载通知栏相关的管理
 */
public class UpdateDownloadManager
{
	
	public static int NOFITY_ID = 20150701;
	private static final String TAG = "UpdateUi.UpdateDownloadManager";
	public static String KEY_FIND_NEW = "FIND_NEW";
	public static String KEY_NOFITY_ID = "NOFITY_ID";
	protected static UpdateDownloadManager instance;
	private DownloadTask mDownTask;
	private Context mContext = null;
	private Context mGlobalContext;
	private NotificationManager mNotificationManager;
	private Notification mDownloadNotify;
	private UpdateNotifyDialog mNotifyDialog = null;
	private boolean isDowning = false;//cheyingkun add	//自更新完善友盟统计
	
	protected UpdateDownloadManager(
			Context context )
	{
		mContext = context;
	}
	
	public static UpdateDownloadManager getInstance(
			Context context )
	{
		if( instance == null )
		{
			synchronized( UpdateDownloadManager.class )
			{
				if( instance == null )
				{
					instance = new UpdateDownloadManager( context );
				}
			}
		}
		return instance;
	}
	
	public DownloadTask getDownTask()
	{
		return mDownTask;
	}
	
	public DownloadTask startDownload()
	{
		//cheyingkun add start	//自更新完善友盟统计
		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG && !isDowning )
		{
			MobclickAgent.onEvent( mContext , UmengStatistics.UPDATE_BY_SELF_DOWN );
			isDowning = true;
		}
		//cheyingkun add end
		if( mDownTask == null )
		{
			mDownTask = new DownloadTask( mContext , null );
			mDownTask.addListener( mDownListener );
		}
		if( mDownTask.getRunState() == false )
		{
			//使用这句话可以节约一个线程开销
			//			mDownTask.runInBack();
			TaskManager.execute( mDownTask );
		}
		showNotify();
		return mDownTask;
	}
	
	public void stopDownload()
	{
		isDowning = false;//cheyingkun add	//自更新完善友盟统计
		// TODO Auto-generated method stub	
		mDownTask.stopDownload();
		cancelNotify();
		mDownTask = null;//chenliang add	//解决“‘关于桌面’下点击‘立即体验’按钮后长按按钮，在弹出的界面点击‘取消更新’，再退出界面，重新进入界面后，按钮显示为‘暂停更新’”的问题。
	}
	
	public void pauseDownload()
	{
		mDownTask.pauseDownload();
	}
	
	private Listener mDownListener = new Listener() {
		
		@Override
		public void onProgress(
				Object ... progress )
		{
			super.onProgress( progress );
			Long pro = (Long)progress[0];
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "down progress: " , pro ) );
			updateNotifyProgress( pro.intValue() );
		}
		
		@Override
		public void onResult(
				TaskResult result )
		{
			TaskResult res = result;
			//-1:下载失败，其他值下载成功
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "down result is " , res.mCode ) );
			if( res.mCode != -1 )
			{
				//下载成功
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "down result success" );
				final String apkPath = getApkPath();
				updateNotifySuccess();
				UpdateUtil.InstallNormalApk( getGlobalContext() , apkPath );
				mDownTask = null;//chenliang add	//解决“‘关于桌面’下点击‘立即体验’按钮后长按按钮，在弹出的界面点击‘取消更新’，再退出界面，重新进入界面后，按钮显示为‘暂停更新’”的问题。
			}
			else
			{
				// 下载中断或者失败
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "down result fail" );
				updateNotifyFail();
			}
			isDowning = false;//cheyingkun add	//自更新完善友盟统计
		}
	};
	
	/**
	 * 更新下载进度
	 */
	private void updateNotifyProgress(
			int progress )
	{
		//step2 更新通知栏
		Notification notify = getNotification();
		if( notify != null )
		{
			RemoteViews contentView = new RemoteViews( getGlobalContext().getPackageName() , R.layout.uiupdate_notify );
			String findView = LauncherDefaultConfig.getString( R.string.updateNotifyTitle );
			contentView.setViewVisibility( R.id.notification_pb , View.VISIBLE );
			contentView.setTextViewText( R.id.notification_downloading_titlev , findView );
			contentView.setTextViewText( R.id.notification_downloading_pbtext , progress + "%" );
			contentView.setProgressBar( R.id.notification_downloading_pb , 100 , progress , false );
			notify.contentView = contentView;
			Intent intent = new Intent();
			intent.setComponent( new ComponentName( getGlobalContext() , UpdateActivity.class ) );
			intent.putExtra( KEY_FIND_NEW , true );
			intent.putExtra( KEY_NOFITY_ID , NOFITY_ID );
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			PendingIntent contentIntent = PendingIntent.getActivity( getGlobalContext() , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			notify.contentIntent = contentIntent;
			NotificationManager manager = this.getNotificationManager();
			manager.notify( NOFITY_ID , notify );
		}
	}
	
	/**
	 * 更新通知栏，下载成功
	 */
	private void updateNotifySuccess()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , " update notification success" );
		Notification notify = getNotification();
		if( notify != null )
		{
			RemoteViews contentView = new RemoteViews( getGlobalContext().getPackageName() , R.layout.uiupdate_notify );
			String downSuccess = LauncherDefaultConfig.getString( R.string.updateNotifyDownSuccess );
			contentView.setTextViewText( R.id.notification_downloading_titlev , downSuccess );
			contentView.setViewVisibility( R.id.notification_pb , View.GONE );
			notify.contentView = contentView;
			Intent intent = new Intent();
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			intent.setAction( android.content.Intent.ACTION_VIEW );
			intent.setDataAndType( Uri.fromFile( new File( this.getApkPath() ) ) , "application/vnd.android.package-archive" );
			PendingIntent contentIntent = PendingIntent.getActivity( getGlobalContext() , 0 , intent , 0 );
			notify.contentIntent = contentIntent;
			NotificationManager manager = this.getNotificationManager();
			manager.notify( NOFITY_ID , notify );
		}
	}
	
	/**
	 * 更新通知栏，下载失败
	 */
	private void updateNotifyFail()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , " update notification fail" );
		Notification notify = getNotification();
		if( notify != null )
		{
			RemoteViews contentView = new RemoteViews( mContext.getPackageName() , R.layout.uiupdate_notify );
			String downFail = LauncherDefaultConfig.getString( R.string.updateNotifyDownFail );
			contentView.setTextViewText( R.id.notification_downloading_titlev , downFail );
			contentView.setViewVisibility( R.id.notification_pb , View.GONE );
			notify.contentView = contentView;
			Intent intent = new Intent();
			intent.setComponent( new ComponentName( getGlobalContext() , UpdateActivity.class ) );
			intent.putExtra( KEY_FIND_NEW , true );
			intent.putExtra( KEY_NOFITY_ID , NOFITY_ID );
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			PendingIntent contentIntent = PendingIntent.getActivity( getGlobalContext() , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			notify.contentIntent = contentIntent;
			NotificationManager manager = getNotificationManager();
			manager.notify( NOFITY_ID , notify );
		}
	}
	
	private NotificationManager getNotificationManager()
	{
		if( mNotificationManager == null )
		{
			mNotificationManager = (NotificationManager)getGlobalContext().getSystemService( Context.NOTIFICATION_SERVICE );
		}
		return mNotificationManager;
	}
	
	private Context getGlobalContext()
	{
		if( mGlobalContext == null )
		{
			mGlobalContext = UpdateUiManager.getInstance().getGlobalContext();
		}
		return mGlobalContext;
	}
	
	private Notification getNotification()
	{
		if( mDownloadNotify == null )
		{
			Context context = getGlobalContext();
			if( context == null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "get notification context is null " );
				return mDownloadNotify;
			}
			Notification notification = new Notification();
			notification.icon = R.mipmap.ic_launcher_home;
			notification.when = System.currentTimeMillis();
			notification.tickerText = LauncherDefaultConfig.getString( R.string.updateNotifyTicker );
			//			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			RemoteViews contentView = new RemoteViews( context.getPackageName() , R.layout.uiupdate_notify );
			contentView.setTextViewText( R.id.notification_downloading_pbtext , "0%" );
			contentView.setProgressBar( R.id.notification_downloading_pb , 100 , 0 , false );
			notification.contentView = contentView;
			Intent intent = new Intent();
			intent.setComponent( new ComponentName( context , UpdateActivity.class ) );
			intent.putExtra( KEY_FIND_NEW , true );
			intent.putExtra( KEY_NOFITY_ID , NOFITY_ID );
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			PendingIntent contentIntent = PendingIntent.getActivity( context , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			notification.contentIntent = contentIntent;
			mDownloadNotify = notification;
		}
		return mDownloadNotify;
	}
	
	private String getApkPath()
	{
		String path;
		path = UiupdateHelper.getInstance( getGlobalContext() ).getApkPath();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "apk:" , path ) );
		return path;
	}
	
	/**
	 * 显示通知栏
	 * @param context
	 */
	private void showNotify()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , " show notification " );
		// zhangjin@2015/12/16 UPD START
		//		Notification notify = getNotification();
		//		if( notify != null )
		//		{
		//			NotificationManager manager = this.getNotificationManager();
		//			manager.notify( NOFITY_ID , notify );
		//		}
		updateNotifyProgress( UiupdateHelper.getInstance( getGlobalContext() ).getDownProgress() );
		// zhangjin@2015/12/16 UPD END
	}
	
	/**
	 * 取消通知栏
	 */
	public void cancelNotify()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , " cancel notification " );
		NotificationManager manager = this.getNotificationManager();
		manager.cancel( NOFITY_ID );
	}
	
	public void showUpdateDialog()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "showUpdateDialog begin" );
		if( mNotifyDialog != null && mNotifyDialog.isShowing() )
		{
			return;
		}
		final Activity mainActivity = (Activity)getGlobalContext();
		mainActivity.runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "showUpdateDialog begin show" );
				if( mNotifyDialog != null && mNotifyDialog.isShowing() )
				{
					return;
				}
				//lvjiangbin add  start 0014431: 【仿S5版本】调节时间后，点击一键壁纸，在换壁纸过程中，自更新提示弹出，一键壁纸图标与自更新框重叠.
				if( !LauncherAppState.getActivityInstance().hasWindowFocus() )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , "excuteUpdatePromptTask no focus ,return" );
					return;
				}
				//lvjiangbin add end 0014431: 【仿S5版本】调节时间后，点击一键壁纸，在换壁纸过程中，自更新提示弹出，一键壁纸图标与自更新框重叠.
				mNotifyDialog = new UpdateNotifyDialog( mainActivity );
				mNotifyDialog.setUpdateList( getUpdateContent() );
				mNotifyDialog.setOnUpdateClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						onDlgBtnClick();
					}
				} );
				mNotifyDialog.show();
				//cheyingkun add start	//自更新添加友盟统计
				if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
				{//自更新对话框
					HashMap<String , String> map = new HashMap<String , String>();
					map.put( UmengStatistics.UPDATE_BY_SELF_TITLE , UmengStatistics.UPDATE_BY_SELF_DIALOG );
					MobclickAgent.onEvent( mContext , UmengStatistics.UPDATE_BY_SELF , map );
				}
				//cheyingkun add end	//自更新添加友盟统计
			}
		} );
	}
	
	/**
	 * 点击下载时，提示当前为gprs下载
	 */
	private void showGprsDlgWhenDownload(
			final Runnable runable ,
			final Runnable cancelRunnable )
	{
		AlertDialog.Builder buidler = new AlertDialog.Builder( getGlobalContext() );
		buidler.setTitle( LauncherDefaultConfig.getString( R.string.updateNotifyTicker ) );
		buidler.setMessage( LauncherDefaultConfig.getString( R.string.updateGprsDlgText ) );
		buidler.setPositiveButton( LauncherDefaultConfig.getString( R.string.updateDialogYes ) , new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				// TODO Auto-generated method stub
				runable.run();
			}
		} );
		buidler.setNegativeButton( LauncherDefaultConfig.getString( R.string.updateDialogNo ) , new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(
					DialogInterface dialog ,
					int which )
			{
				// TODO Auto-generated method stub
				if( cancelRunnable != null )
				{
					cancelRunnable.run();
				}
			}
		} );
		buidler.create().show();
	}
	
	private void onDlgBtnClick()
	{
		if( UiupdateHelper.getInstance( getGlobalContext() ).newDataHasDown() )
		{
			//直接安装
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "updateBtnClick install " );
			String path;
			path = UiupdateHelper.getInstance( getGlobalContext() ).getApkPath();
			boolean install = UpdateUtil.InstallNormalApk( getGlobalContext() , path );
			if( install )
			{
				return;
			}
		}
		//开启下载流程
		Runnable runnable = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				UpdateDownloadManager.getInstance( getGlobalContext() ).startDownload();
			}
		};
		Runnable cancel = new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mNotifyDialog != null )
				{
					mNotifyDialog.dismiss();
				}
			}
		};
		if( DlMethod.IsWifiConnected( getGlobalContext() ) == false && DlMethod.IsNetworkAvailable( getGlobalContext() ) )
		{
			showGprsDlgWhenDownload( runnable , cancel );
		}
		else
		{
			runnable.run();
		}
	}
	
	/**
	 * 设置更新内容
	 */
	private String getUpdateContent()
	{
		String content = UiupdateHelper.getInstance( getGlobalContext() ).getUpdateContent();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "update content:" , content ) );
		content = content.replace( "\\n" , "\n" );
		return content;
	}
	
	public void closeNotifyDialog()
	{
		if( mNotifyDialog != null && mNotifyDialog.isShowing() )
		{
			mNotifyDialog.dismiss();
		}
	}
	
	public void resetAllPrompt()
	{
		cancelNotify();
		closeNotifyDialog();
	}
}
