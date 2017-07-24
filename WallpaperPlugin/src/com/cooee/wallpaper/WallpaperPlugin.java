package com.cooee.wallpaper;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.ViewGroup;

import com.cooee.dynamicload.DLHost;
import com.cooee.dynamicload.DLPlugin;
import com.cooee.dynamicload.update.UpdateCallback;
import com.cooee.dynamicload.update.UpdateService;
import com.cooee.dynamicload.utils.DLUtils;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsBaseNew;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.wallpaper.data.UmengStatistics;
import com.cooee.wallpaper.manager.ChangeWallpaperManager;
import com.cooee.wallpaper.manager.WallpaperReceiver;
import com.cooee.wallpaper.util.Assets;
import com.cooee.wallpaper.util.ThreadUtil;
import com.cooee.wallpaper.wrap.DynamicImageView;
import com.cooee.wallpaper.wrap.IWallpaper;
import com.cooee.wallpaper.wrap.IWallpaperCallbacks;
import com.cooee.wallpaper.wrap.WallpaperConfig;
import com.cooee.wallpaper.wrap.WallpaperConfigString;
import com.cooee.wallpaperManager.WallpaperManagerBase;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.WallpaperControl.WallpaperControlHelper;


public class WallpaperPlugin extends DLPlugin implements IWallpaper , UpdateCallback
{
	
	public static final String PluginPackageName = "com.cooee.wallpaper";
	private Context mContainerContext;
	private SharedPreferences prefs;
	String appid = null;
	String sn = null;
	int mHostVersion = -1;
	int productType = 4;
	private WallpaperConfig config;
	public static final String PREF_KEY_LAST_VERSION = "wallpapers_last_version";
	private ChangeWallpaper mModel;
	
	public WallpaperPlugin(
			DLHost host )
	{
		super( host );
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getVersion()
	{
		// TODO Auto-generated method stub
		PackageInfo packageInfo = DLUtils.getPackageInfo( host.getContext() , DLUtils.getPluginPath( host.getContext() , getPackageName() ) );
		return packageInfo.versionCode;
	}
	
	@Override
	public String getAppID()
	{
		// TODO Auto-generated method stub
		return appid;
	}
	
	@Override
	public String getSN()
	{
		// TODO Auto-generated method stub
		return sn;
	}
	
	@Override
	public void onDownloadFinish(
			String fileName )
	{
		if( host != null )
		{
			host.onPluginUpdate( getPackageName() , fileName );
		}
		StatisticsExpandNew.onCustomEvent(
				mContainerContext ,
				UmengStatistics.one_key_change_wallpapper_downloadfinish ,
				sn ,
				appid ,
				CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
				productType ,
				getPackageName() );
		if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( host.getContext() , UmengStatistics.one_key_change_wallpapper_downloadfinish );
		}
		Log.v( "zjp" , "onDownloadFinish" );
	}
	
	@Override
	public void onRequestConfigStart()
	{
		StatisticsExpandNew.onCustomEvent(
				mContainerContext ,
				UmengStatistics.one_key_change_wallpapper_request_hotupdate ,
				sn ,
				appid ,
				CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
				productType ,
				getPackageName() );
		if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( host.getContext() , UmengStatistics.one_key_change_wallpapper_request_hotupdate );
		}
		Log.v( "zjp" , "onRequestConfigStart" );
	}
	
	@Override
	public void onDownloadStart()
	{
		StatisticsExpandNew.onCustomEvent(
				mContainerContext ,
				UmengStatistics.one_key_change_wallpapper_download ,
				sn ,
				appid ,
				CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
				productType ,
				getPackageName() );
		if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( host.getContext() , UmengStatistics.one_key_change_wallpapper_download );
		}
		Log.v( "zjp" , "onDownloadStart" );
	}
	
	private void checkLoadNewVersion()
	{
		int lastVersion = prefs.getInt( PREF_KEY_LAST_VERSION , -1 );
		int curVersion = getVersion();
		if( curVersion > lastVersion && lastVersion != -1 )
		{
			if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
			{
				MobclickAgent.onEvent( host.getContext() , UmengStatistics.one_key_change_wallpapper_new_version );
			}
			StatisticsExpandNew.onCustomEvent(
					mContainerContext ,
					UmengStatistics.one_key_change_wallpapper_new_version ,
					sn ,
					appid ,
					CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
					productType ,
					getPackageName() );
		}
		if( curVersion != lastVersion )
			prefs.edit().putInt( PREF_KEY_LAST_VERSION , curVersion ).commit();
		Log.v( "zjp" , "checkLoadNewVersion" );
	}
	
	@Override
	public boolean enableUpdateDebug()
	{
		// TODO Auto-generated method stub
		if( config == null )
		{
			return false;
		}
		return config.getBoolean( WallpaperConfigString.ENABLE_UPDATE_DEBUG , false );
	}
	
	@Override
	public String getPackageName()
	{
		// TODO Auto-generated method stub
		return PluginPackageName;
	}
	
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		mContainerContext = host.getContext();
		prefs = mContainerContext.getSharedPreferences( "wallpaper" , Activity.MODE_PRIVATE );
		ThreadUtil.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				statisOnCreate();
			}
		} );
	}
	
	private void initStatisticData()
	{
		Assets.initAssets( mContainerContext );
		StatisticsBaseNew.setApplicationContext( mContainerContext );
		JSONObject tmp = Assets.config;
		//		PackageManager mPackageManager = mContainerContext.getPackageManager();
		try
		{
			JSONObject config = tmp.getJSONObject( "config" );
			appid = config.getString( "app_id" );
			sn = config.getString( "serialno" );
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		try
		{
			PackageManager mPackageManager = mContainerContext.getPackageManager();
			mHostVersion = mPackageManager.getPackageInfo( mContainerContext.getPackageName() , 0 ).versionCode;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
	}
	
	private void statisOnCreate()
	{
		initStatisticData();
		if( prefs.getBoolean( "wallpaper_first_run" , true ) )
		{
			StatisticsExpandNew.register( mContainerContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , productType , getPackageName() , "" + mHostVersion );//cheyingkun add
			prefs.edit().putBoolean( "wallpaper_first_run" , false ).commit();
		}
		else
		{
			StatisticsExpandNew.startUp( mContainerContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , productType , getPackageName() , "" + mHostVersion );//cheyingkun add
		}
		checkLoadNewVersion();
	}
	
	private void statisOnResume()
	{
		ThreadUtil.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( sn == null )
				{
					initStatisticData();
				}
				StatisticsExpandNew.use( mContainerContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , productType , getPackageName() , "" + mHostVersion );//cheyingkun add
			}
		} );
	}
	
	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onReceive(
			Intent intent )
	{
		// TODO Auto-generated method stub
		UpdateService.getInstance( mContainerContext ).setCallback( this );
	}
	
	@Override
	public Object asInterface()
	{
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public void setWallpaperCallbacks(
			IWallpaperCallbacks instance )
	{
		// TODO Auto-generated method stub
		ChangeWallpaperManager.setWallpaperInterface( instance );
	}
	
	@Override
	public void setup(
			WallpaperConfig config )
	{
		// TODO Auto-generated method stub
		this.config = config;
		if( config != null )
		{
			ChangeWallpaperManager.SWITCH_ENABLE_UMENG = config.getBoolean( WallpaperConfigString.ENABLE_UMENG , true );
			ChangeWallpaperManager.SWITCH_ENABLE_ADS = config.getBoolean( WallpaperConfigString.ENABLE_ADS , true );
			ChangeWallpaperManager.SWITCH_ENABEL_ADS_ONLINE = WallpaperControlHelper.getInstance( mContainerContext ).enabelAdS();
			ChangeWallpaperManager.CUSTOM_WALLPAPER_PATH = config.getString( WallpaperConfigString.CUSTOM_WALLPAPERS_PATH , ChangeWallpaperManager.CUSTOM_WALLPAPER_PATH );//获取桌面配置
			// jubingcheng@2016/07/01 UPD START
			//WallpaperManagerBase.disable_set_wallpaper_dimensions = config.getBoolean( WallpaperConfigString.LAUNCHER_SET_WALLPAPER_DIMENSIONS , WallpaperManagerBase.disable_set_wallpaper_dimensions );
			//WallpaperManagerBase.enable_set_wallpaperDim_before_set_wallpaper = config.getBoolean(
			//		WallpaperConfigString.ENABLE_SET_WALLPAPERDIM_BEFORE_SET_WALLPAPER ,
			//		WallpaperManagerBase.enable_set_wallpaperDim_before_set_wallpaper );
			//WallpaperManagerBase.MTK_setWallpaperSize = config.getBoolean( WallpaperConfigString.MTK_SETWALLPAPERSIZE , WallpaperManagerBase.MTK_setWallpaperSize );
			//WallpaperManagerBase.when_change_theme_outofmemory = config.getBoolean( WallpaperConfigString.WHEN_CHANGE_THEME_OUTOFMEMORY , WallpaperManagerBase.when_change_theme_outofmemory );
			//WallpaperManagerBase.disable_move_wallpaper = config.getBoolean( WallpaperConfigString.DISABLE_MOVE_WALLPAPER , WallpaperManagerBase.disable_move_wallpaper );
			WallpaperManagerBase.set_disableSetWallpaperDimensions( config.getBoolean(
					WallpaperConfigString.LAUNCHER_SET_WALLPAPER_DIMENSIONS ,
					WallpaperManagerBase.get_disableSetWallpaperDimensions() ) );
			WallpaperManagerBase.set_enableSetWallpaperDimBeforeSetWallpaper( config.getBoolean(
					WallpaperConfigString.ENABLE_SET_WALLPAPERDIM_BEFORE_SET_WALLPAPER ,
					WallpaperManagerBase.get_enableSetWallpaperDimBeforeSetWallpaper() ) );
			WallpaperManagerBase.set_MTKSetWallpaperSize( config.getBoolean( WallpaperConfigString.MTK_SETWALLPAPERSIZE , WallpaperManagerBase.get_MTKSetWallpaperSize() ) );
			WallpaperManagerBase.set_whenChangeThemeOutofmemory( config.getBoolean( WallpaperConfigString.WHEN_CHANGE_THEME_OUTOFMEMORY , WallpaperManagerBase.get_whenChangeThemeOutofmemory() ) );
			WallpaperManagerBase.set_disableMoveWallpaper( config.getBoolean( WallpaperConfigString.DISABLE_MOVE_WALLPAPER , WallpaperManagerBase.get_disableMoveWallpaper() ) );
			// jubingcheng@2016/07/01 UPD END
			ChangeWallpaperManager.Online_wallpaper_from = WallpaperControlHelper.getInstance( mContainerContext ).getWallpaperFrom(
					config.getInt( WallpaperConfigString.ONLINE_WALLPAPER_FROM , ChangeWallpaperManager.Online_wallpaper_from ) );
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
		filter.addAction( Intent.ACTION_SCREEN_ON );
		filter.addAction( Intent.ACTION_SCREEN_OFF );
		filter.addAction( ConnectivityManager.CONNECTIVITY_ACTION );
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		filter.addAction( "android.intent.action.PHONE_STATE" );
		filter.addAction( "android.intent.action.USER_PRESENT" );
		filter.addAction( "android.provider.Telephony.SMS_RECEIVED" );
		WallpaperReceiver receiver = new WallpaperReceiver();
		mContainerContext.getApplicationContext().registerReceiver( receiver , filter );
	}
	
	@Override
	public void start(
			Activity activity ,
			ViewGroup mParent ,
			DynamicImageView mDynamic )
	{
		// TODO Auto-generated method stub
		if( mContainerContext != activity )
			mContainerContext = activity;
		UpdateService.getInstance( mContainerContext ).setCallback( this );
		ChangeWallpaperManager.setContainerContext( mContainerContext );
		mModel = new ChangeWallpaper( activity , mParent , mDynamic );
		mModel.startChangeWallpaper( pluginContext );
		statisOnResume();
	}
	
	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		if( mModel != null )
		{
			mModel.onBackPressed();
		}
	}
	
	@Override
	public void showSuccessView()
	{
		// TODO Auto-generated method stub
		if( mModel != null )
		{
			mModel.showSuccessView();
		}
	}
}
