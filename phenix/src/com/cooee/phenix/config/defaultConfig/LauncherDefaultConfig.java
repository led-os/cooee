package com.cooee.phenix.config.defaultConfig;


import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.cooee.center.pub.provider.PubContentProvider;
import com.cooee.center.pub.provider.PubProviderHelper;
import com.cooee.framework.config.ConfigUtils;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Grab.GrabService;
import com.cooee.framework.function.OperateExplorer.OperateExplorer;
import com.cooee.framework.function.OperateFavorites.OperateFavorites;
import com.cooee.framework.function.OperateMediaPluginPage.OperateMediaPluginDataManager;
import com.cooee.framework.function.OperateUmeng.OperateUmeng;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.util.Tools;

import cool.sdk.Category.CategoryConstant;
import cool.sdk.search.SearchActivityManager;
import cool.sdk.search.SearchHelper;


public class LauncherDefaultConfig extends BaseDefaultConfig
{
	
	/**相机页开关*/
	public static boolean SWITCH_ENABLE_CAMERAPAGE_SHOW = false;
	/**音乐页开关*/
	public static boolean SWITCH_ENABLE_MUSICPAGE_SHOW = false;
	
	public synchronized static void setApplicationContext(
			Context context )
	{
		//配置文件加载流程修改 , change by shlt@2015/03/04 ADD START
		if( mContext != null )
			return;
		//配置文件加载流程修改 , change by shlt@2015/03/04 ADD END
		mResources = context.getApplicationContext().getResources();
		//配置文件加载流程修改 , change by shlt@2015/03/04 UPD START
		//mContext = context;
		mContext = context.getApplicationContext();
		//配置文件加载流程修改 , change by shlt@2015/03/04 UPD END
		//桌面核心、抽屉形式随意切换 , change by shlt@2015/01/23 ADD START
		initConfigs();
		//桌面核心、抽屉形式随意切换 , change by shlt@2015/01/23 ADD END
	}
	
	private static void initConfigs()
	{
		loadConfigsAndSwitchs();
		adjustConnectionSwitchs();//该方法，务必在loadConfigAndSwitch之后调用
	}
	
	private static void loadConfigsAndSwitchs()
	{
		//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
		//xiatian del start
		//		File f = new File( CUSTOM_DEFAULT_CONFIG );
		//		if( f != null && f.exists() )
		//		{
		//			mConfigUtils = new ConfigUtils();
		//			mConfigUtils.loadConfig( mContext , CUSTOM_DEFAULT_CONFIG , ConfigUtils.FROM_PHONE );
		//		}
		//xiatian del end
		loadCustomDefaultConfig();//xiatian add
		//xiatian end
		//cheyingkun add start	//phenix1.1稳定版移植酷生活
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( mContext );
		SWITCH_ENABLE_FAVORITES = sp.getBoolean( OperateFavorites.OPERATE_FAVORITES_SWITCH_KEY , getBoolean( R.bool.switch_enable_favorites ) );
		//cheyingkun add end
		SWITCH_ENABLE_DEBUG = getBoolean( R.bool.switch_enable_debug );
		//桌面模式
		initLauncherStyle();
		//xiatian add start	//添加配置开关“SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST”
		if( CONFIG_LAUNCHER_STYLE == LAUNCHER_STYLE_DRAWER )
		{//双层模式（【注意】CONFIG_LAUNCHER_STYLE要在SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST之前读取配置信息）
			//			SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST = getBoolean( R.bool.switch_enable_show_appbar_in_applist );
			if( CONFIG_APPLIST_STYLE == APPLIST_SYTLE_KITKAT )
			{
				CONFIG_APPLIST_BAR_STYLE = getInt( R.integer.config_applistbar_style );
			}
		}
		//xiatian add end
		//xiatian add start	//添加配置开关“SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR”
		if( CONFIG_APPLIST_BAR_STYLE == APPLIST_BAR_STYLE_TAB )
		{//（【注意】SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST要在SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR之前读取配置信息）
			SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR = getBoolean( R.bool.switch_enable_show_market_button_in_appbar );
			Launcher.SHOW_MARKET_BUTTON = ( CONFIG_LAUNCHER_STYLE == LAUNCHER_STYLE_DRAWER ) && ( CONFIG_APPLIST_BAR_STYLE == APPLIST_BAR_STYLE_TAB ) && ( SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR == true );
		}
		else
		{
			SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR = false;
			Launcher.SHOW_MARKET_BUTTON = false;
		}
		//xiatian add end
		//lvjiangbin add begin
		SWITCH_ENABLE_MTK_SET_WALLPAPER = getBoolean( R.bool.switch_enable_mtk_set_wallpaper );
		//lvjiangbin add end
		//add start	//配置另一个apk为默认主题。
		CONFIG_DEFAULT_THEME_PACKAGE_NAME = getString( R.string.config_default_theme_package_name );
		// zhujieping@2015/03/12 ADD START
		if( CONFIG_DEFAULT_THEME_PACKAGE_NAME != null )
		{
			if( CONFIG_DEFAULT_THEME_PACKAGE_NAME.equals( "nothing" ) || CONFIG_DEFAULT_THEME_PACKAGE_NAME.equals( "" ) )
			{
				CONFIG_DEFAULT_THEME_PACKAGE_NAME = null;
			}
		}
		// zhujieping@2015/03/12 ADD END
		//add end
		initCategoryConfigsAndSwitchs();//添加智能分类功能 , change by shlt@2015/02/09 ADD
		//xiatian add start	//抢占桌面 [grab] 
		GrabService.switch_enable_grab_service_on_bootup = getBoolean( R.bool.switch_enable_grab_service_on_bootup );
		GrabService.switch_enable_grab_service_on_unlock = getBoolean( R.bool.switch_enable_grab_service_on_unlock );
		//xiatian add end
		SWITCH_ENABLE_DEBUG_FPS = getBoolean( R.bool.switch_enable_debug_fps );
		initSearchBarSwitch();
		initHideAppList();//xiatian add	//桌面支持配置隐藏特定的activity界面。
		// chenchen@2016/04/14 ADD START
		NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION = getBoolean( R.bool.no_simCard_unDisplay_simCard_application );
		if( NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION )
		{
			initHideSimCard();//chenchen add	//手机不插Sim卡时桌面不显示Sim卡应用图标
		}
		// chenchen@2016/04/14 ADD END
		SWITCH_ENABLE_TITLE_SHADOW = getBoolean( R.bool.switch_enable_title_shadow );//xiatian add	//图标名称和文件夹名称，是否显示文字阴影。true为显示（详细配置见<style name="WorkspaceIcon">），false为不显示。
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		CONFIG_ITEM_STYLE = getInt( R.integer.config_item_style );
		if( CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//非默认主题，不使用飞利浦的图标样式（当前是飞利浦图标样式时，需要判断当前主题是不是默认主题）。
			PubContentProvider.init();
			PubProviderHelper.SetContext( mContext );
			String currentThemePackageName = PubProviderHelper.queryValue( "theme" , "theme" );
			//cheyingkun add start	//解决“下载主题安装至SD卡，应用该主题后，关机拔掉T卡，开机后桌面应用图标仍显示为之前主题的图标大小”的问题。【i_0011898】
			String tempThemePackageName = currentThemePackageName;
			boolean apkInstalled = LauncherAppState.isApkInstalled( currentThemePackageName );
			//cheyingkun add end
			if( currentThemePackageName == null// 
					|| !apkInstalled//cheyingkun add start	//解决“下载主题安装至SD卡，应用该主题后，关机拔掉T卡，开机后桌面应用图标仍显示为之前主题的图标大小”的问题。【i_0011898】（如果当前主题未安装,则设置默认主题）
			)
			{
				if( CONFIG_DEFAULT_THEME_PACKAGE_NAME != null && LauncherAppState.isApkInstalled( CONFIG_DEFAULT_THEME_PACKAGE_NAME ) )
				{
					currentThemePackageName = CONFIG_DEFAULT_THEME_PACKAGE_NAME;
				}
				else
				{
					currentThemePackageName = mContext.getPackageName();
				}
				//cheyingkun add start	//解决“下载主题安装至SD卡，应用该主题后，关机拔掉T卡，开机后桌面应用图标仍显示为之前主题的图标大小”的问题。【i_0011898】
				if( tempThemePackageName != null && !apkInstalled )
				{
					PubProviderHelper.addOrUpdateValue( "theme" , "theme" , currentThemePackageName );
					PubProviderHelper.addOrUpdateValue( "theme" , "theme_status" , "1" );
				}
				//cheyingkun add end
			}
			if( currentThemePackageName.equals( mContext.getPackageName() ) == false )
			{
				CONFIG_ITEM_STYLE = BaseDefaultConfig.ITEM_STYLE_NORMAL;
			}
		}
		//xiatian add end
		initAppReplaceTitleList();//xiatian add	//桌面支持配置特定的activity的显示名称。
		CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME = ComponentName.unflattenFromString( getString( R.string.config_customer_wallpaper_component_name ) );//xiatian add	//飞利浦需求，将美化中心改为他们的壁纸设置。
		SWITCH_ENABLE_INSTALL_OPERATEICON_INFOLDER = getBoolean( R.bool.switch_enable_install_operateicon_infolder );//运营文件夹安装的图标显示在文件夹内或者桌面的开关
		SWITCH_ENABLE_DISCLAIMER = getBoolean( R.bool.switch_enable_disclaimer );// cheyingkun add //免责声明布局
		CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE = getInt( R.integer.config_workspace_default_page_style );//xiatian add	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
		initHideWidgetList();//xiatian add	//桌面支持配置隐藏特定的widget插件。
		initHideShortcutList();//xiatian add	//桌面支持配置隐藏特定的快捷方式插件。
		SWITCH_ENABLE_DYNAMIC_ICON_DELETE = getBoolean( R.bool.switch_enable_dynamicicon_delete );//桌面的虚图标、虚链接是否可删除
		SWITCH_ENABLE_DOWNLOAD_CONFIRM_DIALOG = getBoolean( R.bool.switch_enable_show_operate_item_download_confirm_dialog );
		initDelayShowAppList();//xiatian add	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
		SWITCH_ENABLE_ENLARGE_ICON_SIZE_WHEN_SOURCE_SIZE_LESS_THEN_DEST_SIZE = getBoolean( R.bool.switch_enable_enlarge_icon_size_when_source_size_less_then_dest_size );//cheyingkun add	//当图标尺寸小于目标尺寸时，是否放大图标尺寸（放大会导致图标模糊）。true为放大；false为不放大。默认为false。
		SWITCH_ENABLE_CLINGS = getBoolean( R.bool.switch_enable_clings );//cheyingkun add	//是否显示新手引导
		CONFIG_LOADING_PAGE_STYLE = getInt( R.integer.config_loading_page_style );//cheyingkun add	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
		//xiatian add start	//需求：编辑模式底边栏配置打开特定界面。预留两个可配置的button，这两个button配置intent即可打开特定界面（详见“BaseDefaultConfig”中说明）。
		//cheyingkun add start	//phenix仿S5效果,编辑模式底部按钮配置
		try
		{
			CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_1_INTENT = Intent.parseUri( getString( R.string.config_overview_panel_button_fallback_1_intent ) , 0 );
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
			CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_1_INTENT = null;
		}
		try
		{
			CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_2_INTENT = Intent.parseUri( getString( R.string.config_overview_panel_button_fallback_2_intent ) , 0 );
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
			CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_2_INTENT = null;
		}
		//cheyingkun add end
		//xiatian add end
		SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE = getBoolean( R.bool.switch_enable_hotseat_item_show_title );//xiatian add	//底边栏图标是否显示名称。true为显示名称；false为不显示。默认为false。
		SWITCH_ENABLE_BIND_ITEMS_ANIMATE_IN_LOADING = getBoolean( R.bool.switch_enable_bind_items_animate_in_loading );//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页
		SWITCH_ENABLE_CUSTOMER_PHILIPS_WALLPAPER_CHANGED_NOTIFY = getBoolean( R.bool.switch_enable_customer_philips_wallpaper_changed_notify );//cheyingkun add	//是否监听飞利浦壁纸改变广播【c_0003456】
		SWITCH_ENABLE_SHOW_APP_AUTO_CREATE_SHORTCUT = getBoolean( R.bool.switch_enable_show_app_auto_create_shortcut );//cheyingkun add	//是否显示应用自动创建的快捷方式【c_0003466】
		SWITCH_ENABLE_SHOW_SHORTCUT_IN_WIDGET_LIST = getBoolean( R.bool.switch_enable_show_shortcut_in_widget_list );//cheyingkun add	//是否在小部件界面中显示快捷方式。true显示；false不显示。默认true。
		initUmengSwitch();//xiatian add	//需求：运营友盟（详见“OperateUmeng”中说明）。
		CONFIG_ONRESUME_BROADCAST_ACTION = getString( R.string.config_onresume_broadcast_action );//xiatian add	//在桌面onResume的时候，发送广播通知客户。配置为空则不发送广播。默认为空。
		CONFIG_ONSTOP_BROADCAST_ACTION = getString( R.string.config_onstop_broadcast_action );//xiatian add	//在桌面onStop的时候，发送广播通知客户。配置为空则不发送广播。默认为空。
		SWITCH_ENABLE_SHORTCUT_WIDGET_NAME_FOLLOW_SYSTEM_LANGUAGE = getBoolean( R.bool.switch_enable_shortcut_widget_name_follow_system_language );//cheyingkun add	//快捷方式名称是否跟随系统语言变化。true为跟随系统语言变化；false为不跟随。默认true。【c_0003657】
		CONFIG_CUSTOM_WALLPAPERS_PATH = getString( R.string.config_custom_wallpapers_path );//xiatian add	//德盛伟业需求：本地化默认壁纸路径。客户可配置的桌面壁纸路径，如"/system/wallpapers"，再在该路径下放置客户的壁纸图片。配置为空则显示"\assets\launcher\wallpapers"中的壁纸。	
		CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME = getString( R.string.config_custom_default_wallpaper_name );//cheyingkun add	//默认壁纸本地化。【c_0003753】
		SWITCH_ENABLE_STATUS_BAR_AND_NAVIGATION_BAR_TRANSPARENT = getBoolean( R.bool.switch_enable_status_bar_and_navigation_bar_transparent );//cheyingkun add	//是否开启“状态栏透明”和“导航栏透明”效果，安卓4.4以上有效。
		SWITCH_ENABLE_CUSTOM_LAYOUT = getBoolean( R.bool.switch_enable_custom_layout );//cheyingkun add start	//自定义桌面布局
		// @2016/01/05 ADD START 和兴六部 android5.0以上版本实现桌面图标标题过长，渐隐的效果。true默认android5.0以上文字渐隐，false默认文字过长后面加省略号，默认false 
		SWITCH_ENABLE_SHOW_ICON_TITLE_FADE_OUT = getBoolean( R.bool.switch_enable_show_icon_title_fade_out );
		// @2016/01/05 ADD END
		CONFIG_FOLDER_ICON_PREVIEW_STYLE = getInt( R.integer.config_folder_icon_preview_style );//cheyingkun add	//解决“文件夹原生层叠效果，第一次拖动图标生成文件夹时，桌面重启”的问题。【i_0013310】
		SWITCH_ENABLE_OVERVIEW_PANEL_TEXT_HINT = getBoolean( R.bool.switch_enable_overview_panel_text_hint );//cheyingkun add	//编辑模式是否显示提示信息（提示信息内容为“拖动页面可改变页面位置”）。true为显示；false为不显示。默认false。【c_0004055】
		// zhangjin@2016/03/29 ADD START
		HERUNXIN_BIG_LAUNCHER = getBoolean( R.bool.herunxin_big_launcher );
		// zhangjin@2016/03/29 ADD END
		// zhangjin@2015/12/10 ADD START
		LAUNCHER_UPDATE = getBoolean( R.bool.switch_enable_show_launcher_update_menu_in_launcher_setting );
		// zhangjin@2015/12/10 ADD END
		CONFIG_APPLIST_STYLE = getInt( R.integer.config_applist_style );
		SWITCH_ENABLE_SEARCH_BAR_SHOW_VOICE_BUTTON = getBoolean( R.bool.switch_enable_search_bar_show_voice_button );//cheyingkun add	//搜索栏是否支持显示语音搜索的按钮。true为支持；false为不支持。默认true。
		SWITCH_ENABLE_TIMER_WIDGET_SECOND_HAND_FLASH = getBoolean( R.bool.switch_enable_timer_widget_second_hand_flash );//lvjiangbin add 时钟插件中，小时和分钟之间的“两个点（秒针）”是否闪烁。true为闪烁；false为不闪烁。默认true。
		SWITCH_ENABLE_REMOVE_SPACE_IN_APP_TITLE = getBoolean( R.bool.switch_enable_remove_space_in_app_title );//cheyingkun add	//是否除去应用名称中的空格。true为去除空格；false为不去除空格。默认true。【c_0004348】
		SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE = getBoolean( R.bool.switch_enable_set_to_default_launcher_guide );//xiatian add	//设置默认桌面引导（是否支持"设置默认桌面引导"功能。true为支持，false为不支持。默认true。）
		// YANGTIANYU@2016/07/02 ADD START
		// 专属页开关和专属页广告开关
		//gaominghui add start //需求：支持后台运营音乐页和相机页 
		//SWITCH_ENABLE_CAMERAPAGE_SHOW = getBoolean( R.bool.switch_enable_camerapage_show );//gaominghui del
		SWITCH_ENABLE_CAMERAPAGE_SHOW = sp.getBoolean( OperateMediaPluginDataManager.OPERATE_CAMERAPAGE_SWITCH_KEY , getBoolean( R.bool.switch_enable_camerapage_show ) );
		//gaominghui add end 
		if( SWITCH_ENABLE_CAMERAPAGE_SHOW )
		{//（【注意】SWITCH_ENABLE_CAMERAPAGE_SHOW要在SWITCH_ENABLE_CAMERAPAGE_AD_SHOW之前读取配置信息）
			SWITCH_ENABLE_CAMERAPAGE_AD_SHOW = getBoolean( R.bool.switch_enable_camerapage_ad_show );
		}
		else
		{
			SWITCH_ENABLE_CAMERAPAGE_AD_SHOW = false;
		}
		//gaominghui add start //需求：支持后台运营音乐页和相机页 
		//SWITCH_ENABLE_MUSICPAGE_SHOW = getBoolean( R.bool.switch_enable_musicpage_show ); //gaominghui del
		SWITCH_ENABLE_MUSICPAGE_SHOW = sp.getBoolean( OperateMediaPluginDataManager.OPERATE_MUSICPAGE_SWITCH_KEY , getBoolean( R.bool.switch_enable_musicpage_show ) );
		//gaominghui add end 
		if( SWITCH_ENABLE_MUSICPAGE_SHOW )
		{//（【注意】SWITCH_ENABLE_MUSICPAGE_SHOW要在SWITCH_ENABLE_MUSICPAGE_AD_SHOW之前读取配置信息）
			SWITCH_ENABLE_MUSICPAGE_AD_SHOW = getBoolean( R.bool.switch_enable_musicpage_ad_show );
		}
		else
		{
			SWITCH_ENABLE_MUSICPAGE_AD_SHOW = false;
		}
		// YANGTIANYU@2016/07/02 ADD END
		//zhujieping add start,主题本地化配置，资源路径
		CONFIG_CUSTOM_THEME_PATH = getString( R.string.config_custom_theme_path );
		//zhujieping add end
		//zhujieping add start,运营文件夹本地化配置，资源路径
		CONFIG_CUSTOM_OPERATE_PATH = getString( R.string.config_custom_operate_path );
		//zhujieping add end
		SWITCH_ENABLE_OVERVIEW_FREESCROLL = getBoolean( R.bool.switch_enable_overview_freeScroll );//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
		SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR = getBoolean( R.bool.switch_enable_overview_show_pageIndicator );//cheyingkun add	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
		CONFIG_PAGEINDICATOR_SCALE = Float.valueOf( getString( R.string.config_pageIndicator_scale ) );//cheyingkun add	//非当前页面的页面指示器的缩放比。默认为0.5。
		SWITCH_ENABLE_PRESS_NUMS_OR_STAR_OR_POUND_TO_DAIL = getBoolean( R.bool.switch_enable_press_nums_or_star_or_pound_to_dail );//xiatian add	//是否支持"点击数字键打开拨号界面"的功能。true为支持，false为不支持。默认false。
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if(
		//
		CONFIG_APPLIST_STYLE == APPLIST_SYTLE_MARSHMALLOW
		//
		|| ( CONFIG_APPLIST_STYLE == APPLIST_SYTLE_NOUGAT/* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
		//
		)
		{//（【注意】CONFIG_APPLIST_STYLE要在SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH之前读取配置信息）
			SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH = getBoolean( R.bool.switch_enable_marshmallow_mainmenu_search );
			if( CONFIG_APPLIST_STYLE == APPLIST_SYTLE_NOUGAT )
			{
				SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS = getBoolean( R.bool.switch_enable_nougat_mainmenu_favorites_app );
			}
		}
		else
		{
			SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH = false;
		}
		//xiatian add end
		SWITCH_ENABLE_WORKSPACE_ICON_HIGHLIGHT_WHEN_SELECTED = getBoolean( R.bool.switch_enable_workspace_icon_highlight_when_selected );//cheyingkun add	//按键选中图标时，图标是否高亮。true为高亮，false为不高亮。默认true。【c_0004474】
		SWITCH_ONE_KEY_CHANGE_WALLPAPER = getBoolean( R.bool.switch_onekey_change_wallpaper );
		SWITCH_ENABLE_SHOW_LAUNCHER_STYLE_MENU_IN_LAUNCHER_SETTING = getBoolean( R.bool.switch_enable_show_launcher_style_menu_in_launcher_setting );
		// gaominghui@2016/10/24 ADD START
		SWITCH_ENABLE_SHOW_BEAUTYCENTER_THEME_TAB = getBoolean( R.bool.switch_enable_show_beautyCenter_theme_tab );//是否允许美化中心显示主题tab页
		SWITCH_ENABLE_SHOW_BEAUTYCENTER_LOCK_TAB = getBoolean( R.bool.switch_enable_show_beautyCenter_lock_tab );//是否允许美化中心显示锁屏tab页
		SWITCH_ENABLE_SHOW_BEAUTYCENTER_WALLPAPER_TAB = getBoolean( R.bool.switch_enable_show_beautyCenter_wallpaper_tab );//是否允许美化中心显示壁纸tab页
		// gaominghui@2016/10/24 ADD END
		SWITCH_ENABLE_CATEGORY_SHOW_NOTIFICATION = getBoolean( R.bool.switch_enable_category_show_notification );//xiatian add	//智能分类功能开启后，是否允许在通知栏显示建议智能分类的通知。 true为允许显示通知，false为不允许显示通知。默认为true。
		LAST_INSTALLED_APP_SORT_ON_HEAD = getBoolean( R.bool.last_installed_app_sort_on_head );
		CONFIG_MENU_CLICK_STYLE = getInt( R.integer.config_menu_click_style );//xiatian add	//meun键点击后（menu键的onKeyUp事件当做点击事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）或退出编辑模式（当前是编辑模式）；1为打开“最近任务”界面；2为打开“竖直列表”样式的桌面菜单。默认为0。
		CONFIG_MENU_LONG_CLICK_STYLE = getInt( R.integer.config_menu_long_click_style );//xiatian add	//meun键长按后（menu键的onKeyDown事件中判断mKeyEvent.isLongPress()为true，则当做长按事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）；1为打开“最近任务”界面。默认为-1。
		SWITCH_ENABLE_RESPONSE_ONKEYLISTENER = getBoolean( R.bool.switch_enable_response_onkeylistener );//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE = getInt( R.integer.config_edit_mode_button_enter_wallpaper_style );//xiatian add	//编辑模式底边栏按钮配置为壁纸时，打开不同的壁纸选择界面。0为美化中心的壁纸tab；1为launcher3的壁纸选择界面；2为uni3的壁纸选择界面；3为客户自定义的壁纸选择界面；4为系统的选择壁纸应用的界面。默认为0。
		initExplorHomeWebsiteSwitch();//xiatian add	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
		CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST = getInt( R.integer.config_animation_duration_when_workspace_to_applist );
		if( CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST < 0 )
		{
			CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST = 0;
		}
		CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE = getInt( R.integer.config_animation_duration_when_applist_to_workspace );
		if( CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE < 0 )
		{
			CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE = 0;
		}
		CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE = getInt( R.integer.config_animation_duration_when_workspace_to_editmode );
		if( CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE < 0 )
		{
			CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE = 0;
		}
		CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE = getInt( R.integer.config_animation_duration_when_editmode_to_workspace );
		if( CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE < 0 )
		{
			CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE = 0;
		}
		CONFIG_MENU_KEY_STYLE = getInt( R.integer.config_menu_key_style );//xiatian add	//meun键响应事件的类型。0为在onKeyDown和onKeyUp中响应事件（支持menu键点击和长按）；1为在onPrepareOptionsMenu中响应事件（不支持menu键长按）。默认为0。
		initAppsShowInApplist();//xiatian add	//主菜单支持配置显示特定activity。
		CONFIG_EDIT_MODE_BUTTON_ENTER_THEME_STYLE = getInt( R.integer.config_edit_mode_button_enter_theme_style );
		SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE = getBoolean( R.bool.switch_enable_set_home_page_in_overview_mode );//gaominghui add  	//添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
		CONFIG_WINDOW_LOST_FOCUS_BROADCAST_ACTION = getString( R.string.config_window_lost_focus_broadcast_action );
		CONFIG_WINDOW_GET_FOCUS_BROADCAST_ACTION = getString( R.string.config_window_get_focus_broadcast_action );
		initFuntionPagesPosition();//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE = getInt( R.integer.config_applist_in_and_out_anim_style );
		//zhujieping add end
		SCROLL_BY_BROADCAST = getString( R.string.config_scroll_by_broadcast );//xiatian add	//通知桌面切页：“晨想”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerByBroadcast.java”中的备注。
		SWITCH_ENABLE_CUSTOMER_DSWY_PROXIMITY_SENSOR_SNAP_PAGE = getBoolean( R.bool.switch_enable_customer_dswy_proximity_sensor_scroll );//xiatian add	//通知桌面切页：“德盛伟业”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerDSWY.java”中的备注。
		SWITCH_ENABLE_CUSTOMER_RY_PROXIMITY_SENSOR_SNAP_PAGE = getBoolean( R.bool.switch_enable_customer_ry_proximity_sensor_scroll );//xiatian add	//通知桌面切页：“锐益”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerRY.java”中的备注。
		SWITCH_ENABLE_CUSTOMER_XH_FINGER_PRINT_SCROLL = getBoolean( R.bool.switch_enable_customer_xh_finger_print_scroll );//xiatian add	//通知桌面切页：“讯虎”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerCustomerXH.java”中的备注。
		initKeyEventNotifySnapPage();//xiatian add	//需求：支持某些特定按键触发桌面切页。
		XUNHU_SENSOR = getBoolean( R.bool.xunhu_sensor );//gaominghui add  	//添加配置项“xunhu_sensor”,是否支持"讯虎定制特殊传感器切页"的功能。true为支持，false为不支持。默认false。
		XUNHU_PROXIMITY_SENSOR_SCROLL = getBoolean( R.bool.xunhu_proximity_sensor_scroll );//gaominghui add //添加配置项“xunhu_proximity_sensor_scroll”,是否支持"讯虎定制普通光感切页"的功能。true为支持，false为不支持。默认false。
		SWITCH_ENABLE_CUSTOMER_LM_SET_LOCKWALLPAPER = getBoolean( R.bool.switch_enable_customer_lm_set_lockwallpaper );//添加配置项“switch_enable_customer_lm_set_lockwallpaper”，增加锁屏壁纸的功能（进入uni3桌面壁纸，点击设置，弹出“设置桌面”、“设置锁屏”和“同时设置”选项）。true为显示，false不显示。默认为false。备注：该功能需要系统底层支持。	//chenliang add
		SWITCH_ENABLE_RESPONSE_BLANK_OF_LONGCLICK = getBoolean( R.bool.switch_enable_response_blank_of_longclick );//gaominghui add //添加配置项“switch_enable_response_blank_of_longclick”，桌面空白处是否响应长按消息。true为响应长按，false为不响应长按。默认true。
		//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		SWITCH_ENABLE_SHOW_WORKSPACE_SCROLL_TYPE_IN_LAUNCHER_SETTINGS = getBoolean( R.bool.switch_enable_show_workspace_scroll_type_in_launcher_settings );
		initWorkapceScrollType();
		//xiatian add end
		//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
		SWITCH_ENABLE_SHOW_APPLIST_SCROLL_TYPE_IN_LAUNCHER_SETTINGS = getBoolean( R.bool.switch_enable_show_applist_scroll_type_in_launcher_settings );
		initApplistScrollType();
		//xiatian add end
		//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
		SWITCH_ENABLE_SHOW_WIDGET_SCROLL_TYPE_IN_LAUNCHER_SETTINGS = getBoolean( R.bool.switch_enable_show_widget_scroll_type_in_launcher_settings );
		initWidgetScrollType();
		//xiatian add end
		FINISH_ACTIVITY_WHEN_SET_UNI3_WALLPAPER_SUCCESSFULLY = getBoolean( R.bool.finish_activity_when_set_uni3_wallpaper_successfully );
		SWITCH_ENABLE_EXIT_OVERVIEW_MODE_WHEN_APPLY_THEME_FROM_BEAUTYCENTER = getBoolean( R.bool.switch_enable_exit_overview_mode_when_apply_theme_frome_beautycenter );//添加配置项“switch_enable_exit_overview_mode_when_apply_theme_frome_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。//gaomignhui add 
		SWITCH_ENABLE_EXTEND_DROP_BAR_AREA = getBoolean( R.bool.switch_enable_extend_drop_bar_area );//xiatian add	//添加配置项“switch_enable_extend_drop_bar_area”，是否支持扩大“垃圾筐”和“应用信息框”的响应区域。true时为“1、单独显示一个时：上为屏幕顶端、左为搜索框左边框、右为搜索框右边框；2、显示两个时：上为屏幕顶端、宽度均分搜索框”；false时为“图标和文字区域的边界”。默认false。
		//xiatian add start	//添加配置项“config_custom_wallpapers_path_launcher_3”，launcher3壁纸选择界面的壁纸路径的本地化配置。默认为空。
		String mConfigCustomWallpapersPathLauncher3 = getString( R.string.config_custom_wallpapers_path_launcher_3 );
		if( TextUtils.isEmpty( mConfigCustomWallpapersPathLauncher3 ) == false )
		{
			PubContentProvider.init();
			PubProviderHelper.SetContext( mContext );
			PubProviderHelper.addOrUpdateValue( "config" , CUSTOM_WALLPAPER_PATH_LAUNCHER_3_KEY , mConfigCustomWallpapersPathLauncher3 );
		}
		//xiatian add end
		//fulijuan add start //添加配置项“config_launcher_3_wallpapers_preview_style”，launcher3壁纸选择界面的壁纸预览图显示样式。0为可拖动且居中显示 ，1为不可拖动且居中显示。默认为0。 
		int mConfigCustomWallpapersPreviewStyleLauncher3 = getInt( R.integer.config_custom_wallpapers_preview_style_launcher_3 );
		PubContentProvider.init();
		PubProviderHelper.SetContext( mContext );
		if(mConfigCustomWallpapersPreviewStyleLauncher3 != 0 && mConfigCustomWallpapersPreviewStyleLauncher3 != 1){
			mConfigCustomWallpapersPreviewStyleLauncher3 = 0;
		}
		PubProviderHelper.addOrUpdateValue( "config" , LAUNCHER_3_WALLPAPER_PREVIEW_STYLE_KEY , StringUtils.concat(mConfigCustomWallpapersPreviewStyleLauncher3) );
		//fulijuan add end
		SWITCH_ENABLE_CUSTOMER_LJ_NOTIFY_APPLY_THEME = getBoolean( R.bool.switch_enable_customer_lj_notify_apply_theme );//gaominghui add //添加配置项“switch_enable_customer_lj_notify_apply_theme”，应用主题后，是否通知客户主题已经更换，true为通知，false为不通知，默认为false。【c_0004704】。
		CONFIG_TIMER_WIDGET_DATE_STYLE = getInt( R.integer.config_timer_widget_date_style );//yangmengchao  add //需求：迅虎增加桌面小部件时间日期格式样式。0为默认样式，1为迅虎样式,默认为0.
		//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）。
		if( CONFIG_LAUNCHER_STYLE == LAUNCHER_STYLE_DRAWER )
		{
			mConfigEmptyScreenIdArrayInDrawer = getIntArray( R.array.config_empty_screen_id_in_drawer );
		}
		//zhujieping add end
		//zhujieping add start //添加配置项“config_empty_screen_id_in_core”，单层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
		else if( CONFIG_LAUNCHER_STYLE == LAUNCHER_STYLE_CORE )
		{
			mConfigEmptyScreenIdArrayInCore = getIntArray( R.array.config_empty_screen_id_in_core );
		}
		//zhujieping add end
		SWITCH_ENABLE_DRAG_ITEM_PUSH_NORMAL_ITEM_IN_WORKSPACE = getBoolean( R.bool.switch_enable_drag_item_push_other_item_in_workspace );//xiatian add	添加配置项“switch_enable_drag_item_push_other_item_in_workspace”，是否支持“被拖动的桌面图标（应用图标、文件夹、插件），推动其他图标”的功能。true为支持；false为不支持。默认true。
		SWITCH_ENABLE_HOTSEAT_ALLAPPS_BUTTON_SHOW_TITLE_WHEN_HOTSEAT_ITEM_SHOW_TITLE = getBoolean( R.bool.switch_enable_hotseat_allapps_button_show_title_when_hotseat_item_show_title );//yangmengchao add       //添加配置项“switch_enable_hotseat_allapps_button_show_title_when_hotseat_item_show_title”，底边栏显示图标名称前提下，主菜单入口图标是否显示名称。true为显示名称；false为不显示。默认为true。 
		SWITCH_ENABLE_EFFECT_IN_FUNCTION_PAGES = getBoolean( R.bool.switch_enable_effect_in_function_pages );//yangmengchao add //添加配置项“switch_enable_effect_in_function_pages”，功能页是否支持切页特效。true为支持；false为不支持。默认为false。
	}
	
	private static void adjustConnectionSwitchs()
	{//该方法，务必在loadConfigAndSwitch之后调用
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//关联一些配置
			CONFIG_LAUNCHER_STYLE = LAUNCHER_STYLE_CORE;
			SWITCH_ENABLE_TITLE_SHADOW = false;
			ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE = Float.valueOf( getString( R.string.config_item_style_1_third_party_icon_scale ) );
			SWITCH_ENABLE_SHOW_LAUNCHER_STYLE_MENU_IN_LAUNCHER_SETTING = false;
		}
		//xiatian add end
		//cheyingkun add start	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
		if( CONFIG_LOADING_PAGE_STYLE == LOADING_PAGE_STYLE_DEFAULT )
		{
			SWITCH_ENABLE_CLINGS = false;
		}
		//cheyingkun add end
		// zhangjin@2016/03/29 ADD START
		if( HERUNXIN_BIG_LAUNCHER )
		{
			SWITCH_ENABLE_ENLARGE_ICON_SIZE_WHEN_SOURCE_SIZE_LESS_THEN_DEST_SIZE = true;
			SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE = true;
			SWITCH_ENABLE_CUSTOM_LAYOUT = true;
			SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = false;
		}
		// zhangjin@2016/03/29 ADD END
		if( CONFIG_LAUNCHER_STYLE == LAUNCHER_STYLE_CORE )
		{//【注意】CONFIG_LAUNCHER_STYLE要在SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST之前读取配置信息
			CONFIG_APPLIST_BAR_STYLE = APPLIST_BAR_STYLE_NO_BAR;
		}
		if( CONFIG_APPLIST_BAR_STYLE != APPLIST_BAR_STYLE_TAB )
		{//【注意】SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST要在SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR之前读取配置信息
			SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR = false;
		}
		if( SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR == false )
		{
			Launcher.SHOW_MARKET_BUTTON = false;
		}
		//xiatian add start	//编辑模式底边栏按钮配置为壁纸时，打开不同的壁纸选择界面。0为美化中心的壁纸tab；1为launcher3的壁纸选择界面；2为uni3的壁纸选择界面；3为客户自定义的壁纸选择界面；4为系统的选择壁纸应用的界面。默认为0。
		if(
		//
		( CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE == EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_CUSTOMER )
		//
		&& ( CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME == null )
		//
		)
		{
			CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE = EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_BEAUTY_CENTER;
		}
		//xiatian add end
		File file = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , "/cooee/PLS" ) );
		if( file.exists() )
		{
			SWITCH_ENABLE_DEBUG = true;
		}
		//gaominghui add start //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
		if( SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE )
		{
			CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE = DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT;
		}
		//gaominghui add end 
		adjustFuntionPagesPosition();//xiatian add	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
		//zhujieping del start //7.0进入主菜单动画改成也支持4.4主菜单样式
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		//		if( CONFIG_LAUNCHER_STYLE == LAUNCHER_STYLE_DRAWER && CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == APPLIST_IN_AND_OUT_ANIM_STYLE_NOUGAT )
		//		{
		//			if( CONFIG_APPLIST_STYLE == APPLIST_SYTLE_KITKAT ) //当进入主菜单动画的风格为7.0风格（从下往上滑入），主菜单样式为6.0、7.0风格
		//			{
		//				throw new RuntimeException( "applist in and out 7.0 anim can't be 4.4 applist" );
		//			}
		//		}
		//zhujieping add end
		//zhujieping del end
	}
	
	//<Launcher Style> liuhailin@2015-03-05 begin
	private static void initLauncherStyle()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		int mConfigLauncherStyleDefault = getInt( R.integer.config_launcher_style );
		CONFIG_LAUNCHER_STYLE = mSharedPreferences.getInt( CONFIG_LAUNCHER_STYLE_KEY , mConfigLauncherStyleDefault );
		if(
		//
		CONFIG_LAUNCHER_STYLE != LAUNCHER_STYLE_CORE
		//
		&& CONFIG_LAUNCHER_STYLE != LAUNCHER_STYLE_DRAWER
		//
		)
		{
			CONFIG_LAUNCHER_STYLE = LAUNCHER_STYLE_CORE;
			Editor mEditor = mSharedPreferences.edit();
			mEditor.putInt( CONFIG_LAUNCHER_STYLE_KEY , CONFIG_LAUNCHER_STYLE ).commit();
			mEditor = null;
		}
	}
	//<Launcher Style> liuhailin@2015-03-05 end
	;
	
	//xiatian add start	//桌面支持配置隐藏特定的activity界面。
	private static void initHideAppList()
	{
		CharSequence[] mHideAppListStr = getStringArray( R.array.config_hide_app_list );
		int len = mHideAppListStr.length;
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				CharSequence mHideApp = mHideAppListStr[i];
				String mHideAppStr = mHideApp.toString();
				String[] mHideAppStrList = mHideAppStr.split( ";" );
				if( mHideAppStrList.length > 0 )
				{
					mHideAppList.add( new ComponentName( mHideAppStrList[0] , ( mHideAppStrList.length == 1 ? "" : mHideAppStrList[1] ) ) );
				}
			}
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//桌面支持配置特定的activity的显示名称。
	private static void initAppReplaceTitleList()
	{
		CharSequence[] mAppReplaceTitleListStr = getStringArray( R.array.config_app_replace_title_list );
		int len = mAppReplaceTitleListStr.length;
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				CharSequence mItem = mAppReplaceTitleListStr[i];
				String mItemStr = mItem.toString();
				String[] mItemStrList = mItemStr.split( ";" );
				if( mItemStrList.length == 2 )
				{
					ComponentName key = ComponentName.unflattenFromString( mItemStrList[0] );
					int vaule = ResourceUtils.getStringResourceIdByReflectIfNecessary(
					//
							0 , //
							mResources , //
							mContext.getPackageName() , //
							mItemStrList[1] //
							//
							);
					if( vaule > 0 )
					{
						mAppReplaceTitleList.put( key , Integer.valueOf( vaule ) );
					}
				}
			}
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//桌面支持配置隐藏特定的widget插件。
	private static void initHideWidgetList()
	{
		CharSequence[] mHideWidgetListStr = getStringArray( R.array.config_hide_widget_list );
		int len = mHideWidgetListStr.length;
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				CharSequence mHideWidget = mHideWidgetListStr[i];
				String mHideWidgetStr = mHideWidget.toString();
				String[] mHideWidgetStrList = mHideWidgetStr.split( ";" );
				if( mHideWidgetStrList.length > 0 )
				{
					mHideWidgetList.add( new ComponentName( mHideWidgetStrList[0] , ( mHideWidgetStrList.length == 1 ? "" : mHideWidgetStrList[1] ) ) );
				}
			}
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//桌面支持配置隐藏特定的快捷方式插件。
	private static void initHideShortcutList()
	{
		CharSequence[] mHideShortcutListStr = getStringArray( R.array.config_hide_shortcut_list );
		int len = mHideShortcutListStr.length;
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				CharSequence mHideShortcut = mHideShortcutListStr[i];
				String mHideShortcutStr = mHideShortcut.toString();
				String[] mHideShortcutStrList = mHideShortcutStr.split( ";" );
				if( mHideShortcutStrList.length > 0 )
				{
					mHideShortcutList.add( new ComponentName( mHideShortcutStrList[0] , ( mHideShortcutStrList.length == 1 ? "" : mHideShortcutStrList[1] ) ) );
				}
			}
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
	private static void initDelayShowAppList()
	{
		CharSequence[] mDelayShowAppListStr = getStringArray( R.array.config_delay_show_app_list );
		int len = mDelayShowAppListStr.length;
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				CharSequence mItem = mDelayShowAppListStr[i];
				String mItemStr = mItem.toString();
				String[] mItemStrList = mItemStr.split( ";" );
				if( mItemStrList.length == 2 )
				{
					ComponentName key = ComponentName.unflattenFromString( mItemStrList[0] );
					long vaule = Long.valueOf( mItemStrList[1] );
					;
					if( vaule > 0 )
					{
						mDelayShowAppList.put( key , Long.valueOf( vaule ) );
					}
				}
			}
		}
	}
	//xiatian add end
	;
	
	private static void initSearchBarSwitch()
	{
		//xiatian add start	//需求：运营酷搜（通过服务器配置开关来决定桌面显示或者隐藏酷搜）。
		CONFIG_SEARCH_BAR_STYLE = getInt( R.integer.config_search_bar_type );
		//cheyingkun add start	//优化客户搜索和运营酷搜相关逻辑
		//是否显示搜索(本地配置)
		SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = getBoolean( R.bool.switch_enable_search_bar_common_page );//cheyingkun add	//桌面是否显示搜索
		SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = getBoolean( R.bool.switch_enable_search_bar_favorites_page );//cheyingkun add	//酷生活是否显示搜索
		//cheyingkun add end
		SearchActivityManager.initSearchConfig( SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE , SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE , CONFIG_SEARCH_BAR_STYLE == SEARCH_BAR_STYLE_COOEE , false , false );
		//从酷搜获取运营后的实际值
		if( SearchHelper.getInstance( mContext ).enableShowCommonPageSearch() )
		{
			SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = true;
		}
		else
		{
			SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = false;
		}
		if( SearchHelper.getInstance( mContext ).enableShowFavoritesPageSearch() )
		{
			if( SWITCH_ENABLE_FAVORITES )
				SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = true;
		}
		else
		{
			SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = false;
		}
		if( SearchHelper.getInstance( mContext ).enableCooeeSearch() )
		{
			CONFIG_SEARCH_BAR_STYLE = SEARCH_BAR_STYLE_COOEE;
		}
	}
	
	//xiatian add start	//需求：运营友盟（详见“OperateUmeng”中说明）。
	private static void initUmengSwitch()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		int notifyUmengNeedEnableUmengSwitch = mSharedPreferences.getInt( OperateUmeng.OPERATE_UMENG_NEED_ENABLE_UMENG_SWITCH_KEY , -1 );
		if( notifyUmengNeedEnableUmengSwitch == 0 || notifyUmengNeedEnableUmengSwitch == 1 )
		{
			SWITCH_ENABLE_UMENG = notifyUmengNeedEnableUmengSwitch == 0 ? false : true;
		}
		else
		{
			SWITCH_ENABLE_UMENG = getBoolean( R.bool.switch_enable_umeng );
		}
	}
	//xiatian add end
	;
	
	//chenchen add start	//将读取的手机将要隐藏的应用放到集合中
	private static void initHideSimCard()
	{
		CharSequence[] mHideSimCardListStr = getStringArray( R.array.launcher_no_hide_sim_card );
		int len = mHideSimCardListStr.length;
		//		Log.d( "" , "chenchen len : " + len );
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				CharSequence mHideSimCard = mHideSimCardListStr[i];
				String mHideSimCardStr = mHideSimCard.toString();
				String[] mHideSimCardStrList = mHideSimCardStr.split( "/" );
				if( mHideSimCardStrList.length > 0 )
				{
					ComponentName mComponentName = new ComponentName( mHideSimCardStrList[0] , ( mHideSimCardStrList.length == 1 ? "" : mHideSimCardStrList[1] ) );
					mHideSimCardList.add( mComponentName );
					//					Log.d( "" , "chenchen componentName : " + componentName );
				}
			}
		}
	}
	//chenchen add end   
	;
	
	private static void initCategoryConfigsAndSwitchs()
	{
		//添加智能分类功能 , change by shlt@2015/02/09 ADD START
		CONFIG_CATEGORY_TYPE = getInt( R.integer.config_category_type );
		if( CONFIG_CATEGORY_TYPE != CategoryConstant.CANNOT_CATEGORY )
		{//（【注意】CONFIG_CATEGORY_TYPE要在SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE之前读取配置信息）
			SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE = getBoolean( R.bool.switch_enable_category_folder_operate );
			CONFIG_CATEGORY_FOLDER_START_ADD_SCREENS_ID = getInt( R.integer.config_category_folder_start_add_screens_id );
		}
		//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	}
	
	//xiatian add start	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
	private static void initExplorHomeWebsiteSwitch()
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( mContext );
		SWITCH_ENABLE_OPERATE_EXPLORER = sp.getBoolean( OperateExplorer.OPERATE_EXPLORER_ENABLE_KEY , SWITCH_ENABLE_OPERATE_EXPLORER );
		if( SWITCH_ENABLE_OPERATE_EXPLORER )
		{
			CONFIG_OPERATE_EXPLORER_HOME_WEBSITE = sp.getString( OperateExplorer.OPERATE_EXPLORER_HOME_WEBSITE_KEY , CONFIG_OPERATE_EXPLORER_HOME_WEBSITE );
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//主菜单支持配置显示特定activity。
	private static void initAppsShowInApplist()
	{
		CharSequence[] mAppsShowInApplistStr = getStringArray( R.array.config_apps_show_in_applist );
		int len = mAppsShowInApplistStr.length;
		if( len > 0 )
		{
			for( int i = 0 ; i < len ; i++ )
			{
				String mAppShowInApplistComponentNameStr = mAppsShowInApplistStr[i].toString();
				ComponentName mAppShowInApplistComponentName = ComponentName.unflattenFromString( mAppShowInApplistComponentNameStr );
				if( mAppShowInApplistComponentName != null )
				{
					mAppsShowInApplist.add( mAppShowInApplistComponentName );
				}
			}
		}
	}
	
	public static ArrayList<ComponentName> getAppsShowInApplist()
	{
		return mAppsShowInApplist;
	}
	//xiatian add end
	;
	
	//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	private static void initFuntionPagesPosition()
	{
		String[] mConfigFuntionPagesPositionList = null;
		int mConfigFuntionPagesPositionItemNum = 0;
		//读取配置信息
		////从sp中读取
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		String mConfigFuntionPagesPositionInSharedPreferences = mSharedPreferences.getString( FUNCTION_PAGES_POSITION_KEY_IN_SHARED_PREFERENCES , "" );
		if( TextUtils.isEmpty( mConfigFuntionPagesPositionInSharedPreferences ) == false )
		{
			mConfigFuntionPagesPositionList = mConfigFuntionPagesPositionInSharedPreferences.split( File.separator );
			mConfigFuntionPagesPositionItemNum = mConfigFuntionPagesPositionList.length;
			if(
			//
			( mConfigFuntionPagesPositionItemNum <= 0 )
			//
			|| ( mConfigFuntionPagesPositionItemNum > FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_3 )
			//
			)
			{
				mConfigFuntionPagesPositionList = null;
				Editor mEditor = mSharedPreferences.edit();
				mEditor.putString( FUNCTION_PAGES_POSITION_KEY_IN_SHARED_PREFERENCES , "" ).commit();
				mEditor = null;
			}
		}
		////sp中“读取信息为空”或者“数据不正确”，则从配置文件中读取
		if( mConfigFuntionPagesPositionList == null )
		{
			mConfigFuntionPagesPositionList = getStringArray( R.array.config_funtion_pages_position );
		}
		//解析配置信息
		mConfigFuntionPagesPositionItemNum = mConfigFuntionPagesPositionList.length;
		if(
		//
		( mConfigFuntionPagesPositionItemNum > 0 )
		//
		&& ( mConfigFuntionPagesPositionItemNum <= FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_3 )
		//
		)
		{
			for( int i = 0 ; i < mConfigFuntionPagesPositionItemNum ; i++ )
			{
				String mConfigFuntionPagesPositionListItem = mConfigFuntionPagesPositionList[i];
				String[] mConfigFuntionPagesPositionListItemKeys = mConfigFuntionPagesPositionListItem.split( ";" );
				if( mConfigFuntionPagesPositionListItemKeys.length == 2 )
				{
					String mItemPageKey = mConfigFuntionPagesPositionListItemKeys[0];
					if(
					//
					mItemPageKey.equals( FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE )
					//
					|| mItemPageKey.equals( FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE )
					//
					|| mItemPageKey.equals( FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE )
					//
					)
					{
						Integer mItemPageIndexKey = Integer.valueOf( mConfigFuntionPagesPositionListItemKeys[1] );
						if(
						//
						( mItemPageIndexKey != 0 )
						//
						&& ( mItemPageIndexKey >= FUNCTION_PAGES_POSITION_INDEX_KEY_LEFT_OF_NORMAL_PAGE_3 )
						//
						&& ( mItemPageIndexKey <= FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_3 )
						//
						)
						{
							mConfigFuntionPagesPosition.put( mItemPageKey , mItemPageIndexKey );
						}
						else
						{
							throw new IllegalStateException( StringUtils.concat( "config_funtion_pages_position Unknown 页面位置关键字:" , mItemPageIndexKey ) );
						}
					}
					else
					{
						throw new IllegalStateException( StringUtils.concat( "config_funtion_pages_position Unknown 页面关键字:" , mItemPageKey ) );
					}
				}
			}
		}
		else if( mConfigFuntionPagesPositionItemNum == 0 )
		{//酷生活可运营，所以必须要有酷生活的位置信息。
			throw new IllegalStateException( "[config_funtion_pages_position length is 0] , please add FavoritesPage position at least" );
		}
		else if( mConfigFuntionPagesPositionItemNum > FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_3 )
		{//配置项大于三项
			throw new IllegalStateException( "[config_funtion_pages_position length > 3] , please check" );
		}
	}
	
	private static void adjustFuntionPagesPosition()
	{
		//检查：是否缺少和是否多余，即：配置信息是否和开关匹配
		////检查“酷生活”
		boolean isHaveFavoritesPagePositionInConfig = mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE );
		if( isHaveFavoritesPagePositionInConfig == false )
		{//酷生活可运营，所以必须要有酷生活的位置信息。
			throw new IllegalStateException( "[config_funtion_pages_position not hava FavoritesPage position] , please add FavoritesPage position" );
		}
		////检查“相机页”
		boolean isHaveCameraPagePositionInConfig = mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE );
		//gaominghui add start //需求：支持后台运营音乐页和相机页 
		if( isHaveCameraPagePositionInConfig == false )
		{
			throw new IllegalStateException( "[config_funtion_pages_position not hava CameraPage position] , please add CameraPage position" );
		}
		//gaominghui add end
		//gaominghui del start //需求：支持后台运营音乐页和相机页 
		/*if( SWITCH_ENABLE_CAMERAPAGE_SHOW )
		{
			if( isHaveCameraPagePositionInConfig == false )
			{
				throw new IllegalStateException( "[switch_enable_camerapage_show==true] but [config_funtion_pages_position not hava CameraPage position] , please check" );
			}
		}
		else
		{
			if( isHaveCameraPagePositionInConfig )
			{
				throw new IllegalStateException( "[switch_enable_camerapage_show==false] but [config_funtion_pages_position hava CameraPage position] , please check" );
			}
		}*/
		//gaominghui del end
		////检查“音乐页”
		//gaominghui add start //需求：支持后台运营音乐页和相机页 
		boolean isHaveMusicPagePositionInConfig = mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE );
		if( isHaveMusicPagePositionInConfig == false )
		{
			throw new IllegalStateException( "[config_funtion_pages_position not hava MusicPage position], please add MusicPage position " );
		}
		//gaominghui add end
		//gaominghui del start //需求：支持后台运营音乐页和相机页 
		/*if( SWITCH_ENABLE_MUSICPAGE_SHOW )
		{
			if( isHaveMusicPagePositionInConfig == false )
			{
				throw new IllegalStateException( "[switch_enable_musicapage_show==true] but [config_funtion_pages_position not hava MusicPage position] , please check" );
			}
		}
		else
		{
			if( isHaveMusicPagePositionInConfig )
			{
				throw new IllegalStateException( "[switch_enable_musicapage_show==false] but [config_funtion_pages_position hava MusicPage position] , please check" );
			}
		}*/
		//gaominghui del end
		//检查：是否位置重复和是否位置连续：1、用一个数组存储位置的值；2、由小到大排序；3、在看相邻的两个值的差值的绝对值是否为“1”或“2”（两个值为：1和-1）
		String mConfigFuntionPagesPositionToSharedPreferences = "";
		////1、检查：是否位置重复；2、从小到大排序
		ArrayList<Integer> mConfigFuntionPagesPositionTemp = new ArrayList<Integer>( mConfigFuntionPagesPosition.size() );
		int mFavoritesPagePositionIndex = mConfigFuntionPagesPosition.get( FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE );//酷生活可运营，所以必须要有酷生活的位置信息。
		mConfigFuntionPagesPositionToSharedPreferences = StringUtils.concat(
				mConfigFuntionPagesPositionToSharedPreferences ,
				FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE ,
				";" ,
				mFavoritesPagePositionIndex );
		mConfigFuntionPagesPositionTemp.add( mFavoritesPagePositionIndex );
		if( mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE ) )
		{
			int mCameraPagePositionIndex = mConfigFuntionPagesPosition.get( FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE );
			if( mFavoritesPagePositionIndex == mCameraPagePositionIndex )
			{//位置重复
				throw new IllegalStateException( "[Favorites position index == CameraPage position index] in config_funtion_pages_position , please check" );
			}
			else if( mCameraPagePositionIndex < mFavoritesPagePositionIndex )
			{
				mConfigFuntionPagesPositionTemp.add( 0 , mCameraPagePositionIndex );
			}
			else
			{
				mConfigFuntionPagesPositionTemp.add( mCameraPagePositionIndex );
			}
			mConfigFuntionPagesPositionToSharedPreferences = StringUtils.concat(
					mConfigFuntionPagesPositionToSharedPreferences ,
					File.separator ,
					FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE ,
					";" ,
					mCameraPagePositionIndex );
		}
		if( mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE ) )
		{
			int mMusicPagePositionIndex = mConfigFuntionPagesPosition.get( FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE );
			int mConfigFuntionPagesPositionTempSizeCur = mConfigFuntionPagesPositionTemp.size();
			if( mConfigFuntionPagesPositionTempSizeCur == 1 )
			{
				if( mFavoritesPagePositionIndex == mMusicPagePositionIndex )
				{//位置重复
					throw new IllegalStateException( "[Favorites position index == MusicPage position index] in config_funtion_pages_position , please check" );
				}
				else if( mMusicPagePositionIndex < mFavoritesPagePositionIndex )
				{
					mConfigFuntionPagesPositionTemp.add( 0 , mMusicPagePositionIndex );
				}
				else
				{
					mConfigFuntionPagesPositionTemp.add( mMusicPagePositionIndex );
				}
			}
			else if( mConfigFuntionPagesPositionTempSizeCur == 2 )
			{
				int mPositionIndexSmall = mConfigFuntionPagesPositionTemp.get( 0 );
				int mPositionIndexBig = mConfigFuntionPagesPositionTemp.get( 1 );
				if(
				//
				( mMusicPagePositionIndex == mPositionIndexSmall )
				//
				|| ( mMusicPagePositionIndex == mPositionIndexBig )
				//
				)
				{//位置重复
					throw new IllegalStateException(
							"( [MusicPage position index == Favorites position index] || [MusicPage position index == CameraPage position index] ) in config_funtion_pages_position , please check" );
				}
				else if( mMusicPagePositionIndex < mPositionIndexSmall )
				{
					mConfigFuntionPagesPositionTemp.add( 0 , mMusicPagePositionIndex );
				}
				else if( mMusicPagePositionIndex > mPositionIndexBig )
				{
					mConfigFuntionPagesPositionTemp.add( mMusicPagePositionIndex );
				}
				else if(
				//
				( mMusicPagePositionIndex > mPositionIndexSmall )
				//
				&& ( mMusicPagePositionIndex < mPositionIndexBig )
				//
				)
				{
					mConfigFuntionPagesPositionTemp.add( 1 , mMusicPagePositionIndex );
				}
			}
			mConfigFuntionPagesPositionToSharedPreferences = StringUtils.concat(
					mConfigFuntionPagesPositionToSharedPreferences ,
					File.separator ,
					FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE ,
					";" ,
					mMusicPagePositionIndex );
		}
		////检查：是否位置连续
		int mConfigFuntionPagesPositionTempSizeFinal = mConfigFuntionPagesPositionTemp.size();
		for( int i = 0 ; i < ( mConfigFuntionPagesPositionTempSizeFinal - 1 ) ; i++ )
		{
			int mFuntionPagePositionIndexCur = mConfigFuntionPagesPositionTemp.get( i );
			int mFuntionPagePositionIndexRight = mConfigFuntionPagesPositionTemp.get( i + 1 );
			if( Math.abs( mFuntionPagePositionIndexCur - mFuntionPagePositionIndexRight ) != 1 )
			{//差值为1，则位置连续
				if(
				//
				!
				//
				(
				//
				mFuntionPagePositionIndexCur == FUNCTION_PAGES_POSITION_INDEX_KEY_LEFT_OF_NORMAL_PAGE_1
				//
				&& mFuntionPagePositionIndexRight == FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_1
				//
				)
				//
				)
				{//不是“特殊情况”（一个为“-1”，一个为“1” ）。
					throw new IllegalStateException( "[duplicate position index] in config_funtion_pages_position , please check" );
				}
			}
		}
		//存sp
		if( TextUtils.isEmpty( mConfigFuntionPagesPositionToSharedPreferences ) == false )
		{
			String[] mConfigFuntionPagesPositionToSharedPreferencesList = mConfigFuntionPagesPositionToSharedPreferences.split( File.separator );
			int mConfigFuntionPagesPositionToSharedPreferencesItemNum = mConfigFuntionPagesPositionToSharedPreferencesList.length;
			if( mConfigFuntionPagesPositionToSharedPreferencesItemNum > 0 && mConfigFuntionPagesPositionToSharedPreferencesItemNum <= FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_3 )
			{
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
				String mConfigFuntionPagesPositionInSharedPreferences = mSharedPreferences.getString( FUNCTION_PAGES_POSITION_KEY_IN_SHARED_PREFERENCES , "" );
				if( mConfigFuntionPagesPositionInSharedPreferences.equals( mConfigFuntionPagesPositionToSharedPreferences ) == false )
				{
					Editor mEditor = mSharedPreferences.edit();
					mEditor.putString( FUNCTION_PAGES_POSITION_KEY_IN_SHARED_PREFERENCES , mConfigFuntionPagesPositionToSharedPreferences ).commit();
					mEditor = null;
				}
			}
		}
	}
	
	public static int getFavoritesPagePosition()
	{
		int ret = 0;
		if( mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE ) )
		{
			ret = mConfigFuntionPagesPosition.get( FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE );
		}
		return ret;
	}
	
	public static int getCameraPagePosition()
	{
		int ret = 0;
		if( mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE ) )
		{
			ret = mConfigFuntionPagesPosition.get( FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE );
		}
		return ret;
	}
	
	public static int getMusicPagePosition()
	{
		int ret = 0;
		if( mConfigFuntionPagesPosition.containsKey( FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE ) )
		{
			ret = mConfigFuntionPagesPosition.get( FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE );
		}
		return ret;
	}
	//xiatian add end
	;
	
	//xiatian add start	//需求：支持某些特定按键触发桌面切页。
	private static void initKeyEventNotifySnapPage()
	{
		int[] mConfigKeyEventNotifySnapToLeftList = getIntArray( R.array.config_key_event_notify_snap_to_left );
		for( int mConfigKeyEventNotifySnapToLeftListItem : mConfigKeyEventNotifySnapToLeftList )
		{
			mConfigKeyEventNotifySnapToLeft.add( mConfigKeyEventNotifySnapToLeftListItem );
		}
		int[] mConfigKeyEventNotifySnapToRightList = getIntArray( R.array.config_key_event_notify_snap_to_right );
		for( int mConfigKeyEventNotifySnapToRightListItem : mConfigKeyEventNotifySnapToRightList )
		{
			mConfigKeyEventNotifySnapToRight.add( mConfigKeyEventNotifySnapToRightListItem );
		}
	}
	//xiatian add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
	private static void initWorkapceScrollType()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		boolean mConfigWorkapceScrollTypeDefault = getBoolean( R.bool.switch_enable_workspace_loop_slide );
		SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE = mSharedPreferences.getBoolean( CONFIG_WORKSPACE_SCROLL_TYPE_KEY , mConfigWorkapceScrollTypeDefault );
	}
	//xiatian add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
	private static void initApplistScrollType()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		boolean mConfigApplistScrollTypeDefault = getBoolean( R.bool.switch_enable_apps_loop_slide );
		SWITCH_ENABLE_APPLIST_LOOP_SLIDE = mSharedPreferences.getBoolean( CONFIG_APPLIST_SCROLL_TYPE_KEY , mConfigApplistScrollTypeDefault );
	}
	//xiatian add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
	private static void initWidgetScrollType()
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
		boolean mConfigWidgetScrollTypeDefault = getBoolean( R.bool.switch_enable_widget_loop_slide );
		SWITCH_ENABLE_WIDGET_LOOP_SLIDE = mSharedPreferences.getBoolean( CONFIG_WIDGET_SCROLL_TYPE_KEY , mConfigWidgetScrollTypeDefault );
	}
	//xiatian add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
	private static void loadCustomDefaultConfig()
	{
		loadCustomDefaultConfigPath();
		loadCustomDefaultConfigFile();
	}
	
	private static void loadCustomDefaultConfigPath()
	{
		SWITCH_ENABLE_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH = mResources.getBoolean( R.bool.switch_enable_customer_lxt_change_custom_config_path );
		if( SWITCH_ENABLE_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH )
		{
			String mCustomDefaultConfigPathTemp = CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
			int mConfigCustomerLXTChangeCustomPathStyle = SystemProperties.getInt(
					CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_KEY ,
					CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST );
			if( mConfigCustomerLXTChangeCustomPathStyle == CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST )
			{
				//读sp
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( mContext );
				String mCustomDefaultConfigPathTempInSp = mSharedPreferences.getString( CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_PATH , "" );
				if( TextUtils.isEmpty( mCustomDefaultConfigPathTempInSp ) == false )
				{
					mCustomDefaultConfigPathTemp = mCustomDefaultConfigPathTempInSp;
				}
				//读file
				if( TextUtils.isEmpty( mCustomDefaultConfigPathTempInSp ) )
				{
					String mCustomDefaultConfigPathTempInFile = Tools.readStringFromFile( StringUtils.concat(
							LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_FILE_DIR ,
							File.separator ,
							LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_FILE_NAME ) );
					if( TextUtils.isEmpty( mCustomDefaultConfigPathTempInFile ) == false )
					{
						mCustomDefaultConfigPathTemp = mCustomDefaultConfigPathTempInFile;
						//写sp
						Editor mEditor = mSharedPreferences.edit();
						mEditor.putString( CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_PATH , mCustomDefaultConfigPathTemp ).commit();
						mEditor = null;
					}
				}
			}
			else if( mConfigCustomerLXTChangeCustomPathStyle == CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_NV_MAP )
			{
				int mConfigCustomerLXTChangeCustomPathStyleNvMapKeyPath = SystemProperties.getInt( CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_NV_MAP_KEY_PATH , 0 );
				if( mConfigCustomerLXTChangeCustomPathStyleNvMapKeyPath > 0 )
				{
					mCustomDefaultConfigPathTemp = StringUtils.concat( CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT , mConfigCustomerLXTChangeCustomPathStyleNvMapKeyPath + 1 );
				}
			}
			else
			{
				throw new IllegalStateException( "[error] loadCustomDefaultConfigPath - unknow mConfigCustomerLXTChangeCustomPathStyle:" + mConfigCustomerLXTChangeCustomPathStyle );
			}
			if( TextUtils.isEmpty( mCustomDefaultConfigPathTemp ) == false )
			{
				if( mCustomDefaultConfigPathTemp.equals( CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT ) )
				{
					CUSTOM_DEFAULT_CONFIG_PATH = CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
				}
				else
				{
					File mDestDir = new File( mCustomDefaultConfigPathTemp );
					if(
					//
					( mDestDir != null )
					//
					&& ( mDestDir.exists() )
					//
					)
					{
						CUSTOM_DEFAULT_CONFIG_PATH = mCustomDefaultConfigPathTemp;
					}
					else
					{
						CUSTOM_DEFAULT_CONFIG_PATH = CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
					}
				}
			}
			else
			{
				CUSTOM_DEFAULT_CONFIG_PATH = CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
			}
		}
		else
		{
			CUSTOM_DEFAULT_CONFIG_PATH = CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
		}
	}
	
	private static void loadCustomDefaultConfigFile()
	{
		String mCustomDefaultConfigFileFullPath = StringUtils.concat( CUSTOM_DEFAULT_CONFIG_PATH , File.separator , CUSTOM_DEFAULT_CONFIG_FILE_NAME );
		File f = new File( mCustomDefaultConfigFileFullPath );
		if( f != null && f.exists() )
		{
			mConfigUtils = new ConfigUtils();
			mConfigUtils.loadConfig( mContext , mCustomDefaultConfigFileFullPath , ConfigUtils.FROM_PHONE );
		}
	}
	//xiatian add end
	;
	
	//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）。
	public static boolean isAllowEmptyScreen()
	{
		if( mConfigEmptyScreenIdArrayInDrawer != null && mConfigEmptyScreenIdArrayInDrawer.length > 0 )
		{
			return true;
		}
		else if( mConfigEmptyScreenIdArrayInCore != null && mConfigEmptyScreenIdArrayInCore.length > 0 )
		{
			return true;
		}
		return false;
	}
	//zhujieping add end
}
