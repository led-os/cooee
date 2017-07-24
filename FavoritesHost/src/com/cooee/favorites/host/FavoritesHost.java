package com.cooee.favorites.host;


import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import com.cooee.dynamicload.DLHost;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoriteClings;
import com.cooee.uniex.wrap.IFavorites;
import com.cooee.uniex.wrap.IFavoritesGetData;


public class FavoritesHost extends DLHost implements IFavorites
{
	
	public static final String PluginPackageName = "com.cooee.favorites";
	public static final String PluginClassName = "com.cooee.favorites.FavoritesPlugin";
	private static FavoritesHost instance;
	private IFavorites favorites;
	
	public static FavoritesHost getInstance(
			Context containerContext ,
			Context proxyContext )
	{
		if( instance == null )
		{
			synchronized( DLHost.class )
			{
				if( instance == null )
				{
					instance = new FavoritesHost( containerContext , proxyContext );
				}
			}
		}
		return instance;
	}
	
	public FavoritesHost(
			Context containerContext ,
			Context proxyContext )
	{
		super( containerContext , proxyContext );
		start( FROM_ACTIVITY , PluginPackageName , PluginClassName , null );
	}
	
	@Override
	public boolean enableDebug()
	{
		// TODO Auto-generated method stub
		//cheyingkun start	//酷生活代码优化。（改为桌面传FavoritesConfig过来）
		//		return containerContext.getResources().getBoolean( R.bool.switch_enable_dev );//cheyingkun del
		//cheyingkun add start
		if( favorites != null )
		{
			return favorites.config().getBoolean( FavoriteConfigString.ENABLE_DEV , false );
		}
		return false;
		//cheyingkun add end
		//cheyingkun end
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
		if( plugin != null && favorites == null )
		{
			favorites = (IFavorites)plugin.asInterface();
		}
	}
	
	@Override
	public void onPluginUpdate(
			String packageName ,
			String pluginFileName )
	{
		// TODO Auto-generated method stub
		super.onPluginUpdate( packageName , pluginFileName );
	}
	
	@Override
	public void onPause()
	{
		if( favorites != null )
		{
			favorites.onPause();
		}
	}
	
	@Override
	public void onResume()
	{
		if( favorites != null )
		{
			favorites.onResume();
		}
	}
	
	@Override
	public void setAllApp(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		if( favorites != null )
		{
			favorites.setAllApp( componentName , bitmap );
		}
	}
	
	@Override
	public void reLoadAndBindApps(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		if( favorites != null )
		{
			favorites.reLoadAndBindApps( componentName , bitmap );
		}
	}
	
	@Override
	public View getView()
	{
		if( favorites != null )
		{
			return favorites.getView();
		}
		return null;
	}
	
	@Override
	public void removeApps(
			ArrayList<String> list )
	{
		if( favorites != null )
		{
			favorites.removeApps( list );
		}
	}
	
	@Override
	public void onShow()
	{
		if( favorites != null )
		{
			favorites.onShow();
		}
	}
	
	@Override
	public void onHide()
	{
		if( favorites != null )
		{
			favorites.onHide();
		}
	}
	
	@Override
	public void onBackPressed()
	{
		if( favorites != null )
		{
			favorites.onBackPressed();
		}
	}
	
	@Override
	public void onPageBeginMoving()
	{
		if( favorites != null )
		{
			favorites.onPageBeginMoving();
		}
	}
	
	public FavoritesConfig config()
	{
		//cheyingkun add start	//酷生活代码优化。（改为桌面传FavoritesConfig过来）
		//cheyingkun del start
		//		FavoritesConfig config = new FavoritesConfig( getContext() );
		//		config.putBoolean( "enable_cooee_search" , getContext().getResources().getBoolean( R.bool.switch_enable_cooee_search ) );
		//		config.putBoolean( "enable_solo_search" , getContext().getResources().getBoolean( R.bool.switch_enable_solo ) );
		//		config.putBoolean( "enable_news" , getContext().getResources().getBoolean( R.bool.switch_enable_show_news ) );
		//		config.putBoolean( "enable_contacts" , getContext().getResources().getBoolean( R.bool.switch_enable_contacts ) );
		//		config.putBoolean( "enable_apps" , getContext().getResources().getBoolean( R.bool.switch_enable_apps ) );
		//		config.putBoolean( "enable_nearby" , getContext().getResources().getBoolean( R.bool.switch_enable_nearby ) );
		//		config.putBoolean( "enable_debug" , getContext().getResources().getBoolean( R.bool.switch_enable_debug ) );
		//		config.putBoolean( "newsfoldable" , getContext().getResources().getBoolean( R.bool.switch_news_foldable ) );
		//		config.putBoolean( "newsexpand" , getContext().getResources().getBoolean( R.bool.news_default_expand ) );
		//		return config;
		//cheyingkun del end
		//cheyingkun add start
		if( favorites != null )
		{
			return favorites.config();
		}
		return null;
		//cheyingkun add end
		//cheyingkun end
	}
	
	@Override
	public void setup(
			FavoritesConfig config )
	{
		try
		{
			if( favorites != null )
			{
				favorites.setup( config );
			}
		}
		catch( Throwable e )
		{
			e.printStackTrace();
			deleteFileAndRestart( PluginPackageName );
		}
	}
	
	@Override
	public void setFavoritesGetDataCallBack(
			IFavoritesGetData favoritesGetData )
	{
		if( favorites != null )
		{
			favorites.setFavoritesGetDataCallBack( favoritesGetData );
		}
	}
	
	@Override
	public int getVersion()
	{
		// TODO Auto-generated method stub
		return Version.HOST_VERSION_CODE;
	}
	
	@Override
	public void setIconSize(
			int iconSize )
	{
		// TODO Auto-generated method stub
		if( favorites != null )
		{
			favorites.setIconSize( iconSize );
		}
	}
	
	//cheyingkun add start	//酷生活引导页
	@Override
	public boolean isShowFavoriteClings()
	{
		if( favorites != null )
		{
			return favorites.isShowFavoriteClings();
		}
		return false;
	}
	
	@Override
	public void setIFavoriteClingsCallBack(
			IFavoriteClings favoriteClings )
	{
		if( favorites != null )
		{
			favorites.setIFavoriteClingsCallBack( favoriteClings );
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	@Override
	public void clearFavoritesView()
	{
		if( favorites != null )
		{
			favorites.clearFavoritesView();
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	@Override
	public void updateFavoritesAppsIcon(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		if( favorites != null )
		{
			favorites.updateFavoritesAppsIcon( componentName , bitmap );
		}
	}
	
	//cheyingkun add end
	@Override
	public int getFavoriteState()
	{
		// TODO Auto-generated method stub
		if( favorites != null )
		{
			return favorites.getFavoriteState();
		}
		return -1;
	}
}
