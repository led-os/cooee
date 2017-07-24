package com.cooee.framework.function.DynamicEntry.SADownload;


import android.app.NotificationManager;
import android.content.Context;


public class SANotification
{
	
	public static final int Mutiple_SA_notification_id = 1212;
	public static final int Single_Notification_Id_Base = 800;//从1000开始增长。以免跟普通下次的ID相同了
	protected Context context;
	protected NotificationManager notificationManager;
	
	public SANotification(
			Context context ,
			NotificationManager notificationManager )
	{
		this.context = context;
		this.notificationManager = notificationManager;
	}
}
