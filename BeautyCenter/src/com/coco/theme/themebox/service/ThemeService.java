package com.coco.theme.themebox.service;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.ThemeInformation;
import com.coco.theme.themebox.database.model.DownloadThemeItem;
import com.coco.theme.themebox.database.model.ThemeInfoItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.database.service.HotService;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Tools;


public class ThemeService
{
	
	private Context mContext;
	
	public ThemeService(
			Context context )
	{
		mContext = context;
	}
	
	public List<ThemeInformation> queryInstallList()
	{
		//Log.i( "minghui" , "queryInstallList()。。。" );
		List<ThemeInformation> result = new ArrayList<ThemeInformation>();
		List<ActivityInfo> infoList = queryThemeActivityList();
		//Log.i( "minghui" , "infoList = " + infoList.toString() );
		for( ActivityInfo activityInfo : infoList )
		{
			result.add( activityToTheme( activityInfo ) );
		}
		return result;
	}
	
	public List<ThemeInformation> queryDownloadList()
	{
		DownloadThemeService dSv = new DownloadThemeService( mContext );
		List<DownloadThemeItem> downlist = dSv.queryTable( DownloadList.Theme_Type );
		List<ThemeInformation> result = new ArrayList<ThemeInformation>();
		for( DownloadThemeItem item : downlist )
		{
			ThemeInformation infor = new ThemeInformation();
			infor.setDownloadItem( item );
			result.add( infor );
		}
		return result;
	}
	
	public List<ThemeInformation> queryHotList()
	{
		HotService hotSv = new HotService( mContext );
		List<ThemeInfoItem> hotlist = hotSv.queryTable( DownloadList.Theme_Type );
		List<ThemeInformation> result = new ArrayList<ThemeInformation>();
		for( ThemeInfoItem item : hotlist )
		{
			ThemeInformation infor = new ThemeInformation();
			infor.setThemeItem( item );
			result.add( infor );
		}
		return result;
	}
	
	public List<ThemeInformation> queryShowList()
	{
		Map<String , ThemeInformation> allMap = new HashMap<String , ThemeInformation>();
		List<ThemeInformation> hotList = queryHotList();
		for( ThemeInformation item : hotList )
		{
			allMap.put( item.getPackageName() , item );
		}
		List<ThemeInformation> downList = queryDownloadList();
		for( ThemeInformation item : downList )
		{
			allMap.put( item.getPackageName() , item );
		}
		List<ThemeInformation> installList = queryInstallList();
		for( ThemeInformation item : installList )
		{
			allMap.put( item.getPackageName() , item );
		}
		List<ThemeInformation> resultList = new ArrayList<ThemeInformation>();
		for( ThemeInformation item : hotList )
		{
			resultList.add( allMap.get( item.getPackageName() ) );
		}
		return resultList;
	}
	
	public ComponentName queryCurrentTheme()
	{
		ThemesDB db = new ThemesDB( mContext );
		ThemeConfig cfg = db.getTheme();
		List<ActivityInfo> activityList = queryThemeActivityList();
		if( activityList.size() <= 0 )
		{
			return new ComponentName( "" , "" );
		}
		for( ActivityInfo info : activityList )
		{
			if( info.packageName.equals( cfg.theme ) )
			{
				return new ComponentName( info.packageName , info.name );
			}
		}
		return new ComponentName( activityList.get( activityList.size() - 1 ).packageName , activityList.get( activityList.size() - 1 ).name );
	}
	
	public boolean applyTheme(
			ComponentName apply )
	{
		ThemesDB db = new ThemesDB( mContext );
		db.SaveThemes( apply.getPackageName() );
		if( ThemesDB.ACTION_LAUNCHER_RESTART == null || ThemesDB.ACTION_LAUNCHER_RESTART.equals( "" ) )
		{
			ThemesDB.ACTION_LAUNCHER_RESTART = "com.coco.launcher.restart";
		}
		mContext.sendBroadcast( new Intent( ThemesDB.ACTION_LAUNCHER_RESTART ) );
		return true;
	}
	
	public void SaveThemes(
			String packageNames )
	{
		ThemesDB db = new ThemesDB( mContext );
		db.SaveThemes( packageNames );
	}
	
	public ThemeInformation queryTheme(
			String packageName ,
			String className )
	{
		if( className != null && !className.equals( "" ) )
		{
			List<ActivityInfo> installList = queryThemeListByPackageName( packageName );
			for( ActivityInfo item : installList )
			{
				if( item.name.equals( className ) )
				{
					return activityToTheme( item );
				}
			}
		}
		Log.v( "test" , "download" );
		DownloadThemeService dSv = new DownloadThemeService( mContext );
		DownloadThemeItem downItem = dSv.queryByPackageName( packageName , DownloadList.Theme_Type );
		if( downItem != null )
		{
			ThemeInformation infor = new ThemeInformation();
			infor.setDownloadItem( downItem );
			return infor;
		}
		Log.v( "test" , "hot" );
		HotService hotSv = new HotService( mContext );
		ThemeInfoItem hotItem = hotSv.queryByPackageName( packageName , DownloadList.Theme_Type );
		if( hotItem != null )
		{
			ThemeInformation infor = new ThemeInformation();
			infor.setThemeItem( hotItem );
			return infor;
		}
		return null;
	}
	
	public ComponentName queryComponent(
			String packageName )
	{
		List<ActivityInfo> installList = queryThemeListByPackageName( packageName );
		if( installList.size() > 0 )
		{
			ActivityInfo item = installList.get( 0 );
			return new ComponentName( item.packageName , item.name );
		}
		return null;
	}
	
	public List<ActivityInfo> queryThemeActivityList()
	{
		List<ActivityInfo> resultList = new ArrayList<ActivityInfo>();
		Intent intent = new Intent( "com.coco.themes" , null );
		List<ResolveInfo> themesinfo = mContext.getPackageManager().queryIntentActivities( intent , 0 );
		Log.i( "andy" , "themesinfo = " + themesinfo );
		// @2014/11/21 ADD START
		//添加朗易通开关
		if( FunctionConfig.isEnable_langyitong_theme_style() )
		{
			List<ResolveInfo> systemResInfo = new ArrayList<ResolveInfo>();
			List<ResolveInfo> downloadResInfo = new ArrayList<ResolveInfo>();
			//set Application Name  
			if( themesinfo != null )
			{
				for( ResolveInfo info : themesinfo )
				{
					if( ( info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) != 0 )
					{
						systemResInfo.add( info );
					}
					else
					{
						downloadResInfo.add( info );
					}
				}
				Collections.sort( systemResInfo , new SpellComparator() );
				//Collections.sort( systemInfo , new SpellComparator() );//按照应用名（中文、英文）拼音排序
				//添加内置是显示前面还是后面的开关（true前面，false后面）
				if( FunctionConfig.isLangyitong_theme_order_set() )
				{
					for( ResolveInfo info : systemResInfo )
					{
						resultList.add( info.activityInfo );
					}
					for( ResolveInfo info : downloadResInfo )
					{
						resultList.add( info.activityInfo );
					}
				}
				else
				{
					for( ResolveInfo info : downloadResInfo )
					{
						resultList.add( info.activityInfo );
					}
					for( ResolveInfo info : systemResInfo )
					{
						resultList.add( info.activityInfo );
					}
				}
			}
		}
		else
		{
			if( FunctionConfig.getBrzhSortThemeList() != null && FunctionConfig.getBrzhSortThemeList().size() > 0 )
			{
				ArrayList<String> brzhSortList = FunctionConfig.getBrzhSortThemeList();
				for( int i = 0 ; i < brzhSortList.size() ; i++ )
				{
					for( ResolveInfo info : themesinfo )
					{
						if( info.activityInfo.packageName.equals( brzhSortList.get( i ) ) )
						{
							resultList.add( info.activityInfo );
						}
					}
				}
			}
			Collections.sort( themesinfo , new ResolveInfo.DisplayNameComparator( mContext.getPackageManager() ) );
			for( ResolveInfo info : themesinfo )
			{
				if( !resultList.contains( info.activityInfo ) )
					resultList.add( info.activityInfo );
				Log.i( "minghui" , "resultList0 = " + resultList.toString() );
			}
		}
		// @2014/11/21 ADD END
		Intent systemmain = new Intent( "android.intent.action.MAIN" , null );
		systemmain.addCategory( "android.intent.category.LAUNCHER" );
		systemmain.addCategory( "android.intent.category.Theme" );
		systemmain.setPackage( ThemesDB.LAUNCHER_PACKAGENAME );
		themesinfo = mContext.getPackageManager().queryIntentActivities( systemmain , 0 );
		if( themesinfo != null )
		{
			for( ResolveInfo info : themesinfo )
			{
				// gaominghui@2016/06/20 ADD START
				if( FunctionConfig.isDefault_theme_show_front() )
				{
					resultList.add( 0 , info.activityInfo );
				}
				else
				{
					resultList.add( info.activityInfo );
				}
				// gaominghui@2016/06/20 ADD END
			}
		}
		return resultList;
	}
	
	private List<ActivityInfo> queryThemeListByPackageName(
			String pkgName )
	{
		List<ActivityInfo> resultList = new ArrayList<ActivityInfo>();
		Intent intent = new Intent( "com.coco.themes" , null );
		intent.setPackage( pkgName );
		List<ResolveInfo> themesinfo = mContext.getPackageManager().queryIntentActivities( intent , 0 );
		Collections.sort( themesinfo , new ResolveInfo.DisplayNameComparator( mContext.getPackageManager() ) );
		for( ResolveInfo info : themesinfo )
		{
			resultList.add( info.activityInfo );
		}
		if( pkgName == null || pkgName.equals( ThemesDB.LAUNCHER_PACKAGENAME ) )
		{
			Intent systemmain = new Intent( "android.intent.action.MAIN" , null );
			systemmain.addCategory( "android.intent.category.LAUNCHER" );
			systemmain.setPackage( ThemesDB.LAUNCHER_PACKAGENAME );
			themesinfo = mContext.getPackageManager().queryIntentActivities( systemmain , 0 );
			for( ResolveInfo info : themesinfo )
			{
				resultList.add( info.activityInfo );
			}
		}
		return resultList;
	}
	
	private ThemeInformation activityToTheme(
			ActivityInfo activityInfo )
	{
		ThemeInformation themeItem = new ThemeInformation();
		themeItem.setActivity( mContext , activityInfo );
		return themeItem;
	}
	
	// @2014/11/21 ADD START by gaominghui
	/**
	 * 汉字拼音排序比较器
	 */
	class SpellComparator implements Comparator<ResolveInfo>
	{
		
		private final Collator mCollator = Collator.getInstance();
		
		public SpellComparator()
		{
			mCollator.setStrength( Collator.PRIMARY );
		}
		
		public int compare(
				ResolveInfo o1 ,
				ResolveInfo o2 )
		{
			try
			{
				// 取得比较对象的名字，并将其转换成拼音
				ArrayList<String> s1 = Tools.getFullPinYin( o1.loadLabel( mContext.getPackageManager() ).toString() );
				ArrayList<String> s2 = Tools.getFullPinYin( o2.loadLabel( mContext.getPackageManager() ).toString() );
				// 运用String类的 compareTo（）方法对两对象进行比较
				Log.d( "SpellComparator" , "Theme1 = " + s1 );
				int count = ( ( s1.size() < s2.size() ) ? s1.size() : s2.size() );
				for( int i = 0 ; i < count ; i++ )
				{
					int result = mCollator.compare( s1.get( i ) , s2.get( i ) );
					if( result == 0 )
					{
						continue;
					}
					else
					{
						return result;
					}
				}
				return 0;
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			return 0;
		}
	}
}
// @2014/11/21 ADD END by gaominghui
