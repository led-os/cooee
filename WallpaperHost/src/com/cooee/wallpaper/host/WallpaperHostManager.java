package com.cooee.wallpaper.host;


import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.cooee.wallpaper.wrap.IWallpaperCallbacks;
import com.cooee.wallpaper.wrap.WallpaperConfig;
import com.cooee.wallpaper.wrap.WallpaperConfigString;


public class WallpaperHostManager
{
	
	public static final String TAG = "WidgetPageManager";
	private Context containerContext;
	private static WallpaperHostManager instance = null;
	private Context proxyContext;
	private WallpaperConfig config;
	private WallpaperHost mhost;
	
	public static WallpaperHostManager getInstance(
			Context context )
	{
		if( instance == null )
		{
			synchronized( WallpaperHostManager.class )
			{
				if( instance == null )
				{
					instance = new WallpaperHostManager( context );
				}
			}
		}
		return instance;
	}
	
	public static WallpaperHostManager getWallpaperHostManager()
	{
		return instance;
	}
	
	public WallpaperHostManager(
			Context context )
	{
		containerContext = context;
	}
	
	/**
	 * 桌面调用这个方法 map中至少需要包含 FavoriteConfigString.LAUNCHER_ICON_SIZEPX 和 FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT
	 * @param map 桌面传入的开关配置的map,如果没有传入,使用assets下的配置,如果assets没有,使用默认值
	 * @param isCustomerLauncher
	 */
	public void init(
			HashMap<String , Object> launcherConfigMap )
	{
		if( mhost != null )
		{
			return;
		}
		//初始化map文件
		proxyContext = containerContext;
		//根据map初始化config
		config = initConfig( launcherConfigMap );
		//参数
		initByWallpaperConfig();
	}
	
	/**
	 * 初始化config
	 */
	private WallpaperConfig initConfig(
			HashMap<String , Object> launcherConfigMap )
	{
		WallpaperConfig config = new WallpaperConfig( containerContext );
		if( launcherConfigMap != null && launcherConfigMap.size() > 0 )
		{
			Set<String> keySet = launcherConfigMap.keySet();
			for( String key : keySet )
			{
				Object value = launcherConfigMap.get( key );
				if( value != null )
				{
					config.putObject( key , value );
				}
			}
		}
		if( !config.isContainKey( WallpaperConfigString.ENABLE_UMENG ) )
		{
			config.putBoolean( WallpaperConfigString.ENABLE_UMENG , containerContext.getResources().getBoolean( R.bool.enable_umeng ) );
		}
		config.putBoolean( WallpaperConfigString.ENABLE_UPDATE_DEBUG , containerContext.getResources().getBoolean( R.bool.enable_update_debug ) );
		config.putBoolean( WallpaperConfigString.ENABLE_ADS , containerContext.getResources().getBoolean( R.bool.enable_ads ) );
		config.putInt( "online_wallpaper_from" , containerContext.getResources().getInteger( R.integer.online_wallpaper_from ) );
		return config;
	}
	
	private void initByWallpaperConfig()
	{
		mhost = WallpaperHost.getInstance( containerContext , proxyContext );
		if( mhost == null )
		{
			Log.e( TAG , "WidgetPageManager mRootView is null" );
			return;
		}
		if( config != null )
		{
			mhost.setup( config );
		}
	}
	
	public void setWallpaperCallbacks(
			IWallpaperCallbacks callbacks )
	{
		if( mhost != null )
		{
			mhost.setWallpaperCallbacks( callbacks );
		}
	}
}
