package com.coco.theme.themebox;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.coco.download.CustomerHttpClient;
import com.coco.download.DownloadList;
import com.coco.download.ResultEntity;
import com.coco.font.fontbox.FontPreviewActivity;
import com.coco.lock2.lockbox.preview.PreviewHotActivity;
import com.coco.pub.provider.PubContentProvider;
import com.coco.scene.scenebox.preview.ScenePreviewHotActivity;
import com.coco.shortcut.shortcutbox.HotOperateService;
import com.coco.theme.themebox.apprecommend.Profile;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.database.model.DownloadThemeItem;
import com.coco.theme.themebox.database.model.ThemeInfoItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.database.service.HotService;
import com.coco.theme.themebox.service.ThemesDB;
import com.coco.theme.themebox.util.DownApkNode;
import com.coco.theme.themebox.util.DownType;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.PathTool;
import com.coco.wallpaper.wallpaperbox.LiveWallpaperPreviewActivity;
import com.coco.wallpaper.wallpaperbox.WallpaperPreviewActivity;
import com.coco.widget.widgetbox.WidgetPreviewHotActivity;
import com.iLoong.base.themebox.R;


public class DownloadApkContentService extends Service
{
	
	private final String LOG_TAG = "ApkDownload";
	private Context mContext;
	private Object syncObject = new Object();
	private List<DownApkNode> downApkList = new ArrayList<DownApkNode>();
	private List<Intent> notifyIntentList = new ArrayList<Intent>();
	private DownloadThemeService downApkDb;
	private DownloadApkThread downApkThread = null;
	private final int id = 1000;
	private Notification notification;
	private NotificationManager mNotificationManager;
	private RemoteViews mContentView;
	public static final int MSG_UPDATE_PROGRESS = 0;
	public static final int MSG_DOWNLOAD_PAUSE = 1;
	public static final int MSG_SUCCESS = 2;
	public static final int MSG_START_DOWNLOAD = 3;
	public static final int MSG_CANCEL_INDICATE = 4;
	public static final int MSG_SD_FULL = 5;
	public static final int MSG_TIMEOUT_EXCEPTION = 6;
	public final int REQUEST_ACTION_URL = 1301;
	//	public final String SERVER_URL_TEST = "http://uifolder.coolauncher.com.cn/iloong/pui/ServicesEngine/DataService";
	//<c_0000707> liuhailin@2014-08-11 modify begin
	public static boolean isDownloadingAPK = false;
	//<c_0000707> liuhailin@2014-08-11 modify end
	private Handler mMainHandler;
	private String language = null;
	private String appName_en = null;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mContext = this;
		downApkDb = new DownloadThemeService( this );
	}
	
	@Override
	public IBinder onBind(
			Intent intent )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onStart(
			Intent intent ,
			int startId )
	{
		// TODO Auto-generated method stub
		super.onStart( intent , startId );
		if( intent == null )
		{
			return;
		}
		String pkg = intent.getStringExtra( "packageName" );
		String type = intent.getStringExtra( "type" );
		String status = intent.getStringExtra( "status" );
		String name = intent.getStringExtra( "name" );
		language = Locale.getDefault().toString();
		// @2015/01/19 ADD START
		//Log.i( "andy test" , "onStart !!!!" );
		//Log.i( "andy test" , "pkg = " + pkg + "; type = " + type + "; status = " + status + "; name = " + name );
		// @2015/01/19 ADD END
		if( pkg != null && type != null && "download".equals( status ) )
		{
			//<c_0000707> liuhailin@2014-08-08 modify begin
			//notifyManager( name );
			//notifyManager( name , pkg , className );
			isDownloadingAPK = true;
			notifyManager( intent );
			//<c_0000707> liuhailin@2014-08-08 modify end
			//Log.i( "andy test" , "[DownloadApkContentService] before downloadApk !!!!" );
			downloadApk( pkg , type , name );
			//Log.i( "andy test" , "[DownloadApkContentService] after downloadApk !!!!" );
		}
		else if( pkg != null && type != null && "pause".equals( status ) )
		{
			stopDownApk( pkg , type );
		}
	}
	
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		if( downApkThread != null )
		{
			downApkThread.stopRun();
			downApkThread = null;
		}
		//<c_0000707> liuhailin@2014-08-11 modify begin
		isDownloadingAPK = false;
		notifyIntentList.clear();
		//<c_0000707> liuhailin@2014-08-11 modify end
	}
	
	private boolean isAllowDownload(
			Context cxt )
	{
		if( !Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ) )
		{
			return false;
		}
		return true;
	}
	
	//<c_0000707> liuhailin@2014-08-08 modify begin
	//private void notifyManager(
	//		String title )
	private void notifyManager(
			Intent notiIntent )
	//<c_0000707> liuhailin@2014-08-08 modify end
	{
		if( notification == null )
		{
			notification = new Notification();
			//notification = new Notification( android.R.drawable.stat_sys_download , getString( R.string.notify_add_download , notiIntent.getStringExtra( "name" ) ) , System.currentTimeMillis() );
		}
		/*	else
		{
			notification.icon = android.R.drawable.stat_sys_download;
			//notification.tickerText = getString( R.string.notify_add_download , notiIntent.getStringExtra( "name" ) );
			
			

			
			notification.when = System.currentTimeMillis();
		}*/
		// @gaominghui 2015/04/02 ADD START判断系统语言，根据系统语言显示相应语言的提示
		notification.icon = android.R.drawable.stat_sys_download;
		notification.when = System.currentTimeMillis();
		String type = notiIntent.getStringExtra( "type" );
		if( type.equals( DownloadList.Wallpaper_Type ) || type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			//language = Locale.getDefault().toString();
			if( language.equals( "zh_CN" ) || language.equals( "zh_TW" ) )
			{
				notification.tickerText = getString( R.string.notify_add_download , notiIntent.getStringExtra( "name" ) );
			}
			else
			{
				notification.tickerText = getString( R.string.notify_add_download , notiIntent.getStringExtra( "name_en" ) );
			}
		}
		else
		{
			notification.tickerText = getString( R.string.notify_add_download , notiIntent.getStringExtra( "name" ) );
		}
		// @gaominghui 2015/04/02 ADD END
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		if( mContentView == null )
			mContentView = new RemoteViews( mContext.getPackageName() , R.layout.download_notification );
		//<c_0000707> liuhailin@2014-08-08 modify begin
		//		notification.contentView = contentView;
		//		Intent intent = new Intent();
		//		intent.putExtra( "notifyID" , 1000 );
		Intent intent = new Intent();
		intent.putExtra( "notifyID" , 1000 );
		BindActivityData( notiIntent , intent );
		intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
		notifyIntentList.add( intent );
		notification.flags = Notification.FLAG_ONGOING_EVENT; // 设置常驻 Flag  
		//PendingIntent contentIntent = PendingIntent.getActivity( mContext , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		//notification.setLatestEventInfo( mContext , null , null , contentIntent );
		//notification.contentView = contentView;
		//PendingIntent contentIntent = PendingIntent.getActivity( mContext , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
		//<c_0000707> liuhailin@2014-08-08 modify end
		///notification.contentIntent = contentIntent;
		if( mNotificationManager == null )
			mNotificationManager = (NotificationManager)mContext.getSystemService( Context.NOTIFICATION_SERVICE );
		//Log.d( "andy test" , "[DownloadApkContentService]notifyManager download===========" );
		//Log.d( "notify" , "notifyManager download===========" );
		//mNotificationManager.notify( id , notification );
		mMainHandler = new Handler() {
			
			@Override
			public void handleMessage(
					Message msg )
			{
				super.handleMessage( msg );
				switch( msg.what )
				{
					case MSG_UPDATE_PROGRESS:
						RemoteViews contentView = notification.contentView;
						contentView.setTextViewText( R.id.notificationTitle , mContext.getResources().getString( R.string.notify_downloading ) + msg.obj );
						contentView.setTextViewText( R.id.notificationPercent , msg.arg1 + "%" );
						contentView.setViewVisibility( R.id.notificationPercent , View.VISIBLE );
						contentView.setProgressBar( R.id.notificationProgress , 100 , msg.arg1 , false );
						contentView.setViewVisibility( R.id.notificationProgress , View.VISIBLE );
						//Log.d( "notify" , "MSG_UPDATE_PROGRESS" );
						mNotificationManager.notify( msg.arg2 , notification );
						break;
					case MSG_SUCCESS:
						notification.icon = R.drawable.download;
						notification.flags = 0;
						notification.flags |= Notification.FLAG_AUTO_CANCEL;
						RemoteViews contentView1 = notification.contentView;
						contentView1.setTextViewText( R.id.notificationPercent , "100%" );
						contentView1.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
						Log.d( "notify" , "MSG_SUCCESS" );
						mNotificationManager.notify( msg.arg2 , notification );
						break;
					case MSG_DOWNLOAD_PAUSE:
						Toast.makeText( mContext , getString( R.string.notify_pause_download , msg.obj ) , Toast.LENGTH_SHORT ).show();
						break;
					case MSG_TIMEOUT_EXCEPTION:
						Toast.makeText( mContext , getString( R.string.notify_exception_download , msg.obj ) , Toast.LENGTH_SHORT ).show();
						break;
					case MSG_START_DOWNLOAD:
						if( notifyIntentList.size() > 0 )
						{
							PendingIntent contentIntent = PendingIntent.getActivity( mContext , 0 , notifyIntentList.get( 0 ) , PendingIntent.FLAG_UPDATE_CURRENT );
							notification.setLatestEventInfo( mContext , null , null , contentIntent );
							notification.contentIntent = contentIntent;
							notification.contentView = mContentView;
							notifyIntentList.remove( 0 );
							//mNotificationManager.notify( msg.arg2 , notification );
						}
						RemoteViews contentView4 = notification.contentView;
						contentView4.setTextViewText( R.id.notificationTitle , mContext.getResources().getString( R.string.notify_downloading ) + msg.obj );
						contentView4.setViewVisibility( R.id.notificationPercent , View.INVISIBLE );
						contentView4.setProgressBar( R.id.notificationProgress , 100 , 0 , true );
						contentView4.setViewVisibility( R.id.notificationProgress , View.VISIBLE );
						Log.d( "notify" , "MSG_START_DOWNLOAD" );
						mNotificationManager.notify( msg.arg2 , notification );
						break;
					case MSG_CANCEL_INDICATE:
						Log.d( "notify" , "MSG_CANCEL_INDICATE" );
						mNotificationManager.cancel( id );
						//<> liuhailin@2014-08-11 modify begin
						isDownloadingAPK = false;
						//<> liuhailin@2014-08-11 modify end
						break;
					case MSG_SD_FULL:
						Toast.makeText( mContext , getString( R.string.sdcar_full ) , Toast.LENGTH_SHORT ).show();
						break;
				}
			}
		};
	}
	
	//<c_0000707> liuhailin@2014-08-08 modify begin
	private void BindActivityData(
			Intent notiIntent ,
			Intent intent )
	{
		String type = notiIntent.getStringExtra( "type" );
		if( type.equals( DownloadList.Theme_Type ) )
		{
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( StaticClass.EXTRA_CLASS_NAME , notiIntent.getStringExtra( "className" ) );
			intent.setClass( mContext , com.coco.theme.themebox.preview.ThemePreviewHotActivity.class );
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			intent.putExtra( "type" , "hot" );
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( "position" , notiIntent.getIntExtra( "position" , 0 ) );
			intent.putExtra( "showpreviewwallpaperbyadapter" , FunctionConfig.isEnablePreviewWallpaperByAdapter() );
			intent.putExtra( "showpreviewbtn" , notiIntent.getBooleanExtra( "showpreviewbtn" , false ) );
			intent.putExtra( "showapplylockbtn" , FunctionConfig.isEnableShowApplyLockWallpaper() );
			intent.putExtra( "currentLauncherPackageName" , ThemesDB.LAUNCHER_PACKAGENAME );
			intent.putExtra( "currentLauncherProvider" , PubContentProvider.LAUNCHER_AUTHORITY );
			intent.putExtra( "enable_delete_current_wallpaper" , FunctionConfig.isEnableDeleteCurrentDeskWallpaper() );
			intent.setClass( mContext , WallpaperPreviewActivity.class );
			intent.putExtra( "isPriceVisible" , FunctionConfig.isPriceVisible() );
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( "position" , notiIntent.getIntExtra( "position" , 0 ) );
			intent.setClass( mContext , LiveWallpaperPreviewActivity.class );
			intent.putExtra( "isPriceVisible" , FunctionConfig.isPriceVisible() );
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( StaticClass.EXTRA_CLASS_NAME , intent.getStringExtra( "className" ) );
			intent.putExtra( "ishowmore" , FunctionConfig.isThemeMoreShow() );
			intent.putExtra( "isshare" , FunctionConfig.isShareVisible() );
			intent.putExtra( "CustomRootPath" , intent.getStringExtra( "CustomRootPath" ) );
			intent.setClass( mContext , PreviewHotActivity.class );
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( StaticClass.EXTRA_CLASS_NAME , notiIntent.getStringExtra( "className" ) );
			intent.setClass( mContext , WidgetPreviewHotActivity.class );
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( StaticClass.EXTRA_CLASS_NAME , notiIntent.getStringExtra( "className" ) );
			intent.setClass( mContext , ScenePreviewHotActivity.class );
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , notiIntent.getStringExtra( "packageName" ) );
			intent.putExtra( StaticClass.EXTRA_CLASS_NAME , notiIntent.getStringExtra( "className" ) );
			intent.setClass( mContext , FontPreviewActivity.class );
		}
	}
	
	//<c_0000707> liuhailin@2014-08-08 modify end
	private boolean findApkDownData(
			String pkgName ,
			DownType type ,
			String tabtype )
	{
		//Log.i( "andy test" , "[DownloadApkContentService] findApkDownData!!!" );
		Log.i( "andy test" , "[DownloadApkContentService] into findApkDownData pkgName = " + pkgName + "; type = " + type + ";tabtype = " + tabtype );
		if( downApkList != null )
		{
			for( int i = 0 ; i < downApkList.size() ; i++ )
			{
				DownApkNode node = downApkList.get( i );
				Log.i( "andy test" , "[DownloadApkContentService] findApkDownData!!!downApkList不为空" );
				Log.i( "andy test" , "[DownloadApkContentService] findApkDownData!!! node.packname = " + node.packname + "; node.tabTypr = " + node.tabType + "; node.downType = " + node.downType );
			}
		}
		for( int i = 0 ; i < downApkList.size() ; i++ )
		{
			DownApkNode node = downApkList.get( i );
			if( node.packname.equals( pkgName ) && node.downType == type && node.tabType.equals( tabtype ) )
			{
				return true;
			}
		}
		return false;
	}
	
	public void downloadApk(
			String pkgName ,
			String type ,
			String name )
	{
		Log.v( LOG_TAG , "downloadApk=" + pkgName );
		//Log.i( "andy test" , "[DownloadApkContentService] downloadApk ing !!!!" );
		if( !isAllowDownload( mContext ) )
		{
			return;
		}
		//Log.i( "andy test" , "[DownloadApkContentService] before synchronized( this.syncObject )" );
		synchronized( this.syncObject )
		{
			//Log.i( "andy test" , "[DownloadApkContentService] into synchronized( this.syncObject )" );
			if( findApkDownData( pkgName , DownType.TYPE_APK_DOWNLOAD , type ) )
			{
				//Log.i( "andy test" , " [DownloadApkContentService]findApkDownData return!!!!" );
				return;
			}
			if( downApkThread != null && downApkThread.isPackage( pkgName , type ) )
			{
				//Log.i( "andy test" , " [DownloadApkContentService]downApkThread！=null return!!!" );
				return;
			}
			downApkList.add( new DownApkNode( pkgName , DownType.TYPE_APK_DOWNLOAD , type , name ) );
			downloadApkStatusUpdate( pkgName , DownloadStatus.StatusDownloading , type );
			if( downApkThread == null )
			{
				downApkThread = new DownloadApkThread();
				downApkThread.start();
			}
		}
	}
	
	public void stopDownApk(
			String pkgName ,
			String type )
	{
		Log.v( LOG_TAG , "stopDownApk=" + pkgName );
		//Log.v( "andy test " , "[DownloadApkContentService] stopDownApk!! packageName = " + pkgName );
		//Log.v( "andy test " , "[DownloadApkContentService] stopDownApk!! packageName = " + pkgName + "before synchronized( this.syncObject ) " );
		synchronized( this.syncObject )
		{
			//Log.v( "andy test " , "[DownloadApkContentService] stopDownApk!! packageName = " + pkgName + "into synchronized( this.syncObject ) " );
			for( int i = downApkList.size() - 1 ; i >= 0 ; i-- )
			{
				DownApkNode node = downApkList.get( i );
				if( node.packname.equals( pkgName ) && node.downType == DownType.TYPE_APK_DOWNLOAD && node.tabType.equals( type ) )
				{
					Log.v( LOG_TAG , "remove array" );
					downApkList.remove( i );
				}
			}
			if( downApkThread != null )
			{
				Log.v( LOG_TAG , "stop apk thread" );
				//Log.v( "123" , "stop apk thread" );
				downApkThread.stopApk( pkgName , type );
			}
			else
				downloadApkStatusUpdate( pkgName , DownloadStatus.StatusPause , type );
		}
	}
	
	private void downloadApkStatusUpdate(
			String pkgName ,
			DownloadStatus status ,
			String type )
	{
		//Log.i( "andy test" , "[DownloadApkContentService] downloadApkStatusUpdate!!! before synchronized( downApkDb )" );
		synchronized( downApkDb )
		{
			//Log.i( "andy test" , "[DownloadApkContentService] downloadApkStatusUpdate!!! into synchronized( downApkDb )" );
			downApkDb.updateDownloadStatus( pkgName , status , type );
		}
		Intent intent = new Intent( getActionDownloadStatusChanged( type ) );
		intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , pkgName );
		mContext.sendBroadcast( intent );
	}
	
	private String getAppDir(
			String type )
	{
		if( type.equals( DownloadList.Theme_Type ) )
		{
			return com.coco.theme.themebox.util.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			return com.coco.lock2.lockbox.util.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			return com.coco.widget.widgetbox.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			return com.coco.scene.scenebox.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			return com.coco.font.fontbox.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getAppDir();
		}
		else if( type.equals( DownloadList.Operate_Type ) )
		{
			return com.coco.shortcut.shortcutbox.PathTool.getAppDir();
		}
		return null;
	}
	
	private String getDownloadingDir(
			String type )
	{
		if( type.equals( DownloadList.Theme_Type ) )
		{
			return com.coco.theme.themebox.util.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			return com.coco.lock2.lockbox.util.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			return com.coco.widget.widgetbox.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			return com.coco.scene.scenebox.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			return com.coco.font.fontbox.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getDownloadingDir();
		}
		else if( type.equals( DownloadList.Operate_Type ) )
		{
			return com.coco.shortcut.shortcutbox.PathTool.getDownloadingDir();
		}
		return null;
	}
	
	private String getDownloadingApp(
			String pkgName ,
			String type )
	{
		if( type.equals( DownloadList.Theme_Type ) )
		{
			return com.coco.theme.themebox.util.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			return com.coco.lock2.lockbox.util.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			return com.coco.widget.widgetbox.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			return com.coco.scene.scenebox.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			return com.coco.font.fontbox.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getDownloadingApp( pkgName );
		}
		else if( type.equals( DownloadList.Operate_Type ) )
		{
			return com.coco.shortcut.shortcutbox.PathTool.getDownloadingApp( pkgName );
		}
		return null;
	}
	
	private String getAppFile(
			String pkgName ,
			String type )
	{
		if( type.equals( DownloadList.Theme_Type ) )
		{
			return com.coco.theme.themebox.util.PathTool.getAppFile( pkgName );
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			return com.coco.lock2.lockbox.util.PathTool.getAppFile( pkgName );
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			return com.coco.widget.widgetbox.PathTool.getAppFile( pkgName );
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			return com.coco.scene.scenebox.PathTool.getAppFile( pkgName );
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getAppFile( pkgName );
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			return com.coco.font.fontbox.PathTool.getAppFile( pkgName );
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.PathTool.getAppLiveFile( pkgName );
		}
		else if( type.equals( DownloadList.Operate_Type ) )
		{
			return com.coco.shortcut.shortcutbox.PathTool.getAppFile( pkgName );
		}
		return null;
	}
	
	private String getActionDownloadStatusChanged(
			String type )
	{
		if( type.equals( DownloadList.Theme_Type ) )
		{
			return com.coco.theme.themebox.StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			return com.coco.lock2.lockbox.StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			return com.coco.widget.widgetbox.StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			return com.coco.scene.scenebox.StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			return com.coco.font.fontbox.StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.StaticClass.ACTION_LIVE_DOWNLOAD_STATUS_CHANGED;
		}
		else if( type.equals( DownloadList.Operate_Type ) )
		{
			return com.coco.shortcut.shortcutbox.UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED;
		}
		return null;
	}
	
	private String getActionDownloadSizeChanged(
			String type )
	{
		if( type.equals( DownloadList.Theme_Type ) )
		{
			return com.coco.theme.themebox.StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.Lock_Type ) )
		{
			return com.coco.lock2.lockbox.StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.Widget_Type ) )
		{
			return com.coco.widget.widgetbox.StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.Scene_Type ) )
		{
			return com.coco.scene.scenebox.StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.Wallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.Font_Type ) )
		{
			return com.coco.font.fontbox.StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			return com.coco.wallpaper.wallpaperbox.StaticClass.ACTION_LIVE_DOWNLOAD_SIZE_CHANGED;
		}
		else if( type.equals( DownloadList.Operate_Type ) )
		{
			return com.coco.shortcut.shortcutbox.UtilsBase.ACTION_DOWNLOAD_SIZE_CHANGED;
		}
		return null;
	}
	
	private void downloadApkError(
			String pkgName ,
			String type )
	{
		synchronized( downApkDb )
		{
			downApkDb.updateDownloadStatus( pkgName , DownloadStatus.StatusInit , type );
		}
		new File( getDownloadingApp( pkgName , type ) ).delete();
		new File( getAppFile( pkgName , type ) ).delete();
		Intent intent = new Intent( getActionDownloadStatusChanged( type ) );
		intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , pkgName );
		mContext.sendBroadcast( intent );
	}
	
	private void downloadApkFinish(
			String pkgName ,
			String type )
	{
		synchronized( downApkDb )
		{
			downApkDb.updateDownloadStatus( pkgName , DownloadStatus.StatusFinish , type );
		}
		String downloading = getDownloadingApp( pkgName , type );
		if( !new File( downloading ).exists() )
		{
			mHandler.sendEmptyMessage( 2 );
			downloadApkError( pkgName , type );
			return;
		}
		File dir = new File( getAppDir( type ) );
		if( !dir.exists() && !dir.isDirectory() )
		{
			PathTool.makeDir( getAppDir( type ) );
		}
		String app = getAppFile( pkgName , type );
		if( downloading != null && app != null )
			PathTool.moveFile( downloading , app );
		if( !type.equals( DownloadList.Wallpaper_Type ) && !type.equals( DownloadList.Font_Type ) )
		{
			if( !verifyAPKFile( mContext , app ) )
			{
				mHandler.sendEmptyMessage( 1 );
				downloadApkError( pkgName , type );
				return;
			}
			if( FunctionConfig.isInatall_silently_ThemeApk() && type.equals( DownloadList.Theme_Type ) )
			{
				silentInstallTheme( pkgName );
			}
			else
			{
				installApk( pkgName , type );
			}
		}
		if( type.equals( DownloadList.Wallpaper_Type ) || type.equals( DownloadList.LiveWallpaper_Type ) )
		{
			if( pkgName.split( "#" ) != null )
			{
				PathTool.copyFile( com.coco.wallpaper.wallpaperbox.PathTool.getThumbFile( pkgName.split( "#" )[0] ) , com.coco.wallpaper.wallpaperbox.PathTool.getAppSmallFile( pkgName ) );
			}
			else
			{
				PathTool.copyFile( com.coco.wallpaper.wallpaperbox.PathTool.getThumbFile( pkgName ) , com.coco.wallpaper.wallpaperbox.PathTool.getAppSmallFile( pkgName ) );
			}
		}
		Intent intent = new Intent( getActionDownloadStatusChanged( type ) );
		intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , pkgName );
		String[] tempPackageName = ( pkgName.split( "#" ) );
		if( tempPackageName.length > 1 )
		{
			intent.putExtra( com.coco.wallpaper.wallpaperbox.StaticClass.EXTRA_APP_NAME , tempPackageName[1] );
		}
		mContext.sendBroadcast( intent );
		String resid = null;
		if( !type.equals( DownloadList.Operate_Type ) )
		{
			HotService sv = new HotService( mContext );
			resid = sv.queryResid( pkgName , type );
		}
		else
		{
			HotOperateService sv = new HotOperateService( mContext );
			resid = sv.queryResid( pkgName );
		}
		if( resid != null )
		{
			DownloadList.getInstance( mContext ).startUICenterLog( DownloadList.ACTION_DOWNLOAD_LOG , resid , pkgName );
		}
	}
	
	public boolean verifyAPKFile(
			Context context ,
			String path )
	{
		File packageFile = new File( path );
		if( packageFile.exists() )
		{
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo( path , PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS );
			if( info != null )
			{
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	
	public void installApk(
			String pkgName ,
			String type )
	{
		String filepath = getAppFile( pkgName , type );
		File file = new File( filepath );
		Log.v( "OpenFile" , file.getName() );
		Intent intent = new Intent();
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.setAction( android.content.Intent.ACTION_VIEW );
		intent.setDataAndType( Uri.fromFile( file ) , "application/vnd.android.package-archive" );
		mContext.startActivity( intent );
		Log.i( "minghui" , "安装完成" );
	}
	
	public void silentInstallTheme(
			String pkgName )
	{
		final File fileDir = getFilesDir();
		final String packageName = pkgName;
		Thread thread = new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				Log.e( "silentInstallTheme" , " packageName:" + packageName );
				PackageManager pm = getPackageManager();
				File tmp = new File( fileDir.getAbsolutePath() + "/" + packageName + ".apk" );
				if( tmp.exists() )
					tmp.delete();
				try
				{
					pm.getPackageInfo( packageName , PackageManager.GET_ACTIVITIES );
				}
				catch( NameNotFoundException e3 )
				{
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				try
				{
					tmp.createNewFile();
					Log.e( "silentInstallTheme" , " tmp.exists = " + tmp.exists() );
				}
				catch( IOException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.e( "silentInstallTheme" , " IOException e1 = " + e1 );
				}
				FileOutputStream fos = null;
				FileInputStream bis = null;
				int BUFFER_SIZE = 1024;
				byte[] buf = new byte[BUFFER_SIZE];
				int size = 0;
				try
				{
					File apkFile = new File( PathTool.getAppFile( packageName ) );
					bis = new FileInputStream( apkFile );
					Log.e( "silentInstallTheme" , " PathTool.getAppFile( packageName ).getBytes() = " + PathTool.getAppFile( packageName ).getBytes() );
					fos = new FileOutputStream( tmp );
					while( ( size = bis.read( buf ) ) != -1 )
					{
						fos.write( buf , 0 , size );
					}
					fos.close();
					bis.close();
				}
				catch( FileNotFoundException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e( "silentInstallTheme" , " FileNotFoundException e = " + e );
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e( "silentInstallTheme" , " IOException e = " + e );
				}
				PackageInfo info = pm.getPackageArchiveInfo( tmp.getAbsolutePath() , PackageManager.GET_ACTIVITIES );
				String pkgName = null;
				if( info != null )
				{
					try
					{
						ApplicationInfo appInfo = info.applicationInfo;
						pkgName = appInfo.packageName; // 得到安装包名
						pm.getPackageInfo( pkgName , PackageManager.GET_ACTIVITIES );
						Log.e( "silentInstallTheme" , "APK already exist:" + pkgName );
						tmp.delete();
					}
					catch( Exception e )
					{
						Log.e( "silentInstallTheme" , "APK need install:" + pkgName );
					}
				}
				int n = 0;
				while( n < 5 )
				{
					String s = sync_do_exec( "chmod 777 " + tmp.getAbsolutePath() );
					boolean success = do_exec( "pm install " + tmp.getAbsolutePath() , pkgName );
					if( !success )
					{
						Log.e( "silentInstallTheme" , "APK install fail:" + pkgName );
						//smHandler.sendEmptyMessage( SILENT_INSTALL_FAILED );
						break;
					}
					// Log.e("apk", "install:"+s);
					if( pkgName != null )
					{
						try
						{
							pm.getPackageInfo( pkgName , PackageManager.GET_ACTIVITIES );
							Log.e( "silentInstallTheme" , "APK install ok:" + pkgName );
							//mHandler.sendEmptyMessage( SILENT_INSTALL_SUCESS );
							break;
						}
						catch( Exception e )
						{
							Log.e( "silentInstallTheme" , "APK install package again:" + pkgName );
						}
					}
					try
					{
						Thread.sleep( 5000 );
						Log.e( "silentInstallTheme" , "APK sleep:" + pkgName );
					}
					catch( InterruptedException e2 )
					{
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					n++;
				}
				tmp.delete();
			}
		} );
		thread.setPriority( android.os.Process.THREAD_PRIORITY_BACKGROUND );
		thread.start();
	}
	
	private Object exe_lock = new Object();
	
	public String sync_do_exec(
			String cmd )
	{
		String s = "\n";
		try
		{
			java.lang.Process p = Runtime.getRuntime().exec( cmd );
			BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			String line = null;
			while( ( line = in.readLine() ) != null )
			{
				s += line + "\n";
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	public boolean do_exec(
			final String cmd ,
			String packageName )
	{
		boolean success = false;
		new Thread() {
			
			@Override
			public void run()
			{
				String s = "\n";
				try
				{
					java.lang.Process p = Runtime.getRuntime().exec( cmd );
					BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
					String line = null;
					while( ( line = in.readLine() ) != null )
					{
						s += line + "\n";
					}
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				super.run();
				synchronized( exe_lock )
				{
					Log.d( "apk" , "exe_lock notify" );
					exe_lock.notify();
				}
			}
		}.start();
		int i = 0;
		PackageManager pm = mContext.getPackageManager();
		synchronized( exe_lock )
		{
			while( !success && i < 12 )
			{
				Log.d( "apk" , "exe_lock wait" );
				try
				{
					exe_lock.wait( 10000 );
					Log.d( "apk" , "exe_lock wait finish" );
				}
				catch( InterruptedException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if( packageName != null )
				{
					try
					{
						pm.getPackageInfo( packageName , PackageManager.GET_ACTIVITIES );
						Log.e( "apk" , "has install,do not wait:" + packageName );
						success = true;
						break;
					}
					catch( Exception e )
					{
						Log.e( "apk" , "wait again:" + packageName );
					}
				}
				i++;
			}
		}
		return success;
	}
	
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			switch( msg.what )
			{
				case 0:
					Toast.makeText( mContext , mContext.getString( R.string.reLoadApk ) , Toast.LENGTH_SHORT ).show();
					break;
				case 1:
					Toast.makeText( mContext , mContext.getString( R.string.internet_unusual ) , Toast.LENGTH_SHORT ).show();
					break;
				case 2:
					Toast.makeText( mContext , mContext.getString( R.string.server_download_fail ) , Toast.LENGTH_SHORT ).show();
					break;
				default:
					break;
			}
			super.handleMessage( msg );
		}
	};
	
	private class DownloadApkThread extends Thread
	{
		
		private volatile DownApkNode curDownApk;
		private volatile HttpURLConnection urlConn;
		private volatile boolean isExit = false;
		private HotService hotServer = new HotService( mContext );
		private DownloadThemeService threadDb = new DownloadThemeService( mContext );
		private HotOperateService hotopServer;
		private int tryTimes = 0;
		
		private String getResid(
				String packageName ,
				String type )
		{
			if( type.equals( DownloadList.Operate_Type ) )
			{
				if( hotopServer == null )
				{
					hotopServer = new HotOperateService( mContext );
				}
				return hotopServer.queryResid( packageName );
			}
			return hotServer.queryResid( packageName , type );
		}
		
		public void stopRun()
		{
			isExit = true;
			if( mMainHandler != null )//将提示提前,提高用户体验
				if( language.equals( "zh_CN" ) || language.equals( "zh_TW" ) )
				{
					mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_DOWNLOAD_PAUSE , 0 , id , curDownApk.apkName ) );
				}
				else
				{
					mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_DOWNLOAD_PAUSE , 0 , id , appName_en ) );
				}
			//mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_DOWNLOAD_PAUSE , 0 , id , curDownApk.apkName ) );
			DownApkNode dNode = curDownApk;
			if( dNode != null )
			{
				threadDb.updateDownloadStatus( dNode.packname , DownloadStatus.StatusPause , dNode.tabType );
			}
		}
		
		public void stopApk(
				String pkgName ,
				String type )
		{
			DownApkNode dNode = curDownApk;
			if( type.equals( DownloadList.Wallpaper_Type ) )
			{
				String[] dNodepkgName = dNode.packname.split( "#" );
				dNode.packname = dNodepkgName[0];
			}
			if( dNode != null && pkgName.equals( dNode.packname ) && dNode.tabType.equals( type ) )
			{
				stopRun();// 停止当前的
				//				isExit = false;// 启动之后的
				Log.v( LOG_TAG , "isExit stopApk = " + isExit );
			}
			else
			{
				downloadApkStatusUpdate( pkgName , DownloadStatus.StatusPause , type );
			}
		}
		
		public boolean isPackage(
				String pkgName ,
				String type )
		{
			DownApkNode node = curDownApk;
			//Log.i( "andy test" , " [DownloadApkContentService]isPackage()!!!" );
			//Log.i( "andy test" , " [DownloadApkContentService]isPackage() pkgName = " + pkgName + "; type = " + type );
			//if( curDownApk != null )
			//
			//	Log.i( "andy test" , " [DownloadApkContentService]isPackage() curDownApk.packname =  " + curDownApk.packname + ";curDownApk.tabType = " + curDownApk.tabType );
			//}
			if( node != null && node.packname.equals( pkgName ) && node.tabType.equals( type ) )
			{
				return true;
			}
			return false;
		}
		
		// 通过包名获取下载url
		private String getDownloadUrl(
				String packageName ,
				String resID )
		{
			String url = DownloadList.SERVER_URL_TEST;
			String params = DownloadList.getInstance( mContext ).getParams( REQUEST_ACTION_URL , resID , packageName );
			String downloadUrl = null;
			if( params != null )
			{
				CustomerHttpClient client = new CustomerHttpClient( mContext );
				//				String res[] = client.post( url , params );
				ResultEntity res = client.postEntity( url , params );
				if( res.exception != null )
				{
					//Log.i( "andy test" , "[DownloadApkContentService] getDownloadUrl  res.exception = " + res.exception.getStackTrace() );
					return null;
				}
				else
				{
					String content = res.content;
					JSONObject json = null;
					try
					{
						json = new JSONObject( content );
						int retCode = json.getInt( "retcode" );
						if( retCode == 0 )
						{
							downloadUrl = json.getString( "url" );
						}
					}
					catch( JSONException e )
					{
						Log.i( "andy test" , "[DownloadApkContentService] getDownloadUrl  JSONException e = " + e.getStackTrace() );
						e.printStackTrace();
					}
				}
			}
			return downloadUrl;
		}
		
		private long sizeChangeTimeMillis = 0;
		
		private void downloadApkContinue(
				String pkgName ,
				int curSize ,
				int totalSize ,
				String type )
		{
			threadDb.updateDownloadSizeAndStatus( pkgName , curSize , totalSize , DownloadStatus.StatusDownloading , type );
			long currentTimeMillis = System.currentTimeMillis();
			if( currentTimeMillis - sizeChangeTimeMillis > 0 && currentTimeMillis - sizeChangeTimeMillis < 1000 )
			{
				return;
			}
			sizeChangeTimeMillis = currentTimeMillis;
			Intent intent = new Intent( getActionDownloadSizeChanged( type ) );
			intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , pkgName );
			intent.putExtra( StaticClass.EXTRA_DOWNLOAD_SIZE , curSize );
			intent.putExtra( StaticClass.EXTRA_TOTAL_SIZE , totalSize );
			mContext.sendBroadcast( intent );
			if( mMainHandler != null )
			{
				mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_UPDATE_PROGRESS , curSize * 100 / totalSize , id , curDownApk.apkName ) );
			}
		}
		
		@Override
		public void run()
		{
			//Log.i( "andy test" , "[DownloadApkContentService] download apk run()!!!" );
			while( true )
			{
				//Log.v( "andy test " , "[DownloadApkContentService] run()!! before synchronized( syncObject ) " );
				synchronized( syncObject )
				{
					//Log.v( "andy test " , "[DownloadApkContentService] run()!! into synchronized( syncObject ) " );
					if( downApkList.size() == 0 )
					{
						Log.i( "andy test" , "[DownloadApkContentService] downApkList.size() == 0" );
						break;
					}
					curDownApk = downApkList.get( 0 );
					//Log.v( "andy test " , "[DownloadApkContentService] run()!! into synchronized( syncObject )  curDownapk.packname = " + curDownApk.packname );
					downApkList.remove( 0 );
					isExit = false;
				}
				if( mMainHandler != null )
				{
					mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_START_DOWNLOAD , 0 , id , curDownApk.apkName ) );
				}
				RandomAccessFile fileOut = null;
				File file = null;
				InputStream netStream = null;
				boolean isSucceed = false;
				boolean isfull = false;
				long downSize = 0;
				int totalLength = 0;
				// @2015/09/09 ADD START 客户在美化中心下载的壁纸，
				//将壁纸的中英文名字作为保存到本地COCO文件夹下壁纸的文件夹名字，为了方便桌面编辑模式下可以获取到美华中心下载的壁纸的名字（中英文名字中间用“#”拼接）
				String[] url_package = null;
				try
				{
					if( curDownApk.tabType.equals( DownloadList.Wallpaper_Type ) )
					{
						HotService dSv = new HotService( mContext );
						ThemeInfoItem item = dSv.queryByPackageName( curDownApk.packname , DownloadList.Wallpaper_Type );
						if( item != null )
						{
							String appName = item.getApplicationName();
							curDownApk.packname = curDownApk.packname + "#" + appName;
						}
					}
					downloadApkStatusUpdate( curDownApk.packname , DownloadStatus.StatusDownloading , curDownApk.tabType );
					// @2015/09/09 UPD END
					String sdpath = getDownloadingApp( curDownApk.packname , curDownApk.tabType );
					//Log.i( "andy test" , "[DownloadApkContentService] download apk run() sdpath = " + sdpath );
					File dir = new File( getDownloadingDir( curDownApk.tabType ) );
					if( !dir.exists() && !dir.isDirectory() )
					{
						PathTool.makeDir( getDownloadingDir( curDownApk.tabType ) );
					}
					file = new File( sdpath );
					// @2015/01/14 ADD END
					url_package = curDownApk.packname.split( "#" );
					String downloadUrl = null;
					if( url_package != null )
					{
						//Log.i( "andy" , "url_package[0] = " + url_package[0] + "; curDownApk.tabType = " + curDownApk.tabType );
						downloadUrl = getDownloadUrl( url_package[0] , getResid( url_package[0] , curDownApk.tabType ) );
						//Log.i( "andy" , "RESID = " + getResid( url_package[0] , curDownApk.tabType ) );
						//Log.i( "andy" , "downloadUrl = " + downloadUrl );
					}
					if( downloadUrl != null )
					{
						URL url = new URL( downloadUrl );
						urlConn = (HttpURLConnection)url.openConnection();
						// @gaominghui 2015/01/23 ADD START设置超时链接时间，设置读取数据流超时时间
						urlConn.setConnectTimeout( 30000 );
						urlConn.setReadTimeout( 30000 );
						// @gaominghui 2015/01/23 ADD END
						DownloadThemeItem item = null;
						if( url_package != null )
						{
							item = threadDb.queryByPackageName( url_package[0] , curDownApk.tabType );
						}
						else
						{
							item = threadDb.queryByPackageName( curDownApk.packname , curDownApk.tabType );
						}
						appName_en = item.getApplicationName_en();
						int curSize = 0;
						if( item == null )
						{
							curSize = 0;
						}
						else
						{
							curSize = (int)item.getDownloadSize();
						}
						// @2015/01/19 ADD START
						//Log.i( "andy test" , "[DownloadApkContentService]curSize = " + curSize );
						// @2015/01/19 ADD END
						if( curSize > 0 )
						{
							fileOut = new RandomAccessFile( sdpath , "rw" );
							fileOut.seek( curSize );
							String ranges = String.format( "bytes=%d-" , curSize );
							urlConn.addRequestProperty( "RANGE" , ranges );
						}
						else
						{
							if( file.exists() )
								file.delete();
							fileOut = new RandomAccessFile( sdpath , "rw" );
						}
						urlConn.connect();
						// 获取文件大小
						// int length = conn.getContentLength();
						totalLength = urlConn.getContentLength();
						if( curSize > 0 )
						{
							totalLength = (int)item.getApplicationSize();
							// @2015/01/19 ADD START
							//Log.i( "andy test" , "[DownloadApkContentService]run() curSize > 0  totalLength = " + totalLength );
							// @2015/01/19 ADD END
						}
						if( curSize == totalLength )
						{
							isSucceed = true;
							Log.i( "andy test" , "[DownloadApkContentService]run() isSucceed = " + isSucceed );
						}
						if( mMainHandler != null )
						{
							mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_UPDATE_PROGRESS , curSize * 100 / totalLength , id , curDownApk.apkName ) );
						}
						// 创建输入�?
						{
							netStream = urlConn.getInputStream();
							int count = 0;
							// 缓存
							byte buf[] = new byte[1024];
							// 写入到文件中
							// 创建输入�?
							// 写入到文件中
							while( true )
							{
								int numread = netStream.read( buf );
								count += numread;
								// @2015/01/19 ADD START
								//Log.i( "andy test " , "[DownloadApkContentService]run() numread = " + numread + "; count = " + count );
								// @2015/01/19 ADD END
								// 计算进度条位�?
								// int progress = (int) (((float) count / length) *
								// 100);
								if( numread < 0 )
								{
									// 下载完成
									Log.i( "andy test " , "[DownloadApkContentService]run() numread = " + numread );
									isSucceed = true;
									break;
								}
								if( isExit )
								{
									Log.v( "andy test " , "[DownloadApkContentService]run() isExit stopApk0000 = " + isExit );
									break;
								}
								// \B8\FC\D0½\F8\B6\C8
								//Log.v( "andy test " , "[DownloadApkContentService]run() before  downloadApkContinue !!!" );
								downloadApkContinue( url_package[0] , curSize + count , totalLength , curDownApk.tabType );
								//Log.v( "andy test " , "[DownloadApkContentService]run() after  downloadApkContinue !!!" );
								// 写入文件
								//Log.v( "andy test " , "[DownloadApkContentService]run() before  fileOut.write !!!" );
								fileOut.write( buf , 0 , numread );
								//Log.v( "andy test " , "[DownloadApkContentService]run() after  fileOut.write !!!" );
							}
						}
					}
				}
				catch( MalformedURLException e )
				{
					Log.i( "andy test" , "[DownloadApkContentService]run()  MalformedURLException Message = " + e.getStackTrace() );
					e.printStackTrace();
				}
				catch( SocketTimeoutException e )
				{
					//Log.i( "andy test" , "[DownloadApkContentService]run()  SocketTimeoutException Message = " + e.getStackTrace() );
					Log.i( "andy test" , "[DownloadApkContentService]run()SocketTimeoutException Socket读取数据流异常或者链接connect url异常！ " );
					tryTimes++;
					if( tryTimes > 3 )
					{
						tryTimes = 0;
						if( mMainHandler != null )
						{
							mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_TIMEOUT_EXCEPTION , 0 , id , null ) );
						}
						break;
					}
				}
				catch( IOException e )
				{
					e.printStackTrace();
					Log.i( "andy test" , "[DownloadApkContentService]run()  IOException Message = " + e.getStackTrace() );
					if( com.coco.theme.themebox.StaticClass.getAvailableSDMemorySize() == 0 )
					{
						Log.i( "andy test" , "[DownloadApkContentService]内存不足 " );
						isfull = true;
					}
				}
				finally
				{
					if( fileOut != null )
					{
						try
						{
							downSize = fileOut.length();
							fileOut.close();
						}
						catch( IOException e )
						{
							Log.i( "andy test" , "[DownloadApkContentService] fileout!=null IOException Message = " + e.getMessage() );
							Log.i( "andy test" , "[DownloadApkContentService] fileout!=null IOException Message = " + e.getStackTrace() );
							e.printStackTrace();
						}
						fileOut = null;
					}
					if( netStream != null )
					{
						try
						{
							netStream.close();
						}
						catch( IOException e )
						{
							Log.i( "andy test" , "fileout!=null IOException Message = " + e.getMessage() );
							Log.i( "andy test" , "fileout!=null IOException Message = " + e.getStackTrace() );
							e.printStackTrace();
						}
						netStream = null;
					}
					if( urlConn != null )
					{
						urlConn.disconnect();
						urlConn = null;
					}
				}
				if( isSucceed )
				{
					// downloadApkStatusUpdate(curDownApk.packname,
					// DownloadStatus.StatusFinish);
					if( downSize < totalLength )
					{
						//Log.i( "andy test" , "[DownloadApkContentService] run() bfeore downloadApkError()" );
						mHandler.sendEmptyMessage( 1 );
						downloadApkError( curDownApk.packname , curDownApk.tabType );
						//Log.i( "andy test" , "[DownloadApkContentService] run() after downloadApkError()" );
					}
					else
					{
						//Log.i( "andy test" , "[DownloadApkContentService] run() bfeore downloadApkFinish" );
						downloadApkFinish( curDownApk.packname , curDownApk.tabType );
						//Log.i( "andy test" , "[DownloadApkContentService] run() after downloadApkFinish" );
					}
				}
				else
				{
					//Log.i( "andy test" , "[DownloadApkContentService] run() bfeore downloadApkStatusUpdate()!!" );
					downloadApkStatusUpdate( url_package[0] , DownloadStatus.StatusPause , curDownApk.tabType );
					//Log.i( "andy test" , "[DownloadApkContentService] run() after downloadApkStatusUpdate()!!" );
					if( isfull )
					{
						if( mMainHandler != null )
							mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_SD_FULL , 0 , id , curDownApk.apkName ) );
					}
					else
					{
						Log.v( LOG_TAG , "isExit MSG_DOWNLOAD_PAUSE!!" );
						if( mMainHandler != null && !isExit )
						{
							if( language.equals( "zh_CN" ) || language.equals( "zh_TW" ) )
							{
								mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_DOWNLOAD_PAUSE , 0 , id , curDownApk.apkName ) );
							}
							else
							{
								mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_DOWNLOAD_PAUSE , 0 , id , appName_en ) );
							}
						}
					}
				}
			}
			//Log.i( "andy test" , "[DownloadApkContentService] run() end! before synchronized(syncObject )" );
			synchronized( syncObject )
			{
				//Log.i( "andy test" , "[DownloadApkContentService] run() end! into synchronized(syncObject )" );
				//Log.i( "123" , "[DownloadApkContentService] run() end! into synchronized(syncObject )" );
				downApkThread = null;
				if( mMainHandler != null )
					mMainHandler.sendMessage( Message.obtain( mMainHandler , MSG_CANCEL_INDICATE , 0 , id , null ) );
				stopSelf();
			}
		}
	}
}
