package com.cooee.util;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;


public class NotificationUtils
{
	
	private static int notificationLayoutId = R.layout.notificationlayout;
	private static int notificationTitleId = R.id.notificationTitle;
	private static int notificationPercentId = R.id.notificationPercent;
	private static int notificationProgressId = R.id.notificationProgress;
	private static int msgNotifyDownloading = R.string.notify_downloading;
	private static int msgNotifyDownloadFail = R.string.notify_download_fail;
	private static int msgNotifyDownloadFinish = R.string.notify_download_finish;
	private static int icDownloadDrawable = R.drawable.download_notification_icon;
	private static int notifyID = 0;
	
	public static class NotificationInfo
	{
		
		private int curNotifyID = 0;
		private Notification notification = null;
		
		public NotificationInfo(
				int notifyID ,
				Notification notification )
		{
			this.curNotifyID = notifyID;
			this.notification = notification;
		}
		
		public int getNotifyID()
		{
			return curNotifyID;
		}
		
		public Notification getNotification()
		{
			return notification;
		}
	}
	
	public static NotificationInfo getNotification(
			Context context ,
			String title )
	{
		notifyID++;
		//
		Notification notification = new Notification( android.R.drawable.stat_sys_download , title , System.currentTimeMillis() );
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		RemoteViews contentView = new RemoteViews( context.getPackageName() , notificationLayoutId );
		contentView.setTextViewText( notificationTitleId , StringUtils.concat( context.getResources().getString( msgNotifyDownloading ) , title ) );
		contentView.setTextViewText( notificationPercentId , "0%" );
		contentView.setProgressBar( notificationProgressId , 100 , 0 , true );
		notification.contentView = contentView;
		Intent intent = new Intent();
		intent.putExtra( "OperateFolderNotifyID" , notifyID );
		PendingIntent contentIntent = PendingIntent.getActivity( context , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.contentIntent = contentIntent;
		//
		NotificationInfo notificationInfo = new NotificationInfo( notifyID , notification );
		return notificationInfo;
	}
	
	public static void updateNotificationByDownloadFail(
			Context context ,
			String title ,
			NotificationInfo notificationInfo )
	{
		Notification notification = notificationInfo.getNotification();
		notification.icon = icDownloadDrawable;
		notification.flags = 0;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		RemoteViews contentView = notification.contentView;
		contentView.setViewVisibility( notificationProgressId , View.INVISIBLE );
		contentView.setTextViewText( notificationTitleId , StringUtils.concat( title , context.getResources().getString( msgNotifyDownloadFail ) ) );
	}
	
	public static void updateNotificationByDownloading(
			Context context ,
			int progress ,
			NotificationInfo notificationInfo )
	{
		Notification notification = notificationInfo.getNotification();
		RemoteViews contentView = notification.contentView;
		contentView.setTextViewText( notificationPercentId , StringUtils.concat( progress , "%" ) );
		contentView.setProgressBar( notificationProgressId , 100 , progress , false );
	}
}
