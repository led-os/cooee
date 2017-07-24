package com.cooee.framework.function.DynamicEntry;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUnInstall.UnInstallItem;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.DynamicEntry.DynamicEntry;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.manager.dl_info;


public class OperateDynamicContentParser
{
	
	private Context mContext;
	private OperateDynamicProxy mDynamicProxy;
	private ArrayList<OperateDynamicData> mAdded = new ArrayList<OperateDynamicData>();
	private ArrayList<OperateDynamicData> mRemoved = new ArrayList<OperateDynamicData>();
	private boolean DebugLog = false;
	static private final int CHARNUM = 11;
	private static final String DEFAULT_ICON_FOLDER = "operate_folder/icon/";
	private static final String DEFAULT_ICON_KEY = "f100";
	
	protected OperateDynamicContentParser(
			Context context ,
			OperateDynamicProxy dynamicProxy )
	{
		mContext = context;
		mDynamicProxy = dynamicProxy;
	}
	
	private void logData(
			List<OperateDynamicData> receiveList ,
			String tag )
	{
		if( DebugLog )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				OperateDynamicData data = null;
				OperateDynamicItem item = null;
				for( int i = 0 ; i < receiveList.size() ; i++ )
				{
					data = receiveList.get( i );
					if( data.dynamicType == OperateDynamicUtils.FOLDER )
					{
						Log.e( "COOL" , StringUtils.concat( tag , " Folder Title=" , data.mDeskNameCN ) );
						for( int j = 0 ; j < data.mDynamicItems.size() ; j++ )
						{
							item = data.mDynamicItems.get( j );
							Log.v( "COOL" , StringUtils.concat( tag , " Folder Item index=" , j , " title=" , item.mCNTitle ) );
						}
					}
					else
					{
						Log.e( "COOL" , StringUtils.concat( tag , " Data Title=" , data.mDeskNameCN , " type=" , data.dynamicType ) );
					}
				}
			}
		}
	}
	
	public List<OperateDynamicData> parseContent(
			List<OperateDynamicData> receiveList )
	{
		logData( receiveList , "Before filter " );
		OperateDynamicUtils.removeDuplicate( receiveList );
		logData( receiveList , "after removeDuplicate " );
		//if( receiveList.size() > 0 )
		{
			//List<OperateDynamicData> oldList = processContent( dynamicContent );
			MergeDefaultData( receiveList );
			logData( receiveList , "after MergeDefaultData " );
			removeUninstallData( receiveList );
			mDynamicProxy.reSetAlldata( receiveList , false );
		}
		return receiveList;
	}
	
	//需要在Removed列表中将已经安装的应用找出来，添加到delaySaveList中，并且从mReoved中移除
	private void dealRemovedData(
			List<OperateDynamicData> receiveList )
	{
		if( mContext == null )
		{
			return;
		}
		int DynamicType;
		String pkgName;
		OperateDynamicData dataItem;
		// 处理 DynamicType为VIRTUAL_APP
		for( int j = mRemoved.size() - 1 ; j >= 0 ; j-- )
		{
			dataItem = mRemoved.get( j );
			DynamicType = mRemoved.get( j ).dynamicType;
			if( DynamicType == OperateDynamicUtils.VIRTUAL_APP )
			{
				pkgName = mRemoved.get( j ).mPkgnameOrAddr;
				if( OperateDynamicUtils.checkApkExist( mContext , pkgName ) )
				{
					receiveList.add( dataItem );
					mRemoved.remove( j );
				}
			}
		}
		// 处理 DynamicType为FOLDER,拆分为已安装的和未安装的
		ArrayList<OperateDynamicData> added = new ArrayList<OperateDynamicData>();
		for( int j = mRemoved.size() - 1 ; j >= 0 ; j-- )
		{
			dataItem = mRemoved.get( j );
			DynamicType = mRemoved.get( j ).dynamicType;
			if( DynamicType == OperateDynamicUtils.FOLDER )
			{
				OperateDynamicData tobeAdded = new OperateDynamicData( dataItem );
				ArrayList<OperateDynamicItem> folderItems = dataItem.mDynamicItems;
				OperateDynamicItem folderItem;
				for( int folderi = folderItems.size() - 1 ; folderi >= 0 ; folderi-- )
				{
					folderItem = folderItems.get( folderi );
					pkgName = folderItem.mPackageName;
					if( OperateDynamicUtils.checkApkExist( mContext , pkgName ) )
					{
						folderItems.remove( folderItem );
						tobeAdded.mDynamicItems.add( folderItem );
					}
				}
				if( tobeAdded.mDynamicItems.size() > 0 )
				{
					added.add( tobeAdded );
				}
				if( folderItems.size() == 0 )
				{
					mRemoved.remove( dataItem );
				}
			}
		}
		//将added中的数据合并到receiveList中，这里又两种可能，一种是dynamicID相同，一种是
		//DynamicID不相同。dynamicID相同的合并，dynamicID不同的直接加入
		if( added.size() > 0 )
		{
			String dynamicID;
			String aDynamicID;
			OperateDynamicData aDataItem = null;
			for( int j = added.size() - 1 ; j >= 0 ; j-- )
			{
				aDataItem = added.get( j );
				aDynamicID = aDataItem.dynamicID;
				if( aDataItem.dynamicType == OperateDynamicUtils.FOLDER )
				{
					boolean bFindSame = false;
					for( int i = receiveList.size() - 1 ; i >= 0 ; i-- )
					{
						dataItem = receiveList.get( i );
						dynamicID = dataItem.dynamicID;
						if( dynamicID.equals( aDynamicID ) )
						{
							dataItem.mDynamicItems.addAll( aDataItem.mDynamicItems );
							bFindSame = true;
							break;
						}
					}
					if( !bFindSame && aDataItem != null )
					{
						receiveList.add( aDataItem );
					}
					added.remove( aDataItem );
				}
			}
		}
	}
	
	private void updateUninstallData()
	{
		if( OperateDynamicUtils.EXPIRED_MS_TIME == 0 )
		{
			return;
		}
		ArrayList<UnInstallItem> unInstallItems = mDynamicProxy.getUnInstallItems();
		long currentTime = System.currentTimeMillis();
		for( int i = unInstallItems.size() - 1 ; i >= 0 ; i-- )
		{
			UnInstallItem data = unInstallItems.get( i );
			if( currentTime - data.mRemovedTime > OperateDynamicUtils.EXPIRED_MS_TIME )
			{
				unInstallItems.remove( i );
			}
		}
		mDynamicProxy.saveUnInstallItems();
	}
	
	private boolean findFromList(
			String dynamicID ,
			String pkgname ,
			List<OperateDynamicData> list )
	{
		for( OperateDynamicData data : list )
		{
			if( !data.dynamicID.equals( dynamicID ) )
			{
				if( data.dynamicType == OperateDynamicUtils.FOLDER )
				{
					for( OperateDynamicItem item : data.mDynamicItems )
					{
						if( item.mPackageName.equals( pkgname ) )
						{
							return true;
						}
					}
				}
				else
				{
					if( data.mPkgnameOrAddr.equals( pkgname ) )
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// 需要将default的数据和receive的数据进行比较，如果default有，receive没有，要加入到receive中
	//实现方法：第一步是先看default中非文件夹的数据receive中是否存在，如果不存在，需要加入
	//第二步，看default中的文件夹数据receive中是否存在，如果不存在，直接加入。如果存在，看里面
	//的内容是否有需要添加的。
	// 新来的数据和defaultLayout中数据比较的时候，对于虚应用或者虚链接，新数据如果ID一样，直接替换，并且canDelete
	//属性取defaultLayout中的
	//如果ID不一样，但是包名或者URL一样，去掉receive中的
	private void MergeDefaultData(
			List<OperateDynamicData> receiveList )
	{
		String defaultDataString = mDynamicProxy.getDefaultDynamicDataContent();
		List<OperateDynamicData> defaultList = processContent( defaultDataString );
		Iterator<OperateDynamicData> ite = defaultList.iterator();
		//defaultList中去掉不保留的项，只有要求保留的才需要merge
		//		while( ite.hasNext() )
		//		{
		//			OperateDynamicData data = ite.next();
		//			if( !data.keepItem )
		//			{
		//				ite.remove();
		//			}
		//		}
		//		if( defaultList.size() == 0 )
		//		{
		//			return;
		//		}
		dealDefaultListForKeepItem( receiveList , defaultList );
		List<OperateDynamicData> toMergeList = new ArrayList<OperateDynamicData>();
		ite = receiveList.iterator();
		while( ite.hasNext() )
		{
			OperateDynamicData recData = ite.next();
			if( recData.dynamicType == OperateDynamicUtils.FOLDER )
			{
				//receiveList中的文件夹中去掉defaultList中已经有的数据,但id不相同的
				Iterator<OperateDynamicItem> iteItem = recData.mDynamicItems.iterator();
				while( iteItem.hasNext() )
				{
					OperateDynamicItem recItem = iteItem.next();
					if( findFromList( recData.dynamicID , recItem.mPackageName , defaultList ) )
					{
						iteItem.remove();
					}
				}
				if( recData.mDynamicItems.size() == 0 )
				{
					//ite.remove();
				}
			}
		}
		ite = receiveList.iterator();
		while( ite.hasNext() )
		{
			OperateDynamicData recData = ite.next();
			if( recData.dynamicType != OperateDynamicUtils.FOLDER )
			{
				//从defaultList中的文件夹中去掉receiveList中虚图标或虚链接数据
				Iterator<OperateDynamicData> defIte = defaultList.iterator();
				while( defIte.hasNext() )
				{
					OperateDynamicData defData = defIte.next();
					if( defData.dynamicType == OperateDynamicUtils.FOLDER )
					{
						Iterator<OperateDynamicItem> iteItem = defData.mDynamicItems.iterator();
						while( iteItem.hasNext() )
						{
							OperateDynamicItem defItem = iteItem.next();
							if( recData.mPkgnameOrAddr.equals( defItem.mPackageName ) )
							{
								iteItem.remove();
								break;
							}
						}
						if( defData.mDynamicItems.size() == 0 && defData.canDelete == true )
						{
						}
					}
					//added by zfshi 
					else
					{
						if( recData.dynamicID.equals( defData.dynamicID ) == false )
						{
							if( recData.dynamicType == defData.dynamicType && recData.mPkgnameOrAddr.equals( defData.mPkgnameOrAddr ) )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.e( "COOL" , StringUtils.concat( "ite.remove Title=" , recData.mDeskNameCN ) );
								ite.remove();
							}
						}
					}
					// added by zfshi ended;	
				}
			}
		}
		//2.merge
		for( int i = 0 ; i < defaultList.size() ; i++ )
		{
			OperateDynamicData defaultData = defaultList.get( i );
			boolean isFind = false;
			for( int j = 0 ; j < receiveList.size() ; j++ )
			{
				OperateDynamicData newData = receiveList.get( j );
				if( defaultData.dynamicType == OperateDynamicUtils.VIRTUAL_APP || defaultData.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
				{
					if( defaultData.dynamicID.equals( newData.dynamicID ) )
					{
						newData.canDelete = defaultData.canDelete;
						isFind = true;
						break;
					}
					else
					{
						if( newData.dynamicType == defaultData.dynamicType && newData.mPkgnameOrAddr.equals( defaultData.mPkgnameOrAddr ) )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( "COOL" , "can not run here" );
							//isFind = true;
							//break;
						}
					}
				}
				else if( defaultData.dynamicType == OperateDynamicUtils.FOLDER )
				{
					if( defaultData.dynamicID.equals( newData.dynamicID ) && newData.dynamicType == defaultData.dynamicType )
					{
						newData.canDelete = defaultData.canDelete;
						List<OperateDynamicItem> mergeItems = new ArrayList<OperateDynamicItem>();
						for( OperateDynamicItem defaultItem : defaultData.mDynamicItems )
						{
							boolean itemFind = false;
							for( OperateDynamicItem newItem : newData.mDynamicItems )
							{
								if( defaultItem.mPackageName.equals( newItem.mPackageName ) )
								{
									itemFind = true;
									break;
								}
							}
							if( !itemFind )
							{
								mergeItems.add( defaultItem );
							}
						}
						if( mergeItems.size() > 0 )
						{
							newData.mDynamicItems.addAll( mergeItems );
							mergeItems.clear();
						}
						isFind = true;
						break;
					}
				}
			}
			if( !isFind )
			{
				toMergeList.add( defaultData );
			}
		}
		receiveList.addAll( toMergeList );
	}
	
	// @2015/01/05 ADD START Keepitem功能
	//A:本地内置数据和服务器数据合并.B:以服务器配置的数据为准。默认是B 即keepItem值为true
	public void dealDefaultListForKeepItem(
			List<OperateDynamicData> receiveList ,
			List<OperateDynamicData> defaultList )
	{
		if( receiveList.size() == 0 || defaultList.size() == 0 )
		{
			return;
		}
		Iterator<OperateDynamicData> ite = receiveList.iterator();
		while( ite.hasNext() )
		{
			OperateDynamicData recData = ite.next();
			if( recData.dynamicType == OperateDynamicUtils.FOLDER )
			{
				Iterator<OperateDynamicData> defIte = defaultList.iterator();
				while( defIte.hasNext() )
				{
					OperateDynamicData defData = defIte.next();
					if( recData.dynamicID.equals( defData.dynamicID ) && defData.dynamicType == OperateDynamicUtils.FOLDER )
					{
						if( !defData.keepItem && recData.mDynamicItems.size() != 0 )
						{
							defIte.remove();
							break;
						}
					}
				}
			}
		}
	}
	
	// @2015/01/05 ADD END
	public void removeUninstallData(
			List<OperateDynamicData> dataList )
	{
		if( dataList.size() == 0 )
		{
			return;
		}
		if( OperateDynamicUtils.EXPIRED_MS_TIME == 0 )
		{
			return;
		}
		updateUninstallData();
		ArrayList<UnInstallItem> unInstallItems = mDynamicProxy.getUnInstallItems();
		if( unInstallItems.size() == 0 )
		{
			return;
		}
		int DynamicType;
		String comparePkgName;
		OperateDynamicData dataItem;
		for( int i = unInstallItems.size() - 1 ; i >= 0 ; i-- )
		{
			String pkgName = unInstallItems.get( i ).mPackageName;
			String unInstallDynamicID = unInstallItems.get( i ).dynamicID;
			for( int j = dataList.size() - 1 ; j >= 0 ; j-- )
			{
				dataItem = dataList.get( j );
				DynamicType = dataItem.dynamicType;
				if( DynamicType == OperateDynamicUtils.VIRTUAL_APP || DynamicType == OperateDynamicUtils.VIRTUAL_LINK )
				{
					comparePkgName = dataItem.mPkgnameOrAddr;
					if( pkgName.equals( comparePkgName ) )
					{
						if( unInstallDynamicID != null && unInstallDynamicID.equals( dataItem.dynamicID ) )
						{
							dataList.remove( dataItem );
						}
					}
				}
				else
				{
					ArrayList<OperateDynamicItem> folderItems = dataItem.mDynamicItems;
					OperateDynamicItem folderItem;
					for( int folderi = folderItems.size() - 1 ; folderi >= 0 ; folderi-- )
					{
						folderItem = folderItems.get( folderi );
						comparePkgName = folderItem.mPackageName;
						if( pkgName.equals( comparePkgName ) )
						{
							if( unInstallDynamicID != null && unInstallDynamicID.equals( dataItem.dynamicID ) )
							{
								folderItems.remove( folderItem );
							}
						}
					}
					if( folderItems.size() == 0 && dataItem.canDelete == true )
					{
						dataList.remove( dataItem );
					}
				}
			}
		}
	}
	
	private void getAddAndRemoved(
			List<OperateDynamicData> oldList ,
			List<OperateDynamicData> receiveList )
	{
		int aDynamicType; //类型：1：文件夹；2：虚图标；3：虚链接
		String aDynamicID; //动态入口id
		int rDynamicType;
		String rDynamicID;
		mAdded = (ArrayList<OperateDynamicData>)receiveList;
		mRemoved = (ArrayList<OperateDynamicData>)oldList;
		for( int i = mAdded.size() - 1 ; i >= 0 ; i-- )
		{
			OperateDynamicData aData = mAdded.get( i );
			aDynamicType = aData.dynamicType;
			aDynamicID = aData.dynamicID;
			for( int j = mRemoved.size() - 1 ; j >= 0 ; j-- )
			{
				OperateDynamicData rData = mRemoved.get( j );
				rDynamicType = rData.dynamicType;
				rDynamicID = rData.dynamicID;
				if( aDynamicType == rDynamicType && aDynamicID.equals( rDynamicID ) )
				{
					if( aDynamicType == OperateDynamicUtils.VIRTUAL_APP || aDynamicType == OperateDynamicUtils.VIRTUAL_LINK )
					{
						if( aData.mPkgnameOrAddr.equals( rData.mPkgnameOrAddr ) )
						{
							mRemoved.remove( rData );
							mAdded.remove( aData );
							break;
						}
					}
					else
					{
						// TODO
						removeFolderSameData( aData , rData );
						if( aData.mDynamicItems.size() == 0 )
						{
							mAdded.remove( aData );
						}
						if( rData.mDynamicItems.size() == 0 )
						{
							mRemoved.remove( rData );
						}
					}
				}
			}
		}
	}
	
	private void mergeFolderData(
			OperateDynamicData src ,
			OperateDynamicData dest )
	{
		ArrayList<OperateDynamicItem> srcFolder = src.mDynamicItems;
		ArrayList<OperateDynamicItem> destFolder = dest.mDynamicItems;
		ArrayList<OperateDynamicItem> removed = new ArrayList<OperateDynamicItem>();
		String srcPkgName;
		String destPkgName;
		int srcFolderOrigLen = srcFolder.size();
		boolean bFind = false;
		for( int i = srcFolder.size() - 1 ; i >= 0 ; i-- )
		{
			OperateDynamicItem srcItem = srcFolder.get( i );
			srcPkgName = srcItem.mPackageName;
			bFind = false;
			for( int j = destFolder.size() - 1 ; j >= 0 ; j-- )
			{
				destPkgName = destFolder.get( j ).mPackageName;
				if( srcPkgName.equals( destPkgName ) )
				{
					bFind = true;
					break;
				}
			}
			if( bFind )
			{
				removed.add( srcItem );
			}
		}
		if( removed.size() == srcFolderOrigLen )
		{
			return;
		}
		if( removed.size() > 0 )
		{
			srcFolder.removeAll( removed );
		}
		if( srcFolder.size() > 0 )
		{
			destFolder.addAll( srcFolder );
		}
	}
	
	private boolean mergeData(
			OperateDynamicData src ,
			OperateDynamicData dest )
	{
		int nDynamicType = dest.dynamicType;
		if( nDynamicType == OperateDynamicUtils.VIRTUAL_APP || nDynamicType == OperateDynamicUtils.VIRTUAL_LINK )
		{
			if( src.mPkgnameOrAddr.equals( dest.mPkgnameOrAddr ) )
			{
				return true;
			}
		}
		else if( nDynamicType == OperateDynamicUtils.FOLDER )
		{
			mergeFolderData( src , dest );
			return true;
		}
		return false;
	}
	
	private void removeFolderSameData(
			OperateDynamicData aData ,
			OperateDynamicData rData )
	{
		ArrayList<OperateDynamicItem> aFolder = aData.mDynamicItems;
		ArrayList<OperateDynamicItem> rFolder = rData.mDynamicItems;
		String aPkgName;
		String rPkgName;
		boolean bFind = false;
		for( int i = aFolder.size() - 1 ; i >= 0 ; i-- )
		{
			OperateDynamicItem aItem = aFolder.get( i );
			aPkgName = aItem.mPackageName;
			bFind = false;
			for( int j = rFolder.size() - 1 ; j >= 0 ; j-- )
			{
				OperateDynamicItem rItem = rFolder.get( j );
				rPkgName = rItem.mPackageName;
				if( rPkgName.equals( aPkgName ) )
				{
					rFolder.remove( rItem );
					bFind = true;
					break;
				}
			}
			if( bFind )
			{
				aFolder.remove( aItem );
			}
		}
	}
	
	public List<OperateDynamicData> processContent(
			String dynamicContent )
	{
		List<OperateDynamicData> dataList = new ArrayList<OperateDynamicData>();
		if( dynamicContent == null )
		{
			return dataList;
		}
		try
		{
			JSONObject list = new JSONObject( dynamicContent );
			Iterator<?> keys = (Iterator<?>)list.keys();
			String key;
			while( keys.hasNext() )
			{
				//	r1	list	数字	入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
				//  r2  list    类型：1：文件夹 2：应用程序 3：网页链接
				//	r3	list	对象，字符	在桌面的英文名
				//	r4	list	对象，字符	在桌面的中文名称
				//	r5	list	对象，字符	在桌面的繁体名称
				//	r3_1	list	对象，字符	在主菜单的英文名称
				//	r4_1	list	对象，字符	在主菜单的中文名称
				//	r5_1	list	对象，字符	在主菜单的繁体名称
				//	r6	list	对象，数字	应用程序列表 0:不显示 1:显示
				//	r7	list	对象，数字	显示在第几屏。0为第一页
				//  r7_1                     对象，数字	桌面 0:不显示 1:显示
				//	r8	list	对象，数字	快捷方式显示屏幕位置x
				//	r9	list	对象，数字	快捷方式显示屏幕位置y
				//	r10	list	对象，字符	图标地址url
				//	r11	list	对象，字符	网页链接入口url地址或packname
				//  r12 list    对象，数字      桌面是否显示N标
				//  r12_1 list    对象，数字     主菜单是否显示N标
				//  f10            对象，数字  是否导流量
				OperateDynamicData data = new OperateDynamicData();
				key = (String)keys.next();
				JSONObject item = list.getJSONObject( key );
				data.dynamicID = item.optString( "r1" );
				data.dynamicType = item.optInt( "r2" );
				data.mDeskName = item.optString( "r3" );
				data.mDeskNameCN = item.optString( "r4" );
				data.mDeskNameTW = item.optString( "r5" );
				data.mName = item.optString( "r3_1" );
				data.mNameCN = item.optString( "r4_1" );
				data.mNameTW = item.optString( "r5_1" );
				data.mIsShow = item.optBoolean( "r6" );
				data.screen = item.optInt( "r7" );
				data.mIsDeskShow = item.optBoolean( "r7_1" );
				data.cellX = item.optInt( "r8" );
				data.cellY = item.optInt( "r9" );
				data.iconPath = item.optString( "r10" );
				data.mPkgnameOrAddr = item.optString( "r11" );
				data.mAppDownloadType = item.optInt( "f10" );//dynamicEntry1010
				data.mWeblinkPkg = item.optString( "f11" , null );
				if( item.has( "r11_1" ) )
				{
					data.downloadTip = item.optString( "r11_1" );
				}
				data.mDeskHot = item.optBoolean( "r12" );
				data.mIsShowHot = item.optBoolean( "r12_1" );
				//data.iconPath = item.optString( "r20" );
				//data.mIsDeskShow = item.optBoolean( "r21" );
				//data.mIsShow = item.optBoolean( "r22" );
				data.canDelete = item.optBoolean( OperateDynamicUtils.DYNAMIC_DATA_CAN_DELETE , true );
				data.keepItem = item.optBoolean( OperateDynamicUtils.DYNAMIC_DATA_KEEP_ITEM , false );
				if( item.has( "folder" ) )
				{
					JSONArray folders = item.getJSONArray( "folder" );
					for( int j = 0 ; j < folders.length() ; j++ )
					{
						OperateDynamicItem dynamicItem = new OperateDynamicItem();
						JSONObject folder = folders.getJSONObject( j );
						dynamicItem.dynamicType = folder.optInt( "f0" );
						dynamicItem.mPackageName = folder.optString( "f1" );
						if( folder.has( "f2" ) )
						{
							dynamicItem.mDownloadTip = folder.optString( "f2" );
						}
						dynamicItem.mCNTitle = folder.optString( "f6" );
						dynamicItem.mTitle = folder.optString( "f7" );
						dynamicItem.mTWTitle = folder.optString( "f8" );
						dynamicItem.mBitmapPath = folder.optString( "f9" );
						dynamicItem.mAppDownloadType = folder.optInt( "f10" );//dynamicEntry1010
						dynamicItem.mAppSize = folder.optInt( "f5" );
						dynamicItem.mWeblinkPkg = folder.optString( "f11" , null );
						try
						{
							dynamicItem.mIconBitmap = BitmapFactory.decodeStream( mContext.getAssets().open( dynamicItem.mBitmapPath ) );
						}
						catch( IOException ex )
						{
							dynamicItem.mIconBitmap = BitmapFactory.decodeFile( dynamicItem.mBitmapPath );
						}
						data.mDynamicItems.add( dynamicItem );
					}
				}
				dataList.add( data );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return dataList;
	}
	
	public static List<OperateDynamicData> parseDynamicData(
			Context context ,
			JSONObject list ,
			boolean isLocal )
	{
		List<OperateDynamicData> dataList = new ArrayList<OperateDynamicData>();
		try
		{
			CoolDLMgr dlMgr = null;
			if( !isLocal )
				dlMgr = DynamicEntry.CoolDLMgr( context , "DICON" );
			Iterator<?> keys = (Iterator<?>)list.keys();
			String key;
			while( keys.hasNext() )
			{
				OperateDynamicData data = new OperateDynamicData();
				key = (String)keys.next();
				JSONObject item = list.getJSONObject( key );
				//	r1	list	数字	入口ID [入口ID是客户端标示或者定位入口的唯一ID。]
				//  r2  list    类型：1：文件夹 2：应用程序 3：网页链接
				//	r3	list	对象，字符	英文名称
				//	r4	list	对象，字符	中文名称
				//	r5	list	对象，字符	繁体名称
				//	r6	list	对象，数字	应用程序列表 0:不显示 1:显示
				//	r7	list	对象，数字	桌面 0:不显示 1:显示
				//	r8	list	对象，数字	快捷方式显示屏幕位置x
				//	r9	list	对象，数字	快捷方式显示屏幕位置y
				//	r10	list	对象，字符	图标地址url
				//	r11	list	对象，字符	网页链接入口url地址或packname
				//  r12 list    对象，数字      是否显示N标
				//  f10 list    对象，数字      是否导流量
				int r1 = item.optInt( "r1" );
				int r2 = item.optInt( "r2" );
				String r3 = item.optString( "r3" );
				String r4 = item.optString( "r4" );
				String r5 = item.optString( "r5" );
				int r6 = item.optInt( "r6" );
				int r7 = item.optInt( "r7" );
				int r8 = item.optInt( "r8" );
				int r9 = item.optInt( "r9" );
				String r10 = item.optString( "r10" );
				String r11 = item.optString( "r11" );
				int r12 = item.optInt( "r12" );
				data.dynamicID = String.valueOf( r1 );
				data.dynamicType = r2;
				// zhujieping@2015/07/15 ADD START
				data.mWeblinkPkg = null;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "parseDynamicData" , StringUtils.concat( "r3=" , r3 ) );
				if( data.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
				{
					String r13 = item.optString( "r13" , null );
					if( r13 != null )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "parseDynamicData" , StringUtils.concat( "r13=" , r13 ) );
						r13 = r13.trim();
						if( r13.length() > 3 )
						{
							data.mWeblinkPkg = r13;
						}
					}
				}
				// zhujieping@2015/07/15 ADD END
				if( r3.length() > CHARNUM )
				{
					r3 = r3.substring( 0 , CHARNUM );
				}
				if( r4.length() > CHARNUM )
				{
					r4 = r4.substring( 0 , CHARNUM );
				}
				if( r5.length() > CHARNUM )
				{
					r5 = r5.substring( 0 , CHARNUM );
				}
				data.mDeskName = data.mName = r3;
				data.mDeskNameCN = data.mNameCN = r4;
				data.mDeskNameTW = data.mNameTW = r5;
				data.mIsShow = r6 == 1;
				if( r7 > 0 )
				{
					data.mIsDeskShow = true;
					data.screen = r7;//我们桌面是从id=1开始的，因此不需要-1
				}
				data.cellX = r8 - 1;
				data.cellY = r9 - 1;
				if( data.dynamicType == OperateDynamicUtils.FOLDER && dlMgr != null )
				{
					dl_info info = dlMgr.UrlGetInfo( r10 );
					if( info != null && info.IsDownloadSuccess() )
					{
						data.iconPath = info.getFilePath();
					}
				}
				data.mAppDownloadType = item.optInt( "f10" );//dynamicEntry1010 start
				data.mPkgnameOrAddr = r11;
				data.mDeskHot = data.mIsShowHot = r12 == 1;
				if( item.has( "folder" ) )
				{
					JSONArray folders = item.getJSONArray( "folder" );
					//  f0  folder  对象，字符      类型：2：应用程序  3：网页链接
					//	f1	folder	对象，字符	应用程序包名
					//	f2	folder	对象，字符	下载的时候文字提示
					//	f3	folder	对象，字符	应用版本号
					//	f4	folder	对象，字符	版本名称
					//	f5	folder	对象，数字	APK 文件大小
					//	f6	folder	对象，字符	中文名
					//	f7	folder	对象，字符	英文名
					//	f8	folder	对象，字符	繁体名
					//	f9	folder	对象，字符	icon
					for( int j = 0 ; j < folders.length() ; j++ )
					{
						OperateDynamicItem dynamicItem = new OperateDynamicItem();
						JSONObject folder = folders.getJSONObject( j );
						int f0 = folder.optInt( "f0" );
						String f1 = folder.optString( "f1" );
						String f2 = folder.optString( "f2" );
						String f3 = folder.optString( "f3" );
						String f4 = folder.optString( "f4" );
						int f5 = folder.optInt( "f5" );
						String f6 = folder.optString( "f6" );
						String f7 = folder.optString( "f7" );
						String f8 = folder.optString( "f8" );
						String f9 = folder.optString( "f9" );
						int f10 = folder.optInt( "f10" );//dynamicEntry1010 
						String defaultIconPath = folder.optString( DEFAULT_ICON_KEY );
						dl_info info = null;
						if( f0 == 3 && dlMgr != null )
						{
							info = dlMgr.UrlGetInfo( f9 );
						}
						else if( dlMgr != null )
						{
							info = dlMgr.IconGetInfo( f1 , "drawable" );
						}
						if( data.dynamicType == OperateDynamicUtils.FOLDER )
						{
							dynamicItem.mPackageName = f1;
							dynamicItem.mDownloadTip = f2;
							dynamicItem.mCNTitle = f6;
							dynamicItem.mTitle = f7;
							dynamicItem.mTWTitle = f8;
							dynamicItem.dynamicType = f0;
							dynamicItem.mAppDownloadType = f10;//dynamicEntry1010
							dynamicItem.mAppSize = f5;
							// zhujieping@2015/07/15 ADD START
							String f11 = folder.optString( "f11" , null );
							if( f11 != null )
							{
								f11 = f11.trim();
								if( f11.length() <= 3 )
								{
									dynamicItem.mWeblinkPkg = null;
								}
								else
								{
									if( dynamicItem.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
									{
										dynamicItem.mWeblinkPkg = f11;
									}
									else
									{
										dynamicItem.mWeblinkPkg = null;
									}
								}
							}
							// zhujieping@2015/07/15 ADD END
							if( f1 != null && !f1.equals( "" ) )
							{
								if( info != null )
								{
									dynamicItem.mBitmapPath = info.getFilePath();
									dynamicItem.mIconBitmap = BitmapFactory.decodeFile( dynamicItem.mBitmapPath );
								}
								if( dynamicItem.mIconBitmap == null )
								{
									if( defaultIconPath != null && !defaultIconPath.equals( "" ) )
									{
										if( !TextUtils.isEmpty( BaseDefaultConfig.CONFIG_CUSTOM_OPERATE_PATH ) )
										{
											dynamicItem.mBitmapPath = StringUtils.concat( BaseDefaultConfig.CONFIG_CUSTOM_OPERATE_PATH , File.separator , defaultIconPath );
											dynamicItem.mIconBitmap = BitmapFactory.decodeStream( new FileInputStream( dynamicItem.mBitmapPath ) );
										}
										else
										{
											dynamicItem.mBitmapPath = StringUtils.concat( DEFAULT_ICON_FOLDER , defaultIconPath );
											dynamicItem.mIconBitmap = BitmapFactory.decodeStream( context.getAssets().open( dynamicItem.mBitmapPath ) );
										}
									}
								}
								if( dynamicItem.mIconBitmap != null )
								{
									data.mDynamicItems.add( dynamicItem );
								}
							}
						}
						else
						{
							if( f1 != null && !f1.trim().equals( "" ) )
							{
								data.mDeskName = data.mName = f7;
								data.mDeskNameCN = data.mNameCN = f6;
								data.mDeskNameTW = data.mNameTW = f8;
								data.mPkgnameOrAddr = f1;
								data.downloadTip = f2;
								data.mAppDownloadType = f10;//dynamicEntry1010
								data.mAppSize = f5;
								if( info != null )
								{
									data.iconPath = info.getFilePath();
								}
								else
								{
									if( defaultIconPath != null )
									{
										data.iconPath = defaultIconPath;
									}
								}
								// zhujieping@2015/07/15 ADD START
								if( data.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
								{
									String f11 = folder.optString( "f11" ).trim();
									if( f11 != null && f11.length() > 3 )
									{
										data.mWeblinkPkg = f11;
									}
								}
								else if( data.dynamicType == OperateDynamicUtils.VIRTUAL_APP )
								{
									data.mWeblinkPkg = null;
								}
								// zhujieping@2015/07/15 ADD END
							}
						}
					}
				}
				else
				{
					continue;
				}
				if( data.dynamicType != OperateDynamicUtils.FOLDER || data.mDynamicItems.size() > 0 )
				{
					if( data.dynamicType != OperateDynamicUtils.FOLDER )
					{
						// 如果路径为空或者根据路径找不到图片，不应加入
						if( data.iconPath == null || BitmapFactory.decodeFile( data.iconPath ) == null )
						{
							continue;
						}
					}
					if( data.mIsDeskShow || data.mIsShow )
					{
						dataList.add( data );
					}
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		// 在加入免责声明后，代码很多地方都将dataList size()为空的
		//的情况下给过滤了，而现在后台要求如果不配置任何东西的时候，会传递
		//NULL过来，要将前次更新的数据全部清除，为了尽量少的修改代码
		//在这里生成一个NULL OperateDynamicData，保证dataList
		// size()大于0
		if( dataList.size() == 0 )
		{
			dataList.add( generateNULLData() );
		}
		return dataList;
	}
	
	private static OperateDynamicData generateNULLData()
	{
		OperateDynamicData nullData = new OperateDynamicData();
		nullData.dynamicID = "dummyNullID";
		nullData.dynamicType = OperateDynamicUtils.VIRTUAL_APP;
		nullData.mPkgnameOrAddr = nullData.mDeskName = nullData.mDeskNameTW = nullData.mDeskNameCN = "dummyNullName";
		nullData.mName = nullData.mNameCN = nullData.mNameTW = "dummyNullName";
		nullData.iconPath = null;
		return nullData;
	}
}
