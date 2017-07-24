package com.cooee.favorites;


import com.cooee.favorites.manager.FavoritesManager;


/**读取config时，为了方便修改定义静态字符串。
 * 需要和host里的字符串统一
 * 为了防止插件和桌面依赖，分开写
 * */
public class FavoriteConfigString
{
	
	//FavoriteConfig key start
	/**酷搜*/
	private static final String ENABLE_COOEE_SEARCH = "enableCooeeSearch";
	/**酷生活新闻*/
	private static final String ENABLE_NEWS = "enableNews";
	/**酷生活联系人*/
	private static final String ENABLE_CONTACTS = "enableContacts";
	/**酷生活常用应用*/
	private static final String ENABLE_APPS = "enableApps";
	/**路生活附近*/
	private static final String ENABLE_NEARBY = "enableNearby";
	/**debug模式*/
	private static final String ENABLE_DEBUG = "enableDebug";
	/**dev模式*/
	private static final String ENABLE_DEV = "enableDev";
	/**是否是老人桌面*/
	private static final String ENABLE_SIMPLE_LAUNCHER = "enableSimpleLauncher";
	/**启用友盟统计*/
	private static final String ENABLE_UMENG = "enableUmeng";
	/**-1屏是否显示自己的搜索(加载)*/
	private static final String ENABLE_FAVORITES_SEARCH = "isSearchEnable";
	/**桌面是否显示搜索*/
	private static final String ENABLE_SHOW_LAUNCHER_SEARCH = "enableShowLauncherSearch";
	/**-1屏是否显示搜索*/
	private static final String ENABLE_SHOW_FAVORITES_SEARCH = "enableShowFavoritesSearch";
	/**桌面搜索栏高度*/
	private static final String LAUNCHER_SEARCHBAR_HEIGHT = "launcherSearchBarHeight";
	/**桌面图标大小*/
	private static final String LAUNCHER_ICON_SIZEPX = "launcherIconSizePx";
	/**是否是客户项目*/
	private static final String IS_CUSTOMER_LAUNCHER = "isCustomerLauncher";
	/**新闻是否可折叠*/
	private static final String ENABLE_NEWS_FOLDABLE = "newsfoldable";
	/**新闻可折叠时，默认状态*/
	private static final String NEWS_DEFAULT_EXPAND = "newsexpand";
	/**酷生活引导页key*/
	private static final String SWITCH_ENABLE_FAVORITES_CLINGS = "switch_enable_favorites_clings";
	/**酷生活S5模式*/
	private static final String SWITCH_ENABLE_IS_S5 = "switch_enable_favorites_s5";
	/**酷生活是否适配虚拟按键*/
	private static final String SWITCH_ENABLE_ADAPTER_VIRTUALKEY = "switch_enable_adapter_virtualkey";
	// zhangjin@2016/06/08 ADD START
	/**酷生活附近几个图标，本地配置文件路径 */
	private static final String CONFIG_NEARBY_LOCAL_PATH = "config_nearby_local_path";
	// zhangjin@2016/06/08 ADD END
	//lvjiangbin 20160614 ADD START
	private static final String CONFIG_DEFAULT_BROWSER_KEY = "config_default_browser";
	//lvjiangbin 20160614 ADD END
	/*酷生活服务四个广告位配置*/
	private static final String NEARBY_AD_PLACE_ID_KEY = "nearby_ad_place_id";
	//FavoriteConfig key end
	private static final String HOST_VERSION_CODE = "host_version_code";//cheyingkun add	//酷生活编辑失败,初始化时传递host版本给酷生活
	/**强行添加layout监听窗口的key*/
	public static final String FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY = "force_add_layout_change_listener";//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
	//
	//
	//
	//
	//FavoriteConfig defaultValue start
	/**酷搜*/
	private static final boolean ENABLE_COOEE_SEARCH_DEFAULTVALUE = true;
	/**酷生活新闻*/
	private static final boolean ENABLE_NEWS_DEFAULTVALUE = true;
	/**酷生活联系人*/
	private static final boolean ENABLE_CONTACTS_DEFAULTVALUE = true;
	/**酷生活常用应用*/
	private static final boolean ENABLE_APPS_DEFAULTVALUE = true;
	/**酷生活附近*/
	private static final boolean ENABLE_NEARBY_DEFAULTVALUE = true;
	/**debug模式*/
	private static final boolean ENABLE_DEBUG_DEFAULTVALUE = false;
	/**dev模式*/
	private static final boolean ENABLE_DEV_DEFAULTVALUE = false;
	/**是否是老人桌面*/
	private static final boolean ENABLE_SIMPLE_LAUNCHER_DEFAULTVALUE = false;
	/**启用友盟统计*/
	private static final boolean ENABLE_UMENG_DEFAULTVALUE = true;
	/**-1屏是否显示自己的搜索(加载)*/
	private static final boolean ENABLE_FAVORITES_SEARCH_DEFAULTVALUE = false;
	/**桌面是否显示搜索*/
	private static final boolean ENABLE_SHOW_LAUNCHER_SEARCH_DEFAULTVALUE = true;
	/**-1屏是否显示搜索*/
	private static final boolean ENABLE_SHOW_FAVORITES_SEARCH_DEFAULTVALUE = true;
	/**桌面搜索栏高度*/
	private static final int LAUNCHER_SEARCHBAR_HEIGHT_DEFAULTVALUE = 96;
	/**桌面图标大小*/
	private static final int LAUNCHER_ICON_SIZEPX_DEFAULTVALUE = 96;
	/**是否是客户项目*/
	private static final boolean IS_CUSTOMER_LAUNCHER_DEFAULTVALUE = false;
	/**新闻是否可折叠*/
	private static final boolean ENABLE_NEWS_FOLDABLE_DEFAULTVALUE = false;
	/**新闻可折叠时，默认状态*/
	private static final boolean NEWS_DEFAULT_EXPAND_DEFAULTVALUE = true;
	/**酷生活引导页defaultVaule*/
	private static final boolean SWITCH_ENABLE_FAVORITES_CLINGS_DEFAULTVALUE = false;
	/**酷生活是否S5模式  默认不是false*/
	private static final boolean SWITCH_ENABLE_FAVORITES_S5_DEFAULTVALUE = false;
	/**酷生活新闻类型的默认值*/
	private static final int CONFIG_NEWS_TYPE_CLOSE = -1;
	private static final int CONFIG_NEWS_TYPE_COOEE = 0;
	private static final int CONFIG_NEWS_TYPE_DEFAULTVALUE = CONFIG_NEWS_TYPE_COOEE;
	/**酷生活是否适配虚拟按键默认值true*/
	private static final boolean SWITCH_ENABLE_ADAPTER_VIRTUALKEY_DEFAULTVALUE = true;
	//FavoriteConfig defaultValue end
	// zhangjin@2016/06/08 ADD START
	private static final String CONFIG_NEARBY_LOCAL_PATH_DEFAULTVALUE = "/system/launcher/";
	// zhangjin@2016/06/08 ADD END
	//lvjiangbin 20160614 ADD START
	private static final String CONFIG_DEFAULT_BROWSER_VALUE = "";
	//lvjiangbin 20160614 ADD END
	/*酷生活服务四个广告位配置*/
	private static final String NEARBY_AD_PLACE_ID_VALUE = "";
	// jubingcheng@2016/06/15 ADD START
	/**酷搜：搜索结果默认打开方式 true:浏览器 false:webview*/
	private static final String KUSO_USE_EXPLORER_KEY = "kuso_use_explorer";
	/**酷搜：默认是否显示运营页 true:显示 false:不显示*/
	private static final String KUSO_SHOW_OPERATE_PAGE_KEY = "kuso_show_operate_page";
	/**酷搜：用浏览器打开搜索结果时指定浏览器的包名*/
	private static final String KUSO_EXPLORER_PACKAGE_NAME_KEY = "kuso_explorer_package_name";
	/**酷搜：用浏览器打开搜索结果时指定浏览器的类名*/
	private static final String KUSO_EXPLORER_CLASS_NAME_KEY = "kuso_explorer_class_name";
	// jubingcheng@2016/06/15 ADD END
	/*酷生活新闻广告配置*/
	private static final String NEWS_AD_PLACE_ID_KEY = "news_ad_place_id";
	/*酷生活新闻广告配置值*/
	private static final String NEWS_AD_PLACE_ID_VALUE = "";
	private static final int HOST_VERSION_CODE_VALUE = 0;//cheyingkun add	//酷生活编辑失败,初始化时传递host版本给酷生活
	/**强行添加layout监听窗口的默认值*/
	public static final boolean FORCE_ADD_LAYOUT_CHANGE_LISTENER_DEFAULT_VALUE = false;//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
	
	//fulijuan add start    //需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
	/**酷生活一级界面开屏广告配置*/
	private static final String ENABLE_DEMO_AD_WHEN_ON_SHOW = "enableDemoAdWhenOnShow";
	/**酷生活一级界面开屏广告配置的开关  默认false*/
	private static final boolean SWITCH_ENABLE_DEMO_AD_WHEN_ON_SHOW_DEFAULTVALUE = false;
	//fulijuan add end
	public static String getEnableCooeeSearchKey()
	{
		if( FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41706 ) )//41701的下一个版本支持该功能
		{
			return FavoriteConfigString.ENABLE_COOEE_SEARCH;
		}
		else
		{
			return "enable_cooee_search";
		}
	}
	
	public static String getEnableNewsKey()
	{
		if( FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41706 ) )
		{
			return FavoriteConfigString.ENABLE_NEWS;
		}
		else
		{
			return "enable_news";
		}
	}
	
	public static String getEnableContactsKey()
	{
		if( FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41706 ) )
		{
			return FavoriteConfigString.ENABLE_CONTACTS;
		}
		else
		{
			return "enable_contacts";
		}
	}
	
	public static String getEnableAppsKey()
	{
		if( FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41706 ) )
		{
			return FavoriteConfigString.ENABLE_APPS;
		}
		else
		{
			return "enable_apps";
		}
	}
	
	public static String getEnableNearbyKey()
	{
		if( FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41706 ) )
		{
			return FavoriteConfigString.ENABLE_NEARBY;
		}
		else
		{
			return "enable_nearby";
		}
	}
	
	public static String getEnableDebugKey()
	{
		if( FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41706 ) )
		{
			return FavoriteConfigString.ENABLE_DEBUG;
		}
		else
		{
			return "enable_debug";
		}
	}
	
	public static String getEnableDevKey()
	{
		return FavoriteConfigString.ENABLE_DEV;
	}
	
	public static String getEnableSimpleLauncherKey()
	{
		return FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER;
	}
	
	public static String getEnableUmengKey()
	{
		return FavoriteConfigString.ENABLE_UMENG;
	}
	
	public static String getEnableFavoritesSearchKey()
	{
		return FavoriteConfigString.ENABLE_FAVORITES_SEARCH;
	}
	
	public static String getEnableShowLauncherSearchKey()
	{
		return FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH;
	}
	
	public static String getEnableShowFavoritesSearchKey()
	{
		return FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH;
	}
	
	public static String getLauncherSearchBarHeightKey()
	{
		return FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT;
	}
	
	public static String getLauncherIconSizePxKey()
	{
		return FavoriteConfigString.LAUNCHER_ICON_SIZEPX;
	}
	
	public static String getIsCustomerLauncherKey()
	{
		return FavoriteConfigString.IS_CUSTOMER_LAUNCHER;
	}
	
	public static String getEnableNewsFoldableKey()
	{
		return FavoriteConfigString.ENABLE_NEWS_FOLDABLE;
	}
	
	public static String getNewsDefaultExpandKey()
	{
		return FavoriteConfigString.NEWS_DEFAULT_EXPAND;
	}
	
	public static String getEnableFavoritesClingsKey()
	{
		return FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS;
	}
	
	public static String getEnableIsS5Key()
	{
		return FavoriteConfigString.SWITCH_ENABLE_IS_S5;
	}
	
	public static String getEnableAdapterVirtualkey()
	{
		return FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY;
	}
	
	public static String getNearbyLocalPathKey()
	{
		return FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH;
	}
	
	public static String getDefaultBrowserKey()
	{
		return FavoriteConfigString.CONFIG_DEFAULT_BROWSER_KEY;
	}
	
	public static String getNearbyAdPlaceIdKey()
	{
		return FavoriteConfigString.NEARBY_AD_PLACE_ID_KEY;
	}
	
	public static String getHostVersionCodeKey()
	{
		return FavoriteConfigString.HOST_VERSION_CODE;
	}
	
	public static boolean isEnableCooeeSearchDefaultValue()
	{
		return FavoriteConfigString.ENABLE_COOEE_SEARCH_DEFAULTVALUE;
	}
	
	public static boolean isEnableNewsDefaultValue()
	{
		return FavoriteConfigString.ENABLE_NEWS_DEFAULTVALUE;
	}
	
	public static boolean isEnableContactsDefaultValue()
	{
		return FavoriteConfigString.ENABLE_CONTACTS_DEFAULTVALUE;
	}
	
	public static boolean isEnableAppsDefaultValue()
	{
		return FavoriteConfigString.ENABLE_APPS_DEFAULTVALUE;
	}
	
	public static boolean isEnableNearbyDefaultValue()
	{
		return FavoriteConfigString.ENABLE_NEARBY_DEFAULTVALUE;
	}
	
	public static boolean isEnableDebugDefaultValue()
	{
		return FavoriteConfigString.ENABLE_DEBUG_DEFAULTVALUE;
	}
	
	public static boolean isEnableDevDefaultValue()
	{
		return FavoriteConfigString.ENABLE_DEV_DEFAULTVALUE;
	}
	
	public static boolean isEnableSimpleLauncherDefaultValue()
	{
		return FavoriteConfigString.ENABLE_SIMPLE_LAUNCHER_DEFAULTVALUE;
	}
	
	public static boolean isEnableUmengDefaultValue()
	{
		return FavoriteConfigString.ENABLE_UMENG_DEFAULTVALUE;
	}
	
	public static boolean isEnableFavoritesSearchDefaultValue()
	{
		return FavoriteConfigString.ENABLE_FAVORITES_SEARCH_DEFAULTVALUE;
	}
	
	public static boolean isEnableShowLauncherSearchDefaultValue()
	{
		return FavoriteConfigString.ENABLE_SHOW_LAUNCHER_SEARCH_DEFAULTVALUE;
	}
	
	public static boolean isEnableShowFavoritesSearchDefaultValue()
	{
		return FavoriteConfigString.ENABLE_SHOW_FAVORITES_SEARCH_DEFAULTVALUE;
	}
	
	public static int getLauncherSearchBarHeightDefaultValue()
	{
		return FavoriteConfigString.LAUNCHER_SEARCHBAR_HEIGHT_DEFAULTVALUE;
	}
	
	public static int getLauncherIconSizePxDefaultValue()
	{
		return FavoriteConfigString.LAUNCHER_ICON_SIZEPX_DEFAULTVALUE;
	}
	
	public static boolean isCustomerLauncherDefaultValue()
	{
		return FavoriteConfigString.IS_CUSTOMER_LAUNCHER_DEFAULTVALUE;
	}
	
	public static boolean isEnableNewsFoldableDefaultValue()
	{
		return FavoriteConfigString.ENABLE_NEWS_FOLDABLE_DEFAULTVALUE;
	}
	
	public static boolean isNewsDefaultExpandDefaultValue()
	{
		return FavoriteConfigString.NEWS_DEFAULT_EXPAND_DEFAULTVALUE;
	}
	
	public static boolean isEnableFavoritesClingsDefaultValue()
	{
		return FavoriteConfigString.SWITCH_ENABLE_FAVORITES_CLINGS_DEFAULTVALUE;
	}
	
	public static boolean isEnableFavoritesS5DefaultValue()
	{
		return FavoriteConfigString.SWITCH_ENABLE_FAVORITES_S5_DEFAULTVALUE;
	}
	
	public static int getNewsTypeClose()
	{
		return FavoriteConfigString.CONFIG_NEWS_TYPE_CLOSE;
	}
	
	public static int getNewsTypeCooee()
	{
		return FavoriteConfigString.CONFIG_NEWS_TYPE_COOEE;
	}
	
	public static int getNewsTypeDefaultValue()
	{
		return FavoriteConfigString.CONFIG_NEWS_TYPE_DEFAULTVALUE;
	}
	
	public static boolean isEnableAdapterVirtualKeyDefaultValue()
	{
		return FavoriteConfigString.SWITCH_ENABLE_ADAPTER_VIRTUALKEY_DEFAULTVALUE;
	}
	
	public static String getNearbyLocalPathDefaultValue()
	{
		return FavoriteConfigString.CONFIG_NEARBY_LOCAL_PATH_DEFAULTVALUE;
	}
	
	public static String getDefaultBrowserValue()
	{
		return FavoriteConfigString.CONFIG_DEFAULT_BROWSER_VALUE;
	}
	
	public static String getNearbyAdPlaceIdValue()
	{
		return FavoriteConfigString.NEARBY_AD_PLACE_ID_VALUE;
	}
	
	public static String getKusoUseExplorerKey()
	{
		return FavoriteConfigString.KUSO_USE_EXPLORER_KEY;
	}
	
	public static String getKusoShowOperatePageKey()
	{
		return FavoriteConfigString.KUSO_SHOW_OPERATE_PAGE_KEY;
	}
	
	public static String getKusoExplorerPackageNameKey()
	{
		return FavoriteConfigString.KUSO_EXPLORER_PACKAGE_NAME_KEY;
	}
	
	public static String getKusoExplorerClassNameKey()
	{
		return FavoriteConfigString.KUSO_EXPLORER_CLASS_NAME_KEY;
	}
	
	public static String getNewsAdPlaceIdKey()
	{
		return FavoriteConfigString.NEWS_AD_PLACE_ID_KEY;
	}
	
	public static String getNewsAdPlaceIdValue()
	{
		return FavoriteConfigString.NEWS_AD_PLACE_ID_VALUE;
	}
	
	public static int getHostVersionCodeValue()
	{
		return FavoriteConfigString.HOST_VERSION_CODE_VALUE;
	}
	//fulijuan add	 //需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
	public static String getEnableDemoAdWhenOnShow()
	{
		return FavoriteConfigString.ENABLE_DEMO_AD_WHEN_ON_SHOW;
	}
	public static boolean isEnableDemoAdWhenOnShowDefaultValue()
	{
		return FavoriteConfigString.SWITCH_ENABLE_DEMO_AD_WHEN_ON_SHOW_DEFAULTVALUE;
	}
	//fulijuan add end
}
