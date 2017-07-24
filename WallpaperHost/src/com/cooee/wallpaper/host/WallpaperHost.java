package com.cooee.wallpaper.host;


import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import com.cooee.dynamicload.DLHost;
import com.cooee.wallpaper.wrap.DynamicImageView;
import com.cooee.wallpaper.wrap.IWallpaper;
import com.cooee.wallpaper.wrap.IWallpaperCallbacks;
import com.cooee.wallpaper.wrap.WallpaperConfig;


public class WallpaperHost extends DLHost implements IWallpaper
{
	
	public static final String PluginPackageName = "com.cooee.wallpaper";
	public static final String PluginClassName = "com.cooee.wallpaper.WallpaperPlugin";
	private static WallpaperHost instance;
	private IWallpaper iWallpaper;
	private String preLanguage;
	private WallpaperConfig config;
	
	public static WallpaperHost getInstance(
			Context containerContext ,
			Context proxyContext )
	{
		if( instance == null )
		{
			synchronized( DLHost.class )
			{
				if( instance == null )
				{
					instance = new WallpaperHost( containerContext , proxyContext );
				}
			}
		}
		return instance;
	}
	
	public WallpaperHost(
			Context containerContext ,
			Context proxyContext )
	{
		super( containerContext , proxyContext );
		start( FROM_ACTIVITY , PluginPackageName , PluginClassName , null );
	}
	
	@Override
	public void start(
			String from ,
			String packageName ,
			String pluginClassName ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		super.start( from , packageName , pluginClassName , intent );
		if( plugin != null && iWallpaper == null )
		{
			iWallpaper = (IWallpaper)plugin.asInterface();
		}
		preLanguage = Locale.getDefault().toString();
	}
	
	@Override
	public int getVersion()
	{
		// TODO Auto-generated method stub
		return Version.HOST_VERSION_CODE;
	}
	
	@Override
	public void start(
			Activity activity ,
			ViewGroup mParent ,
			DynamicImageView mDynamic )
	{
		// TODO Auto-generated method stub
		if( !preLanguage.equals( Locale.getDefault().toString() ) )
		{
			preLanguage = Locale.getDefault().toString();
			onConfigurationChanged( null );
		}
		if( iWallpaper != null )
		{
			iWallpaper.start( activity , mParent , mDynamic );
		}
	}
	
	@Override
	public void setWallpaperCallbacks(
			IWallpaperCallbacks instance )
	{
		// TODO Auto-generated method stub
		if( iWallpaper != null )
		{
			iWallpaper.setWallpaperCallbacks( instance );
		}
	}
	
	@Override
	public void setup(
			WallpaperConfig config )
	{
		// TODO Auto-generated method stub
		this.config = config;
		if( iWallpaper != null )
		{
			iWallpaper.setup( config );
		}
	}
	
	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		if( iWallpaper != null )
		{
			iWallpaper.onBackPressed();
		}
	}
	
	@Override
	public void showSuccessView()
	{
		// TODO Auto-generated method stub
		if( iWallpaper != null )
		{
			iWallpaper.showSuccessView();
		}
	}
	
	public WallpaperConfig getConfig()
	{
		return config;
	}
}
