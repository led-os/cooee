package com.coco.shortcut.shortcutbox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.database.model.DownloadThemeItem;
import com.coco.theme.themebox.database.model.ThemeInfoItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;


public class OperateService
{
	
	private Context mContext;
	
	public OperateService(
			Context context )
	{
		mContext = context;
	}
	
	private List<ResolveInfo> getResloveInfoByPackagename(
			String pkg )
	{
		PackageManager pm = mContext.getPackageManager();
		Intent localIntent = new Intent( Intent.ACTION_MAIN );
		localIntent.setPackage( pkg );
		List<ResolveInfo> list = pm.queryIntentActivities( localIntent , 0 );
		return list;
	}
	
	public List<OperateInformation> queryInstallList()
	{
		List<OperateInformation> hotList = queryHotList();
		List<OperateInformation> result = new ArrayList<OperateInformation>();
		for( OperateInformation info : hotList )
		{
			if( info.isInstalled( mContext ) )
			{
				result.add( info );
			}
		}
		return result;
	}
	
	public List<OperateInformation> queryDownloadList()
	{
		DownloadThemeService dSv = new DownloadThemeService( mContext );
		List<DownloadThemeItem> downlist = dSv.queryTable( DownloadList.Operate_Type );
		List<OperateInformation> result = new ArrayList<OperateInformation>();
		for( DownloadThemeItem item : downlist )
		{
			OperateInformation infor = new OperateInformation();
			infor.setDownloadItem( item );
			result.add( infor );
		}
		return result;
	}
	
	public List<OperateInformation> queryHotList()
	{
		HotOperateService hotSv = new HotOperateService( mContext );
		List<ThemeInfoItem> hotlist = hotSv.queryTable();
		List<OperateInformation> result = new ArrayList<OperateInformation>();
		for( ThemeInfoItem item : hotlist )
		{
			OperateInformation infor = new OperateInformation();
			infor.setThemeItem( item );
			result.add( infor );
		}
		return result;
	}
	
	public int queryShowList(
			List<OperateInformation> resultList )
	{
		Map<String , OperateInformation> allMap = new HashMap<String , OperateInformation>();
		List<OperateInformation> hotList = queryHotList();
		for( OperateInformation item : hotList )
		{
			allMap.put( item.getPackageName() , item );
		}
		List<OperateInformation> downList = queryDownloadList();
		for( OperateInformation item : downList )
		{
			allMap.put( item.getPackageName() , item );
		}
		List<OperateInformation> installList = new ArrayList<OperateInformation>();
		for( OperateInformation info : hotList )
		{
			if( info.isInstalled( mContext ) )
			{
				installList.add( info );
			}
		}
		for( OperateInformation item : installList )
		{
			allMap.put( item.getPackageName() , item );
		}
		for( OperateInformation item : hotList )
		{
			if( !allMap.get( item.getPackageName() ).isInstalled( mContext ) )
				resultList.add( allMap.get( item.getPackageName() ) );
		}
		return hotList.size();
	}
	
	public OperateInformation queryOperate(
			String packageName ,
			String className )
	{
		if( className != null && !className.equals( "" ) )
		{
			List<ResolveInfo> installList = getResloveInfoByPackagename( packageName );
			for( ResolveInfo item : installList )
			{
				if( item.activityInfo.name.equals( className ) )
				{
					return activityToOperate( item.activityInfo );
				}
			}
		}
		DownloadThemeService dSv = new DownloadThemeService( mContext );
		DownloadThemeItem downItem = dSv.queryByPackageName( packageName , DownloadList.Operate_Type );
		if( downItem != null )
		{
			OperateInformation infor = new OperateInformation();
			infor.setDownloadItem( downItem );
			return infor;
		}
		HotOperateService hotSv = new HotOperateService( mContext );
		ThemeInfoItem hotItem = hotSv.queryByPackageName( packageName );
		if( hotItem != null )
		{
			OperateInformation infor = new OperateInformation();
			infor.setThemeItem( hotItem );
			return infor;
		}
		return null;
	}
	
	public ComponentName queryComponent(
			String packageName )
	{
		List<ResolveInfo> installList = getResloveInfoByPackagename( packageName );
		if( installList.size() > 0 )
		{
			ActivityInfo item = installList.get( 0 ).activityInfo;
			return new ComponentName( item.packageName , item.name );
		}
		return null;
	}
	
	private OperateInformation activityToOperate(
			ActivityInfo activityInfo )
	{
		OperateInformation item = new OperateInformation();
		item.setActivity( mContext , activityInfo );
		return item;
	}
}
