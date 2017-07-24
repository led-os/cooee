package com.cooee.phenix.AppList.Nougat.favorites;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.util.ComponentKey;



/**
 *  常用应用综合类
 *
 */
public class FavoritesAppData
{
	
	
	private ArrayList<FavoritesComponentKey> datas = new ArrayList<FavoritesComponentKey>();//常用应用列表
	private AlphabeticalAppsList mApps;//所有应用列表
	private boolean isUpdate = false;//更新
	private boolean isNewAdd = false;//新加
	public static int DEFAULT_FAVORITE_NUM = 4;
	
	/**
	 * 通过ComponentName从mApps列表中查找对应的应用信息
	 * @param name:ComponentName
	 * @return
	 */
	public AppInfo getApplicationInfoFromAll(
			ComponentName name )
	{
		if( mApps == null )
		{
			return null;
		}
		AppInfo temp = null;
		for( AppInfo info : mApps.getApps() )
		{
			String packageName = info.getComponentName().getPackageName();
			String className = info.getComponentName().getClassName();
			if( packageName.equals( name.getPackageName() ) )//zhujieping modify,防止一个apk有两个launcher的activity，如果只有一个，匹配包名既可，如果是另一个launcher属性的activity，则包类名完全匹配
			{
				temp = info;
				if( className.equals( name.getClassName() ) )
				{
					return info;
				}
			}
		}
		return temp;
	}
	
	public ArrayList<AppInfo> getApplicationInfoFromAllForPackageName(
			String packageName )
	{
		ArrayList<AppInfo> info = new ArrayList<AppInfo>();
		if( mApps == null )
		{
			return info;
		}
		for( AppInfo app : mApps.getApps() )
		{
			String pn = app.getComponentName().getPackageName();
			if( pn.equals( packageName ) )
			{
				info.add( app );
			}
		}
		return info;
	}
	
	public ArrayList<FavoritesComponentKey> getApplicationInfoFromDatasForPackageName(
			String packageName )
	{
		ArrayList<FavoritesComponentKey> info = new ArrayList<FavoritesComponentKey>();
		for( FavoritesComponentKey app : datas )
		{
			String pn = app.componentName.getPackageName();
			if( pn.equals( packageName ) )
			{
				info.add( app );
			}
		}
		return info;
	}
	
	
	/**
	 *更新启动次数
	 * @param name
	 */
	public void updateTimes(
			ComponentName name )
	{
		AppInfo info = getApplicationInfoFromAll( name );
		if( info == null )
		{
			return;
		}
		FavoritesComponentKey app = getApplicationInfoFromData( info.getComponentName() );
		if( app == null )
		{
			app = new FavoritesComponentKey( info.getComponentName() );
			if( app != null )
			{
				app.launchTimes++;
				if( datas.size() < FavoritesAppData.DEFAULT_FAVORITE_NUM )
				{
					setNewAdd( true );
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
					setNewAdd( true );
				}
			}
			else if( isNew() )
			{
				setNewAdd( true );
			}
			setUpdate( true );
		}

	}
	
	/**
	 * (datas)添加一个Application Info
	 * @param app
	 */
	public void add(
			FavoritesComponentKey app )
	{
		if( getApplicationInfoFromData( app.componentName ) == null )
		{
			if( !datas.contains( app ) )
				datas.add( app );
		}
	}
	
	
	public void clear()
	{
		datas.clear();
		setUpdate( false );
		setNewAdd( false );
	}
	
	/**
	 * 一天一天的衰减
	 */
	public void dayDecrease()
	{
		FavoritesComponentKey app = null;
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
	 * 存
	 * @param context
	 */
	public static void saveFavoritesToDatabase(
			Context context ,
			ArrayList<FavoritesComponentKey> datas )
	{
		Set<String> appslist = new HashSet<String>();
		for( FavoritesComponentKey app : datas )
		{
			if( app.launchTimes > 0 )
			{
				appslist.add( app.toSaveString() );
			}
		}
		SharedPreferences sp = context.getSharedPreferences( "favorites_apps" , Context.MODE_PRIVATE );
		sp.edit().putStringSet( "favorites" , appslist ).commit();
	}
	
	public static ArrayList<FavoritesComponentKey> getFavoritesDatas(
			Context context )
	{
		ArrayList<FavoritesComponentKey> data = new ArrayList<FavoritesComponentKey>();
		SharedPreferences sp = context.getSharedPreferences( "favorites_apps" , Context.MODE_PRIVATE );
		Set<String> appslist = sp.getStringSet( "favorites" , null );
		if( appslist != null )
		{
			for( String item : appslist )
			{
				String[] temp = item.split( ":" );
				if( temp.length == 2 )
				{
					FavoritesComponentKey comp = new FavoritesComponentKey( temp[0] );
					comp.launchTimes = Integer.parseInt( temp[1] );
					data.add( comp );
				}
			}
		}
		sort( data );
		return data;
	}
	
	/**
	 * 排序
	 */
	public static void sort(
			ArrayList<FavoritesComponentKey> appInfos )
	{
		List<FavoritesComponentKey> temp = new ArrayList<FavoritesComponentKey>( appInfos );
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
	private FavoritesComponentKey getApplicationInfoFromData(
			ComponentName comp )
	{
		for( FavoritesComponentKey app : datas )
		{
			ComponentName cp = app.componentName;
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
	private boolean isNew()
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
	
	public boolean isUpdate()
	{
		return isUpdate;
	}

	public void setUpdate(
			boolean isUpdate )
	{
		this.isUpdate = isUpdate;
	}

	public boolean isNewAdd()
	{
		return isNewAdd;
	}

	public void setNewAdd(
			boolean isNewAdd )
	{
		this.isNewAdd = isNewAdd;
	}

	/**
	 * 排序比较器(降序)
	 *
	 */
	public static class DatasComparator implements Comparator<FavoritesComponentKey>
	{
		
		@Override
		public int compare(
				FavoritesComponentKey lhs ,
				FavoritesComponentKey rhs )
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
	
	public void updateFavoritesApps(
			Context context )
	{
		// TODO Auto-generated method stub
		sort( datas );
		if( mApps != null )
		{
			ArrayList<ComponentKey> list = new ArrayList<ComponentKey>();
			for( FavoritesComponentKey key : datas )
			{
				AppInfo info = getApplicationInfoFromAll( key.componentName );
				if( info != null )
				{
					list.add( info.toComponentKey() );
				}
			}
			if( list.size() > 0 )
				mApps.setFavoritesApps( list );
		}
		saveFavoritesToDatabase( context , datas );
	}
	
	public void setApps(
			AlphabeticalAppsList apps )
	{
		mApps = apps;
	}
	
	public void addAppsData(
			List<FavoritesComponentKey> list )
	{
		datas.clear();
		datas.addAll( list );
	}
	
	
	public static void setDefaultFavoritesNum(
			int num )
	{
		DEFAULT_FAVORITE_NUM = num;
	}
}
