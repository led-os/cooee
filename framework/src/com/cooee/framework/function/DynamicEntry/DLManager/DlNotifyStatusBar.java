package com.cooee.framework.function.DynamicEntry.DLManager;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;


public class DlNotifyStatusBar implements DlObserverInterface
{
	
	private Context mContext;
	private Handler handler;
	NotificationManager mNotificationManager;
	
	public DlNotifyStatusBar(
			Context context ,
			String title )
	{
		mContext = context;
		initHandle();
		mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
	}
	
	@Override
	public void update(
			DownloadingItem dlItem )
	{
		// TODO Auto-generated method stub
		if( DlManager.getInstance().getDownloadingItem( dlItem.packageName ) == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "DlManager" , StringUtils.concat( "DLManager update our pkgName:" , dlItem.packageName , "-id:" , dlItem.notifyID , "-state:" , dlItem.state ) );
			return;
		}
		if( dlItem.state == Constants.DL_STATUS_ING )
		{
			//doStatusIng( dlItem );
			if( handler != null )
			{
				handler.sendMessage( Message.obtain( handler , Constants.DL_STATUS_ING , dlItem ) );
			}
		}
		else if( dlItem.state == Constants.DL_STATUS_SUCCESS )
		{
			//doStatusSuccess( dlItem );
			if( handler != null )
			{
				handler.sendMessage( Message.obtain( handler , Constants.DL_STATUS_SUCCESS , dlItem ) );
			}
		}
		else if( dlItem.state == Constants.DL_STATUS_FAIL )
		{
			//doStatusFail( dlItem );
			if( handler != null )
			{
				handler.sendMessage( Message.obtain( handler , Constants.DL_STATUS_FAIL , dlItem ) );
			}
		}
		else if( dlItem.state == Constants.DL_STATUS_PAUSE )
		{
			//doStatusFail( dlItem );
			if( handler != null )
			{
				handler.sendMessage( Message.obtain( handler , Constants.DL_STATUS_PAUSE , dlItem ) );
			}
		}
	}
	
	// this function run on UI Thread , to update UI content
	private void doStatusIng(
			DownloadingItem dlItem )
	{
		if( !needShowNotification( dlItem ) )
		{
			return;
		}
		Notification notification = dlItem.notification;
		notification.icon = R.drawable.download;
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		RemoteViews contentView = notification.contentView;
		String titleString = null;
		if( OperateDynamicUtils.getCurLanguage() == 0 )
		{
			if( dlItem.isWifiReDownload )
			{
				titleString = StringUtils.concat( "Downloading " , titleString , " " , BaseDefaultConfig.getString( R.string.dynamic_wifi_re_download ) );
			}
			else
			{
				titleString = StringUtils.concat( BaseDefaultConfig.getString( R.string.notify_downloading ) , " " , dlItem.title );
			}
		}
		else
		{
			if( dlItem.isWifiReDownload )
			{
				titleString = StringUtils.concat( BaseDefaultConfig.getString( R.string.dynamic_wifi_re_download ) , dlItem.title );
			}
			else
			{
				titleString = StringUtils.concat( dlItem.title , BaseDefaultConfig.getString( R.string.notify_downloading ) );
			}
		}
		contentView.setTextViewText( R.id.notificationTitle , titleString );
		contentView.setViewVisibility( R.id.notificationContent , View.INVISIBLE );
		contentView.setViewVisibility( R.id.notificationProgress , View.VISIBLE );
		contentView.setViewVisibility( R.id.notificationPercent , View.VISIBLE );
		contentView.setTextViewText( R.id.notificationPercent , StringUtils.concat( dlItem.progress , "%" ) );
		contentView.setProgressBar( R.id.notificationProgress , 100 , dlItem.progress , dlItem.progress == 0 );
		notifyActivityDoing( mContext , notification , dlItem.packageName , dlItem.title , dlItem.notifyID );
		mNotificationManager.notify( dlItem.notifyID , (Notification)notification );
	}
	
	private void doStatusSuccess(
			DownloadingItem dlItem )
	{
		String className = OperateDynamicUtils.getClassName( mContext , dlItem.packageName );
		String notifyText = null;
		if( OperateDynamicUtils.getCurLanguage() == 0 )
		{
			notifyText = StringUtils.concat( dlItem.title , " " , BaseDefaultConfig.getString( R.string.notify_download_finish ) );
		}
		else
		{
			notifyText = StringUtils.concat( dlItem.title , BaseDefaultConfig.getString( R.string.notify_download_finish ) );
		}
		boolean isInstalled = false;
		if( className != null )
		{
			isInstalled = true;
		}
		else
		{
			isInstalled = false;
		}
		Notification notification = dlItem.notification;
		notification.icon = R.drawable.download;
		RemoteViews contentView1 = notification.contentView;
		contentView1.setTextViewText( R.id.notificationTitle , notifyText );
		contentView1.setViewVisibility( R.id.notificationContent , View.VISIBLE );
		contentView1.setTextViewText( R.id.notificationContent , BaseDefaultConfig.getString( R.string.dialog_click_install ) );
		contentView1.setViewVisibility( R.id.notificationPercent , View.INVISIBLE );
		contentView1.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
		notifyAcitivitySuccess( mContext , notification , dlItem.packageName , dlItem.filePath , dlItem.notifyID , isInstalled , className );
		mNotificationManager.notify( dlItem.notifyID , (Notification)notification );
	}
	
	private void doStatusFail(
			DownloadingItem dlItem )
	{
		if( !needShowNotification( dlItem ) )
		{
			return;
		}
		Notification notification = dlItem.notification;
		RemoteViews contentView2 = notification.contentView;
		notification.icon = R.drawable.download;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "ontify " , StringUtils.concat( "ontify = " , dlItem.notifyID , ",packageName" , dlItem.packageName ) );
		String titleString = null;
		if( OperateDynamicUtils.getCurLanguage() == 0 )
		{
			titleString = StringUtils.concat( dlItem.title , " " , BaseDefaultConfig.getString( R.string.notify_download_fail ) );
		}
		else
		{
			titleString = StringUtils.concat( dlItem.title , BaseDefaultConfig.getString( R.string.notify_download_fail ) );
		}
		contentView2.setTextViewText( R.id.notificationTitle , titleString );
		contentView2.setViewVisibility( R.id.notificationContent , View.VISIBLE );
		contentView2.setTextViewText( R.id.notificationContent , BaseDefaultConfig.getString( R.string.dialog_check_network ) );
		contentView2.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
		contentView2.setViewVisibility( R.id.notificationPercent , View.INVISIBLE );
		notifyActivityFailure( mContext , notification , dlItem.packageName , dlItem.title , dlItem.notifyID );
		mNotificationManager.notify( dlItem.notifyID , notification );
		OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
		client.upateDownloadItemState( dlItem.packageName , Constants.DL_STATUS_FAIL );
	}
	
	private boolean needShowNotification(
			DownloadingItem dlItem )
	{
		if( dlItem.notifyID == Constants.DL_INVALID_NOTIFYID )
		{
			return false;
		}
		return true;
	}
	
	private void doStatusPause(
			DownloadingItem dlItem )
	{
		Notification notification = dlItem.notification;
		notification.icon = R.drawable.download;
		notification.flags = 0;
		RemoteViews contentView = notification.contentView;
		String titleString = null;
		if( OperateDynamicUtils.getCurLanguage() == 0 )
		{
			titleString = StringUtils.concat( dlItem.title , " " , BaseDefaultConfig.getString( R.string.dynamic_download_app_paused ) );
		}
		else
		{
			titleString = StringUtils.concat( dlItem.title , BaseDefaultConfig.getString( R.string.dynamic_download_app_paused ) );
		}
		contentView.setViewVisibility( R.id.notificationContent , View.INVISIBLE );
		contentView.setTextViewText( R.id.notificationTitle , titleString );
		contentView.setTextViewText( R.id.notificationPercent , StringUtils.concat( dlItem.progress , "%" ) );
		contentView.setProgressBar( R.id.notificationProgress , 100 , dlItem.progress , false );
		notifyActivityPause( mContext , notification , dlItem.packageName , dlItem.title , dlItem.notifyID );
		mNotificationManager.notify( dlItem.notifyID , (Notification)notification );
	}
	
	private void notifyActivityPause(
			final Context context ,
			final Notification notification ,
			String packageName ,
			String title ,
			int notifyID )
	{
		if( context == null )
		{
			return;
		}
		Intent intent = new Intent();
		intent.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
		Bundle bundle = new Bundle();
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "DlManager" , StringUtils.concat( "notifyActivityPause packageName:" , packageName , "-id:" , notifyID ) );
		bundle.putString( Constants.PKG_NAME , packageName );
		bundle.putString( Constants.APK_TITLE , title );
		bundle.putString( Constants.MSG , Constants.MSG_PAUSE );
		bundle.putInt( Constants.NOTIFY_ID , notifyID );
		bundle.putParcelable( Constants.NOTIFY , notification );
		intent.putExtras( bundle );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		PendingIntent contentIntent = PendingIntent.getActivity( context , notifyID , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.flags = 0;
		notification.contentIntent = contentIntent;
	}
	
	private void notifyActivityFailure(
			final Context context ,
			final Notification notification ,
			String packageName ,
			String title ,
			int notifyID )
	{
		if( context == null )
		{
			return;
		}
		Intent intent = new Intent();
		intent.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
		Bundle bundle = new Bundle();
		bundle.putString( Constants.PKG_NAME , packageName );
		bundle.putString( Constants.APK_TITLE , title );
		bundle.putString( Constants.MSG , Constants.MSG_FAILURE );
		bundle.putInt( Constants.NOTIFY_ID , notifyID );
		bundle.putParcelable( Constants.NOTIFY , notification );
		intent.putExtras( bundle );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		PendingIntent contentIntent = PendingIntent.getActivity( context , notifyID , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.flags = 0;
		notification.contentIntent = contentIntent;
	}
	
	private void notifyAcitivitySuccess(
			final Context context ,
			final Notification notification ,
			String packageName ,
			String filePath ,
			int notifyID ,
			boolean isInstalled ,
			String className )
	{
		if( context == null )
		{
			return;
		}
		Intent intent = new Intent();
		intent.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
		Bundle bundle = new Bundle();
		bundle.putString( Constants.PKG_NAME , packageName );
		bundle.putString( Constants.APK_PATH , filePath );
		bundle.putString( Constants.MSG , Constants.MSG_SUCCESS );
		bundle.putInt( Constants.NOTIFY_ID , notifyID );
		bundle.putString( Constants.APK_CLASSNAME , className );
		bundle.putBoolean( Constants.APK_INSTALLED , isInstalled );
		bundle.putParcelable( Constants.NOTIFY , notification );
		intent.putExtras( bundle );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		PendingIntent contentIntent = PendingIntent.getActivity( context , notifyID , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.flags = Notification.FLAG_NO_CLEAR;
		notification.contentIntent = contentIntent;
	}
	
	private void notifyActivityDoing(
			final Context context ,
			final Notification notification ,
			String packageName ,
			String title ,
			int notifyId )
	{
		Intent intent = new Intent();
		intent.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
		Bundle bundle = new Bundle();
		bundle.putInt( Constants.NOTIFY_ID , notifyId );
		bundle.putString( Constants.PKG_NAME , packageName );
		bundle.putString( Constants.APK_TITLE , title );
		bundle.putString( Constants.MSG , Constants.MSG_DOING );
		intent.putExtras( bundle );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
		PendingIntent contentIntent = PendingIntent.getActivity( context , notifyId , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.flags = 0;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.contentIntent = contentIntent;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "DlManager" , StringUtils.concat( "notifyActivityDoing packageName:" , packageName , "-id:" , notifyId ) );
	}
	
	private void initHandle()
	{
		if( handler == null )
		{
			Runnable runnable = new Runnable() {
				
				@Override
				public void run()
				{
					handler = new Handler() {
						
						@Override
						public void handleMessage(
								Message msg )
						{
							super.handleMessage( msg );
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( "ontify " , StringUtils.concat( "msg.what:" , msg.what ) );
							switch( msg.what )
							{
								case Constants.DL_STATUS_ING:
									DownloadingItem item = (DownloadingItem)msg.obj;
									if( item.isWifiReDownload )
									{
										doStatusIng( (DownloadingItem)msg.obj );
									}
									else
									{
										DlManager.getInstance().getDialogHandle().doStatusDownloading( (DownloadingItem)msg.obj );
									}
									break;
								case Constants.DL_STATUS_SUCCESS:
									doStatusSuccess( (DownloadingItem)msg.obj );
									DlManager.getInstance().getDialogHandle().doStatusDownloading( (DownloadingItem)msg.obj );
									break;
								case Constants.DL_STATUS_FAIL:
									doStatusFail( (DownloadingItem)msg.obj );
									DlManager.getInstance().getDialogHandle().doStatusDownloading( (DownloadingItem)msg.obj );
									break;
								case Constants.DL_STATUS_PAUSE:
									doStatusPause( (DownloadingItem)msg.obj );
									DlManager.getInstance().getDialogHandle().doStatusDownloading( (DownloadingItem)msg.obj );
									break;
							}
						}
					};
				}
			};
			BaseAppState.getActivityInstance().runOnUiThread( runnable );
		}
	}
}
