package com.cooee.favorites;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.cooee.dynamicload.DLHost;
import com.cooee.dynamicload.DLPlugin;
import com.cooee.dynamicload.update.UpdateCallback;
import com.cooee.dynamicload.update.UpdateService;
import com.cooee.dynamicload.utils.DLUtils;
import com.cooee.favorites.apps.FavoritesAppData;
import com.cooee.favorites.apps.FavoritesAppManager;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.utils.Assets;
import com.cooee.favorites.utils.RunningAppHelper;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsBaseNew;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoriteClings;
import com.cooee.uniex.wrap.IFavorites;
import com.cooee.uniex.wrap.IFavoritesGetData;
import com.cooee.uniex.wrap.IFavoritesReady;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;


public class FavoritesPlugin extends DLPlugin implements IFavorites , UpdateCallback
{
	
	private static final String TAG = FavoritesPlugin.class.getSimpleName();
	public static final String PROXY_PACKAGE_NAME = "com.cooee.favorites.proxy";
	public static final String PROXY_CLASS_NAME = "com.cooee.favorites.proxy.ProxyService";
	public static final String PluginPackageName = "com.cooee.favorites";
	public static final String PREF_KEY_LAST_VERSION = "favorites_last_version";
	private Context mContainerContext;// launcher的context
	private Context proxyContext;//移植到原生桌面时，代理apk的context
	private FavoritesConfig config;
	private String mAppId = "565bf2f4e0f55ad086001d1b";// 友盟
	//我们自己的统计Statistics begin
	private SharedPreferences prefs;
	public static String APPID = null;
	public static String SN = null;
	public static int UPLOAD_VERSION = -1;
	//表示是否收费
	private boolean isCharge = true;
	private JSONObject appendObj = new JSONObject();
	public static int PRODUCTTYPE = 4;
	
	//我们自己的统计Statistics end
	public FavoritesPlugin(
			DLHost host )
	{
		super( host );
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate()
	{
		mContainerContext = host.getContext();
		Log.v( "lvjiangbin" , "FavoritesPlugin onCreate mContainerContext = " + mContainerContext );
		prefs = mContainerContext.getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
	}
	
	@Override
	public void onReceive(
			Intent intent )
	{
		UpdateService.getInstance( mContainerContext ).setCallback( this );
	}
	
	@Override
	public void onPause()
	{
		if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
		{
			MobclickAgent.onPause( mContainerContext );//友盟统计
		}
	}
	
	@Override
	public void onPageBeginMoving()
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void removeApps(
			ArrayList<String> list )
	{
		FavoritesAppData.removeApps( list );
	}
	
	public void onBackPressed()
	{
		//		FavoritesManager.getInstance().hideNews();
	}
	
	@Override
	public void onDestroy()
	{
		FavoritesManager.getInstance().onDestroy();
	}
	
	@Override
	public void onResume()
	{
		statisOnResume();
		if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
		{
			MobclickAgent.onResume( mContainerContext );//友盟统计
			MobclickAgent.onEvent( mContainerContext , "favoritesplugin_onResume" );//cheyingkun add	//添加酷生活onResume时umeng统计和内部统计
		}
		if( config.getBoolean( FavoriteConfigString.getIsCustomerLauncherKey() , FavoriteConfigString.isCustomerLauncherDefaultValue() ) )
		{
			// huwenhao@2016/04/06 ADD START 启动push
			resumeKpsh();
			// huwenhao@2016/04/06 ADD END
		}
	}
	
	private void resumeKpsh()
	{
		Intent intent = new Intent();
		intent.setClassName( PROXY_PACKAGE_NAME , PROXY_CLASS_NAME );
		mContainerContext.startService( intent );
	}
	
	@Override
	public View getView()
	{
		// TODO Auto-generated method stub
		return FavoritesManager.getInstance().getView();
	}
	
	public boolean isSystemApp()
	{
		return FavoritesManager.getInstance().isSystemApp( mContainerContext );
	}
	
	@Override
	public void onShow()
	{
		FavoritesManager.getInstance().onShow();
		//cheyingkun add start	//cheyingkun add start	//添加附近点击统计和滑入-1屏统计
		if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
		{//滑入-1屏
			MobclickAgent.onEvent( mContainerContext , "search_page_in" );
		}
		try
		{
			StatisticsExpandNew.onCustomEvent(
					FavoritesManager.getInstance().getContainerContext() ,
					"search_page_in" ,
					FavoritesPlugin.SN ,
					FavoritesPlugin.APPID ,
					CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
					FavoritesPlugin.PRODUCTTYPE ,
					FavoritesPlugin.PluginPackageName ,
					FavoritesPlugin.UPLOAD_VERSION + "" ,
					null );
		}
		catch( NoSuchMethodError e )
		{
			try
			{
				StatisticsExpandNew.onCustomEvent(
						FavoritesManager.getInstance().getContainerContext() ,
						"search_page_in" ,
						FavoritesPlugin.SN ,
						FavoritesPlugin.APPID ,
						CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
						FavoritesPlugin.PRODUCTTYPE ,
						FavoritesPlugin.PluginPackageName );
			}
			catch( NoSuchMethodError e1 )
			{
				StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "search_page_in" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
			}
		}
		//cheyingkun add end
	}
	
	@Override
	public void onHide()
	{
		FavoritesManager.getInstance().onHide();
	}
	
	@Override
	public void setAllApp(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		RunningAppHelper.queryAppInfo( mContainerContext );
		HashMap<ComponentName , Bitmap> hashmap = new HashMap<ComponentName , Bitmap>();
		Log.v( "lvjiangbin" , "mLastAppCount != bitmap.size() = " + bitmap.size() );
		for( int i = 0 ; i < componentName.size() ; i++ )
		{
			hashmap.put( componentName.get( i ) , bitmap.get( i ) );
		}
		FavoritesManager.getInstance().loadAndBindApps( hashmap );
		FavoritesManager.getInstance().onLoadFinish();
	}
	
	@Override
	public void reLoadAndBindApps(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		RunningAppHelper.queryAppInfo( mContainerContext );
		HashMap<ComponentName , Bitmap> hashmap = new HashMap<ComponentName , Bitmap>();
		for( int i = 0 ; i < componentName.size() ; i++ )
		{
			hashmap.put( componentName.get( i ) , bitmap.get( i ) );
		}
		FavoritesManager.getInstance().onThemeChanged( hashmap );
	}
	
	@Override
	public Object asInterface()
	{
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public String getPackageName()
	{
		// TODO Auto-generated method stub
		return PluginPackageName;
	}
	
	@Override
	public void setup(
			FavoritesConfig cfg )
	{
		this.config = cfg;
		initIconSize();
		if( config.getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
		{
			config.putBoolean( FavoriteConfigString.getEnableAppsKey() , false );
			//			config.putBoolean( FavoriteConfigString.getEnableContactsKey() , false );
			config.putBoolean( FavoriteConfigString.getEnableNearbyKey() , false );
		}
		if( config.getBoolean( FavoriteConfigString.getIsCustomerLauncherKey() , FavoriteConfigString.isCustomerLauncherDefaultValue() ) )
		{
			Log.d( "fav" , "is customer launcher" );
			PRODUCTTYPE = 3;
			AnalyticsConfig.setAppkey( mAppId );//友盟统计
			int contextPermission = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;
			try
			{
				proxyContext = mContainerContext.createPackageContext( PROXY_PACKAGE_NAME , contextPermission );
				Assets.initAssets( proxyContext );
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			Log.d( "fav" , "not customer launcher" );
			proxyContext = mContainerContext;
			Assets.initAssets( mContainerContext );
		}
		try
		{
			PackageManager mPackageManager = proxyContext.getPackageManager();
			UPLOAD_VERSION = mPackageManager.getPackageInfo( proxyContext.getPackageName() , 0 ).versionCode; //客户桌面时候 使用我们的proxy插件的版本号
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		Log.d( "fav" , "cyk mHostVersion =" + UPLOAD_VERSION );
		statisOnCreate();//cooee统计
		statisOnResume();//cooee统计
		AnalyticsConfig.setChannel( SN );
		if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
		{
			MobclickAgent.onResume( mContainerContext );//友盟统计
		}
		final FavoritesPlugin favoritesPlugin = this;
		( (Activity)mContainerContext ).runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "lvjangbin" , "cyk init all view 0" );
				FavoritesManager.getInstance().init( favoritesPlugin , config );
				UpdateService.getInstance( mContainerContext ).setCallback( favoritesPlugin );
				checkLoadNewVersion();
				//lvjiangbin del 酷生活在新闻加载完毕后才算加载完成
				//不显示新闻或者显示的不是cooee新闻,直接加载结束
				if( !FavoritesManager.getInstance().isShowNews() )//cheyingkun add	//解决“关闭新闻时，酷生活加载一直显示进度圈”的问题【i_0014903】【车盈坤】
				{
					int VERSION = config.getInt( FavoriteConfigString.getHostVersionCodeKey() , FavoriteConfigString.getHostVersionCodeValue() );//cheyingkun add	//酷生活编辑失败,获取host版本
					Log.v( "lvjangbin" , "cyk init all view 1" );
					if( VERSION >= 12 )
					{
						Log.v( "lvjangbin" , "cyk init all view 2" );
						if( mContainerContext instanceof IFavoritesReady )
						{
							Log.v( "lvjangbin" , "cyk init all view 3" );
							( (IFavoritesReady)mContainerContext ).onFavoritesReady();
						}
					}
				}
			}
		} );
	}
	
	/**
	 * 检查是否加载了新版本，是则上传统计数据：cl_dl_loadnewversion
	 */
	private void checkLoadNewVersion()
	{
		if( config == null )
			return;
		int lastVersion = prefs.getInt( PREF_KEY_LAST_VERSION , -1 );
		int curVersion = getVersion();
		if( curVersion > lastVersion && lastVersion != -1 )
		{
			if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
			{
				MobclickAgent.onEvent( host.getContext() , "cl_dl_loadnewversion" );
			}
			JSONObject obj = new JSONObject();
			try
			{
				obj.put( "param1" , "cl_dl_loadnewversion" + ":" + curVersion );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try
			{
				StatisticsExpandNew.onCustomEvent(
						mContainerContext ,
						"cl_dl_loadnewversion" ,
						SN ,
						APPID ,
						CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
						PRODUCTTYPE ,
						getPackageName() ,
						FavoritesPlugin.UPLOAD_VERSION + "" ,
						obj );
			}
			catch( NoSuchMethodError e )
			{
				try
				{
					StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_loadnewversion" , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() , obj );
				}
				catch( NoSuchMethodError e1 )
				{
					StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_loadnewversion" , PRODUCTTYPE , getPackageName() , obj );
				}
			}
			Log.v( "lvjiangbin" , "checkLoadNewVersion" );
		}
		if( curVersion != lastVersion )
			prefs.edit().putInt( PREF_KEY_LAST_VERSION , curVersion ).commit();
	}
	
	public int getVersion()
	{
		PackageInfo packageInfo = DLUtils.getPackageInfo( host.getContext() , DLUtils.getPluginPath( host.getContext() , getPackageName() ) );
		return packageInfo.versionCode;
	}
	
	private void initStatisticData()
	{
		StatisticsBaseNew.setApplicationContext( mContainerContext );
		JSONObject tmp = Assets.config;
		//		PackageManager mPackageManager = mContainerContext.getPackageManager();
		try
		{
			JSONObject config = tmp.getJSONObject( "config" );
			APPID = config.getString( "app_id" );
			SN = config.getString( "serialno" );
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
	}
	
	private void statisOnCreate()
	{
		//	Log.v( "lvjiangbin" , "初始化负一屏自己的统计" );
		initStatisticData();
		try
		{
			appendObj.put( "param1" , "charge = " + isCharge );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		StatisticsExpandNew.setStatiisticsLogEnable( true );
		//		StatisticsExpandNew.setTestURL();
		if( prefs.getBoolean( "favorites_first_run" , true ) )
		{
			//		Log.v( "lvjiangbin" , "初始化负一屏自己的统计  统计重启" );
			//zhujieping modify，不传true，统计sdk会读取免责申明的值，若没有免责申明则默认为true
			try
			{
				StatisticsExpandNew.register(
						mContainerContext ,
						SN ,
						APPID ,
						CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
						PRODUCTTYPE ,
						getPackageName() ,
						"" + UPLOAD_VERSION ,
						appendObj.toString() );//cheyingkun add
			}
			catch( NoSuchMethodError e )
			{
				StatisticsExpandNew.register( mContainerContext , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() , "" + UPLOAD_VERSION );//cheyingkun add
			}
		}
		else
		{
			//		Log.v( "lvjiangbin" , "初始化负一屏自己的统计  统计第一次" );
			//zhujieping modify，不传true，统计sdk会读取免责申明的值，若没有免责申明则默认为true
			try
			{
				StatisticsExpandNew.startUp(
						mContainerContext ,
						SN ,
						APPID ,
						CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
						PRODUCTTYPE ,
						getPackageName() ,
						"" + UPLOAD_VERSION ,
						appendObj.toString() );//cheyingkun add
			}
			catch( NoSuchMethodError e )
			{
				StatisticsExpandNew.startUp( mContainerContext , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() , "" + UPLOAD_VERSION );//cheyingkun add
			}
		}
		if( prefs.getBoolean( "favorites_first_run" , true ) )
		{
			prefs.edit().putBoolean( "favorites_first_run" , false ).commit();
		}
		//	Log.v( "lvjiangbin" , "初始化负一屏自己的统计" );
	}
	
	private void statisOnResume()
	{
		//	Log.v( "lvjiangbin" , "初始化负一屏自己的统计 统计使用次数" );
		//zhujieping modify，不传true，统计sdk会读取免责申明的值，若没有免责申明则默认为true
		try
		{
			StatisticsExpandNew.use( mContainerContext , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() , "" + UPLOAD_VERSION , appendObj.toString() );//cheyingkun add
		}
		catch( NoSuchMethodError e )
		{
			StatisticsExpandNew.use( mContainerContext , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() , "" + UPLOAD_VERSION );//cheyingkun add
		}
		//cheyingkun add start	//添加酷生活onResume时umeng统计和内部统计
		try
		{
			StatisticsExpandNew.onCustomEvent(
					mContainerContext ,
					"favoritesplugin_onResume" ,
					SN ,
					APPID ,
					CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
					PRODUCTTYPE ,
					getPackageName() ,
					FavoritesPlugin.UPLOAD_VERSION + "" ,
					null );
		}
		catch( NoSuchMethodError e )
		{
			StatisticsExpandNew.onCustomEvent( mContainerContext , "favoritesplugin_onResume" , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() );
		}
		//cheyingkun add end
	}
	
	@Override
	public void setFavoritesGetDataCallBack(
			IFavoritesGetData favoritesGetData )
	{
		FavoritesAppManager.getInstance().setFavoritesGetDataCallBack( favoritesGetData );
	}
	
	@Override
	public void onDownloadFinish(
			String fileName )
	{
		if( host != null )
		{
			host.onPluginUpdate( getPackageName() , fileName );
		}
		try
		{
			StatisticsExpandNew.onCustomEvent(
					mContainerContext ,
					"cl_dl_downloadfinish" ,
					SN ,
					APPID ,
					CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
					PRODUCTTYPE ,
					getPackageName() ,
					FavoritesPlugin.UPLOAD_VERSION + "" ,
					null );
		}
		catch( NoSuchMethodError e )
		{
			try
			{
				StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_downloadfinish" , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() );
			}
			catch( NoSuchMethodError e1 )
			{
				StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_downloadfinish" , PRODUCTTYPE , getPackageName() );
			}
		}
		if( config == null )
			return;
		if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )//cheyingkun add	//添加友盟统计自定义事�?
		{
			MobclickAgent.onEvent( host.getContext() , "cl_dl_downloadfinish" );
		}
	}
	
	@Override
	public boolean enableUpdateDebug()
	{
		if( config != null )
			return config.getBoolean( FavoriteConfigString.getEnableDebugKey() , FavoriteConfigString.isEnableDebugDefaultValue() );
		return false;
	}
	
	@Override
	public void onRequestConfigStart()
	{
		try
		{
			StatisticsExpandNew.onCustomEvent(
					mContainerContext ,
					"cl_dl_reqconfig" ,
					SN ,
					APPID ,
					CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
					PRODUCTTYPE ,
					getPackageName() ,
					FavoritesPlugin.UPLOAD_VERSION + "" ,
					null );
		}
		catch( NoSuchMethodError e )
		{
			try
			{
				StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_reqconfig" , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() );
			}
			catch( NoSuchMethodError e1 )
			{
				StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_reqconfig" , PRODUCTTYPE , getPackageName() );
			}
		}
		if( config == null )
			return;
		if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )//cheyingkun add	//添加友盟统计自定义事件
		{
			MobclickAgent.onEvent( host.getContext() , "cl_dl_reqconfig" );
		}
	}
	
	@Override
	public void onDownloadStart()
	{
		try
		{
			StatisticsExpandNew.onCustomEvent(
					mContainerContext ,
					"cl_dl_downloadstart" ,
					SN ,
					APPID ,
					CooeeSdk.cooeeGetCooeeId( mContainerContext ) ,
					PRODUCTTYPE ,
					getPackageName() ,
					FavoritesPlugin.UPLOAD_VERSION + "" ,
					null );
		}
		catch( NoSuchMethodError e )
		{
			try
			{
				StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_downloadstart" , SN , APPID , CooeeSdk.cooeeGetCooeeId( mContainerContext ) , PRODUCTTYPE , getPackageName() );
			}
			catch( NoSuchMethodError e1 )
			{
				StatisticsExpandNew.onCustomEvent( mContainerContext , "cl_dl_downloadstart" , PRODUCTTYPE , getPackageName() );
			}
		}
		if( config == null )
			return;
		if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )//cheyingkun add	//添加友盟统计自定义事件
		{
			MobclickAgent.onEvent( host.getContext() , "cl_dl_downloadstart" );
		}
	}
	
	@Override
	public FavoritesConfig config()
	{
		// TODO Auto-generated method stub
		return config;
	}
	
	@Override
	public void setIconSize(
			int iconSize )
	{
		// TODO Auto-generated method stub
		if( config != null )
		{
			int oldIconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );//cheyingkun add	//酷生活支持动态修改图标大小
			if( canChangeIconSize( iconSize ) )//cheyingkun add	//桌面改变主题时，设置图标大小进行判断。（酷生活配置图标大小，则忽略桌面传过来的iconSize）【i_0014019】
			{
				Log.d( "" , "cyk setIconSize oldIconSize: " + oldIconSize );
				int newIconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
				Log.d( "" , "cyk setIconSize newIconSize: " + newIconSize );
				FavoritesManager.getInstance().onIconSizeChanged( newIconSize - oldIconSize );
			}
		}
	}
	
	@Override
	public void changeLocale()
	{
		// TODO Auto-generated method stub
		super.changeLocale();
		FavoritesManager.getInstance().initViews();//语言变化了重新生成view
	}
	
	@Override
	public String getAppID()
	{
		// TODO Auto-generated method stub
		return APPID;
	}
	
	@Override
	public String getSN()
	{
		// TODO Auto-generated method stub
		return SN;
	}
	
	public Context getContainerContext()
	{
		return mContainerContext;
	}
	
	public Context getProxyContext()
	{
		return proxyContext;
	}
	
	public int getProductType()
	{
		return PRODUCTTYPE;
	}
	
	public Context getPluginContext()
	{
		// TODO Auto-generated method stub
		return pluginContext;
	}
	
	//cheyingkun add start	//酷生活引导页
	@Override
	public boolean isShowFavoriteClings()
	{
		boolean switchEnableClings = config.getBoolean( FavoriteConfigString.getEnableFavoritesClingsKey() , FavoriteConfigString.isEnableFavoritesClingsDefaultValue() );//启动引导页
		if( switchEnableClings && FavoritesManager.getInstance().isShowFavoriteClings()//引导页正在显示
		)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void setIFavoriteClingsCallBack(
			IFavoriteClings mIFavoriteClings )
	{
		if( mIFavoriteClings != null )
		{
			FavoritesManager.getInstance().setIFavoriteClings( mIFavoriteClings );
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	@Override
	public void clearFavoritesView()
	{
		FavoritesManager.getInstance().clearFavoritesView();
	}
	
	//cheyingkun add end
	//cheyingkun add start	//桌面改变主题时，设置图标大小进行判断。（酷生活配置图标大小，则忽略桌面传过来的iconSize）【i_0014019】
	private void initIconSize()
	{
		//cheyingkun add start	//酷生活图标大小可配
		if( pluginContext != null && config != null )
		{
			int iconSize = pluginContext.getResources().getDimensionPixelSize( R.dimen.favorites_default_icon_size );
			if( iconSize > 0 )
			{
				config.putInt( FavoriteConfigString.getLauncherIconSizePxKey() , iconSize );
			}
		}
		//cheyingkun add end
		int iconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
		Log.d( "" , "cyk iconSize: " + iconSize );
	}
	
	private boolean canChangeIconSize(
			int newIconSize )
	{
		int tmpIconSize = 0;
		if( pluginContext != null && config != null )
		{
			tmpIconSize = pluginContext.getResources().getDimensionPixelSize( R.dimen.favorites_default_icon_size );
			if( tmpIconSize > 0 )
			{
				return false;
			}
			else
			{
				int oldIconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
				Log.d( "" , "cyk iconSize: " + oldIconSize );
				tmpIconSize = newIconSize;
				config.putInt( FavoriteConfigString.getLauncherIconSizePxKey() , tmpIconSize );
				return true;
			}
		}
		return false;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	@Override
	public void updateFavoritesAppsIcon(
			List<ComponentName> componentName ,
			List<Bitmap> bitmap )
	{
		if( componentName == null || bitmap == null )
		{
			Log.e( "" , "cyk FavoritesPlugin updateFavoritesAppsIcon return " );
			return;
		}
		FavoritesManager.getInstance().updateFavoritesAppsIcon( componentName , bitmap );
	}
	
	//cheyingkun add end
	@Override
	public int getFavoriteState()
	{
		// TODO Auto-generated method stub
		return FavoritesManager.getInstance().getFavoritesState();
	}
}
