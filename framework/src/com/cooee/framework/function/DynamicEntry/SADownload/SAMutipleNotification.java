package com.cooee.framework.function.DynamicEntry.SADownload;


import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
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
import cool.sdk.log.CoolLog;


public class SAMutipleNotification extends SANotification
{
	
	public SAMutipleNotification(
			Context context ,
			NotificationManager notificationManager )
	{
		super( context , notificationManager );
		// TODO Auto-generated constructor stub
	}
	
	private class CustomNotifuctionSAMutipleView extends RemoteViews
	{
		
		private int[] iconid = new int[]{ R.id.icon_1 , R.id.icon_2 , R.id.icon_3 , R.id.icon_4 , R.id.icon_5 };
		
		public CustomNotifuctionSAMutipleView(
				Context context ,
				List<dl_info> infolist )
		{
			super( context.getPackageName() , R.layout.operate_sa_download_notifaction );
			String titlestr = BaseDefaultConfig.getString( R.string.notification_silent_download_title_mutiple );
			SpannableStringBuilder strBuilder = new SpannableStringBuilder( StringUtils.concat( infolist.size() , titlestr ) );
			ForegroundColorSpan greenSpan = new ForegroundColorSpan( Color.GREEN );
			//			ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
			if( infolist.size() < 10 )
			{
				strBuilder.setSpan( greenSpan , 0 , 1 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
			}
			else
			{
				strBuilder.setSpan( greenSpan , 0 , 2 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
			}
			this.setTextViewText( R.id.notificationTitle , strBuilder );
			this.setViewVisibility( R.id.notificationContent , View.INVISIBLE );
			this.setViewVisibility( R.id.download_app_list , View.VISIBLE );
			this.setViewVisibility( R.id.btn_check , View.VISIBLE );
			this.setViewVisibility( R.id.btn_run , View.INVISIBLE );
			this.setImageViewResource( R.id.notificationImage , OperateDynamicProxy.getLauncherIcon() );
			if( infolist.size() >= iconid.length )
			{
				for( int i = 0 ; i < iconid.length ; i++ )
				{
					String packageName = (String)infolist.get( i ).getValue( "p2" );
					Bitmap icon = DlManager.getInstance().getDownloadHandle().getDownBitmap( packageName );
					new CoolLog( context ).v( "icon" , "Bitmap:" + icon );
					this.setImageViewBitmap( iconid[i] , icon );
				}
				this.setViewVisibility( R.id.text_icon , View.VISIBLE );
			}
			else
			{
				for( int i = 0 ; i < infolist.size() ; i++ )
				{
					String packageName = (String)infolist.get( i ).getValue( "p2" );
					Bitmap icon = DlManager.getInstance().getDownloadHandle().getDownBitmap( packageName );
					this.setImageViewBitmap( iconid[i] , icon );
				}
				this.setViewVisibility( R.id.text_icon , View.INVISIBLE );
			}
		}
	}
	
	public void showSAMutipleNotification(
			NotifyType type ,
			List<dl_info> infolist )
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
		notification.contentView = new CustomNotifuctionSAMutipleView( context , infolist );
		Intent intent = new Intent();
		intent.setClassName( context.getPackageName() , Constants.NOTIFY_ACTIVITY_CLASS_NAME );
		Bundle bundle = new Bundle();
		bundle.putString( Constants.MSG , Constants.MSG_WIFI_SA );
		bundle.putInt( Constants.SINGLE_OR_MUTIPLE , Constants.SILENT_MUTIPLE );
		bundle.putInt( Constants.SILENT_TYPE , intType );
		bundle.putInt( Constants.NOTIFY_ID , Mutiple_SA_notification_id );
		bundle.putInt( Constants.SHOW_WHICH_VIEW , Constants.SHOW_INSTALL_VIEW );
		bundle.putParcelable( Constants.NOTIFY , notification );
		intent.putExtras( bundle );
		PendingIntent contentIntent = PendingIntent.getActivity( context , Mutiple_SA_notification_id , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		notification.contentIntent = contentIntent;
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.cancel( Mutiple_SA_notification_id );
		clearAllNotify( mNotificationManager , infolist );
		mNotificationManager.notify( Mutiple_SA_notification_id , notification );
	}
	
	//取消原来弹过的单个Notify,以免原来T1弹出来的单条NOTIFY又显示在多个应用里面
	private void clearAllNotify(
			NotificationManager mNotificationManager ,
			List<dl_info> infolist )
	{
		for( dl_info info : infolist )
		{
			mNotificationManager.cancel( Single_Notification_Id_Base + info.getID() );
		}
	}
}
