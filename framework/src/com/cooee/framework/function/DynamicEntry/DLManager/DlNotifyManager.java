package com.cooee.framework.function.DynamicEntry.DLManager;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Notification;
import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.manager.dl_info;


public class DlNotifyManager implements DlObserverInterface
{
	
	private Context mContext;
	private HashMap<String , DownloadingItem> mDownloadingList = new HashMap<String , DownloadingItem>();
	private HashMap<String , DownloadingItem> mUninstallList = new HashMap<String , DownloadingItem>();
	public final static String OPERATE_FOLDER_DOWNLOAD_DONE_ACTION = "operateFolderLoadDone";//添加在应用已经下载了100%的时候发送广播使用 wanghongjian add
	public final static String OPERATE_FOLDER_PKG_NAME_KEY = "operateFolderPkgNameKey";
	
	public DlNotifyManager(
			Context context )
	{
		mContext = context;
	}
	
	@Override
	public void update(
			DownloadingItem dlItem )
	{
		if( dlItem.state == Constants.DL_STATUS_ING )
		{
			doStatusIng( dlItem );
			DlManager.getInstance().getDialogHandle().popDLOneDialog( dlItem );
		}
		else if( dlItem.state == Constants.DL_STATUS_SUCCESS )
		{
			doStatusSuccess( dlItem );
			DlManager.getInstance().getDialogHandle().saveDownloadAppTime( dlItem.packageName );
		}
		else if( dlItem.state == Constants.DL_STATUS_FAIL )
		{
			doStatusFail( dlItem );
		}
	}
	
	public void changeItemStateAndProgress(
			DownloadingItem item ,
			dl_info info )
	{
		int status = Constants.DL_STATUS_NOTDOWN;
		if( info == null )
		{
			return;
		}
		if( info.getCurBytes() > 0 )
		{
			int ret = OperateDynamicUtils.verifyAPKFile( mContext , info.getFilePath() );
			if( ret == OperateDynamicUtils.STATE_FILE_NOTEXIST )
			{
				item.progress = 0;
				item.state = status;
				return;
			}
		}
		if( info.IsDownloadSuccess() )
		{
			//下载完成，T卡从存储状态过来，会再次提示用户安装。如果是状态是DL_STATUS_NOTDOWN，但IsDownloadSuccess()是成功的。说明是从T卡插拔过来的，到这边直接返回
			if( item.state == Constants.DL_STATUS_NOTDOWN )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "DlManager" , StringUtils.concat( "changeItemStateAndProgress flow item.title:" , item.title , "-item.state:" , item.state , "-item.pkg:" , item.packageName ) );
				item.state = Constants.DL_STATUS_SUCCESS;
				item.progress = 100;
				return;
			}
			if( item.state != Constants.DL_STATUS_SUCCESS )
			{
				// 添加Patch，在实际下载过程中，发现有时候下载提示100%，但是状态不是Success
				// 强制将状态切换为下载完成
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "DlManager" , StringUtils.concat( "changeItemStateAndProgress  go patch flow item.title:" , item.title , "-item.state:" , item.state , "-item.pkg:" , item.packageName ) );
				item.state = Constants.DL_STATUS_SUCCESS;
				item.progress = 100;
				if( item.callback != null )
				{
					item.callback.NotifyUpdate();
				}
			}
		}
		else
		{
			status = info.getDownloadState();
			// 在下载完成后，删除T卡下载应用所在的文件夹，需要根据downloadState 和
			// curBytes共同判断状态，在status为0的情况下，如果curBytes为0
			// 应该将状态设定为未下载状态
			//解决   下载，360卸载文件，解锁，图标显示为错误的暂停状态 故障号6704
			if( status == 0 )
			{
				//提示下载失败的话。不清除状态。下载失败不会显示下载进度打。所以无所谓清除不清除。因为下载失败的。又会提示下载暂停
				if( item.state == Constants.DL_STATUS_FAIL )
				{
					return;
				}
				if( info.getCurBytes() > 0 )
				{
					//Log.v( "DlManager" , "item.state = Constants.DL_STATUS_PAUSE" );
					//只有状态不是暂停状态，才更新notify，避免清楚的已暂停的NOTIFY。调用launcher的 onresume又再次显示出来
					if( item.state == Constants.DL_STATUS_ING )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( "COOL" , "STATE ERROR Constants.DL_STATUS_PAUSE" );
						String packName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
						dl_info tempInfo = DlManager.getInstance().getDlInfo( packName );
						//因为调 用launcher的 onresume的同时会调用继续下载与这边状态检查。避免那边后面更新了状态，这边还是使用的老状态。因此重新获取一下状态
						if( tempInfo != null )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( "COOL" , StringUtils.concat( "STATE ERROR Constants.DL_STATUS_PAUSE tempInfo.getDownloadState():" , tempInfo.getDownloadState() ) );
						}
						if( tempInfo != null && tempInfo.getDownloadState() == 0 )
						{
							item.state = Constants.DL_STATUS_PAUSE;
						}
						// 在lwg三星机器上偶尔发现下载状态为暂停下载，但是通知栏显示
						//正在下载状态，出现这种情况的可能性为下载后失联，在恢复的时候
						//应该通知状态栏更新状态为暂停状态 added by zfshi 
						// 2014-09-10
						if( item.callback != null )
						{
							item.callback.NotifyUpdate();
						}
					}
					// added by zfshi ended
				}
				else
				{
					//下载暂停时。进 度为0时。不应该重置他的状态。导致图标的显示状态异常
					if( item.progress == 0 && item.state == Constants.DL_STATUS_PAUSE )
					{
						return;
					}
					item.state = Constants.DL_STATUS_NOTDOWN;
					//Log.v( "DlManager" , "item.state = Constants.DL_STATUS_NOTDOWN" );
					item.progress = 0;
					return;
				}
			}
			else
			{
				item.state = Constants.DL_STATUS_ING;
			}
			item.progress = (int)( (float)info.getCurBytes() / info.getTotalBytes() * 100 );
		}
	}
	
	//读取下载列表中未下载完成的数据
	public void getDownloadListItem(
			Context context ,
			List<dl_info> dl_infos )
	{
		if( dl_infos != null && dl_infos.size() > 0 )
		{
			for( dl_info info : dl_infos )
			{
				String pKgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
				if( pKgName == null || pKgName.trim().equals( "" ) )
				{
					continue;
				}
				DownloadingItem item = new DownloadingItem();
				item.packageName = pKgName;
				item.filePath = info.getFilePath();
				item.notifyID = info.getID();
				changeItemStateAndProgress( item , info );
				if( !mDownloadingList.containsKey( item.packageName ) )
				{
					mDownloadingList.put( item.packageName , item );
				}
			}
		}
	}
	
	public Iterator<Entry<String , DownloadingItem>> getDownLoadingListIter()
	{
		if( mDownloadingList == null || mDownloadingList.size() == 0 )
		{
			return null;
		}
		Iterator<Entry<String , DownloadingItem>> iter = mDownloadingList.entrySet().iterator();
		return iter;
	}
	
	public void refreshUI()
	{
		//		Gdx.graphics.requestRendering();
	}
	
	public void removeFromDownloadList(
			String pkgName )
	{
		synchronized( mDownloadingList )
		{
			DownloadingItem downloadingItem = getDownloadingItem( pkgName );
			if( downloadingItem != null )
			{
				mDownloadingList.remove( pkgName );
				mUninstallList.put( pkgName , downloadingItem );
				refreshUI();
			}
		}
	}
	
	public ArrayList<Integer> getAllNotifyID()
	{
		ArrayList<Integer> notifyids = new ArrayList<Integer>();
		Set<String> downloadSet = mDownloadingList.keySet();
		for( String set : downloadSet )
		{
			DownloadingItem downloadingItem = getDownloadingItem( set );
			notifyids.add( downloadingItem.notifyID );
		}
		Set<String> uninstallSet = mUninstallList.keySet();
		for( String set : uninstallSet )
		{
			DownloadingItem downloadingItem = getUninstallItem( set );
			notifyids.add( downloadingItem.notifyID );
		}
		return notifyids;
	}
	
	public DownloadingItem getDownloadingItem(
			String pkgName )
	{
		DownloadingItem dlItem = mDownloadingList.get( pkgName );
		return dlItem;
	}
	
	public DownloadingItem getUninstallItem(
			String pkgName )
	{
		return mUninstallList.get( pkgName );
	}
	
	public DownloadingItem addToDownloadList(
			String pkgName ,
			String title ,
			int id ,
			int state ,
			int progress ,
			Notification notification )
	{
		DownloadingItem mDownloadingItem = new DownloadingItem();
		mDownloadingItem.packageName = pkgName;
		mDownloadingItem.title = title;
		mDownloadingItem.notifyID = id;
		mDownloadingItem.state = state;
		mDownloadingItem.progress = progress;
		mDownloadingItem.notification = notification;
		mDownloadingList.put( pkgName , mDownloadingItem );
		return mDownloadingItem;
	}
	
	private void doStatusIng(
			DownloadingItem dlItem )
	{
		refreshUI();
	}
	
	private void doStatusSuccess(
			DownloadingItem dlItem )
	{
		int flag = OperateDynamicUtils.verifyAPKFile( mContext , dlItem.filePath );
		if( flag != OperateDynamicUtils.STATE_FILE_EXIST_ALL )
		{
			dlItem.state = Constants.DL_STATUS_PAUSE;
			return;
		}
		//		boolean builtIn = ( mContext.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0;
		//		if( builtIn )
		//		{
		//			OperateFolderManager.getDownloadHandle().installAPKSilent( mContext , dlItem.filePath , dlItem.packageName );
		//		}
		//		else
		{
			OperateDynamicUtils.installAPKFile( mContext , dlItem.filePath );
		}
		//		sendBroadcastTolauncher( dlItem.packageName );zhujieping del，不需要通过广播，通过接口更新状态
	}
	
	private void doStatusFail(
			DownloadingItem dlItem )
	{
		refreshUI();
	}
	//	private void sendBroadcastTolauncher(
	//			String packName )
	//	{
	//		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance( BaseAppState.getActivityInstance() );
	//		Intent intent = new Intent();
	//		intent.setAction( OPERATE_FOLDER_DOWNLOAD_DONE_ACTION );
	//		intent.putExtra( OPERATE_FOLDER_PKG_NAME_KEY , packName );
	//		localBroadcastManager.sendBroadcast( intent );
	//	}
}
