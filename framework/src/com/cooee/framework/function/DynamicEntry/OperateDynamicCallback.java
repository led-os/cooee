package com.cooee.framework.function.DynamicEntry;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.cooee.framework.function.DynamicEntry.OperateDynamicUnInstall.UnInstallItem;


public class OperateDynamicCallback
{
	
	private Context mContext;
	private OperateDynamicProxy mDynamicProxy;
	
	protected OperateDynamicCallback(
			Context context ,
			OperateDynamicProxy dynamicProxy )
	{
		mContext = context;
		mDynamicProxy = dynamicProxy;
	}
	
	public OperateDynamicData getDynamicFolderData(
			List<OperateDynamicData> list ,
			String folderid )
	{
		if( folderid == null || folderid.length() == 0 )
		{
			return null;
		}
		for( OperateDynamicData data : list )
		{
			if( data.dynamicType == OperateDynamicUtils.FOLDER && data.dynamicID.equals( folderid ) )
			{
				return data;
			}
		}
		return null;
	}
	
	public boolean containsApp(
			List<OperateDynamicData> list ,
			String pkgname )
	{
		for( OperateDynamicData data : list )
		{
			if( data.dynamicType == OperateDynamicUtils.VIRTUAL_APP || data.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
			{
				if( pkgname.equals( data.mPkgnameOrAddr ) )
				{
					return true;
				}
			}
			else if( data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				for( OperateDynamicItem item : data.mDynamicItems )
				{
					if( item.mPackageName.equals( pkgname ) )
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean getIsShowDesktopHot(
			List<OperateDynamicData> list ,
			String dynamicID )
	{
		for( OperateDynamicData data : list )
		{
			if( dynamicID.equals( data.dynamicID ) && data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				return data.mDeskHot;
			}
		}
		return false;
	}
	
	public boolean getIsShowDesktopAppHot(
			List<OperateDynamicData> list ,
			String packageName )
	{
		for( OperateDynamicData data : list )
		{
			if( data.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
			{
				packageName = packageName.replaceAll( "dummy" , "//" );
			}
			if( packageName.equals( data.mPkgnameOrAddr ) && data.dynamicType != OperateDynamicUtils.FOLDER )
			{
				return data.mDeskHot;
			}
		}
		return false;
	}
	
	public boolean getIsShowAppMainMenuHot(
			List<OperateDynamicData> list ,
			String packageName )
	{
		for( OperateDynamicData data : list )
		{
			if( packageName.equals( data.mPkgnameOrAddr ) && data.dynamicType != OperateDynamicUtils.FOLDER )
			{
				return data.mIsShowHot;
			}
		}
		return false;
	}
	
	//	public boolean getIsShowDesktopAppHot(
	//			List<OperateDynamicData> list ,
	//			String packageName )
	//	{
	//		for( OperateDynamicData data : list )
	//		{
	//			if( packageName.equals( data.mPkgnameOrAddr ) && data.dynamicType != OperateDynamicUtils.FOLDER )
	//			{
	//				return data.mDeskHot;
	//			}
	//		}
	//		return false;
	//	}
	public boolean getIsShowMainMenuHot(
			List<OperateDynamicData> list ,
			String dynamicID )
	{
		for( OperateDynamicData data : list )
		{
			if( dynamicID.equals( data.dynamicID ) && data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				return data.mIsShowHot;
			}
		}
		return false;
	}
	
	public void hideOperateFolderHot(
			List<OperateDynamicData> list ,
			String dynamicID ,
			boolean isDesk )
	{
		for( OperateDynamicData data : list )
		{
			if( dynamicID.equals( data.dynamicID ) && data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				if( isDesk && data.mDeskHot )
				{
					data.mDeskHot = false;
					mDynamicProxy.saveDynamicContent( list , false );
					return;
				}
				if( !isDesk && data.mIsShowHot )
				{
					data.mIsShowHot = false;
					mDynamicProxy.saveDynamicContent( list , false );
					return;
				}
			}
		}
	}
	
	public String getItemLocalTitle(
			List<OperateDynamicData> list ,
			String pkgName ,
			String dynamicID )
	{
		for( OperateDynamicData data : list )
		{
			if( dynamicID.equals( data.dynamicID ) )
			{
				for( OperateDynamicItem item : data.mDynamicItems )
				{
					if( item.mPackageName.equals( pkgName ) )
					{
						return item.getDynamicItemTitle();
					}
				}
			}
		}
		return null;
	}
	
	//wifi1118 start
	//针对WIFI静默下载获取名字。只值包名过来。就去自己去获取，
	//以确保拿到名字。
	public String getDynamicIconTitle(
			List<OperateDynamicData> list ,
			String pkgName )
	{
		for( OperateDynamicData data : list )
		{
			if( data.dynamicType == OperateDynamicUtils.VIRTUAL_APP )
			{
				if( pkgName.equals( data.mPkgnameOrAddr ) )
				{
					String title = data.getDynamicEntryTitle( true );
					if( title == null )
					{
						title = data.getDynamicEntryTitle( false );
					}
					return title;
				}
			}
			else if( data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				for( OperateDynamicItem item : data.mDynamicItems )
				{
					if( pkgName.equals( item.mPackageName ) )
					{
						return item.getDynamicItemTitle();
					}
				}
			}
		}
		return null;
	}
	
	//wifi1118 end
	public String getLocalTitle(
			List<OperateDynamicData> list ,
			String dynamicID ,
			boolean isDesk )
	{
		for( OperateDynamicData data : list )
		{
			if( dynamicID.equals( data.dynamicID ) )
			{
				return data.getDynamicEntryTitle( isDesk );
			}
		}
		return null;
	}
	
	public String getDownloadTip(
			List<OperateDynamicData> list ,
			String pkgName )
	{
		for( OperateDynamicData data : list )
		{
			if( data.dynamicType == OperateDynamicUtils.VIRTUAL_APP )
			{
				if( pkgName.equals( data.mPkgnameOrAddr ) )
				{
					return data.downloadTip;
				}
			}
			else if( data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				for( OperateDynamicItem item : data.mDynamicItems )
				{
					if( pkgName.equals( item.mPackageName ) )
					{
						return item.mDownloadTip;
					}
				}
			}
		}
		return null;
	}
	
	public void changeFolderName(
			List<OperateDynamicData> list ,
			String dynamicID ,
			String newName ,
			boolean isDesk )
	{
		for( OperateDynamicData data : list )
		{
			if( dynamicID.equals( data.dynamicID ) )
			{
				data.setDynamicEntryTitle( newName , isDesk );
				mDynamicProxy.saveDynamicContent( list , false );
				return;
			}
		}
	}
	
	public void noitfyUnInstallApp(
			ArrayList<UnInstallItem> unInstallItems ,
			String dynamicID ,
			String packageName ,
			int from ,
			String folderID ,
			boolean isInstalled )
	{
		boolean bFind = false;
		for( int i = 0 ; i < unInstallItems.size() ; i++ )
		{
			String pkgName = unInstallItems.get( i ).mPackageName;
			if( pkgName.equals( packageName ) )
			{
				if( dynamicID.equals( unInstallItems.get( i ).dynamicID ) )
				{
					bFind = true;
					break;
				}
			}
		}
		if( !bFind )
		{
			mDynamicProxy.addUnInstallItem( dynamicID , packageName , from , folderID , isInstalled );
		}
	}
}
