package com.cooee.favorites.host;


/**FavoriteConfig 的key值和默认值*/
public class FavoriteConfigString
{
	
	//FavoriteConfig key start
	/**酷搜*/
	public static final String ENABLE_COOEE_SEARCH = "enableCooeeSearch";
	/**酷生活新闻*/
	public static final String ENABLE_NEWS = "enableNews";
	/**酷生活联系人*/
	public static final String ENABLE_CONTACTS = "enableContacts";
	/**酷生活常用英语*/
	public static final String ENABLE_APPS = "enableApps";
	/**路生活附近*/
	public static final String ENABLE_NEARBY = "enableNearby";
	/**debug模式*/
	public static final String ENABLE_DEBUG = "enableDebug";
	/**dev模式*/
	public static final String ENABLE_DEV = "enableDev";
	/**是否是老人桌面*/
	public static final String ENABLE_SIMPLE_LAUNCHER = "enableSimpleLauncher";
	/**启用友盟统计*/
	public static final String ENABLE_UMENG = "enableUmeng";
	/**-1屏是否显示自己的搜索(加载)*/
	public static final String ENABLE_FAVORITES_SEARCH = "isSearchEnable";
	/**桌面是否显示搜索*/
	public static final String ENABLE_SHOW_LAUNCHER_SEARCH = "enableShowLauncherSearch";
	/**-1屏是否显示搜索*/
	public static final String ENABLE_SHOW_FAVORITES_SEARCH = "enableShowFavoritesSearch";
	/**桌面搜索栏高度*/
	public static final String LAUNCHER_SEARCHBAR_HEIGHT = "launcherSearchBarHeight";
	/**桌面图标大小*/
	public static final String LAUNCHER_ICON_SIZEPX = "launcherIconSizePx";
	/**是否是客户项目*/
	public static final String IS_CUSTOMER_LAUNCHER = "isCustomerLauncher";
	/**新闻是否可折叠*/
	public static final String ENABLE_NEWS_FOLDABLE = "newsfoldable";
	/**新闻可折叠时，默认状态*/
	public static final String NEWS_DEFAULT_EXPAND = "newsexpand";
	/**酷生活引导页key*/
	public static final String SWITCH_ENABLE_FAVORITES_CLINGS = "switch_enable_favorites_clings";
	/**酷生活S5模式*/
	public static final String SWITCH_ENABLE_IS_S5 = "switch_enable_favorites_s5";
	/**酷生活是否适配虚拟按键*/
	public static final String SWITCH_ENABLE_ADAPTER_VIRTUALKEY = "switch_enable_adapter_virtualkey";
	// zhangjin@2016/06/08 ADD START
	/**酷生活附近几个图标，本地配置文件路径 */
	public static final String CONFIG_NEARBY_LOCAL_PATH = "config_nearby_local_path";
	// zhangjin@2016/06/08 ADD END
	//lvjiangbin 20160614 ADD START
	public static final String CONFIG_DEFAULT_BROWSER_KEY = "config_default_browser";
	//lvjiangbin 20160614 ADD END
	/*酷生活服务四个广告位配置*/
	public static final String NEARBY_AD_PLACE_ID_KEY = "nearby_ad_place_id";
	public static final String HOST_VERSION_CODE = "host_version_code";//cheyingkun add	//酷生活编辑失败,初始化时传递host版本给酷生活
	/**强行添加layout监听窗口的key*/
	public static final String FORCE_ADD_LAYOUT_CHANGE_LISTENER_KEY = "force_add_layout_change_listener";//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
	//FavoriteConfig key end
	//
	//
	//
	//
	//FavoriteConfig defaultValue start
	/**酷搜*/
	public static final boolean ENABLE_COOEE_SEARCH_DEFAULTVALUE = true;
	/**酷生活新闻*/
	public static final boolean ENABLE_NEWS_DEFAULTVALUE = true;
	/**酷生活联系人*/
	public static final boolean ENABLE_CONTACTS_DEFAULTVALUE = true;
	/**酷生活常用应用*/
	public static final boolean ENABLE_APPS_DEFAULTVALUE = true;
	/**酷生活附近*/
	public static final boolean ENABLE_NEARBY_DEFAULTVALUE = true;
	/**debug模式*/
	public static final boolean ENABLE_DEBUG_DEFAULTVALUE = false;
	/**dev模式*/
	public static final boolean ENABLE_DEV_DEFAULTVALUE = false;
	/**是否是老人桌面*/
	public static final boolean ENABLE_SIMPLE_LAUNCHER_DEFAULTVALUE = false;
	/**启用友盟统计*/
	public static final boolean ENABLE_UMENG_DEFAULTVALUE = true;
	//	/**-1屏是否显示自己的搜索(加载)-----是否加载根据 桌面是否显示  酷生活是否显示开关得到默认值   不需要额外配置*/
	//	public static final boolean ENABLE_FAVORITES_SEARCH_DEFAULTVALUE = false;
	/**桌面是否显示搜索*/
	public static final boolean ENABLE_SHOW_LAUNCHER_SEARCH_DEFAULTVALUE = true;
	/**-1屏是否显示搜索*/
	public static final boolean ENABLE_SHOW_FAVORITES_SEARCH_DEFAULTVALUE = true;
	/**桌面搜索栏高度*/
	public static final int LAUNCHER_SEARCHBAR_HEIGHT_DEFAULTVALUE = 96;
	/**桌面图标大小*/
	public static final int LAUNCHER_ICON_SIZEPX_DEFAULTVALUE = 96;
	/**是否是客户项目*/
	public static final boolean IS_CUSTOMER_LAUNCHER_DEFAULTVALUE = false;
	/**新闻是否可折叠*/
	public static final boolean ENABLE_NEWS_FOLDABLE_DEFAULTVALUE = false;
	/**新闻可折叠时，默认状态*/
	public static final boolean NEWS_DEFAULT_EXPAND_DEFAULTVALUE = true;
	/**酷生活引导页defaultVaule*/
	public static final boolean SWITCH_ENABLE_FAVORITES_CLINGS_DEFAULTVALUE = false;
	/**酷生活是否S5模式  默认不是false*/
	public static final boolean SWITCH_ENABLE_FAVORITES_S5_DEFAULTVALUE = false;
	/**酷生活是否适配虚拟按键默认值true*/
	public static final boolean SWITCH_ENABLE_ADAPTER_VIRTUALKEY_DEFAULTVALUE = true;
	//FavoriteConfig defaultValue end
	// zhangjin@2016/06/08 ADD START
	public static final String CONFIG_NEARBY_LOCAL_PATH_DEFAULTVALUE = "/system/launcher/";
	// zhangjin@2016/06/08 ADD END
	//lvjiangbin 20160614 ADD START
	public static final String CONFIG_DEFAULT_BROWSER_VALUE = "";
	//lvjiangbin 20160614 ADD END
	/*酷生活服务四个广告位配置*/
	public static final String NEARBY_AD_PLACE_ID_VALUE = "";
	// jubingcheng@2016/06/15 ADD START
	/**酷搜：搜索结果默认打开方式 true:浏览器 false:webview*/
	public static final String KUSO_USE_EXPLORER_KEY = "kuso_use_explorer";
	/**酷搜：默认是否显示运营页 true:显示 false:不显示*/
	public static final String KUSO_SHOW_OPERATE_PAGE_KEY = "kuso_show_operate_page";
	/**酷搜：用浏览器打开搜索结果时指定浏览器的包名*/
	public static final String KUSO_EXPLORER_PACKAGE_NAME_KEY = "kuso_explorer_package_name";
	/**酷搜：用浏览器打开搜索结果时指定浏览器的类名*/
	public static final String KUSO_EXPLORER_CLASS_NAME_KEY = "kuso_explorer_class_name";
	// jubingcheng@2016/06/15 ADD END
	/*酷生活新闻广告配置*/
	public static final String NEWS_AD_PLACE_ID_KEY = "news_ad_place_id";
	/*酷生活新闻广告配置值*/
	public static final String NEWS_AD_PLACE_ID_VALUE = "";
	public static final int HOST_VERSION_CODE_VALUE = 0;//cheyingkun add	//酷生活编辑失败,初始化时传递host版本给酷生活
	/**强行添加layout监听窗口的默认值*/
	public static final boolean FORCE_ADD_LAYOUT_CHANGE_LISTENER_DEFAULT_VALUE = false;//cheyingkun add	//无论有没有导航栏，根据开关强行添加layout监听窗口(开关值传递给酷生活)
	//fulijuan add start    //需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
	/**酷生活一级界面开屏广告配置*/
	public static final String ENABLE_DEMO_AD_WHEN_ON_SHOW = "enableDemoAdWhenOnShow";
	/**酷生活一级界面开屏广告配置的开关  默认false*/
	public static final boolean SWITCH_ENABLE_DEMO_AD_WHEN_ON_SHOW_DEFAULTVALUE = false;
	//fulijuan add end

}
