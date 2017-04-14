// xiatian add whole file //OperateFolder
package com.cooee.weather.download;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.widget.ClearWeatherClock.R;


public class DownloadHelper
{
	
	public static int APK_STATUS_HAS_INSTALL = 0;
	public static int APK_STATUS_NEED_DOWNLOAD = 1;
	public static int APK_STATUS_NEED_INSTALL = 2;
	public static int APK_STATUS_DOWNLOADING = 3;
	public static final String DIR_PATH = "/Coco/download/";
	public static final String DEFAULT_KEY = "f24657aafcb842b185c98a9d3d7c6f4725f6cc4597c3a4d531c70631f7c7210fd7afd2f8287814f3dfa662ad82d1b02268104e8ab3b2baee13fab062b3d27bff";
	public static final int MSG_UPDATE_PROGRESS = 0;
	public static final int MSG_FAIL = 1;
	public static final int MSG_SUCCESS = 2;
	public static int notifyID = 20130710;
	public static Handler handler = new Handler();
	public static ArrayList<DownloadingItem> mDownloadingList = new ArrayList<DownloadingItem>();
	public static HashMap<String , Integer> mDownloadFinish = new HashMap<String , Integer>();
	
	public interface DownloadListener
	{
		
		public void setProxy(
				Object obj );
		
		public void onDownloadSuccess();
		
		public void onDownloadFail();
		
		public void onDownloadProgress(
				int progress );
		
		public void onInstallSuccess(
				String packageName );
	}
	
	static class DownloadingItem
	{
		
		String packageName;
		String title;
		int id;
		Notification notification;
	}
	
	public static int checkStatus(
			Activity activity ,
			String packname )
	{
		PackageManager pm = activity.getPackageManager();
		try
		{
			PackageInfo a = pm.getPackageInfo( packname , PackageManager.GET_ACTIVITIES );
		}
		catch( NameNotFoundException e )
		{
			String dirPath = Environment.getExternalStorageDirectory() + DIR_PATH;
			String path = Environment.getExternalStorageDirectory() + DIR_PATH + packname + ".apk";
			File dir = new File( dirPath );
			dir.mkdirs();
			if( DownloadUtils.verifyAPKFile( activity , path ) == 2 )
			{
				return APK_STATUS_NEED_INSTALL;
			}
			if( isDownloading( packname ) )
				return APK_STATUS_DOWNLOADING;
			return APK_STATUS_NEED_DOWNLOAD;
		}
		return APK_STATUS_HAS_INSTALL;
	}
	
	public static void download(
			Context activity ,
			DownloadListener listener ,
			String title ,
			String packname ,
			boolean install ,
			boolean notificationClick )
	{
		String sdDir = DownloadUtils.getSDPath();
		if( sdDir == null )
		{
			DownloadUtils.toast( activity , activity.getResources().getString( R.string.msg_insert_SD ) );
			return;
		}
		String dirPath = Environment.getExternalStorageDirectory() + DIR_PATH;
		String path = Environment.getExternalStorageDirectory() + DIR_PATH + packname + ".apk";
		File dir = new File( dirPath );
		dir.mkdirs();
		if( DownloadUtils.verifyAPKFile( activity , path ) == 2 )
		{
			DownloadUtils.installAPKFile( activity , listener , path );
			return;
		}
		if( !DownloadUtils.isNetworkAvailable( activity ) )
		{
			DownloadUtils.toast( activity , activity.getResources().getString( R.string.internet_err ) );
			return;
		}
		downloadFile( activity , listener , title , path , packname , null , install , notificationClick );
	}
	
	public static void install(
			Activity activity ,
			DownloadListener listener ,
			String title ,
			String packname ,
			boolean notificationClick )
	{
		String dirPath = Environment.getExternalStorageDirectory() + DIR_PATH;
		String path = Environment.getExternalStorageDirectory() + DIR_PATH + packname + ".apk";
		File dir = new File( dirPath );
		dir.mkdirs();
		if( DownloadUtils.verifyAPKFile( activity , path ) == 2 )
		{
			DownloadUtils.installAPKFile( activity , listener , path );
		}
		else
			download( activity , listener , title , packname , true , notificationClick );
	}
	
	private synchronized static void downloadFile(
			final Context activity ,
			final DownloadListener listener ,
			final String title ,
			final String path ,
			final String pkgName ,
			final String resID ,
			final boolean install ,
			boolean notificationClick )
	{
		if( isDownloading( pkgName ) )
		{
			String downloading = activity.getResources().getString( R.string.downloading_toast );
			// Messenger.sendMsg(Messenger.MSG_TOAST, title + downloading);
			DownloadUtils.toast( activity , title + downloading );
			return;
		}
		initHandler( activity );
		final Notification notification = new Notification( android.R.drawable.stat_sys_download , title , System.currentTimeMillis() );
		// notification.defaults |= Notification.DEFAULT_SOUND;
		// notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		RemoteViews contentView = new RemoteViews( activity.getPackageName() , R.layout.operate_folder_notification );
		contentView.setTextViewText( R.id.notificationTitle , activity.getResources().getString( R.string.notify_downloading ) + title );
		contentView.setTextViewText( R.id.notificationPercent , "0%" );
		contentView.setProgressBar( R.id.notificationProgress , 100 , 0 , true );
		notification.contentView = contentView;
		if( notificationClick )
		{
			Intent intent = new Intent();
			ComponentName component = new ComponentName( activity , "" );
			intent.setComponent( component );
			intent.putExtra( "OperateFolderNotifyID" , notifyID );
			PendingIntent contentIntent = PendingIntent.getActivity( activity , (int)System.currentTimeMillis() , intent , PendingIntent.FLAG_UPDATE_CURRENT );
			notification.contentIntent = contentIntent;
		}
		NotificationManager mNotificationManager = (NotificationManager)activity.getSystemService( Context.NOTIFICATION_SERVICE );
		final int id = notifyID;
		mNotificationManager.notify( id , notification );
		addToDownloadList( pkgName , title , id , notification );
		notifyID++;
		Thread thread = new Thread() {
			
			@Override
			public void run()
			{
				super.run();
				String url = null;
				String[] result = null;
				String serverResID = null;
				result = DownloadProxy.getInstance( activity ).getVirtureIconDownloadUrl( activity , pkgName );
				if( result != null )
				{
					url = result[0];
					serverResID = result[1];
				}
				// }
				if( url == null )
				{
					if( handler != null )
					{
						RemoteViews contentView2 = notification.contentView;
						contentView2.setTextViewText( R.id.notificationTitle , title + activity.getResources().getString( R.string.notify_download_fail ) );
						handler.sendMessage( Message.obtain( handler , MSG_FAIL , 0 , id , notification ) );
						removeFromDownloadList( pkgName );
					}
					listener.onDownloadFail();
					return;
				}
				Log.i( "OPFolder" , "url=" + url );
				try
				{
					long downloadLength = 0;
					long totalLength = 0;
					int progress = 0;
					RandomAccessFile fos = null;
					totalLength = DownloadUtils.getDownloadLength( url );
					if( path.startsWith( "/data/data" ) )
					{
						long availableSize = readAvailableSystem();
						// 下载apk后的剩余rom空间必须大于100M
						if( availableSize < totalLength + 100 * 1024 * 1024 )
						{
							RemoteViews contentView2 = notification.contentView;
							contentView2.setTextViewText( R.id.notificationTitle , title + activity.getResources().getString( R.string.notify_download_fail ) );
							handler.sendMessage( Message.obtain( handler , MSG_FAIL , 0 , id , notification ) );
							removeFromDownloadList( pkgName );
							return;
						}
					}
					final File file = new File( path );
					if( file.exists() )
					{
						long curPosition = file.length();
						if( curPosition == -1 )
						{
							file.delete();
							file.createNewFile();
						}
						else
						{
							downloadLength = curPosition;
							if( downloadLength >= totalLength )
							{
								downloadLength = 0;
								file.delete();
								file.createNewFile();
							}
							else
							{
								fos = new RandomAccessFile( file , "rw" );
								fos.seek( downloadLength );
							}
							// Log.d(tag, "continue download:"+downloadLength);
						}
					}
					else
					{
						file.createNewFile();
						if( path.startsWith( "/data/data" ) )
						{
							String str = "chmod " + "777 " + path;
							try
							{
								Runtime.getRuntime().exec( str );
							}
							catch( IOException e )
							{
								return;
							}
						}
					}
					if( fos == null )
						fos = new RandomAccessFile( file , "rw" );
					LogHelper.log( activity , LogHelper.LOG_ACTION_REQUEST_DOWNLOAD_VIRTURE_ICON , pkgName , serverResID );
					InputStream in = DownloadUtils.sendDownload( url , downloadLength , totalLength );
					if( in == null )
					{
						if( handler != null )
						{
							RemoteViews contentView2 = notification.contentView;
							contentView2.setTextViewText( R.id.notificationTitle , title + activity.getResources().getString( R.string.notify_download_fail ) );
							handler.sendMessage( Message.obtain( handler , MSG_FAIL , 0 , id , notification ) );
							removeFromDownloadList( pkgName );
						}
						// ackDownloadFail(info);
						listener.onDownloadFail();
						return;
					}
					byte[] buf = new byte[256];
					while( true )
					{
						if( url != null )
						{
							int numRead = in.read( buf );
							if( numRead <= 0 )
							{
								fos.close();
								if( downloadLength == 0 )
								{
									file.delete();
									// ackDownloadFail(info);
									listener.onDownloadFail();
									return;
								}
								if( install )
									DownloadUtils.installAPKFile( activity , listener , path );
								else
									listener.onDownloadSuccess();
								if( handler != null )
								{
									Intent intent = new Intent();
									intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
									intent.setAction( android.content.Intent.ACTION_VIEW );
									intent.setDataAndType( Uri.fromFile( new File( path ) ) , "application/vnd.android.package-archive" );
									PendingIntent contentIntent = PendingIntent.getActivity( activity , 0 , intent , 0 );
									notification.contentIntent = contentIntent;
									RemoteViews contentView1 = notification.contentView;
									contentView1.setTextViewText( R.id.notificationTitle , title + activity.getResources().getString( R.string.notify_download_finish ) );
									handler.sendMessage( Message.obtain( handler , MSG_SUCCESS , 100 , id , notification ) );
									removeFromDownloadList( pkgName );
									mDownloadFinish.put( pkgName , id );
									if( resID != null )
										LogHelper.log( activity , LogHelper.LOG_ACTION_DOWNLOAD , pkgName , resID );
									else
										LogHelper.log( activity , LogHelper.LOG_ACTION_DOWNLOAD_VIRTURE_ICON , pkgName , serverResID );
									DownloadProxy.getInstance( activity ).markDownload( pkgName , resID );
								}
								// ackDownloadFinish(info);
								break;
							}
							else
							{
								fos.write( buf , 0 , numRead );
								downloadLength += numRead;
								int tmp = (int)( downloadLength * 100 / totalLength );
								if( tmp != progress && progress != 100 && tmp > 0 )
								{
									progress = tmp;
									if( progress > 100 )
										progress = 100;
									if( handler != null )
									{
										handler.sendMessage( Message.obtain( handler , MSG_UPDATE_PROGRESS , progress , id , notification ) );
									}
									if( listener != null )
									{
										listener.onDownloadProgress( progress );
									}
								}
							}
						}
						else
						{
							// ackDownloadFail(info);
							break;
						}
					}
				}
				catch( IOException e )
				{
					if( handler != null )
					{
						RemoteViews contentView2 = notification.contentView;
						contentView2.setTextViewText( R.id.notificationTitle , title + activity.getResources().getString( R.string.notify_download_fail ) );
						handler.sendMessage( Message.obtain( handler , MSG_FAIL , 0 , id , notification ) );
						removeFromDownloadList( pkgName );
					}
					listener.onDownloadFail();
				}
			}
		};
		thread.start();
	}
	
	public static long readAvailableSystem()
	{
		File root = Environment.getDataDirectory();
		StatFs sf = new StatFs( root.getPath() );
		long blockSize = sf.getBlockSize();
		// long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		return availCount * blockSize;
		// Log.d("", "block大小:"+ blockSize+",block数目:"+
		// blockCount+",总大�?"+blockSize*blockCount/1024+"KB");
		// Log.d("", "可用的block数目�?"+ availCount+",可用大小:"+
		// availCount*blockSize/1024+"KB");
	}
	
	private static Handler mHandler = new Handler();
	
	public synchronized static void initHandler(
			final Context context )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				handler = new Handler() {
					
					@Override
					public void handleMessage(
							Message msg )
					{
						super.handleMessage( msg );
						switch( msg.what )
						{
							case MSG_UPDATE_PROGRESS:
								Notification notification = (Notification)msg.obj;
								RemoteViews contentView = notification.contentView;
								contentView.setTextViewText( R.id.notificationPercent , msg.arg1 + "%" );
								contentView.setProgressBar( R.id.notificationProgress , 100 , msg.arg1 , false );
								NotificationManager mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
								mNotificationManager.notify( msg.arg2 , (Notification)msg.obj );
								break;
							case MSG_SUCCESS:
								Notification notification1 = (Notification)msg.obj;
								notification1.icon = R.drawable.download;
								notification1.flags = 0;
								notification1.flags |= Notification.FLAG_AUTO_CANCEL;
								RemoteViews contentView1 = notification1.contentView;
								// contentView1.setTextViewText(R.id.notificationTitle,
								// title);
								contentView1.setTextViewText( R.id.notificationPercent , "100%" );
								contentView1.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
								NotificationManager mNotificationManager2 = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
								mNotificationManager2.notify( msg.arg2 , (Notification)msg.obj );
								break;
							case MSG_FAIL:
								Notification notification2 = (Notification)msg.obj;
								notification2.icon = R.drawable.download;
								notification2.flags = 0;
								notification2.flags |= Notification.FLAG_AUTO_CANCEL;
								RemoteViews contentView2 = notification2.contentView;
								contentView2.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
								NotificationManager mNotificationManager3 = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
								mNotificationManager3.notify( msg.arg2 , (Notification)msg.obj );
								break;
						}
					}
				};
			}
		} );
		//		if( handler == null )
		//		{
		//			Runnable runnable = new Runnable() {
		//				
		//				@Override
		//				public void run()
		//				{
		//					handler = new Handler() {
		//						
		//						@Override
		//						public void handleMessage(
		//								Message msg )
		//						{
		//							super.handleMessage( msg );
		//							switch( msg.what )
		//							{
		//								case MSG_UPDATE_PROGRESS:
		//									Notification notification = (Notification)msg.obj;
		//									RemoteViews contentView = notification.contentView;
		//									contentView.setTextViewText( R.id.notificationPercent , msg.arg1 + "%" );
		//									contentView.setProgressBar( R.id.notificationProgress , 100 , msg.arg1 , false );
		//									NotificationManager mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		//									mNotificationManager.notify( msg.arg2 , (Notification)msg.obj );
		//									break;
		//								case MSG_SUCCESS:
		//									Notification notification1 = (Notification)msg.obj;
		//									notification1.icon = R.drawable.download;
		//									notification1.flags = 0;
		//									notification1.flags |= Notification.FLAG_AUTO_CANCEL;
		//									RemoteViews contentView1 = notification1.contentView;
		//									// contentView1.setTextViewText(R.id.notificationTitle,
		//									// title);
		//									contentView1.setTextViewText( R.id.notificationPercent , "100%" );
		//									contentView1.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
		//									NotificationManager mNotificationManager2 = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		//									mNotificationManager2.notify( msg.arg2 , (Notification)msg.obj );
		//									break;
		//								case MSG_FAIL:
		//									Notification notification2 = (Notification)msg.obj;
		//									notification2.icon = R.drawable.download;
		//									notification2.flags = 0;
		//									notification2.flags |= Notification.FLAG_AUTO_CANCEL;
		//									RemoteViews contentView2 = notification2.contentView;
		//									contentView2.setViewVisibility( R.id.notificationProgress , View.INVISIBLE );
		//									NotificationManager mNotificationManager3 = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		//									mNotificationManager3.notify( msg.arg2 , (Notification)msg.obj );
		//									break;
		//							}
		//						}
		//					};
		//				}
		//			};
		//			( (Activity)context ).runOnUiThread( runnable );
		//		}
	}
	
	private static void addToDownloadList(
			String pkgName ,
			String title ,
			int id ,
			Notification notification )
	{
		DownloadingItem mDownloadingItem = new DownloadingItem();
		mDownloadingItem.packageName = pkgName;
		mDownloadingItem.title = title;
		mDownloadingItem.id = id;
		mDownloadingItem.notification = notification;
		mDownloadingList.add( mDownloadingItem );
	}
	
	private static void removeFromDownloadList(
			String pkgName )
	{
		DownloadingItem mDownloadingItem = getDownloadingItem( pkgName );
		if( mDownloadingItem != null )
		{
			mDownloadingList.remove( mDownloadingItem );
		}
	}
	
	private static boolean isDownloading(
			String pkgName )
	{
		DownloadingItem mDownloadingItem = getDownloadingItem( pkgName );
		if( mDownloadingItem != null )
		{
			return true;
		}
		return false;
	}
	
	private static DownloadingItem getDownloadingItem(
			String pkgName )
	{
		for( DownloadingItem mDownloadingItem : mDownloadingList )
		{
			if( mDownloadingItem.packageName.equals( pkgName ) )
			{
				return mDownloadingItem;
			}
		}
		return null;
	}
	
	public static void failAllDowningNotification(
			Context context )
	{
		for( DownloadingItem mDownloadingItem : mDownloadingList )
		{
			RemoteViews contentView2 = mDownloadingItem.notification.contentView;
			contentView2.setTextViewText( R.id.notificationTitle , mDownloadingItem.title + context.getResources().getString( R.string.notify_download_fail ) );
			handler.sendMessage( Message.obtain( handler , MSG_FAIL , 0 , mDownloadingItem.id , mDownloadingItem.notification ) );
			mDownloadingList.remove( mDownloadingItem );
		}
	}
	
	public static void removeFinishNotification(
			Context context ,
			String pkgName )
	{
		if( mDownloadFinish == null )
			return;
		if( !mDownloadFinish.containsKey( pkgName ) )
			return;
		int id = mDownloadFinish.get( pkgName );
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.cancel( id );
		mDownloadFinish.remove( pkgName );
	}
	
	public static void removeFailNotification(
			Context context ,
			int id )
	{
		if( mDownloadingList != null )
		{
			for( DownloadingItem mDownloadingItem : mDownloadingList )
			{
				if( mDownloadingItem.id == id )
					return;
			}
		}
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.cancel( id );
	}
}
