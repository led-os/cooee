package com.cooee.framework.function.DynamicEntry.DLManager;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.Category.Category;
import cool.sdk.Category.CategoryConstant;
import cool.sdk.DynamicEntry.DynamicEntry;
import cool.sdk.SAManager.SAHelper;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_task;


public class DlManager
{
	
	private static CoolDLMgr mDlMgr = null;
	private static DlManager mInstance = null;
	private static DlNotifyManager mDlNotifyManager;
	private static NotificationManager mNotificationManager;
	private static final String TAG = "DlManager";
	private static Object obj = new Object();
	private ReDownloadHelperHandle mReDownloadHelperHandle = null;
	private DownloadHandle mDownloadHandle = null;
	private DialogHandle mDialogHandle = null;
	private SharedPreferenceHandle mSharedPreferenceHandle = null;
	private WifiSAHandle mWifiSAHandle = null;
	private long LEFT_ROM_SIZE = 10 * 1024 * 1024;
	public final static int UN_KNOW = -1000;
	
	public void downloadFile(
			final Context context ,
			final String title ,
			final String pkgName ,
			final boolean isWifiRedownload )
	{
		//		long availableSize = getSDAvailaleSize();
		//		//下载apk后的剩余rom空间必须大于指定的值
		//		if( availableSize < LEFT_ROM_SIZE )
		//		{
		//			// Fixed ME 
		//			Toast.makeText( context , context.getResources().getString( R.string.msg_insert_SD ) , Toast.LENGTH_LONG ).show();
		//			return;
		//		}
		Thread thread = new Thread() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				//super.run();
				dl_info info = mDlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
				DownloadingItem item = mDlNotifyManager.getDownloadingItem( pkgName );
				if( null != item && ( item.state == Constants.DL_STATUS_ING ) )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "packageName [" , pkgName , "] is downloading , return " ) );
					return;
				}
				if( info != null && info.IsDownloadSuccess() )
				{
					// 添加Patch，在实际下载过程中，发现有时候下载提示100%，但是状态不是Success
					// 强制将状态切换为下载完成
					if( item != null && item.state != Constants.DL_STATUS_SUCCESS )
					{
						final DlCallback dlItemCb = item.callback;
						item.notifyID = info.getID();
						item.filePath = info.getFilePath();
						item.state = Constants.DL_STATUS_SUCCESS;
						item.progress = 100;
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( TAG , StringUtils.concat( "packageName  go patch flow [" , pkgName , "] is downloading , return " ) );
						updateDowningItem( dlItemCb );
					}
					return;
				}
				downloadFile2( context , pkgName , title , isWifiRedownload );
				//增加一个WIFI下载点
				mReDownloadHelperHandle.startImproperStopTasks( BaseAppState.getActivityInstance() );//dynamicEntry
				SAHelper.getInstance( context ).removeSlientItem( pkgName );
			}
		};
		thread.start();
	}
	
	private long getSDAvailaleSize()
	{
		File SDdir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED );
		if( sdCardExist )
		{
			SDdir = Environment.getExternalStorageDirectory();
		}
		if( SDdir != null )
		{
			StatFs stat = new StatFs( SDdir.getPath() );
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize / 1024 / 1024;
		}
		return 0;
	}
	
	private void initDownloadCallback(
			final Context context ,
			final String pkgName ,
			final String title ,
			DownloadingItem item )
	{
		RemoteViews contentView;
		if( item.notification == null )
		{
			final Notification notification = new Notification( R.drawable.download , title , System.currentTimeMillis() );
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			contentView = new RemoteViews( context.getPackageName() , R.layout.dynamicentry_folder_notification );
			notification.contentView = contentView;
			item.notification = notification;
		}
		else
		{
			contentView = item.notification.contentView;
			item.notification.flags = Notification.FLAG_ONGOING_EVENT;
		}
		Bitmap bitmap = getDownloadHandle().getDownBitmap( pkgName );
		if( bitmap != null )
		{
			contentView.setImageViewBitmap( R.id.notificationImage , bitmap );
		}
		contentView.setViewVisibility( R.id.notificationProgress , View.VISIBLE );
		contentView.setTextViewText( R.id.notificationTitle , StringUtils.concat( title , BaseDefaultConfig.getString( R.string.notify_downloading ) ) );
		item.title = title;
		item.state = Constants.DL_STATUS_ING;
		if( item.callback == null )
		{
			final DlCallback dlItemNewCb = new DlCallback( item );
			//if( DefaultLayout.showAppDownloadNotification )
			{
				DlNotifyStatusBar notifyBar = new DlNotifyStatusBar( context , title );
				dlItemNewCb.Attach( notifyBar );
			}
			dlItemNewCb.Attach( mDlNotifyManager );
		}
	}
	
	private synchronized void downloadFile2(
			final Context context ,
			final String pkgName ,
			final String title ,
			final boolean isWifiRedownload )
	{
		BaseAppState.getActivityInstance().runOnUiThread( new Runnable() {//放在最后执行，可能在下载过程中导致失败，这个被后执行，导致显示状态不对，因此拿到最前面
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
						client.upateDownloadItemState( pkgName , Constants.DL_STATUS_ING );
					}
				} );
		DownloadingItem item = mDlNotifyManager.getDownloadingItem( pkgName );
		if( item == null )
		{
			item = mDlNotifyManager.addToDownloadList( pkgName , title , Constants.DL_INVALID_NOTIFYID , Constants.DL_STATUS_ING , 0 , null );
			item.progress = 0;
		}
		item.isWifiReDownload = isWifiRedownload;
		initDownloadCallback( context , pkgName , title , item );
		final DlCallback dlItemCb = item.callback;
		dl_task task = null;
		int categoryID = CategoryParse.getInstance().getCategoryAppID( pkgName );
		if( CategoryConstant.UN_KNOW != categoryID )
		{
			task = mDlMgr.ResDownloadNewTask( CoolDLResType.RES_TYPE_APK , pkgName , dlItemCb , Category.h12 , StringUtils.concat( Category.h13 , categoryID ) );
		}
		else
		{
			task = mDlMgr.ResDownloadNewTask( CoolDLResType.RES_TYPE_APK , pkgName , dlItemCb );
		}
		//		{
		//			Log.i( TAG , "PRODUCT_NORMAL pkgName: " + pkgName );
		//			task = mDlMgr.ResDownloadNewTask( CoolDLResType.RES_TYPE_APK , pkgName , dlItemCb );
		//		}
		task.setValue( "p101" , title );
		mDlMgr.ResDownloadStart( task );
		dl_info info = mDlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		if( info != null && 0 != info.getTotalBytes() )
		{
			item.progress = (int)( (float)info.getCurBytes() / info.getTotalBytes() * 100 );
		}
		if( info != null )
		{
			item.notifyID = info.getID();
			item.filePath = info.getFilePath();
			if( !info.IsDownloadSuccess() || 2 != info.getDownloadState() )
			{
				//				if( item.progress == 0 )
				{
					updateDowningItem( dlItemCb );
				}
			}
		}
	}
	
	private DlManager()
	{
		mReDownloadHelperHandle = new ReDownloadHelperHandle();
		mDownloadHandle = new DownloadHandle();
		mDialogHandle = new DialogHandle();
		mSharedPreferenceHandle = new SharedPreferenceHandle();
		mWifiSAHandle = new WifiSAHandle();
	}
	
	public ReDownloadHelperHandle getReDownloadHelperHandle()
	{
		return mReDownloadHelperHandle;
	}
	
	public DownloadHandle getDownloadHandle()
	{
		return mDownloadHandle;
	}
	
	public DialogHandle getDialogHandle()
	{
		return mDialogHandle;
	}
	
	public SharedPreferenceHandle getSharedPreferenceHandle()
	{
		return mSharedPreferenceHandle;
	}
	
	public WifiSAHandle getWifiSAHandle()
	{
		return mWifiSAHandle;
	}
	
	private static void init()
	{
		mInstance = new DlManager();
		mDlNotifyManager = new DlNotifyManager( BaseAppState.getActivityInstance() );
		mNotificationManager = (NotificationManager)BaseAppState.getActivityInstance().getSystemService( Context.NOTIFICATION_SERVICE );
		if( mDlMgr == null )
		{
			//CoolLog.setEnableLog( true );
			mDlMgr = DynamicEntry.CoolDLMgr( BaseAppState.getActivityInstance() , "DAPP" );
			mDlMgr.dl_mgr.setMaxConnectionCount( DlApkMangerActivity.DOWNLOAD_MAX_NUM );
		}
	}
	
	public static DlManager getInstance()
	{
		if( mInstance == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "getInstance myPID=" , Process.myPid() ) );
			init();
		}
		return mInstance;
	}
	
	public DlNotifyManager getDlNotifyManager()
	{
		return mDlNotifyManager;
	}
	
	public CoolDLMgr getDlMgr()
	{
		return mDlMgr;
	}
	
	public DownloadingItem getDownloadingItem(
			String pkgName )
	{
		DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		if( downloadingItem == null )
		{
			return null;
		}
		return downloadingItem;
	}
	
	public void removeFromDownloadList(
			String pkgName )
	{
		synchronized( obj )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "removeFromDownloadList pkgName=" , pkgName ) );
			mDlNotifyManager.removeFromDownloadList( pkgName );
		}
	}
	
	public void removeAllNotify()
	{
		synchronized( obj )
		{
			ArrayList<Integer> ids = mDlNotifyManager.getAllNotifyID();
			for( int id : ids )
			{
				mNotificationManager.cancel( id );
			}
			mNotificationManager.cancel( DialogHandle.getDialogNotifyId() );
		}
	}
	
	public boolean clearUnistallApkNotification(
			String pkgName )
	{
		DownloadingItem downloadingItem = mDlNotifyManager.getUninstallItem( pkgName );
		if( downloadingItem != null )
		{
			if( downloadingItem.state == Constants.DL_STATUS_ING )
			{
				mDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , pkgName , true );
			}
			mNotificationManager.cancel( downloadingItem.notifyID );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "DLManager clearUnistallApkNotification our pkgName=" , pkgName ) );
			return true;
		}
		return false;
	}
	
	public synchronized void pauseAppDownload(
			final String pkgName )
	{
		final DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		if( downloadingItem != null && downloadingItem.state == Constants.DL_STATUS_ING )
		{
			// TODO Auto-generated method stub
			if( mDlMgr == null )
			{
				return;
			}
			Thread thread = new Thread() {
				
				@Override
				public void run()
				{
					try
					{
						if( downloadingItem.state == Constants.DL_STATUS_ING )
						{
							mDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , pkgName );
							downloadingItem.state = Constants.DL_STATUS_PAUSE;
							DlCallback dlItemCb = downloadingItem.callback;
							if( dlItemCb != null )
							{
								updateDowningItem( dlItemCb );
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( TAG , StringUtils.concat(
											"pauseAppDownload title:" ,
											downloadingItem.title ,
											"-progress:" ,
											downloadingItem.progress ,
											"-DLpkgName:" ,
											downloadingItem.packageName ,
											"-pkgName:" ,
											pkgName ) );
							}
						}
						else
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( TAG , StringUtils.concat( "pauseAppDownload State is " , downloadingItem.state ) );
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}
	
	public void onResume()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "DlManager" , "Dl Manager getAllState onResume begin" );
		new Thread() {
			
			@Override
			public void run()
			{
				synchronized( obj )
				{
					List<dl_info> dlInfos = getAllState();
					//mDlNotifyManager.onResume( dlInfos , mNotificationManager );
					//重新获取下列表.以免列表让清空了
					mDlNotifyManager.getDownloadListItem( BaseAppState.getActivityInstance() , dlInfos );
					Iterator<Entry<String , DownloadingItem>> iter = mDlNotifyManager.getDownLoadingListIter();
					if( dlInfos == null || dlInfos.size() == 0 )
					{
						if( iter != null )
						{
							while( iter.hasNext() )
							{
								Map.Entry<String , DownloadingItem> entry = (Map.Entry<String , DownloadingItem>)iter.next();
								DownloadingItem item = entry.getValue();
								if( item != null )
								{
									mNotificationManager.cancel( item.notifyID );
								}
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "DlManager" , StringUtils.concat( "onResume dlInfos empty DL_STATUS_ING dlItem.title:" , item.title ) );
								iter.remove();
							}
						}
					}
					else
					{
						if( iter != null )
						{
							while( iter.hasNext() )
							{
								Map.Entry<String , DownloadingItem> entry = (Map.Entry<String , DownloadingItem>)iter.next();
								DownloadingItem item = entry.getValue();
								if( item != null )
								{
									onResumeDownloadItem( item , dlInfos );
									OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
									if( client != null )
									{
										client.upateDownloadItemState( item.packageName , item.state );
									}
								}
							}
						}
					}
				}
			}
		}.start();
	}
	
	private void onResumeDownloadItem(
			DownloadingItem item ,
			final List<dl_info> dlInfos )
	{
		String pkgName = null;
		boolean bFind = false;
		for( dl_info info : dlInfos )
		{
			pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
			if( pkgName == null || pkgName.trim().equals( "" ) )
			{
				continue;
			}
			if( pkgName.equals( item.packageName ) )
			{
				bFind = true;
				//Log.v( "DlManager" , "onResume onResumeDownloadItem find dlItem.title=" + item.title );
				mDlNotifyManager.changeItemStateAndProgress( item , info );
				if( !info.IsDownloadSuccess() && item.state == Constants.DL_STATUS_ING )
				{
					updateDowningItem( item.callback );
				}
				if( item.state == Constants.DL_STATUS_NOTDOWN )
				{
					mNotificationManager.cancel( item.notifyID );
				}
				break;
			}
		}
		if( !bFind )
		{
			item.progress = 0;
			item.state = Constants.DL_STATUS_NOTDOWN;
			mNotificationManager.cancel( item.notifyID );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "DlManager" , StringUtils.concat( "onResume onResumeDownloadItem  mNotificationManager.cancel dlItem.title:" , item.title ) );
		}
	}
	
	public int getPkgNameCurrentProgress(
			String pkgName )
	{
		DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		if( downloadingItem != null )
		{
			return downloadingItem.progress;
		}
		return 0;
	}
	
	public void installApkByPackageName(
			String pkgName )
	{
		String path = getDownSuccessFilePath( pkgName );
		if( path != null )
		{
			OperateDynamicUtils.installAPKFile( BaseAppState.getActivityInstance() , path );
		}
	}
	
	private String getDownSuccessFilePath(
			String pkgName )
	{
		dl_info info = getDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		if( info == null || !info.IsDownloadSuccess() || info.getFilePath().equals( "" ) )
		{
			//wifi1118 start WIFI下载的要从这边返回。
			CoolDLMgr wifidlMgr = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getCoolDLMgrApk();
			info = wifidlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
			if( info == null || !info.IsDownloadSuccess() || info.getFilePath().equals( "" ) )
			{
				return null;
			}
			//wifi1118 end
			//Fixed Me 出现这样的情况应该如何处理呢？目前是直接返回
		}
		return info.getFilePath();
	}
	
	public int getPkgNameCurrentState(
			String pkgName )
	{
		DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		if( downloadingItem != null )
		{
			// Fixed Me,需要验证速度
			if( downloadingItem.state == Constants.DL_STATUS_SUCCESS )
			{
				if( getDownSuccessFilePath( pkgName ) == null )
				{
					downloadingItem.state = Constants.DL_STATUS_NOTDOWN;
					downloadingItem.progress = 0;
					mNotificationManager.cancel( downloadingItem.notifyID );
				}
			}
			return downloadingItem.state;
		}
		//		if( OperateDynamicUtils.checkApkExist( iLoongLauncher.getInstance() , pkgName ) )
		//		{
		//			return Constants.DL_STATUS_INSTALL;
		//		}
		return Constants.DL_STATUS_NOTDOWN;
	}
	
	public int getPkgNameCurrentState_ex(
			String pkgName )
	{
		DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		if( downloadingItem != null )
		{
			return downloadingItem.state;
		}
		return Constants.DL_STATUS_NOTDOWN;
	}
	
	public List<dl_info> getAllState()
	{
		List<dl_info> localList = getLocalList();
		List<dl_info> wifiList = getWifiDownloadAllState();
		if( localList != null && wifiList != null )
		{
			localList.addAll( wifiList );
			return localList;
		}
		else if( localList == null && wifiList != null )
		{
			return wifiList;
		}
		else if( localList != null && wifiList == null )
		{
			return localList;
		}
		return null;
		//return mDlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
	}
	
	public List<dl_info> getLocalList()
	{
		if( mDlMgr == null )
		{
			mDlMgr = DynamicEntry.CoolDLMgr( BaseAppState.getActivityInstance() , "DAPP" );
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "DlManager" , "Dl Manager getAllState begin" );
		try
		{
			return mDlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
		}
		catch( Exception e )
		{
			return new ArrayList<dl_info>();
		}
	}
	
	public dl_info getDlInfo(
			String packageName )
	{
		if( packageName == null )
		{
			return null;
		}
		if( mDlMgr == null )
		{
			mDlMgr = DynamicEntry.CoolDLMgr( BaseAppState.getActivityInstance() , "DAPP" );
		}
		try
		{
			return mDlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , packageName );
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	private void updateDowningItem(
			DlCallback dlItemCb )
	{
		if( dlItemCb == null )
		{
			return;
		}
		mDlNotifyManager.refreshUI();
		dlItemCb.NotifyUpdate();
	}
	
	public void dealMicroEntrceItemUpdate(
			String pkgName ,
			int state )
	{
		if( pkgName != null )
		{
			OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
			if( state == Constants.DL_STATUS_SUCCESS && getDownloadHandle().getMeApkDownloadPath( pkgName ) != null )//不为空时这个说明下载成功
				client.upateDownloadItemState( pkgName , Constants.DL_STATUS_SUCCESS );
			else if( state == Constants.DL_STATUS_NOTDOWN )
				client.upateDownloadItemState( pkgName , state );
		}
	}
	
	public void dealOperateIconRemove(
			String pkgName )
	{
		final DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		mDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , pkgName , true );
		mNotificationManager.cancel( downloadingItem.notifyID );
		removeFromDownloadList( pkgName );
		getDialogHandle().doStatusDownloading( downloadingItem );
	}
	
	public void dealDownMgrActivityMsg(
			String action ,
			String pkgName ,
			String title ,
			int progress )
	{
		final DownloadingItem downloadingItem = mDlNotifyManager.getDownloadingItem( pkgName );
		if( downloadingItem == null )
		{
			return;
		}
		DlCallback dlItemCb = downloadingItem.callback;
		if( dlItemCb == null )
		{
			initDownloadCallback( BaseAppState.getActivityInstance() , pkgName , title , downloadingItem );
		}
		if( action.equals( Constants.DL_MGR_ACTION_DOWNING ) )
		{
			downloadingItem.state = Constants.DL_STATUS_ING;
			downloadingItem.progress = progress;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "DlManager" , StringUtils.concat( "dealDownMgrActivityMsg DL_STATUS_ING dlItem.title:" , downloadingItem.title ) );
			updateDowningItem( dlItemCb );
		}
		else if( action.equals( Constants.DL_MGR_ACTION_FAILURE ) )
		{
			downloadingItem.state = Constants.DL_STATUS_FAIL;
			updateDowningItem( dlItemCb );
		}
		else if( action.equals( Constants.DL_MGR_ACTION_PAUSE ) )
		{
			downloadingItem.state = Constants.DL_STATUS_PAUSE;
			updateDowningItem( dlItemCb );
		}
		else if( action.equals( Constants.DL_MGR_ACTION_REMOVED ) )
		{
			mNotificationManager.cancel( downloadingItem.notifyID );
			removeFromDownloadList( pkgName );
			getDialogHandle().doStatusDownloading( downloadingItem );
			OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
			client.upateDownloadItemState( pkgName , Constants.DL_STATUS_NOTDOWN );//删除apk，置apk的状态为没有下载的状态
		}
		else if( action.equals( Constants.DL_MGR_ACTION_SUCCESS ) )
		{
			// 这里因为有两个dlCallback在竞争，有肯能一个Callback已经发送了
			// success,不需要重复发送，避免在下载完成后重复提示安装问题
			if( downloadingItem.state == Constants.DL_STATUS_SUCCESS )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "DlManager" , StringUtils.concat( "dealDownMgrActivityMsg doStatusSuccess dlItem.title:" , downloadingItem.title ) );
				return;
			}
			downloadingItem.state = Constants.DL_STATUS_SUCCESS;
			if( downloadingItem.filePath == null )
			{
				dl_info info = mDlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
				if( info != null )
				{
					downloadingItem.filePath = info.getFilePath();
				}
				else
				{
					return;
				}
			}
			OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
			client.upateDownloadItemState( downloadingItem.packageName , Constants.DL_STATUS_SUCCESS );
			updateDowningItem( dlItemCb );
		}
	}
	
	public List<dl_info> getWifiDownloadAllState()
	{
		List<dl_info> ret_dl_info_list = new ArrayList<dl_info>();
		List<dl_info> dl_info_list = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getSuccessButNotInstallList();
		if( dl_info_list != null )
		{
			for( dl_info info : dl_info_list )
			{
				String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
				if( pkgName != null )
				{
					String value = getReDownloadHelperHandle().getValue( StringUtils.concat( "prefix_silent" , pkgName ) );
					if( value != null && value.equals( String.valueOf( 1 ) ) )
					{
						ret_dl_info_list.add( info );
					}
				}
			}
		}
		return ret_dl_info_list;
	}
	
	public void dealReceiverAction(
			final String action ,
			final String packageName )
	{
		{
			if( Intent.ACTION_PACKAGE_ADDED.equals( action ) )
			{
				getDownloadHandle().showInStallSuccessNotification( packageName );
			}
			else if( Intent.ACTION_PACKAGE_REMOVED.equals( action ) )
			{
				getDownloadHandle().clearUninstallNotification( packageName );
				getWifiSAHandle().setUninstallToDB( packageName );
			}
		}
	}
}
