package com.cooee.favorites.apps;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import com.cooee.favorites.data.AppInfo;
import com.cooee.favorites.manager.FavoritesManager;


/**
 *  常用应用综合类
 *
 */
public class FavoritesAppData
{
	
	public static CopyOnWriteArrayList<AppInfo> datas = new CopyOnWriteArrayList<AppInfo>();//常用应用列表
	public static CopyOnWriteArrayList<AppInfo> mApps = new CopyOnWriteArrayList<AppInfo>();//所有应用列表
	public static boolean isUpdate = false;//更新
	public static boolean isNewAdd = false;//新加
	public static final int DEFAULT_FAVORITE_NUM = 4;
	private static HashMap<ComponentName , Bitmap> mHashMap = null;
	
	/**
	 * 通过ComponentName从mApps列表中查找对应的应用信息
	 * @param name:ComponentName
	 * @return
	 */
	public static AppInfo getApplicationInfoFromAll(
			ComponentName name )
	{
		for( AppInfo app : mApps )
		{
			String pn = app.getComponentName().getPackageName();
			String cn = app.getComponentName().getClassName();
			// jubingcheng@2016/05/05 UPD START 修改包名相同类名不同时也会返回app的问题
			//if( pn.equals( name.getPackageName() ) )
			//{
			//	info = app;
			//	if( app.getComponentName().getClassName().equals( name.getClassName() ) )//zhujieping，这种是防止一个apk有多个launcher属性的activity，这样在桌面有多个图标，更多应用显示的不准确
			//	{
			//		return app;
			//	}
			//}
			if( pn.equals( name.getPackageName() ) && cn.equals( name.getClassName() ) )
			{
				return app;
			}
			// jubingcheng@2016/05/05 UPD END
		}
		return null;
	}
	
	public static ArrayList<AppInfo> getApplicationInfoFromAllForPackageName(
			String packageName )
	{
		ArrayList<AppInfo> info = new ArrayList<AppInfo>();
		for( AppInfo app : mApps )
		{
			String pn = app.getComponentName().getPackageName();
			if( pn.equals( packageName ) )
			{
				info.add( app );
			}
		}
		return info;
	}
	
	public static ArrayList<AppInfo> getApplicationInfoFromDatasForPackageName(
			String packageName )
	{
		ArrayList<AppInfo> info = new ArrayList<AppInfo>();
		for( AppInfo app : datas )
		{
			String pn = app.getComponentName().getPackageName();
			if( pn.equals( packageName ) )
			{
				info.add( app );
			}
		}
		return info;
	}
	
	/**
	 * mApps过滤掉一些特殊应用
	 * @param context
	 */
	public static void filterApps(
			Context context )
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
		mainIntent.addCategory( Intent.CATEGORY_HOME );
		List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );
		if( apps == null || apps.isEmpty() )
		{
			return;
		}
		for( int i = 0 ; i < apps.size() ; i++ )
		{
			// filter home app
			ResolveInfo app = apps.get( i );
			if( app.activityInfo != null )
			{
				String pkgName = app.activityInfo.applicationInfo.packageName;
				String className = app.activityInfo.name;
				AppInfo info = getApplicationInfoFromAll( new ComponentName( pkgName , className ) );
				if( info != null )
				{
					mApps.remove( info );
				}
			}
		}
	}
	
	/**
	 *更新启动次数
	 * @param name
	 */
	public static void updateTimes(
			ComponentName name )
	{
		AppInfo app = getApplicationInfoFromData( name );
		if( app == null )
		{
			app = getApplicationInfoFromAll( name );
			if( app != null )
			{
				app.launchTimes++;
				if( datas.size() < FavoritesAppData.DEFAULT_FAVORITE_NUM )
				{
					isNewAdd = true;
				}
				if( !datas.contains( app ) )
					datas.add( app );
			}
		}
		else
		{
			app.launchTimes++;
			int i = datas.indexOf( app );
			if( i < FavoritesAppData.DEFAULT_FAVORITE_NUM && i > 0 )
			{
				if( app.launchTimes > datas.get( i - 1 ).launchTimes )
				{
					isNewAdd = true;
				}
			}
			else if( isNew() )
			{
				isNewAdd = true;
			}
			isUpdate = true;
		}
	}
	
	/**
	 * (datas)添加一个Application Info
	 * @param app
	 */
	public static void add(
			AppInfo app )
	{
		if( getApplicationInfoFromData( app.getComponentName() ) == null )
		{
			if( !datas.contains( app ) )
				datas.add( app );
		}
	}
	
	/**
	 * 删除packageName
	 * @param apps
	 */
	public static void removeApps(
			String packageName )
	{
		ArrayList<AppInfo> appInfo = getApplicationInfoFromAllForPackageName( packageName );
		for( AppInfo app : appInfo )
		{
			if( app != null )
			{
				mApps.remove( app );
			}
		}
		appInfo = getApplicationInfoFromDatasForPackageName( packageName );
		for( AppInfo app : appInfo )
		{
			if( app != null )
			{
				datas.remove( app );
				if( FavoritesAppManager.getInstance().getFavoritesGetDataCallBack() != null )
				{
					Bitmap bitmap = app.getIconBitmap();
					if( bitmap != null && !bitmap.isRecycled() )
					{
						bitmap.recycle();
						bitmap = null;
					}
				}
			}
		}
		FavoritesAppManager.getInstance().updateFavoritesApps();
	}
	
	/**
	 * 删除packageName
	 * @param apps
	 */
	public static void removeApps(
			List<String> packageName )
	{
		for( int i = 0 ; i < packageName.size() ; i++ )
		{
			ArrayList<AppInfo> appInfo = getApplicationInfoFromAllForPackageName( packageName.get( i ) );
			for( AppInfo app : appInfo )
			{
				if( app != null )
				{
					mApps.remove( app );
				}
			}
			appInfo = getApplicationInfoFromDatasForPackageName( packageName.get( i ) );
			for( AppInfo app : appInfo )
			{
				if( app != null )
				{
					datas.remove( app );
					if( FavoritesAppManager.getInstance().getFavoritesGetDataCallBack() != null )
					{
						Bitmap bitmap = app.getIconBitmap();
						if( bitmap != null && !bitmap.isRecycled() )
						{
							bitmap.recycle();
							bitmap = null;
						}
					}
				}
			}
		}
		FavoritesAppManager.getInstance().updateFavoritesApps();
	}
	
	private static ArrayList<AppInfo> plintPkgAndCls(
			List<ResolveInfo> resolveInfos )
	{
		// RuntimeException e2 = new RuntimeException( "leon is here" );
		// e2.fillInStackTrace();
		// Log.i( "lvjiangbin" , "plintPkgAndCls" , e2 );
		PackageManager pm = FavoritesManager.getInstance().getContainerContext().getPackageManager();
		ArrayList<AppInfo> addedApps = new ArrayList<AppInfo>();
		for( int i = 0 ; i < resolveInfos.size() ; i++ )
		{
			String pkg = resolveInfos.get( i ).activityInfo.packageName;
			String cls = resolveInfos.get( i ).activityInfo.name;
			AppInfo tmpInfo = new AppInfo();
			ComponentName componentName = new ComponentName( pkg , cls );
			tmpInfo.setComponentName( componentName );
			Bitmap icon = null;
			if( mHashMap != null )
			{
				icon = mHashMap.get( componentName );
			}
			if( icon != null )
			{
				//				Log.v( "lvjiangbin" , "获取到正确的icon了 = " + componentName );
				tmpInfo.setIconBitmap( icon );
			}
			try
			{
				// jubingcheng@2016/05/05 UPD START 修改同包名有多个应用时title设置错误的BUG【0004224】
				//info = pm.getApplicationInfo( pkg , 0 );
				//tmpInfo.setTitle( info.loadLabel( pm ).toString() );
				String title = resolveInfos.get( i ).activityInfo.loadLabel( pm ).toString().replaceAll( " " , "" ).trim();// 注意！！两个空格不一样
				tmpInfo.setTitle( title );
				// jubingcheng@2016/05/05 UPD END
				if( icon == null )
				{
					ApplicationInfo info = pm.getApplicationInfo( pkg , 0 );
					//					Log.v( "lvjiangbin" , "获取还是原来的icon=" + componentName );
					if( !( info.loadIcon( pm ) instanceof LayerDrawable ) )
					{
						tmpInfo.setIconBitmap( ( (BitmapDrawable)info.loadIcon( pm ) ).getBitmap() );
					}
				}
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			addedApps.add( tmpInfo );
		}
		return addedApps;
	}
	
	private static List<ResolveInfo> getResolveInfos()
	{
		List<ResolveInfo> appList = null;
		Intent intent = new Intent( Intent.ACTION_MAIN , null );
		intent.addCategory( Intent.CATEGORY_LAUNCHER );
		PackageManager pm = FavoritesManager.getInstance().getContainerContext().getPackageManager();
		appList = pm.queryIntentActivities( intent , 0 );
		Collections.sort( appList , new ResolveInfo.DisplayNameComparator( pm ) );
		return appList;
	}
	
	/**
	 * (mApps)添加ApplicationInfo列表
	 * @param apps
	 */
	public static void addAppsToAll(
			HashMap<ComponentName , Bitmap> hashmap )
	{
		if( hashmap != null )
		{
			mHashMap = hashmap;
		}
		mApps.clear();
		ArrayList<AppInfo> apps = plintPkgAndCls( getResolveInfos() );
		for( AppInfo appInfo : apps )
		{
			AppInfo app = getApplicationInfoFromAll( appInfo.getComponentName() );
			if( app == null )
			{
				mApps.add( appInfo );
			}
		}
		filterApps( FavoritesManager.getInstance().getContainerContext() );// 要过滤下滴
	}
	
	public static void clear()
	{
		datas.clear();
		if( FavoritesAppManager.getInstance().getFavoritesGetDataCallBack() != null )
		{
			for( int i = 0 ; i < datas.size() ; i++ )
			{
				Bitmap bitmap = datas.get( i ).getIconBitmap();
				if( bitmap != null && !bitmap.isRecycled() )
				{
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
		isUpdate = false;
		isNewAdd = false;
	}
	
	/**
	 * 一天一天的衰减
	 */
	public static void dayDecrease()
	{
		AppInfo app = null;
		int num = datas.size();
		int max = FavoritesAppData.DEFAULT_FAVORITE_NUM + 1;
		if( num < FavoritesAppData.DEFAULT_FAVORITE_NUM )
		{
			max = num + 1;
		}
		for( int i = 0 ; i < num ; i++ )
		{
			app = datas.get( i );
			app.launchTimes -= 3;
			if( i < FavoritesAppData.DEFAULT_FAVORITE_NUM )
			{
				if( app.launchTimes < max - i )
				{
					app.launchTimes = max - i;
				}
			}
			else if( app.launchTimes < 1 )
			{
				app.launchTimes = 1;
			}
		}
	}
	
	/**
	 * 存入数据库
	 * @param context
	 */
	public static void saveFavoritesToDatabase(
			Context context )
	{
		ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
		for( AppInfo app : mApps )
		{
			if( app.launchTimes > 0 )
			{
				apps.add( app );
			}
		}
		FavoritesManager.getInstance().saveFavoritesToDatabase( context , apps );
	}
	
	/**
	 * 排序
	 */
	public static void sort(
			CopyOnWriteArrayList<AppInfo> appInfos )
	{
		List<AppInfo> temp = new ArrayList<AppInfo>( appInfos );
		Collections.sort( temp , new DatasComparator() );
		appInfos.clear();
		appInfos.addAll( temp );
		temp.clear();
		temp = null;
	}
	
	/**
	 * 通过包名获得对应的应用信息(datas)
	 * @param name packageName
	 * @return
	 */
	private static AppInfo getApplicationInfoFromData(
			ComponentName comp )
	{
		for( AppInfo app : datas )
		{
			ComponentName cp = app.getComponentName();
			if( cp.getPackageName().equals( comp.getPackageName() ) )
			{
				if( cp.getClassName().equals( comp.getClassName() ) )//这种情况是防止同一个包名，有多个launcher属性的activity
					return app;
			}
		}
		return null;
	}
	
	/**
	 * 是否是新增
	 * @return
	 */
	private static boolean isNew()
	{
		int num = datas.size();
		if( num <= FavoritesAppData.DEFAULT_FAVORITE_NUM )
		{
			return false;
		}
		for( int i = FavoritesAppData.DEFAULT_FAVORITE_NUM ; i < num ; i++ )
		{
			if( datas.get( i ).launchTimes > datas.get( FavoritesAppData.DEFAULT_FAVORITE_NUM - 1 ).launchTimes )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 排序比较器(降序)
	 *
	 */
	public static class DatasComparator implements Comparator<AppInfo>
	{
		
		@Override
		public int compare(
				AppInfo lhs ,
				AppInfo rhs )
		{
			if( lhs.launchTimes > rhs.launchTimes )
			{
				return -1;
			}
			else if( lhs.launchTimes < rhs.launchTimes )
			{
				return 1;
			}
			return 0;
		}
	}
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	public static void updateAppIcon(
			HashMap<ComponentName , Bitmap> hashmap )
	{
		if( hashmap == null )
		{
			Log.e( "" , "cyk FavoritesAppData updateAppIcon return " );
			return;
		}
		boolean hasChanged = false;
		for( AppInfo appInfo : datas )
		{
			Bitmap bitmap = hashmap.get( appInfo.getComponentName() );
			if( bitmap != null && !bitmap.isRecycled() )
			{
				appInfo.setIconBitmap( bitmap );
				hasChanged = true;
			}
		}
		for( AppInfo appInfo : mApps )
		{
			Bitmap bitmap = hashmap.get( appInfo.getComponentName() );
			if( bitmap != null && !bitmap.isRecycled() )
			{
				appInfo.setIconBitmap( bitmap );
				hasChanged = true;
			}
		}
		Log.d( "" , "cyk updateAppIcon: " + hasChanged );
		if( hasChanged )
		{
			FavoritesManager.getInstance().bindApp( datas );
		}
	}
	//cheyingkun add end
}
