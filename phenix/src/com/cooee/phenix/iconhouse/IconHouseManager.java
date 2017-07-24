package com.cooee.phenix.iconhouse;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Log;

import com.cooee.favorites.host.FavoritesPageManager;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.DefaultIcon;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.iconhouse.provider.IconHouseProvider;
import com.cooee.phenix.iconhouse.provider.IconHouseProvider.UpdateListener;


public class IconHouseManager
//
implements IOnThemeChanged//zhujieping add  //需求：桌面动态图标支持随主题变化
{
	
	private static final String TAG = "IconHouseManager";
	protected static IconHouseManager instance;
	private AppConfigBean mIconHouseConfig;
	private Launcher mContext = null;
	//	private CalendarProvider mCalndarProvider = null;
	private UpdateListener mUpdateListener = null;
	private HashMap<ComponentName , IconHouseProvider> mProviders = new HashMap<ComponentName , IconHouseProvider>();
	
	public static IconHouseManager getInstance()
	{
		if( instance == null )
		{
			synchronized( IconHouseManager.class )
			{
				if( instance == null )
				{
					instance = new IconHouseManager();
				}
			}
		}
		return instance;
	}
	
	public void setUp(
			Launcher context )
	{
		mContext = context;
		mIconHouseConfig = new AppConfigBean( context , R.array.icon_house_title , -1 , R.array.icon_house_package , R.array.icon_house_class , R.array.icon_house_provider );
		mUpdateListener = new UpdateListener() {
			
			public boolean isCmpVisible(
					ComponentName cmp )
			{
				return mContext.isCmpVisible( cmp );
			}
			
			@Override
			public void onUpdate(
					ComponentName cmp )
			{
				// TODO Auto-generated method stub
				//清楚原有图标
				LauncherAppState app = LauncherAppState.getInstance();
				IconCache iconCache = app.getIconCache();
				iconCache.remove( cmp , true );
				//走更新流程
				mContext.updateIconHouse( cmp );
			}
		};
		//		mCalndarProvider = new CalendarProvider( mContext );
		//		mCalndarProvider.setUpdateListener( mUpdateListener );
	}
	
	public Bitmap getIconHouse(
			ResolveInfo info )
	{
		String appName = info.activityInfo.applicationInfo.packageName;
		String className = info.activityInfo.name;
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( "cyk_bug : c_0004400" , StringUtils.concat( "getIconHouse 0 : packageName:" , appName , " className:" , className ) );
		}
		//cheyingkun add end
		//cheyingkun add start	//解决“安装phenix几率性重启”的问题。（动态图标空指针，动态图标初始化太晚，添加非空判断）
		//注册广播在launcherApplication里,但是动态图标初始化用要传入launcher,只好加非空判断
		if( mIconHouseConfig == null )
		{
			return null;
		}
		//cheyingkun add end
		ArrayList<DefaultIcon> appConfigList = mIconHouseConfig.getAppList();
		for( int inter = 0 ; inter < appConfigList.size() ; inter++ )
		{
			List<String> mPackageNameList = appConfigList.get( inter ).pkgNameArray;//allPackageName.split( ";" );
			List<String> mClassNameList = appConfigList.get( inter ).classNameArray;//allClassName.split( ";" );			
			if( mPackageNameList == null || mPackageNameList.size() == 0 )
			{
				break;
			}
			if( mClassNameList == null || mClassNameList.size() == 0 )
			{
				break;
			}
			if( mPackageNameList.contains( appName ) && mClassNameList.contains( className ) )
			{
				ComponentName cmp = new ComponentName( appName , className );
				IconHouseProvider provider = null;
				provider = mProviders.get( cmp );
				if( provider == null )
				{
					provider = buildProvider( appConfigList.get( inter ).houseProvider , cmp );
				}
				//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "cyk_bug : c_0004400" , StringUtils.concat( "getIconHouse 1 : provider.getBitmap(): " , ( provider.getBitmap() == null ) ) );
				}
				//cheyingkun add end
				return provider.getBitmap();
			}
		}
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "cyk_bug : c_0004400" , "getIconHouse 2 : return null " );
		}
		//cheyingkun add end
		return null;
	}
	
	private IconHouseProvider buildProvider(
			String providerName ,
			ComponentName cmp )
	{
		IconHouseProvider provider = null;
		try
		{
			Class<?> pc = Class.forName( providerName );
			Constructor<?> construct = pc.getConstructor( new Class[]{ Context.class } );
			provider = (IconHouseProvider)construct.newInstance( mContext );
			provider.setUpdateListener( mUpdateListener );
			provider.setTarget( cmp );
			mProviders.put( cmp , provider );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , e.toString() );
		}
		return provider;
	}
	
	//cheyingkun add start	//解决“常用应用显示动态时，改变日期后返回桌面，桌面重启”的问题【c_0004419】
	public boolean isIconHouse(
			ComponentName cmp )
	{
		if( mProviders != null && mProviders.size() > 0 )
		{
			Set<ComponentName> keySet = mProviders.keySet();
			for( ComponentName componentName : keySet )
			{
				if( componentName.equals( cmp ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	/**
	 * 
	 * @param componentName
	 * @param bitmap
	 */
	public void updateFavoritesIconHouseApps(
			ComponentName componentName ,
			Bitmap bitmap )
	{
		if( componentName == null || bitmap == null || bitmap.isRecycled() )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "" , "cyk updateFavoritesIconHouseApps return " );
			return;
		}
		HashMap<ComponentName , Bitmap> iconHouseMap = new HashMap<ComponentName , Bitmap>();
		iconHouseMap.put( componentName , bitmap );
		//更新动态图标
		FavoritesPageManager.getInstance( mContext ).updateFavoritesAppsIcon( iconHouseMap );
	}
	//cheyingkun add end
	
	//zhujieping add start //需求：桌面动态图标支持随主题变化
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		for( ComponentName comp : mProviders.keySet() )
		{
			IconHouseProvider provider = mProviders.get( comp );
			provider.onThemeChanged( arg0 , arg1 );
		}
	}
	//zhujieping add end
}
