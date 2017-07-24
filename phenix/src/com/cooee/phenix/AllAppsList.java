package com.cooee.phenix;


import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.theme.ThemeManager;
import com.cooee.util.Tools;


/**
 * Stores the list of all applications for the all apps view.
 */
//添加智能分类功能 , change by shlt@2015/02/09 UPD START
//class AllAppsList
public class AllAppsList
//添加智能分类功能 , change by shlt@2015/02/09 UPD END
{
	
	public static final int DEFAULT_APPLICATIONS_NUMBER = 42;
	/** The list off all apps. */
	public ArrayList<AppInfo> data = new ArrayList<AppInfo>( DEFAULT_APPLICATIONS_NUMBER );
	/** The list of apps that have been added since the last notify() call. */
	public ArrayList<AppInfo> added = new ArrayList<AppInfo>( DEFAULT_APPLICATIONS_NUMBER );
	/** The list of apps that have been removed since the last notify() call. */
	public ArrayList<AppInfo> removed = new ArrayList<AppInfo>();
	/** The list of apps that have been modified since the last notify() call. */
	public ArrayList<AppInfo> modified = new ArrayList<AppInfo>();
	private IconCache mIconCache;
	/**不可用的应用(挂载T卡应用)列表,在T卡拔出时赋值,把不可用的应用图标变灰,T卡插入时,比较加入的应用和不可用列表,相同的取消灰色,多余的添加到桌面,仍有灰色则删除图标*/
	public ArrayList<AppInfo> unavailable = new ArrayList<AppInfo>();//cheyingkun add	//TCardMount
	public ArrayList<AppInfo> initSDCardAllApps = new ArrayList<AppInfo>();//cheyingkun add	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)//初始化sd卡的应用列表
	
	/**
	 * Boring constructor.
	 */
	public AllAppsList(
			IconCache iconCache )
	{
		mIconCache = iconCache;
	}
	
	/**
	 * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
	 * list to broadcast when notify() is called.
	 *
	 * If the app is already in the list, doesn't add it.
	 */
	public void add(
			final AppInfo info )
	{
		if( findActivity( data , info.getComponentName() ) )
		{
			return;
		}
		data.add( info );
		added.add( info );
	}
	
	public void clear()
	{
		data.clear();
		// TODO: do we clear these too?
		added.clear();
		removed.clear();
		modified.clear();
	}
	
	public int size()
	{
		return data.size();
	}
	
	public AppInfo get(
			int index )
	{
		return data.get( index );
	}
	
	/**
	 * Add the icons for the supplied apk called packageName.
	 */
	public void addPackage(
			Context context ,
			String packageName )
	{
		final List<ResolveInfo> matches = findActivitiesForPackage( context , packageName );
		if( matches.size() > 0 )
		{
			for( ResolveInfo info : matches )
			{
				add( new AppInfo( context.getPackageManager() , info , mIconCache , null ) );
			}
		}
	}
	
	/**
	 * Remove the apps for the given apk identified by packageName.
	 */
	public void removePackage(
			Context context ,
			String packageName )
	{
		final List<AppInfo> data = this.data;
		for( int i = data.size() - 1 ; i >= 0 ; i-- )
		{
			AppInfo info = data.get( i );
			final ComponentName component = info.getIntent().getComponent();
			if( packageName.equals( component.getPackageName() ) )
			{
				removed.add( info );
				//zhujieping add start
				if(
				//
				LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
				//
				|| ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE == LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
				//
				)
				{
					info.removeHide( context );
					info.removeUseFrequency( context );
				}
				//zhujieping add end
				data.remove( i );
			}
		}
		// This is more aggressive than it needs to be.
		mIconCache.flush();
	}
	
	// zhangjin@2015/12/14 ADD START
	public void remove(
			AppInfo src )
	{
		final List<AppInfo> apps = this.data;
		ComponentName component = src.getComponentName();
		for( int i = apps.size() - 1 ; i >= 0 ; i-- )
		{
			final AppInfo info = apps.get( i );
			if( info.getComponentName().equals( component ) )
			{
				removed.add( info );
				data.remove( i );
				break;
			}
		}
		mIconCache.flush();
	}
	
	// zhangjin@2015/12/14 ADD END
	/**
	 * Add and remove icons for this package which has been updated.
	 */
	public void updatePackage(
			Context context ,
			String packageName )
	{
		final List<ResolveInfo> matches = findActivitiesForPackage( context , packageName );
		if( matches.size() > 0 )
		{
			// Find disabled/removed activities and remove them from data and add them
			// to the removed list.
			for( int i = data.size() - 1 ; i >= 0 ; i-- )
			{
				final AppInfo applicationInfo = data.get( i );
				final ComponentName component = applicationInfo.getIntent().getComponent();
				if( packageName.equals( component.getPackageName() ) )
				{
					if( !findActivity( matches , component ) )
					{
						removed.add( applicationInfo );
						mIconCache.remove( component );
						data.remove( i );
					}
				}
			}
			// Find enabled activities and add them to the adapter
			// Also updates existing activities with new labels/icons
			int count = matches.size();
			for( int i = 0 ; i < count ; i++ )
			{
				final ResolveInfo info = matches.get( i );
				AppInfo applicationInfo = findApplicationInfoLocked( info.activityInfo.applicationInfo.packageName , info.activityInfo.name );
				if( applicationInfo == null )
				{
					add( new AppInfo( context.getPackageManager() , info , mIconCache , null ) );
				}
				else
				{
					mIconCache.remove( applicationInfo.getComponentName() );
					mIconCache.getTitleAndIcon( applicationInfo , info , null );
					modified.add( applicationInfo );
				}
			}
		}
		else
		{
			//cheyingkun add start	//1、解决“默认配置美化中心图标，收到美化中心更新广播后，图标消失的问题”的问题。【c_0003553】2、解决“设置界面隐藏输入法图标后，桌面输入法图标没有删除”的问题。【c_0004501】
			//【问题原因】1、默认配置的美化中心，没有main、launcher属性，导致matches.size==0，在收到美化中心update消息后，图标就会被下面的逻辑删除（该次修改注释掉了下面的删除逻辑）
			//2、当调用类似于BaseAppState中setComponentDisabled方法的的代码时，会设置某个类不可用，此时需要删除图标。收到的广播是update，matches==0，【c_0003553】修改注释掉了下面的删除逻辑，导致桌面图标没有删除。
			//【解决方案】对美化中心进行特殊处理，其他应用matches==0删除。
			//判断是否是美化中心,如果不是则走删除逻辑
			if( !ThemeManager.BEAUTY_CENTER_PACKAGE_NAME.equals( packageName ) )
			//cheyingkun add end
			{
				// Remove all data for this package.
				for( int i = data.size() - 1 ; i >= 0 ; i-- )
				{
					final AppInfo applicationInfo = data.get( i );
					final ComponentName component = applicationInfo.getIntent().getComponent();
					if( packageName.equals( component.getPackageName() ) )
					{
						removed.add( applicationInfo );
						mIconCache.remove( component );
						data.remove( i );
					}
				}
			}
		}
	}
	
	public static List<ResolveInfo> findActivitiesForPackage(
			Context context ,
			String packageName )
	{
		return findActivitiesForPackage( context , packageName , false );
	}
	
	/**
	 * 查找新安装的主题apk，不用findActivitiesForPackage方法，因为该方法会屏蔽主题apk wanghongjian add
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static List<ResolveInfo> findAllActivitiesForPackage(
			Context context ,
			String packageName )
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		mainIntent.setPackage( packageName );
		final List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		List<ResolveInfo> ret = apps != null ? apps : new ArrayList<ResolveInfo>();
		return ret;
	}
	
	/**
	 * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
	 */
	public static List<ResolveInfo> findActivitiesForPackage(
			Context context ,
			String packageName ,
			boolean isIngoreHide )//isIngoreHide表示是否忽略配置的隐藏列表，true表示忽略，即获得实际的安装acitvity，false则隐藏的不获取
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		mainIntent.setPackage( packageName );
		//xiatian start	//桌面支持配置隐藏特定的activity界面。
		//xiatian del start
		//		final List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		//		List<ResolveInfo> ret = apps != null ? apps : new ArrayList<ResolveInfo>();
		//xiatian del end
		//xiatian add start
		List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		List<ResolveInfo> ret = new ArrayList<ResolveInfo>();
		for( ResolveInfo app : apps )
		{
			if( isIngoreHide || LauncherAppState.hideAppList( context , app.activityInfo.applicationInfo.packageName , app.activityInfo.name ) == false )
			{
				ret.add( app );
			}
		}
		//xiatian add end
		//xiatian end
		return ret;
	}
	
	/**
	 * Returns whether <em>apps</em> contains <em>component</em>.
	 */
	private static boolean findActivity(
			List<ResolveInfo> apps ,
			ComponentName component )
	{
		final String className = component.getClassName();
		for( ResolveInfo info : apps )
		{
			final ActivityInfo activityInfo = info.activityInfo;
			if( activityInfo.name.equals( className ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns whether <em>apps</em> contains <em>component</em>.
	 */
	private static boolean findActivity(
			ArrayList<AppInfo> apps ,
			ComponentName component )
	{
		final int N = apps.size();
		for( int i = 0 ; i < N ; i++ )
		{
			final AppInfo info = apps.get( i );
			if( info.getComponentName().equals( component ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Find an ApplicationInfo object for the given packageName and className.
	 */
	private AppInfo findApplicationInfoLocked(
			String packageName ,
			String className )
	{
		for( AppInfo info : data )
		{
			final ComponentName component = info.getIntent().getComponent();
			if( packageName.equals( component.getPackageName() ) && className.equals( component.getClassName() ) )
			{
				return info;
			}
		}
		return null;
	}
	
	//cheyingkun add start	//TCardMount
	/**
	 * unavailable the apps for the given apk identified by packageName.
	 * @param packageName
	 */
	public void unavailablePackage(
			String packageName )
	{
		final List<AppInfo> data = this.data;
		for( int i = data.size() - 1 ; i >= 0 ; i-- )
		{
			AppInfo info = data.get( i );
			final ComponentName component = info.getIntent().getComponent();
			if( packageName.equals( component.getPackageName() ) )
			{
				if( !unavailable.contains( info ) )
				{
					unavailable.add( info );
					//cheyingkun add start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
					info.setAvailable( false );
					if( info.getIconUnavailable() == null )
					{
						info.setIconUnavailable( Tools.getGrayBitmap( info.getIconBitmap() ) );
					}
					//cheyingkun add end
				}
			}
		}
		// This is more aggressive than it needs to be.
		mIconCache.flush();
	}
	
	//cheyingkun add end
	//cheyingkun add start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
	/**
	 * 根据传入的列表初始化sd卡的应用信息
	 * @param apps 
	 */
	public void initSDCardAllApps(
			ArrayList<AppInfo> apps ,
			PackageManager packageManager )
	{
		if( packageManager == null )
		{
			return;
		}
		if( apps == null )
		{
			apps = data;
		}
		if( apps != null && apps.size() > 0 )
		{
			for( AppInfo appInfo : apps )
			{
				if( appInfo.getComponentName() != null && LauncherAppState.isAppInstalledSdcard( appInfo.getComponentName().getPackageName() , packageManager ) )//如果安装在sd卡
				{
					if( !findActivity( initSDCardAllApps , appInfo.getComponentName() ) )//并且不在列表中
					{
						initSDCardAllApps.add( appInfo );//则添加到sd卡app列表中
					}
				}
			}
		}
	}
	//cheyingkun add end
}
