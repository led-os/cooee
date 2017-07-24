package com.iLoong.launcher.MList;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.iLoong.base.themebox.R;

import cool.sdk.download.manager.dl_info;


public class MeApkDlNotifyManager
{
	
	//Class<?> mActivityClass[] = { Main_FirstActivity.class , Main_SecondActivity.class };
	private static MeApkDlNotifyManager instance = null;
	//以微入口为单位的ID初始值，M77 E69 ID 1-4
	private static int meEntryNotifyID = 77690;
	//以APK为单位的ID初始值，M77 E69 ID 1-4 apkinfoID 1-999
	private static int meApkNotifyID = 77690000;
	Context context = null;
	
	public static MeApkDlNotifyManager getInstance(
			Context context )
	{
		synchronized( MeApkDlNotifyManager.class )
		{
			if( instance == null )
			{
				instance = new MeApkDlNotifyManager( context );
			}
		}
		return instance;
	}
	
	public MeApkDlNotifyManager(
			Context context )
	{
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public MeApkDownloadManager GetMeApkMgr(
			int entryID )
	{
		return MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
	}
	
	private Bitmap getApkIconByPkgname(
			MeApkDownloadManager CurMeDlMgr ,
			String pkgName )
	{
		Bitmap iconBitmap = null;
		if( null != CurMeDlMgr.GetSdkIconMgr().IconGetInfo( pkgName ) )
		{
			String ImgPath = CurMeDlMgr.GetSdkIconMgr().IconGetInfo( pkgName ).getFilePath();
			if( null != ImgPath && ImgPath.length() > 3 )
			{
				iconBitmap = BitmapFactory.decodeFile( ImgPath );
			}
		}
		return iconBitmap;
	}
	
	private boolean StartActivityByPackageName(
			String pkgName ,
			Context mContect )
	{
		PackageManager packageManager = mContect.getPackageManager();
		Intent intent = null;
		try
		{
			intent = packageManager.getLaunchIntentForPackage( pkgName );
		}
		catch( Exception e )
		{
			intent = null;
		}
		if( null != intent )
		{
			mContect.startActivity( intent );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void showOnMeApkDlStartNotify(
			int entryID ,
			String moudleName ,
			int downlodingCount )
	{
		int notifyID = meEntryNotifyID + entryID;
		//int[] MeIconArry = {R.drawable.cool_ml_wonderful_game_small , R.drawable.cool_ml_software_small , R.drawable.cool_ml_ku_store_small , R.drawable.cool_ml_know_small };
		//int[] MeBigIconArry = { R.drawable.cool_ml_wonderful_game , R.drawable.cool_ml_software , R.drawable.cool_ml_ku_store , R.drawable.cool_ml_know };
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		Intent notificationIntent = new Intent( context , MEServiceActivity.class );
		notificationIntent.putExtra( "MeServiceType" , MeServiceType.MEApkOnDownloading );
		notificationIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		notificationIntent.putExtra( "moudleName" , moudleName );
		notificationIntent.putExtra( "entryId" , entryID );
		PendingIntent contentItent = PendingIntent.getActivity( context , entryID , notificationIntent , PendingIntent.FLAG_UPDATE_CURRENT );
		if( Integer.parseInt( VERSION.SDK ) >= 11 )
		{
			Notification.Builder builder = new Notification.Builder( context ).setSmallIcon( R.drawable.theme ) //设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap icon)
					.setTicker( downlodingCount + context.getString( R.string.cool_ml_dl_ing ) )//设置在status bar上显示的提示文字
					.setContentTitle( downlodingCount + context.getString( R.string.cool_ml_dl_ing ) )//设置在下拉status bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
					.setContentText( context.getString( R.string.cool_ml_dl_ing_text ) )//TextView中显示的详细内容
					.setContentIntent( contentItent ); //关联PendingIntent
			//.build(); //需要注意build()是在API level 16增加的，可以使用 getNotificatin()来替代
			Notification notification = builder.getNotification();
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notificationManager.notify( notifyID , notification );
		}
		else
		{
			Notification notification = new Notification( R.drawable.theme , downlodingCount + context.getString( R.string.cool_ml_dl_ing ) , System.currentTimeMillis() );
			RemoteViews contentView = new RemoteViews( context.getPackageName() , R.layout.cool_ml_dwonload_notification );
			contentView.setImageViewResource( R.id.cool_ml_notification_image , R.drawable.theme );
			contentView.setTextViewText( R.id.cool_ml_notification_title , downlodingCount + context.getString( R.string.cool_ml_dl_ing ) );
			contentView.setTextViewText( R.id.cool_ml_notification_text , context.getString( R.string.cool_ml_dl_ing_text ) );
			notification.contentView = contentView;
			notification.contentIntent = contentItent;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notificationManager.notify( notifyID , notification );
		}
		MELOG.v( "ME_RTFSC" , "notifyID:" + notifyID + "entryId:" + entryID );
	}
	
	//当每没有下载项的时候，需要调用这个函数取消“正在下载”的notify
	private void CanelOnMeApkDlStartNotify(
			int entryID )
	{
		int notifyID = meEntryNotifyID + entryID;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		notificationManager.cancel( notifyID );
	}
	
	public void onMeApkDlStart(
			int entryID ,
			String moudleName ,
			String PkgName )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "onMeApkDlStart:" + PkgName + "  entryID:" + entryID );
		MeApkDownloadManager CurMeDlMgr = MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
		dl_info info = CurMeDlMgr.GetInfoByPkgName( PkgName );
		int notifyID = meApkNotifyID + entryID * 1000 + info.getID();
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		notificationManager.cancel( notifyID );
		int downlodingCount = CurMeDlMgr.GetDownLoadingApkCount();
		if( downlodingCount > 0 )
		{
			showOnMeApkDlStartNotify( entryID , moudleName , downlodingCount );
		}
	}
	
	public void onMeApkInstalled(
			int entryID ,
			String moudleName ,
			String PkgName )
	{
		// TODO Auto-generated method stub
		try
		{
			//int[] MeIconArry = { R.drawable.cool_ml_wonderful_game_small , R.drawable.cool_ml_software_small , R.drawable.cool_ml_ku_store_small , R.drawable.cool_ml_know_small };
			MeApkDownloadManager CurMeDlMgr = MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
			dl_info info = CurMeDlMgr.GetInfoByPkgName( PkgName );
			int notifyID = meApkNotifyID + PkgName.hashCode();
			String appName = (String)info.getValue( "p101" );
			Bitmap iconBitmap = getApkIconByPkgname( CurMeDlMgr , PkgName );
			NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
			MELOG.v( "ME_RTFSC" , "onMeApkInstalled notifyID =" + notifyID );
			PackageManager packageManager = context.getPackageManager();
			Intent notificationIntent = null;
			try
			{
				notificationIntent = packageManager.getLaunchIntentForPackage( PkgName );
			}
			catch( Exception e )
			{
				notificationIntent = null;
			}
			if( null != notificationIntent )
			{
				PendingIntent contentItent = PendingIntent.getActivity( context , notifyID , notificationIntent , PendingIntent.FLAG_UPDATE_CURRENT );
				if( Integer.parseInt( VERSION.SDK ) >= 11 )
				{
					Notification.Builder builder = new Notification.Builder( context ).setSmallIcon( R.drawable.theme_small ) //设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap icon)
							.setLargeIcon( iconBitmap ).setTicker( appName + context.getString( R.string.cool_ml_dl_installed ) )//设置在status bar上显示的提示文字
							.setContentTitle( appName + context.getString( R.string.cool_ml_dl_installed ) )//设置在下拉status bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
							.setContentText( context.getString( R.string.cool_ml_dl_installed_text ) )//TextView中显示的详细内容
							.setContentIntent( contentItent ); //关联PendingIntent
					//.build(); //需要注意build()是在API level 16增加的，可以使用 getNotificatin()来替代
					Notification notification = builder.getNotification();
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					MELOG.v( "ME_RTFSC" , "Send onMeApkInstalled Notification:" + notifyID );
					notificationManager.notify( notifyID , notification );
				}
				else
				{
					Notification notification = new Notification( R.drawable.theme , appName + context.getString( R.string.cool_ml_dl_installed ) , System.currentTimeMillis() );
					RemoteViews contentView = new RemoteViews( context.getPackageName() , R.layout.cool_ml_dwonload_notification );
					contentView.setImageViewBitmap( R.id.cool_ml_notification_image , iconBitmap );
					contentView.setTextViewText( R.id.cool_ml_notification_title , appName + context.getString( R.string.cool_ml_dl_installed ) );
					contentView.setTextViewText( R.id.cool_ml_notification_text , context.getString( R.string.cool_ml_dl_installed_text ) );
					notification.contentView = contentView;
					notification.contentIntent = contentItent;
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					MELOG.v( "ME_RTFSC" , "Send onMeApkDlStop Notification:" + notifyID );
					notificationManager.notify( notifyID , notification );
				}
			}
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
	}
	
	public void onMeApkUninstallCanel(
			String pkgName )
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		int notifyID = meApkNotifyID + pkgName.hashCode();
		MELOG.v( "ME_RTFSC" , "onMeApkUninstallCanel notifyID =" + notifyID );
		notificationManager.cancel( notifyID );
	}
	
	public void onMeApkDlSucess(
			int entryID ,
			String moudleName ,
			String PkgName ,
			dl_info info )
	{
		//先清除meApkNotifyID + entryID * 1000 + info.getID()格式的 notifyID
		int notifyID = meApkNotifyID + entryID * 1000 + info.getID();
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		notificationManager.cancel( notifyID );
		//创建meApkNotifyID + PkgName.hashCode(); 格式的 notifyID
		notifyID = meApkNotifyID + PkgName.hashCode();
		//int[] MeIconArry = { R.drawable.cool_ml_wonderful_game_small , R.drawable.cool_ml_software_small , R.drawable.cool_ml_ku_store_small , R.drawable.cool_ml_know_small };
		MeApkDownloadManager CurMeDlMgr = MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
		String appName = (String)info.getValue( "p101" );
		Bitmap iconBitmap = getApkIconByPkgname( CurMeDlMgr , PkgName );
		Intent notificationIntent = new Intent( context , MEServiceActivity.class );
		notificationIntent.putExtra( "MeServiceType" , MeServiceType.MEApkOnSucess );
		notificationIntent.putExtra( "moudleName" , moudleName );
		//		notificationIntent.putExtra( "PkgName" , PkgName );
		//		notificationIntent.putExtra( "entryID" , entryID );
		notificationIntent.putExtra( "FilePath" , info.getFilePath() );
		PendingIntent contentItent = PendingIntent.getActivity( context , notifyID , notificationIntent , PendingIntent.FLAG_UPDATE_CURRENT );
		if( Integer.parseInt( VERSION.SDK ) >= 11 )
		{
			Notification.Builder builder = new Notification.Builder( context ).setSmallIcon( R.drawable.theme_small ) //设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap icon)
					.setLargeIcon( iconBitmap ).setTicker( appName + context.getString( R.string.cool_ml_dl_sucess ) )//设置在status bar上显示的提示文字
					.setContentTitle( appName + context.getString( R.string.cool_ml_dl_sucess ) )//设置在下拉status bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
					.setContentText( context.getString( R.string.cool_ml_dl_sucess_text ) )//TextView中显示的详细内容
					.setContentIntent( contentItent ); //关联PendingIntent
			//.build(); //需要注意build()是在API level 16增加的，可以使用 getNotificatin()来替代
			Notification myNotify = builder.getNotification();
			MELOG.v( "ME_RTFSC" , "Send onMeApkDlSucess Notification:" + notifyID );
			notificationManager.notify( notifyID , myNotify );
		}
		else
		{
			Notification notification = new Notification( R.drawable.theme , appName + appName + context.getString( R.string.cool_ml_dl_sucess ) , System.currentTimeMillis() );
			RemoteViews contentView = new RemoteViews( context.getPackageName() , R.layout.cool_ml_dwonload_notification );
			contentView.setImageViewBitmap( R.id.cool_ml_notification_image , iconBitmap );
			contentView.setTextViewText( R.id.cool_ml_notification_title , appName + context.getString( R.string.cool_ml_dl_sucess ) );
			contentView.setTextViewText( R.id.cool_ml_notification_text , context.getString( R.string.cool_ml_dl_sucess_text ) );
			notification.contentView = contentView;
			notification.contentIntent = contentItent;
			MELOG.v( "ME_RTFSC" , "Send onMeApkDlStop Notification:" + notifyID );
			notificationManager.notify( notifyID , notification );
		}
		int downlodingCount = CurMeDlMgr.GetDownLoadingApkCount();
		if( downlodingCount > 0 )
		{
			showOnMeApkDlStartNotify( entryID , moudleName , downlodingCount );
		}
		else
		{
			CanelOnMeApkDlStartNotify( entryID );
		}
	}
	
	public void onMeApkDlDel(
			int entryID ,
			String moudleName ,
			String PkgName )
	{
		MeApkDownloadManager CurMeDlMgr = MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		dl_info info = CurMeDlMgr.GetInfoByPkgName( PkgName );
		int notifyID = meApkNotifyID + entryID * 1000 + info.getID();
		notificationManager.cancel( notifyID );
		int downlodingCount = CurMeDlMgr.GetDownLoadingApkCount();
		if( downlodingCount > 0 )
		{
			showOnMeApkDlStartNotify( entryID , moudleName , downlodingCount );
		}
		else
		{
			CanelOnMeApkDlStartNotify( entryID );
		}
	}
	
	public void onMeApkDlStop(
			int entryID ,
			String moudleName ,
			String PkgName )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "onMeApkDlStop:" + PkgName + "  entryID:" + entryID );
		//int[] MeIconArry = { R.drawable.cool_ml_wonderful_game_small , R.drawable.cool_ml_software_small , R.drawable.cool_ml_ku_store_small , R.drawable.cool_ml_know_small };
		MeApkDownloadManager CurMeDlMgr = MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
		dl_info info = CurMeDlMgr.GetInfoByPkgName( PkgName );
		int notifyID = meApkNotifyID + entryID * 1000 + info.getID();
		String appName = (String)info.getValue( "p101" );
		Bitmap iconBitmap = getApkIconByPkgname( CurMeDlMgr , PkgName );
		Intent notificationIntent = new Intent( context , MEServiceActivity.class );
		notificationIntent.putExtra( "MeServiceType" , MeServiceType.MEApkOnNotifyReStart );
		notificationIntent.putExtra( "moudleName" , moudleName );
		notificationIntent.putExtra( "entryID" , entryID );
		notificationIntent.putExtra( "PkgName" , PkgName );
		NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
		PendingIntent contentItent = PendingIntent.getActivity( context , notifyID , notificationIntent , PendingIntent.FLAG_UPDATE_CURRENT );
		if( Integer.parseInt( VERSION.SDK ) >= 11 )
		{
			Notification.Builder builder = new Notification.Builder( context ).setSmallIcon( R.drawable.theme_small ) //设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap icon)
					.setLargeIcon( iconBitmap ).setTicker( appName + context.getString( R.string.cool_ml_dl_stop ) )//设置在status bar上显示的提示文字
					.setContentTitle( appName + context.getString( R.string.cool_ml_dl_stop ) )//设置在下拉status bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
					.setContentText( context.getString( R.string.cool_ml_dl_stop_text ) )//TextView中显示的详细内容
					.setContentIntent( contentItent ); //关联PendingIntent
			//.build(); //需要注意build()是在API level 16增加的，可以使用 getNotificatin()来替代
			Notification notification = builder.getNotification();
			MELOG.v( "ME_RTFSC" , "Send onMeApkDlStop Notification:" + notifyID );
			notificationManager.notify( notifyID , notification );
		}
		else
		{
			Notification notification = new Notification( R.drawable.theme , appName + context.getString( R.string.cool_ml_dl_stop ) , System.currentTimeMillis() );
			RemoteViews contentView = new RemoteViews( context.getPackageName() , R.layout.cool_ml_dwonload_notification );
			contentView.setImageViewBitmap( R.id.cool_ml_notification_image , iconBitmap );
			contentView.setTextViewText( R.id.cool_ml_notification_title , appName + context.getString( R.string.cool_ml_dl_stop ) );
			contentView.setTextViewText( R.id.cool_ml_notification_text , context.getString( R.string.cool_ml_dl_stop_text ) );
			notification.contentView = contentView;
			notification.contentIntent = contentItent;
			MELOG.v( "ME_RTFSC" , "Send onMeApkDlStop Notification:" + notifyID );
			notificationManager.notify( notifyID , notification );
		}
		int downlodingCount = CurMeDlMgr.GetDownLoadingApkCount();
		if( downlodingCount > 0 )
		{
			showOnMeApkDlStartNotify( entryID , moudleName , downlodingCount );
		}
		else
		{
			CanelOnMeApkDlStartNotify( entryID );
		}
	}
	
	public void onMeApkDlFailed(
			int entryID ,
			String moudleName ,
			String PkgName ,
			dl_info info )
	{
		MELOG.v( "ME_RTFSC" , "onMeApkDlFailed:" + PkgName + "  entryID:" + entryID );
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				int failedInfoID = R.string.cool_ml_download_failed;
				// TODO: handle exception
				if( false == JSClass.IsNetworkAvailableLocal( context.getApplicationContext() ) )
				{
					failedInfoID = R.string.cool_ml_network_not_available;
				}
				else if( false == JSClass.IsStorageCanUsed() )
				{
					failedInfoID = R.string.cool_ml_storage_not_available;
				}
				else
				{
					failedInfoID = R.string.cool_ml_download_failed;
				}
				Looper.prepare();
				Toast.makeText( context.getApplicationContext() , failedInfoID , Toast.LENGTH_SHORT ).show();
				Looper.loop();
			}
		} ).start();
		if( info != null )
		{
			MeApkDownloadManager CurMeDlMgr = MeApkDlMgrBuilder.GetMeApkDownloadManager( entryID );
			int notifyID = meApkNotifyID + entryID * 1000 + info.getID();
			//int[] MeIconArry = { R.drawable.cool_ml_wonderful_game_small , R.drawable.cool_ml_software_small , R.drawable.cool_ml_ku_store_small , R.drawable.cool_ml_know_small };
			String appName = (String)info.getValue( "p101" );
			Bitmap iconBitmap = getApkIconByPkgname( CurMeDlMgr , PkgName );
			NotificationManager notificationManager = (NotificationManager)context.getSystemService( android.content.Context.NOTIFICATION_SERVICE );
			Intent notificationIntent = new Intent( context , MEServiceActivity.class );
			notificationIntent.putExtra( "MeServiceType" , MeServiceType.MEApkOnNotifyReStart );
			notificationIntent.putExtra( "moudleName" , moudleName );
			notificationIntent.putExtra( "entryID" , entryID );
			notificationIntent.putExtra( "PkgName" , PkgName );
			PendingIntent contentItent = PendingIntent.getActivity( context , notifyID , notificationIntent , PendingIntent.FLAG_UPDATE_CURRENT );
			if( Integer.parseInt( VERSION.SDK ) >= 11 )
			{
				Notification.Builder builder = new Notification.Builder( context ).setSmallIcon( R.drawable.theme_small ) //设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap icon)
						.setLargeIcon( iconBitmap ).setTicker( appName + context.getString( R.string.cool_ml_dl_failed ) )//设置在status bar上显示的提示文字
						.setContentTitle( appName + context.getString( R.string.cool_ml_dl_failed ) )//设置在下拉status bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
						.setContentText( context.getString( R.string.cool_ml_dl_failed_text ) )//TextView中显示的详细内容
						.setContentIntent( contentItent ); //关联PendingIntent
				//.build(); //需要注意build()是在API level 16增加的，可以使用 getNotificatin()来替代
				Notification myNotify = builder.getNotification();
				//myNotify.flags |= Notification.FLAG_NO_CLEAR; 
				notificationManager.notify( notifyID , myNotify );
			}
			else
			{
				Notification notification = new Notification( R.drawable.theme , appName + context.getString( R.string.cool_ml_dl_failed ) , System.currentTimeMillis() );
				RemoteViews contentView = new RemoteViews( context.getPackageName() , R.layout.cool_ml_dwonload_notification );
				contentView.setImageViewBitmap( R.id.cool_ml_notification_image , iconBitmap );
				contentView.setTextViewText( R.id.cool_ml_notification_title , appName + context.getString( R.string.cool_ml_dl_failed ) );
				contentView.setTextViewText( R.id.cool_ml_notification_text , context.getString( R.string.cool_ml_dl_failed_text ) );
				notification.contentView = contentView;
				notification.contentIntent = contentItent;
				MELOG.v( "ME_RTFSC" , "Send onMeApkDlStop Notification:" + notifyID );
				notificationManager.notify( notifyID , notification );
			}
			MELOG.v( "ME_RTFSCX" , "notifyID:" + notifyID + "filepath:" + info.getFilePath() );
			int downlodingCount = CurMeDlMgr.GetDownLoadingApkCount();
			if( downlodingCount > 0 )
			{
				showOnMeApkDlStartNotify( entryID , moudleName , downlodingCount );
			}
			else
			{
				CanelOnMeApkDlStartNotify( entryID );
			}
		}
	}
}
