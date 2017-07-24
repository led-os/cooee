package com.cooee.framework.function.DynamicEntry.SADownload;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.SAManager.SACoolDLMgr.NotifyType;
import cool.sdk.download.manager.dl_info;


public class SASingleNotification extends SANotification
{
	
	public SASingleNotification(
			Context context ,
			NotificationManager notificationManager )
	{
		super( context , notificationManager );
		// TODO Auto-generated constructor stub
	}
	
	private class CustomNotifuctionSASingleView extends RemoteViews
	{
		
		public CustomNotifuctionSASingleView(
				Context context ,
				Bitmap icon ,
				String appName )
		{
			super( context.getPackageName() , R.layout.operate_sa_download_notifaction );
			Resources mResources = context.getResources();
			this.setViewVisibility( R.id.notificationContent , View.VISIBLE );
			this.setViewVisibility( R.id.download_app_list , View.INVISIBLE );
			this.setViewVisibility( R.id.btn_check , View.INVISIBLE );
			this.setViewVisibility( R.id.btn_run , View.VISIBLE );
			this.setTextViewText( R.id.notificationTitle , mResources.getText( R.string.notification_silent_download_title_single ) );
			this.setTextViewText( R.id.notificationContent , StringUtils.concat( mResources.getText( R.string.notification_silent_download_content_single ) , appName ) );
			this.setImageViewBitmap( R.id.notificationImage , icon );
		}
	}
	
	public void showSASingleNotification(
			NotifyType type ,
			dl_info info )
	{
		int intType = 0;
		switch( type )
		{
			case T1://未下载完所有
				intType = 1;
				break;
			case T2://下载完所有
				intType = 2;
				break;
			case T3://T2没有点击，3天后显示
				intType = 3;
				break;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "COOL" , StringUtils.concat( "showSASingleNotification type:" , intType ) );
		String packageName = (String)info.getValue( "p2" );
		Bitmap icon = DlManager.getInstance().getDownloadHandle().getDownBitmap( packageName );
		String appName = null;
		appName = DlManager.getInstance().getWifiSAHandle().getTitleName( info );
		String filePath = info.getFilePath();
		Notification.Builder mBuilder = new Notification.Builder( context );
		mBuilder.setSmallIcon( OperateDynamicProxy.getLauncherIcon() );
		// gaominghui@2016/12/14 ADD START
		Notification notification;
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
		{
			notification = mBuilder.build();
		}
		else
		{
			notification = mBuilder.getNotification();
		}
		// gaominghui@2016/12/14 ADD END
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = new CustomNotifuctionSASingleView( context , icon , appName );
		Intent intent = new Intent();
		intent.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
		Bundle bundle = new Bundle();
		bundle.putString( Constants.MSG , Constants.MSG_WIFI_SA );
		bundle.putInt( Constants.SINGLE_OR_MUTIPLE , Constants.SILENT_SINGLE );
		bundle.putString( Constants.FILEPATH_FLAG , filePath );
		bundle.putString( Constants.PKG_NAME , packageName );
		bundle.putInt( Constants.SILENT_TYPE , intType );
		bundle.putInt( Constants.NOTIFY_ID , Single_Notification_Id_Base + info.getID() );
		bundle.putParcelable( Constants.NOTIFY , notification );
		intent.putExtras( bundle );
		PendingIntent contentIntent = PendingIntent.getActivity( context , Single_Notification_Id_Base + info.getID() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.contentIntent = contentIntent;
		notificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "COOL" , "Single notify  display" );
		notificationManager.cancel( Single_Notification_Id_Base + info.getID() );
		//多个的也取消掉。重新刷新
		notificationManager.cancel( Mutiple_SA_notification_id );
		notificationManager.notify( Single_Notification_Id_Base + info.getID() , notification );
	}
}
