package com.cooee.favorites.host;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoriteClings;
import com.cooee.uniex.wrap.IFavorites;
import com.cooee.uniex.wrap.IFavoritesGetData;

import cool.sdk.search.SearchActivityManager;
import cool.sdk.search.SearchConfig;
import cool.sdk.search.SearchHelper;


public class FavoritesPageManager
{
	
	public static final String TAG = "WidgetPageManager";
	private static final String REMOTE_CLASS_NAME = "com.cooee.favorites.host.FavoritesHost";
	private Context containerContext;
	private WidgetPageInfo mInfo = null;
	private static FavoritesPageManager instance = null;
	private Context proxyContext;
	private FavoritesConfig config;
	private HashMap<String , Object> assetsConfigMap;
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	public static boolean isLoadFavoritesFinish = false;
	public static int LOADING_VIEW_ID = 20160926;
	
	public static FavoritesPageManager getInstance(
			Context context )
	{
		if( instance == null )
		{
			synchronized( FavoritesPageManager.class )
			{
				if( instance == null )
				{
					instance = new FavoritesPageManager( context );
				}
			}
		}
		return instance;
	}
	
	private class WidgetPageInfo
	{
		
		private String mPackageName;
		private IFavorites mUtilsInstance;
		private Context mRemoteContext;
		//		private FavoritesHost mUtilsClass;
	}
	
	public FavoritesPageManager(
			Context context )
	{
		containerContext = context;
	}
	
	//cheyingkun add start	//酷生活代码优化。（改为桌面传FavoritesConfig过来）
	public static final String PROXY_PACKAGE_NAME = "com.cooee.favorites.proxy";
	
	/**
	 * 桌面调用这个方法 map中至少需要包含 FavoriteConfigString.LAUNCHER_ICON_SIZEPX 和 FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT
	 * @param launcherConfigMap 桌面传入的开关配置的map,如果没有传入,使用assets下的配置,如果assets没有,使用默认值
	 * @param isCustomerLauncher 是否是客户桌面
	 * @category
	 * 建议使用下面方法进行初始化
	 * {@link #initFavoritesConfig(HashMap,boolean) } {@link #initPluginAfterInitFavoritesConfig() }
	 */
	@Deprecated
	public void init(
			final HashMap<String , Object> launcherConfigMap ,
			final boolean isCustomerLauncher )
	{
		Log.d( "" , "cyk isCustomerLauncher: " + isCustomerLauncher );
		if( mInfo != null )
		{
			return;
		}
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "lvjangbin" , "init favorites" );
				//初始化map文件
				initConfigMap( isCustomerLauncher );
				//根据map初始化config
				config = initConfig( launcherConfigMap );
				//参数
				initByFavoritesConfig();
			}
		} );
	}
	
	//cheyingkun add start	//解决“打开酷生活引导页，切页到酷生活后，点击引导页按钮，搜索栏显示异常”的问题
	/**
	 * 初始化酷生活开关配置,单独提取出来,在没加载酷生活时,桌面需要知道一些开关状态(比如是否启动引导页,来决定搜索栏的动画)
	 * @param launcherConfigMap 桌面传过来的开关map
	 * @param isCustomerLauncher 是否是客户自己的桌面
	 * @category
	 * 如果调用该方法,之后的初始化请调用{@link #initPluginAfterInitFavoritesConfig() }
	 */
	public void initFavoritesConfig(
			final HashMap<String , Object> launcherConfigMap ,
			final boolean isCustomerLauncher )
	{
		Log.d( "" , "cyk initConfig isCustomerLauncher: " + isCustomerLauncher );
		if( mInfo != null )
		{
			return;
		}
		//初始化map文件
		initConfigMap( isCustomerLauncher );
		//根据map初始化config
		config = initConfig( launcherConfigMap );
	}
	
	/**
	 * 初始化酷生活,耗时操作在这里面.
	 * @category
	 * 如果调用该方法,需要首先调用{@link #initFavoritesConfig(HashMap,boolean) }
	 */
	public void initPluginAfterInitFavoritesConfig()
	{
		Log.d( "" , "cyk init 0: " );
		if( mInfo != null )
		{
			return;
		}
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "lvjangbin" , "init favorites" );
				//参数
				initByFavoritesConfig();
			}
		} );
	}
	
	public FavoritesConfig getConfig()
	{
		return config;
	}
	
	//cheyingkun add end
	/**
	 * 初始化Map
	 * @param isCustomerLauncher
	 */
	private void initConfigMap(
			boolean isCustomerLauncher )
	{
		try
		{
			JSONObject configJson = null;
			if( isCustomerLauncher )
			{
				int contextPermission = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;
				proxyContext = containerContext.createPackageContext( PROXY_PACKAGE_NAME , contextPermission );
			}
			else
			{
				proxyContext = containerContext;
			}
			Log.d( "" , "cyk initConfig: proxyContext: " + proxyContext );
			configJson = getConfigForLauncherAssets( proxyContext , "config_favorite.ini" );
			Log.d( "" , "cyk initConfig: get switch from json: " + configJson );
			//
			//
			//
			//get switch from json
			boolean switch_enable_show_launcher_search = getBooleanForJson( configJson , "switch_enable_show_launcher_search" , FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH_DEFAULTVALUE );
			boolean switch_enable_show_favorite_search = getBooleanForJson( configJson , "switch_enable_show_favorite_search" , FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH_DEFAULTVALUE );
			boolean switch_enable_cooee_search = getBooleanForJson( configJson , "switch_enable_cooee_search" , FavoriteConfigString.ENABLE_COOEE_SEARCH_DEFAULTVALUE );
			boolean switch_enable_dev = getBooleanForJson( configJson , "switch_enable_dev" , FavoriteConfigString.ENABLE_DEV_DEFAULTVALUE );
			boolean switch_enable_debug = getBooleanForJson( configJson , "switch_enable_debug" , FavoriteConfigString.ENABLE_DEBUG_DEFAULTVALUE );
			boolean switch_enable_show_news = getBooleanForJson( configJson , "switch_enable_show_news" , FavoriteConfigString.ENABLE_NEWS_DEFAULTVALUE );
			boolean switch_enable_contacts = getBooleanForJson( configJson , "switch_enable_contacts" , FavoriteConfigString.ENABLE_CONTACTS_DEFAULTVALUE );
			boolean switch_enable_apps = getBooleanForJson( configJson , "switch_enable_apps" , FavoriteConfigString.ENABLE_APPS_DEFAULTVALUE );
			boolean switch_enable_nearby = getBooleanForJson( configJson , "switch_enable_nearby" , FavoriteConfigString.ENABLE_NEARBY_DEFAULTVALUE );
			boolean is_simple_launcher = getBooleanForJson( configJson , "is_simple_launcher" , FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER_DEFAULTVALUE );
			boolean switch_news_foldable = getBooleanForJson( configJson , "switch_news_foldable" , FavoriteConfigString.ENABLE_NEWS_FOLDABLE_DEFAULTVALUE );
			boolean news_default_expand = getBooleanForJson( configJson , "news_default_expand" , FavoriteConfigString.NEWS_DEFAULT_EXPAND_DEFAULTVALUE );
			boolean switch_enable_favorites_clings = getBooleanForJson(
					configJson ,
					FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS ,
					FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS_DEFAULTVALUE );
			boolean switch_enable_favorites_s5 = getBooleanForJson( configJson , FavoriteConfigString.SWITCH_ENABLE_IS_S5 , FavoriteConfigString.SWITCH_ENABLE_FAVORITES_S5_DEFAULTVALUE );
			boolean switchEnableAdapterVirtualKey = getBooleanForJson(
					configJson ,
					FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY ,
					FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY_DEFAULTVALUE );
			// zhangjin@2016/06/08 ADD START
			String neary_local_path = getStringForJson( configJson , FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH , FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH_DEFAULTVALUE );
			String config_default_browser = getStringForJson( configJson , FavoriteConfigString.CONFIG_DEFAULT_BROWSER_KEY , FavoriteConfigString.CONFIG_DEFAULT_BROWSER_VALUE );
			// zhangjin@2016/06/08 ADD END
			String nearby_ad_place_id = getStringForJson( configJson , FavoriteConfigString.NEARBY_AD_PLACE_ID_KEY , FavoriteConfigString.NEARBY_AD_PLACE_ID_VALUE );
			// jubingcheng@2016/06/15 ADD START
			boolean kuso_use_explorer = getBooleanForJson( configJson , FavoriteConfigString.KUSO_USE_EXPLORER_KEY , SearchConfig.SWITCH_ENABLE_USE_EXPLORER_DEFAULT );
			boolean kuso_show_operate_page = getBooleanForJson( configJson , FavoriteConfigString.KUSO_SHOW_OPERATE_PAGE_KEY , SearchConfig.SWITCH_ENABLE_SHOW_OPERATE_PAGE_DEFAULT );
			String kuso_explorer_package_name = getStringForJson( configJson , FavoriteConfigString.KUSO_EXPLORER_PACKAGE_NAME_KEY , null );
			String kuso_explorer_class_name = getStringForJson( configJson , FavoriteConfigString.KUSO_EXPLORER_CLASS_NAME_KEY , null );
			// jubingcheng@2016/06/15 ADD END
			//zhujieping add start
			String news_ad_place_id = getStringForJson( configJson , FavoriteConfigString.NEWS_AD_PLACE_ID_KEY , FavoriteConfigString.NEWS_AD_PLACE_ID_VALUE );
			//zhujieping add end
			//cheyingkun add start	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
			boolean force_add_layout_change_listener = getBooleanForJson(
					configJson ,
					FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY ,
					FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_DEFAULT_VALUE );
			//cheyingkun add end
			//
			//
			//
			//set switch to assetsConfigMap
			assetsConfigMap = new HashMap<String , Object>();
			assetsConfigMap.put( FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH , switch_enable_show_launcher_search );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH , switch_enable_show_favorite_search );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_COOEE_SEARCH , switch_enable_cooee_search );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_DEV , switch_enable_dev );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_DEBUG , switch_enable_debug );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_NEWS , switch_enable_show_news );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_CONTACTS , switch_enable_contacts );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_APPS , switch_enable_apps );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_NEARBY , switch_enable_nearby );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER , is_simple_launcher );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_NEWS_FOLDABLE , switch_news_foldable );
			assetsConfigMap.put( FavoriteConfigString.NEWS_DEFAULT_EXPAND , news_default_expand );
			assetsConfigMap.put( FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS , switch_enable_favorites_clings );
			assetsConfigMap.put( FavoriteConfigString.SWITCH_ENABLE_IS_S5 , switch_enable_favorites_s5 );
			assetsConfigMap.put( FavoriteConfigString.IS_CUSTOMER_LAUNCHER , isCustomerLauncher );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_UMENG , FavoriteConfigString.ENABLE_UMENG_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY , switchEnableAdapterVirtualKey );
			//以下参数必须包含在桌面传入的数据中,否则显示异常
			assetsConfigMap.put( FavoriteConfigString.LAUNCHER_ICON_SIZEPX , FavoriteConfigString.LAUNCHER_ICON_SIZEPX_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT , FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT_DEFAULTVALUE );
			// zhangjin@2016/06/08 ADD START
			assetsConfigMap.put( FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH , neary_local_path );
			// zhangjin@2016/06/08 ADD END
			assetsConfigMap.put( FavoriteConfigString.CONFIG_DEFAULT_BROWSER_KEY , config_default_browser );
			// jubingcheng@2016/06/15 ADD START
			assetsConfigMap.put( FavoriteConfigString.KUSO_USE_EXPLORER_KEY , kuso_use_explorer );
			assetsConfigMap.put( FavoriteConfigString.KUSO_SHOW_OPERATE_PAGE_KEY , kuso_show_operate_page );
			assetsConfigMap.put( FavoriteConfigString.KUSO_EXPLORER_PACKAGE_NAME_KEY , kuso_explorer_package_name );
			assetsConfigMap.put( FavoriteConfigString.KUSO_EXPLORER_CLASS_NAME_KEY , kuso_explorer_class_name );
			// jubingcheng@2016/06/15 ADD END
			assetsConfigMap.put( FavoriteConfigString.NEARBY_AD_PLACE_ID_KEY , nearby_ad_place_id );
			assetsConfigMap.put( FavoriteConfigString.NEWS_AD_PLACE_ID_KEY , news_ad_place_id );
			assetsConfigMap.put( FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY , force_add_layout_change_listener );//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
		
			//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
			boolean switch_enable_demo_ad_when_on_show = getBooleanForJson( configJson , "switch_enable_demo_ad_when_on_show" , FavoriteConfigString.SWITCH_ENABLE_DEMO_AD_WHEN_ON_SHOW_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_DEMO_AD_WHEN_ON_SHOW , switch_enable_demo_ad_when_on_show );
			//fulijuan add end
		
		}
		catch( Exception e )
		{
			e.printStackTrace();
			//文件不存在,使用默认配置
			Log.d( "" , "cyk set default switch to defaultConfigMap: " );
			//set default switch to defaultConfigMap
			assetsConfigMap = new HashMap<String , Object>();
			assetsConfigMap.put( FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH , FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH , FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_COOEE_SEARCH , FavoriteConfigString.ENABLE_COOEE_SEARCH_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_DEV , FavoriteConfigString.ENABLE_DEV_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_DEBUG , FavoriteConfigString.ENABLE_DEBUG_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_NEWS , FavoriteConfigString.ENABLE_NEWS_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_CONTACTS , FavoriteConfigString.ENABLE_CONTACTS_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_APPS , FavoriteConfigString.ENABLE_APPS_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_NEARBY , FavoriteConfigString.ENABLE_NEARBY_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER , FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_NEWS_FOLDABLE , FavoriteConfigString.ENABLE_NEWS_FOLDABLE_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.NEWS_DEFAULT_EXPAND , FavoriteConfigString.NEWS_DEFAULT_EXPAND_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.IS_CUSTOMER_LAUNCHER , isCustomerLauncher );
			assetsConfigMap.put( FavoriteConfigString.ENABLE_UMENG , FavoriteConfigString.ENABLE_UMENG_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.LAUNCHER_ICON_SIZEPX , FavoriteConfigString.LAUNCHER_ICON_SIZEPX_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT , FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS , FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.SWITCH_ENABLE_IS_S5 , FavoriteConfigString.SWITCH_ENABLE_FAVORITES_S5_DEFAULTVALUE );
			assetsConfigMap.put( FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY , FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY_DEFAULTVALUE );
			// zhangjin@2016/06/08 ADD START
			assetsConfigMap.put( FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH , FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH_DEFAULTVALUE );
			// zhangjin@2016/06/08 ADD END
			assetsConfigMap.put( FavoriteConfigString.CONFIG_DEFAULT_BROWSER_KEY , FavoriteConfigString.CONFIG_DEFAULT_BROWSER_VALUE );
			// jubingcheng@2016/06/15 ADD START
			assetsConfigMap.put( FavoriteConfigString.KUSO_USE_EXPLORER_KEY , SearchConfig.SWITCH_ENABLE_USE_EXPLORER_DEFAULT );
			assetsConfigMap.put( FavoriteConfigString.KUSO_SHOW_OPERATE_PAGE_KEY , SearchConfig.SWITCH_ENABLE_SHOW_OPERATE_PAGE_DEFAULT );
			assetsConfigMap.put( FavoriteConfigString.KUSO_EXPLORER_PACKAGE_NAME_KEY , null );
			assetsConfigMap.put( FavoriteConfigString.KUSO_EXPLORER_CLASS_NAME_KEY , null );
			// jubingcheng@2016/06/15 ADD END
			assetsConfigMap.put( FavoriteConfigString.NEARBY_AD_PLACE_ID_KEY , FavoriteConfigString.NEARBY_AD_PLACE_ID_VALUE );
			assetsConfigMap.put( FavoriteConfigString.NEWS_AD_PLACE_ID_KEY , FavoriteConfigString.NEWS_AD_PLACE_ID_VALUE );
			assetsConfigMap.put( FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY , FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_DEFAULT_VALUE );//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
			assetsConfigMap.put( FavoriteConfigString.ENABLE_DEMO_AD_WHEN_ON_SHOW , FavoriteConfigString.SWITCH_ENABLE_DEMO_AD_WHEN_ON_SHOW_DEFAULTVALUE );//fulijuan add		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
		}
	}
	
	private JSONObject getConfigForLauncherAssets(
			Context context ,
			String fileName )
	{
		AssetManager assetManager = context.getAssets();
		InputStream inputStream = null;
		try
		{
			inputStream = assetManager.open( fileName );
			String config = readTextFile( inputStream );
			JSONObject jObject;
			try
			{
				jObject = new JSONObject( config );
				return jObject;
			}
			catch( JSONException e1 )
			{
				e1.printStackTrace();
			}
		}
		catch( IOException e )
		{
			Log.e( "tag" , e.getMessage() );
		}
		return null;
	}
	
	private String readTextFile(
			InputStream inputStream )
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte buf[] = new byte[1024];
		int len;
		try
		{
			while( ( len = inputStream.read( buf ) ) != -1 )
			{
				outputStream.write( buf , 0 , len );
			}
			outputStream.close();
			inputStream.close();
		}
		catch( IOException e )
		{
		}
		return outputStream.toString();
	}
	
	/**
	 * 初始化config
	 */
	private FavoritesConfig initConfig(
			HashMap<String , Object> launcherConfigMap )
	{
		FavoritesConfig config = new FavoritesConfig( containerContext );
		config.putInt( FavoriteConfigString.HOST_VERSION_CODE , Version.HOST_VERSION_CODE );//cheyingkun add	//酷生活编辑失败,初始化时传递host版本给酷生活
		//整合map里的值
		//launcherConfigMap不为空,launcherConfigMap里的值全部复制给assetsConfigMap
		if( launcherConfigMap != null && launcherConfigMap.size() > 0 )
		{
			Set<String> keySet = launcherConfigMap.keySet();
			for( String key : keySet )
			{
				//优先获取launcherConfigMap里的值
				Object value = launcherConfigMap.get( key );
				//如果launcherConfigMap里有,则覆盖assetsConfigMap原来的值
				if( value != null && assetsConfigMap != null )
				{
					assetsConfigMap.put( key , value );
				}
			}
		}
		//assetsConfigMap不为空,使用assetsConfigMap初始化config
		if( assetsConfigMap != null && assetsConfigMap.size() > 0 )
		{
			//
			//
			//
			//get switch form defaultConfigMap
			boolean switch_enable_show_launcher_search = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH ) ) );
			boolean switch_enable_show_favorites_search = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH ) ) );
			boolean switch_enable_cooee_search = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_COOEE_SEARCH ) ) );
			boolean switch_enable_dev = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_DEV ) ) );
			boolean switch_enable_debug = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_DEBUG ) ) );
			boolean switch_enable_show_news = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_NEWS ) ) );
			boolean switch_enable_contacts = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_CONTACTS ) ) );
			boolean switch_enable_apps = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_APPS ) ) );
			boolean switch_enable_nearby = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_NEARBY ) ) );
			boolean is_simple_launcher = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER ) ) );
			boolean switch_news_foldable = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_NEWS_FOLDABLE ) ) );
			boolean news_default_expand = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.NEWS_DEFAULT_EXPAND ) ) );
			boolean isCustomerLauncher = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.IS_CUSTOMER_LAUNCHER ) ) );
			boolean enable_umeng = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_UMENG ) ) );
			int launcherIconSizePx = Integer.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.LAUNCHER_ICON_SIZEPX ) ) );
			int launcherSearchBarHeight = Integer.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT ) ) );
			boolean enable_clings = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS ) ) );
			boolean enable_s5 = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.SWITCH_ENABLE_IS_S5 ) ) );
			boolean switch_enable_adapter_virtualkey = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY ) ) );
			// zhangjin@2016/06/08 ADD START
			String neary_local_path = String.valueOf( assetsConfigMap.get( FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH ) );
			// zhangjin@2016/06/08 ADD END
			// lvjiangb@2016/06/14 ADD START
			String config_default_browser = String.valueOf( assetsConfigMap.get( FavoriteConfigString.CONFIG_DEFAULT_BROWSER_KEY ) );
			// lvjiangb@2016/06/14 ADD END
			String nearby_ad_place_id = String.valueOf( assetsConfigMap.get( FavoriteConfigString.NEARBY_AD_PLACE_ID_KEY ) );
			//zhujieping add start
			String news_ad_place_id = String.valueOf( assetsConfigMap.get( FavoriteConfigString.NEWS_AD_PLACE_ID_KEY ) );
			//zhujieping add end
			boolean force_add_layout_change_listener = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY ) ) );//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
			if( isCustomerLauncher )
			{
				// jubingcheng@2016/06/15 ADD START
				boolean kuso_use_explorer = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.KUSO_USE_EXPLORER_KEY ) ) );
				boolean kuso_show_operate_page = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.KUSO_SHOW_OPERATE_PAGE_KEY ) ) );
				String kuso_explorer_package_name = assetsConfigMap.get( FavoriteConfigString.KUSO_EXPLORER_PACKAGE_NAME_KEY ) == null ? null : String.valueOf( assetsConfigMap
						.get( FavoriteConfigString.KUSO_EXPLORER_PACKAGE_NAME_KEY ) );
				String kuso_explorer_class_name = assetsConfigMap.get( FavoriteConfigString.KUSO_EXPLORER_CLASS_NAME_KEY ) == null ? null : String.valueOf( assetsConfigMap
						.get( FavoriteConfigString.KUSO_EXPLORER_CLASS_NAME_KEY ) );
				// jubingcheng@2016/06/15 ADD END
				SearchActivityManager.initSearchConfig( switch_enable_show_launcher_search , switch_enable_show_favorites_search , switch_enable_cooee_search , false , false );
				SearchActivityManager.setEnableUseExplorerDefault( kuso_use_explorer );
				SearchActivityManager.setEnableShowOperatePageDefault( kuso_show_operate_page );
				SearchActivityManager.setDefaultExplorer( kuso_explorer_package_name , kuso_explorer_class_name );
			}
			boolean enableShowLauncherSearch = isCustomerLauncher ? switch_enable_show_launcher_search : SearchHelper.getInstance( containerContext ).enableShowCommonPageSearch();
			boolean enableShowFavoriteSearch = SearchHelper.getInstance( containerContext ).enableShowFavoritesPageSearch();
			//
			//
			//
			//
			//set switch to config
			config.putBoolean( FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH , enableShowLauncherSearch );
			config.putBoolean( FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH , enableShowFavoriteSearch );
			config.putBoolean( FavoriteConfigString.ENABLE_FAVORITES_SEARCH , enableShowFavoriteSearch && !enableShowLauncherSearch );
			config.putBoolean( FavoriteConfigString.ENABLE_COOEE_SEARCH , SearchHelper.getInstance( containerContext ).enableCooeeSearch() );
			config.putBoolean( FavoriteConfigString.ENABLE_DEV , switch_enable_dev );
			config.putBoolean( FavoriteConfigString.ENABLE_DEBUG , switch_enable_debug );
			config.putBoolean( FavoriteConfigString.ENABLE_NEWS , switch_enable_show_news );
			config.putBoolean( FavoriteConfigString.ENABLE_CONTACTS , switch_enable_contacts );
			config.putBoolean( FavoriteConfigString.ENABLE_APPS , switch_enable_apps );
			config.putBoolean( FavoriteConfigString.ENABLE_NEARBY , switch_enable_nearby );
			config.putBoolean( FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER , is_simple_launcher );
			config.putBoolean( FavoriteConfigString.ENABLE_NEWS_FOLDABLE , switch_news_foldable );
			config.putBoolean( FavoriteConfigString.NEWS_DEFAULT_EXPAND , news_default_expand );
			config.putBoolean( FavoriteConfigString.IS_CUSTOMER_LAUNCHER , isCustomerLauncher );
			config.putBoolean( FavoriteConfigString.ENABLE_UMENG , enable_umeng );
			config.putInt( FavoriteConfigString.LAUNCHER_ICON_SIZEPX , launcherIconSizePx );
			config.putInt( FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT , launcherSearchBarHeight );
			config.putBoolean( FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS , enable_clings );
			config.putBoolean( FavoriteConfigString.SWITCH_ENABLE_IS_S5 , enable_s5 );
			config.putBoolean( FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY , switch_enable_adapter_virtualkey );
			config.putString( FavoriteConfigString.CONFIG_DEFAULT_BROWSER_KEY , config_default_browser );
			// zhangjin@2016/06/08 ADD START
			config.putString( FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH , neary_local_path );
			// zhangjin@2016/06/08 ADD END
			config.putString( FavoriteConfigString.NEARBY_AD_PLACE_ID_KEY , nearby_ad_place_id );
			config.putString( FavoriteConfigString.NEWS_AD_PLACE_ID_KEY , news_ad_place_id );
			config.putBoolean( FavoriteConfigString.FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY , force_add_layout_change_listener );//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
		//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
			boolean switch_enable_demo_ad_when_on_show = Boolean.valueOf( String.valueOf( assetsConfigMap.get( FavoriteConfigString.ENABLE_DEMO_AD_WHEN_ON_SHOW ) ) );
			config.putBoolean( FavoriteConfigString.ENABLE_DEMO_AD_WHEN_ON_SHOW , switch_enable_demo_ad_when_on_show );
		//fulijuan add end
		}
		return config;
	}
	
	private void initByFavoritesConfig()
	{
		Log.d( "" , "cyk init FavoritesConfig 0: " );
		if( mInfo != null )
		{
			return;
		}
		mInfo = new WidgetPageInfo();
		mInfo.mPackageName = containerContext.getPackageName();
		mInfo.mRemoteContext = containerContext;
		//		mInfo.mPackageName = REMOTE_PACKAGE_NAME;
		//		mInfo.mRemoteContext = newWidgetContext( containerContext , mInfo.mPackageName );
		if( mInfo.mRemoteContext == null )
		{
			Log.e( TAG , "WidgetPageManager mRemoteContext is null" );
			mInfo = null;
			return;
		}
		createClassAndInstance( containerContext , REMOTE_CLASS_NAME );
		//		mRootView = createView( mInfo.mRemoteContext , mInfo.mPackageName , "page_main" );
		if( mInfo.mUtilsInstance == null )
		{
			Log.e( TAG , "WidgetPageManager mRootView is null" );
			mInfo = null;
			return;
		}
		WidgetPageInfo info = mInfo;
		if( info != null && config != null )
		{
			info.mUtilsInstance.setup( config );
		}
		Log.d( "" , "cyk init FavoritesConfig 1: " );
	}
	
	//cheyingkun add end
	//cheyingkun del start	//酷生活代码优化。（改为桌面传FavoritesConfig过来）
	//	private static Context newWidgetContext(
	//			Context context ,
	//			String packageName )
	//	{
	//		int contextPermission = Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY;
	//		Context theirContext = null;
	//		try
	//		{
	//			theirContext = context.createPackageContext( packageName , contextPermission );
	//		}
	//		catch( NameNotFoundException e )
	//		{
	//			e.printStackTrace();
	//		}
	//		return theirContext;
	//	}
	//cheyingkun add end
	/**
	 * 创建localClass和instance
	 * @param context
	 */
	private void createClassAndInstance(
			Context context ,
			String className )
	{
		try
		{
			//			Class<?> localClass = mInfo.mRemoteContext.getClassLoader().loadClass( className );
			//			Object instance = null;
			//			mInfo.mUtilsClass = localClass;
			//			Method m = null;
			//			m = localClass.getMethod( "getInstance" , new Class[]{ Context.class , Context.class } );
			//			if( m != null )
			//			{
			//				instance = m.invoke( null , new Object[]{ context , mInfo.mRemoteContext } );
			//			}
			//			mInfo.mUtilsInstance = (IFavorites)instance;
			FavoritesHost mFavoritesHost = FavoritesHost.getInstance( containerContext , proxyContext );
			mInfo.mUtilsInstance = mFavoritesHost;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	//	public static View createView(
	//			Context remoteContext ,
	//			String packagename ,
	//			String resource )
	//	{
	//		Context theirContext = remoteContext;
	//		if( theirContext == null )
	//		{
	//			return null;
	//		}
	//		LayoutInflater theirInflater = (LayoutInflater)theirContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	//		theirInflater = theirInflater.cloneInContext( theirContext );
	//		Resources r = theirContext.getResources();
	//		int id = 0;
	//		id = r.getIdentifier( resource , "layout" , packagename );
	//		if( id == 0 )
	//		{
	//			Log.e( TAG , "ERROR! can't get root layout id." );
	//			return null;
	//		}
	//		View v = null;
	//		try
	//		{
	//			v = theirInflater.inflate( id , null );
	//		}
	//		catch( Exception e )
	//		{
	//			e.printStackTrace();
	//		}
	//		if( v != null )
	//		{
	//			ItemInfo info = new ItemInfo();
	//			v.setTag( info );
	//		}
	//		return v;
	//	}
	public void onPause()
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				//				m = info.mUtilsClass.getDeclaredMethod( "onPause" );
				//				if( m != null )
				//				{
				//					m.invoke( info.mUtilsInstance );
				//				}
				info.mUtilsInstance.onPause();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void onResume()
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				//				m = info.mUtilsClass.getDeclaredMethod( "onResume" );
				//				if( m != null )
				//				{
				//					m.invoke( info.mUtilsInstance );
				//				}
				info.mUtilsInstance.onResume();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void setAllApp(
			final HashMap<ComponentName , Bitmap> map )
	{
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				try
				{
					//			Method m = null;
					WidgetPageInfo info = mInfo;
					if( info != null//
							&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
					)
					{
						List listKey = new ArrayList();
						List listValue = new ArrayList();
						mapToList( map , listKey , listValue );
						info.mUtilsInstance.setAllApp( listKey , listValue );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
	}
	
	public void reLoadAndBindApps(
			final HashMap<ComponentName , Bitmap> map )
	{
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				try
				{
					//			Method m = null;
					WidgetPageInfo info = mInfo;
					if( info != null//
							&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
					)
					{
						List listKey = new ArrayList();
						List listValue = new ArrayList();
						mapToList( map , listKey , listValue );
						info.mUtilsInstance.reLoadAndBindApps( listKey , listValue );
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
	}
	
	public void onPageBeginMoving()
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				//				m = info.mUtilsClass.getDeclaredMethod( "onPageBeginMoving" );
				//				if( m != null )
				//				{
				//					m.invoke( info.mUtilsInstance );
				//				}
				info.mUtilsInstance.onPageBeginMoving();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public View getView()
	{
		View view = null;
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				FrameLayout frame = new FrameLayout( containerContext );
				view = info.mUtilsInstance.getView();
				//cheyingkun add start	//解决“智能分类后，酷生活显示空白”的问题。【i_0013672】
				if( view != null )
				{
					ViewGroup parent = (ViewGroup)view.getParent();
					if( parent != null )
					{
						parent.removeView( view );
					}
				}
				frame.addView( view );
				return frame;
			}
			else
			{
				FrameLayout frame = new FrameLayout( containerContext );
				view = LayoutInflater.from( containerContext ).inflate( R.layout.favoriteshot_loading_plugin , null );
				frame.addView( view );
				frame.setId( LOADING_VIEW_ID );
				return frame;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void removeApps(
			ArrayList<String> list )
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				//				Log.v( "lvjiangbin" , "setAllAppsetAllApp info.mPackageName = " + info.mPackageName );
				//				m = info.mUtilsClass.getDeclaredMethod( "removeApps" , ArrayList.class );
				//				if( m != null )
				//				{
				//					m.invoke( info.mUtilsInstance , list );
				//				}
				info.mUtilsInstance.removeApps( list );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void onShow()
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				//				m = info.mUtilsClass.getDeclaredMethod( "onShow" );
				//				if( m != null )
				//				{
				//					m.invoke( info.mUtilsInstance );
				//				}
				info.mUtilsInstance.onShow();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void onHide()
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				//				m = info.mUtilsClass.getDeclaredMethod( "onHide" );
				//				if( m != null )
				//				{
				//					m.invoke( info.mUtilsInstance );
				//				}
				info.mUtilsInstance.onHide();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void onBackPressed()
	{
		WidgetPageInfo info = mInfo;
		if( info != null//
				&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
		)
		{
			info.mUtilsInstance.onBackPressed();
		}
	}
	
	//cheyingkun add start	//酷生活代码优化。（改为桌面传FavoritesConfig过来）
	private static void mapToList(
			HashMap<ComponentName , Bitmap> source ,
			List key ,
			List listValue )
	{
		Iterator it = source.keySet().iterator();
		while( it.hasNext() )
		{
			Object k = it.next();
			key.add( k );
			listValue.add( source.get( k ) );
		}
	}
	
	//cheyingkun add end	//酷生活代码优化。（改为桌面传FavoritesConfig过来）
	public int getIntForJson(
			JSONObject json ,
			String key ,
			int defValue )
	{
		if( json == null )
		{
			return defValue;
		}
		int integer;
		try
		{
			integer = json.getInt( key );
		}
		catch( JSONException e )
		{
			return defValue;
		}
		return integer;
	}
	
	public boolean getBooleanForJson(
			JSONObject json ,
			String key ,
			boolean defValue )
	{
		boolean bool;
		try
		{
			bool = json.getBoolean( key );
		}
		catch( JSONException e )
		{
			return defValue;
		}
		return bool;
	}
	
	// zhangjin@2016/06/08 ADD START
	public String getStringForJson(
			JSONObject json ,
			String key ,
			String defValue )
	{
		String str;
		try
		{
			str = json.getString( key );
		}
		catch( JSONException e )
		{
			return defValue;
		}
		return str;
	}
	
	// zhangjin@2016/06/08 ADD END
	public void onConfigurationChanged()//切换语言，重新加载
	{
		if( mInfo != null )
		{
			Method m = null;
			try
			{
				m = FavoritesHost.class.getMethod( "onConfigurationChanged" , Configuration.class );
				if( mInfo.mUtilsInstance != null )
				{
					m.invoke( mInfo.mUtilsInstance , containerContext.getResources().getConfiguration() );
				}
			}
			catch( NoSuchMethodException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( IllegalArgumentException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( InvocationTargetException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setFavoritesGetDataCallBack(
			final IFavoritesGetData favoritesGetData )
	{
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "lvjangbin" , "favoritesPageManager  setFavoritesGetDataCallBack" );
				WidgetPageInfo info = mInfo;
				if( info != null && info.mUtilsInstance != null )
				{
					info.mUtilsInstance.setFavoritesGetDataCallBack( favoritesGetData );
				}
			}
		} );
	}
	
	public void setIconSize(
			final int iconSize )
	{
		// 主题变化，icon的大小随主题变化
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "lvjangbin" , "init favorites" );
				WidgetPageInfo info = mInfo;
				if( info != null//
						&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
				)
				{
					info.mUtilsInstance.setIconSize( iconSize );
				}
			}
		} );
	}
	
	//cheyingkun add start	//酷生活引导页
	public void setFavoriteClingsCallBack(
			final IFavoriteClings favoriteClings )
	{
		singleThreadExecutor.execute( new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "lvjangbin" , "favoritesPageManager  setFavoriteClingsCallBack" );
				WidgetPageInfo info = mInfo;
				if( info != null && info.mUtilsInstance != null )
				{
					info.mUtilsInstance.setIFavoriteClingsCallBack( favoriteClings );
				}
			}
		} );
	}
	
	public boolean isShowFavoriteClings()
	{
		WidgetPageInfo info = mInfo;
		if( info != null && info.mUtilsInstance != null )
		{
			return info.mUtilsInstance.isShowFavoriteClings();
		}
		else
		{
			return false;
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	public void clearFavoritesView()
	{
		WidgetPageInfo info = mInfo;
		if( info != null//
				&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
		)
		{
			info.mUtilsInstance.clearFavoritesView();
			mInfo = null;
			assetsConfigMap.clear();
			assetsConfigMap = null;
			containerContext = null;
			proxyContext = null;
			config = null;
			instance = null;
			isLoadFavoritesFinish = false;
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	public void updateFavoritesAppsIcon(
			HashMap<ComponentName , Bitmap> map )
	{
		try
		{
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				List listKey = new ArrayList();
				List listValue = new ArrayList();
				mapToList( map , listKey , listValue );
				info.mUtilsInstance.updateFavoritesAppsIcon( listKey , listValue );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	//cheyingkun add end
	;
	
	//zhujieping add start,返回-1屏的状态，0是cooee新闻的折叠状态，1是cooee新闻的展开状态，2是sohu新闻的折叠状态，3是sohu新闻的展开状态。-1表示异常
	public int getFavoritesState()
	{
		try
		{
			//			Method m = null;
			WidgetPageInfo info = mInfo;
			if( info != null//
					&& isLoadFavoritesFinish//cheyingkun add	//解决“桌面双层模式，安装后立即进入主菜单并返回，桌面重启”的问题【c_0004500】
			)
			{
				return info.mUtilsInstance.getFavoriteState();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return -1;
	}
	//zhujieping add end
	;
	
	public boolean isInitFinish()
	{
		return isLoadFavoritesFinish;
	}
	
	//xiatian add start	//添加初始化方法“initImmediatelyAndNoLoadingView”:1、立刻初始化；2、不显示loading界面。
	public void initImmediatelyAndNoLoadingView(
			final HashMap<String , Object> launcherConfigMap ,
			final boolean isCustomerLauncher )
	{
		Log.d( "" , "cyk isCustomerLauncher: " + isCustomerLauncher );
		if( mInfo != null )
		{
			return;
		}
		Log.v( "lvjangbin" , "initImmediatelyAndNoLoadingView favorites" );
		FavoritesPageManager.isLoadFavoritesFinish = true;//NoLoadingView
		//初始化map文件
		initConfigMap( isCustomerLauncher );
		//根据map初始化config
		config = initConfig( launcherConfigMap );
		//参数
		initByFavoritesConfig();
	}
	//xiatian add end
}
