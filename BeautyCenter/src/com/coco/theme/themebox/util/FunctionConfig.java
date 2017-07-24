package com.coco.theme.themebox.util;


import java.util.ArrayList;


public final class FunctionConfig
{
	
	public static boolean personal_center_internal = true;
	// 是否显示锁屏(无论是否安装锁屏)
	private static boolean displayLock = true;
	// 是否去掉网络不稳定提示
	private static boolean netPromptVisible = true;
	// 是否显示应用推荐功能
	private static boolean recommendVisible = true;
	// 是否显示锁屏
	private static boolean lockVisible = true;
	// 是否需要分享功能
	private static boolean shareVisible = true;
	// 是否需要loading界面
	private static boolean loadVisible = true;
	// 是否需要热门列表
	private static boolean hotThemeVisible = true;
	// 是否下载到手机内存
	private static boolean downToInternal = false;
	// 墙纸、字体是否显示
	private static boolean isWallpaperVisible = false;
	private static boolean isFontVisible = false;
	// 是否需要热门锁屏
	private static boolean hotLockVisible = true;
	private static String customWallpaperPath = "";
	// 顶部状态栏是否需要改变为透明显示
	private static boolean statusbar_translucent = false;
	private static String statusbar_lost_focus_action = "com.konka.action.STATUSBAR_OPAQUE";
	// 是否为doov样式（显示壁纸，字体，去除title，热门主题，热门锁屏）
	private static boolean isdoovStyle = false;
	// 锁屏中设置是否显示
	private static boolean isLockSetVisible = true;
	// 主题预览界面主题简介是否显示
	private static boolean isIntroductionVisible = true;
	private static String galleryPkg = "com.google.android.gallery3d;com.miui.gallery;com.android.gallery;com.cooliris.media;com.htc.album;com.google.android.gallery3d;com.cooliris.media.Gallery;com.sonyericsson.album;com.android.gallery3d;com.sec.android.gallery3d";
	private static boolean isThemeMoreShow = true;
	// 特效界面是否显示
	private static boolean isEffectVisiable = false;
	private static String[] app_list_string;
	private static String[] workSpace_list_string;
	private static boolean isPriceVisible = true;
	private static boolean page_effect_no_radom_style = false;
	// 进入主题盒子，第一次是否显示loading界面
	private static boolean isLoadingShow = false;
	private static boolean isShowSceneTab = true;
	private static boolean isShowHotScene = true;
	private static boolean isShowHotWallpaper = true;
	private static boolean isShowWidgetTab = true;
	private static boolean isShowHotWidget = false;
	private static boolean isShowHotFont = true;
	private static boolean disable_set_wallpaper_dimensions = false;
	private static boolean isInternal = false;
	private static boolean isLiveWallpaperShow = true;
	private static boolean themeVisible = true;
	//后台检测在线功能是否需要开启
	private static boolean isShowHotTab = false;
	// 是否支持后台配置tab功能
	private static boolean enable_background_configuration_tab = true;
	// 是否支持自更新功能
	private static boolean enable_update_self = true;
	private static String tab_sequence = null;
	private static String tab_default_highlight = null;
	private static boolean isStatictoIcon = false;
	private static boolean lockwallpaperShow = false;
	private static String customLockWallpaperPath = null;
	private static boolean enable_topwise_style = false;
	private static boolean enable_tophard_style = false;
	private static boolean enable_manual_update = false;
	private static boolean enable_eastaeon_style = false;
	private static boolean net_version = false;
	private static int gridWidth = 120;
	private static int gridHeight = 200;
	private static boolean enable_add_widget = false;
	private static String wallpapers_from_other_apk = null;
	//朗易通设置壁纸的开关
	private static boolean langyitong_wallpaper_set = false;
	// @2014/11/21 ADD START by gaominghui
	private static boolean enable_langyitong_theme_style = false;
	//朗易通内置主题放置顺序的开关
	private static boolean langyitong_theme_order_set = true;
	// @2014/11/21 ADD END by gaominghui
	private static boolean enable_check_lock_mode = false;
	//是否开启系统锁
	private static boolean is_show_systemlock_in_local = false;
	//
	private static String theme_apply_launcher_package_name = null;
	private static String theme_apply_launcher_class_name = null;
	//按返回键退出程序时是否退出进程
	private static boolean themebox_system_exit = true;
	//桌面壁纸和锁屏壁纸预览界面是否打开
	private static boolean enable_show_preview_wallpaper = true;
	private static boolean enable_show_apply_lock_wallpaper = true;
	//锁屏壁纸的路径
	private static String set_lockwallpaper_path = null;
	private static boolean enable_hedafeng_style = false;
	//内置字体本地化路径
	private static String local_default_font_path = null;
	//是否显示免责申明
	private static boolean enable_disclaimer_dialog = false;
	//设置动态壁纸时,是否调用系统动态壁纸列表
	private static boolean enable_start_livewallpaper_picker = false;
	//显示壁纸的预览图是否可以滑动
	private static boolean enable_preview_wallpaper_by_adapter = true;
	//本地壁纸的图库设置壁纸是否需打开自定义要裁剪功能
	private static boolean enable_setwallpaper_by_gallery_clip = true;
	//当美化中心壁纸被设置为桌面壁纸时,点击删除按钮,提示“当前壁纸正在使用,无法删除”
	private static boolean enable_delete_current_desk_wallpaper = false;
	//启动美化中心时,TabThemeFactory的createTabContent布局加载是否通过postDelay来实现
	private static boolean enable_startactivity_by_async = false;
	//当前桌面壁纸是否显示“使用中”标识
	private static boolean enable_show_current_wallpaper_flag = false;
	//美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据当前桌面应用的壁纸比例来裁剪
	private static boolean enable_wallpaper_clip_by_systemwallpaper_scale = false;
	//智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
	private static boolean enable_wallpaper_clip_by_systemgallery = false;
	//设置桌面壁纸成功后是否退出美化中心,回到桌面。 true 退出  false 不退出     
	private static boolean enable_move_task_back_after_setdeskwallpaper = false;
	//ME_RTFSC  [START]
	private static int app_id = -1;
	private static String strAction = null;
	private static String strActionDescription = null;
	//设置视频壁纸开关
	private static boolean enable_show_video_wallpaper = false;
	//（兴软）去掉系统锁屏或者第三方锁屏是否支持设置锁屏壁纸的判断
	private static boolean remove_enable_support_lockwallpaper_judge = false;
	//友盟关于美化中心进入，退出，使用时长的统计开关
	private static boolean umeng_statistics_key = false;
	// @gaominghui015/07/08 ADD START 是否显示本地图库
	private static boolean is_show_local_gallery = false;
	private static boolean show_local_livewallpaper = false;//是否显示动态壁纸
	// @gaominghui2015/07/08 ADD END
	// @gaominghui2015/07/09 ADD START 获取brzh uni桌面的主题预览图的可配置路径
	private static String custom_theme_path_brzh = null;
	// @gaominghui2015/07/09 ADD END
	// @gaominghui2015/08/31 ADD START 0003415: 铂睿智恒 使用uni3.0 桌面，在美化中心，点击静态壁纸预览界面，增加可以同时设置桌面壁纸和锁屏壁纸的功能
	private static boolean enable_apply_desktopwallpaper_lockwallpaper = false;
	// @gaominghui2015/08/31 ADD END
	// @2015/11/16 ADD START
	private static boolean enable_local_thumb_preview_path = false;
	// @2015/11/16 ADD END
	// @2015/11/20 ADD START 晨想需求，美化中心在线主题的下载，下载完成之后，自动静默安装 
	private static boolean inatall_silently_ThemeApk = false;
	// @2015/11/20 ADD END
	// @2015/11/12 ADD START 设置brzh 桌面传过来的需要排序的主题列表
	private static ArrayList<String> brzhSortThemeList = null;
	// @2015/11/12 ADD END
	// @2015/11/20 ADD START  brzh 要求在本地壁纸，主题，插件，锁屏数据加载出来之前有张背景图片显示 ；true：有这张背景图片，false：没有这张背景图片
	private static boolean brzh_setWaitBackgroundView = false;
	// @2015/11/20 ADD END
	// @gaominghui2016/01/08 ADD START brzh需求：从系统设置界面进入美化中心需要配置的内置桌面的数据库名字参数
	private static String launcher_pub_provider_authority = null;
	// @gaominghui2016/01/08 ADD END
	// @2016/01/21 ADD START 乐今需求，点击应用主题后是否显示toast提示
	private static boolean apply_theme_show_toast = false;
	// @2016/01/21 ADD END
	// gaominghui@2016/06/20 ADD START 美化中心本地主题中默认主题的位置
	private static boolean default_theme_show_front = false;
	// gaominghui@2016/06/20 ADD END 美化中心本地主题中默认主题的位置
	// gaominghui@2016/12/09 ADD START 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
	private static boolean cx_lockwallpaper_show = false;
	
	// gaominghui@2016/12/09 ADD END 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
	public static boolean isUmengStatistics_key()
	{
		return umeng_statistics_key;
	}
	
	public static void setUmengStatistics_key(
			boolean statistics_key )
	{
		FunctionConfig.umeng_statistics_key = statistics_key;
	}
	
	public static int getApp_id()
	{
		return app_id;
	}
	
	public static void setApp_id(
			int app_id )
	{
		FunctionConfig.app_id = app_id;
	}
	
	public static String getStrAction()
	{
		return strAction;
	}
	
	public static void setStrAction(
			String strAction )
	{
		FunctionConfig.strAction = strAction;
	}
	
	public static String getStrActionDescription()
	{
		return strActionDescription;
	}
	
	public static void setStrActionDescription(
			String strActionDescription )
	{
		FunctionConfig.strActionDescription = strActionDescription;
	}
	
	//ME_RTFSC  [END]
	public static boolean isEnableUpdateself()
	{
		return enable_update_self;
	}
	
	public static void setEnableUpdateself(
			boolean enable_update_self )
	{
		FunctionConfig.enable_update_self = enable_update_self;
	}
	
	public static void setNetVersion(
			boolean net_version )
	{
		FunctionConfig.net_version = net_version;
	}
	
	public static boolean isNetVersion()
	{
		//Log.v( "ThemeBox" , " FunctionConfig.net_version :" + FunctionConfig.net_version );
		return FunctionConfig.net_version;
	}
	
	public static boolean isInternal()
	{
		return isInternal;
	}
	
	public static void setInternal(
			boolean isInternal )
	{
		FunctionConfig.isInternal = isInternal;
	}
	
	public static void setStatusBarTranslucent(
			boolean bTranslucent ,
			String lost_focus_action )
	{
		statusbar_translucent = bTranslucent;
		statusbar_lost_focus_action = lost_focus_action;
	}
	
	public static boolean isStatusBarTranslucent()
	{
		return statusbar_translucent;
	}
	
	public static String getLostFocusAction()
	{
		return statusbar_lost_focus_action;
	}
	
	// 设置下载路径
	public static void setThemePath(
			String path )
	{
		com.coco.theme.themebox.StaticClass.set_directory_path = path;
	}
	
	public static void setDownToInternal(
			boolean visible )
	{
		downToInternal = visible;
	}
	
	public static boolean isDownToInternal()
	{
		return downToInternal;
	}
	
	public static void setThemeHotVisible(
			boolean visible )
	{
		hotThemeVisible = visible;
	}
	
	public static boolean isHotThemeVisible()
	{
		return hotThemeVisible;
	}
	
	public static void setIsShowHotTab(
			boolean visable )
	{
		isShowHotTab = visable;
	}
	
	public static boolean isShowHotTab()
	{
		return isShowHotTab;
	}
	
	public static void setLockVisible(
			boolean visible )
	{
		lockVisible = visible;
	}
	
	public static boolean isLockVisible()
	{
		return lockVisible;
	}
	
	public static void setShareVisible(
			boolean visible )
	{
		shareVisible = visible;
	}
	
	public static boolean isShareVisible()
	{
		return shareVisible;
	}
	
	public static void setLoadVisible(
			boolean visible )
	{
		loadVisible = visible;
	}
	
	public static boolean isLoadVisible()
	{
		return loadVisible;
	}
	
	public static void setRecommendVisible(
			boolean visible )
	{
		recommendVisible = visible;
	}
	
	public static boolean isRecommendVisible()
	{
		return recommendVisible;
	}
	
	public static void setPromptVisible(
			boolean visible )
	{
		netPromptVisible = visible;
	}
	
	public static boolean isPromptVisible()
	{
		return netPromptVisible;
	}
	
	public static boolean isWallpaperVisible()
	{
		return isWallpaperVisible;
	}
	
	public static void setWallpaperVisible(
			boolean visible )
	{
		FunctionConfig.isWallpaperVisible = visible;
	}
	
	public static boolean isHotLockVisible()
	{
		return hotLockVisible;
	}
	
	public static void setHotLockVisible(
			boolean hotLockVisible )
	{
		FunctionConfig.hotLockVisible = hotLockVisible;
	}
	
	public static boolean isFontVisible()
	{
		return isFontVisible;
	}
	
	public static void setFontVisible(
			boolean isFontVisible )
	{
		FunctionConfig.isFontVisible = isFontVisible;
	}
	
	public static void setDisplayLock(
			boolean visible )
	{
		displayLock = visible;
	}
	
	public static boolean isDisplayLock()
	{
		return displayLock;
	}
	
	// 获取launcher中壁纸的路径
	public static String getCustomWallpaperPath()
	{
		return customWallpaperPath;
	}
	
	public static void setCustomWallpaperPath(
			String customWallpaperPath )
	{
		FunctionConfig.customWallpaperPath = customWallpaperPath;
	}
	
	public static boolean isdoovStyle()
	{
		return isdoovStyle;
	}
	
	public static void setdoovStyle(
			boolean isdoovStyle )
	{
		FunctionConfig.isdoovStyle = isdoovStyle;
		if( isdoovStyle )
		{
			setThemeMoreShow( false );
		}
	}
	
	public static boolean isLockSetVisible()
	{
		return isLockSetVisible;
	}
	
	public static void setLockSetVisible(
			boolean isLockSetVisible )
	{
		FunctionConfig.isLockSetVisible = isLockSetVisible;
	}
	
	public static boolean isIntroductionVisible()
	{
		return isIntroductionVisible;
	}
	
	public static void setIntroductionVisible(
			boolean isIntroductionVisible )
	{
		FunctionConfig.isIntroductionVisible = isIntroductionVisible;
	}
	
	public static String getGalleryPkg()
	{
		return galleryPkg;
	}
	
	public static void setGalleryPkg(
			String galleryPkg )
	{
		if( galleryPkg != null && !galleryPkg.equals( "" ) )
		{
			Log.i( "sss" , "galleryPkg = " + galleryPkg );
			FunctionConfig.galleryPkg = galleryPkg;
		}
	}
	
	public static boolean isThemeMoreShow()
	{
		return isThemeMoreShow;
	}
	
	public static void setThemeMoreShow(
			boolean isThemeMoreShow )
	{
		FunctionConfig.isThemeMoreShow = isThemeMoreShow;
	}
	
	public static boolean isEffectVisiable()
	{
		return isEffectVisiable;
	}
	
	public static void setEffectVisiable(
			boolean isEffectVisiable )
	{
		if( FunctionConfig.net_version )
			FunctionConfig.isEffectVisiable = false;
		else
			FunctionConfig.isEffectVisiable = isEffectVisiable;
	}
	
	public static boolean isPriceVisible()
	{
		return isPriceVisible;
	}
	
	public static void setPriceVisible(
			boolean isPriceVisible )
	{
		FunctionConfig.isPriceVisible = isPriceVisible;
	}
	
	public static String[] getAppliststring()
	{
		return app_list_string;
	}
	
	public static void setAppliststring(
			String[] app_list_string )
	{
		FunctionConfig.app_list_string = app_list_string;
	}
	
	public static String[] getWorkSpaceliststring()
	{
		return workSpace_list_string;
	}
	
	public static void setWorkSpaceliststring(
			String[] workSpace_list_string )
	{
		FunctionConfig.workSpace_list_string = workSpace_list_string;
	}
	
	public static boolean isPage_effect_no_radom_style()
	{
		return page_effect_no_radom_style;
	}
	
	public static void setPage_effect_no_radom_style(
			boolean page_effect_no_radom_style )
	{
		FunctionConfig.page_effect_no_radom_style = page_effect_no_radom_style;
	}
	
	public static boolean isLoadingShow()
	{
		return isLoadingShow;
	}
	
	public static void setLoadingShow(
			boolean isLoadingShow )
	{
		FunctionConfig.isLoadingShow = isLoadingShow;
	}
	
	public static boolean isShowSceneTab()
	{
		return isShowSceneTab;
	}
	
	public static void setShowSceneTab(
			boolean isShowSceneTab )
	{
		if( FunctionConfig.net_version )
			FunctionConfig.isShowSceneTab = false;
		else
			FunctionConfig.isShowSceneTab = isShowSceneTab;
	}
	
	public static boolean isShowHotScene()
	{
		return isShowHotScene;
	}
	
	public static void setShowHotScene(
			boolean isShowHotScene )
	{
		FunctionConfig.isShowHotScene = isShowHotScene;
	}
	
	public static boolean isShowHotWallpaper()
	{
		return isShowHotWallpaper;
	}
	
	public static void setShowHotWallpaper(
			boolean isShowHotWallpaper )
	{
		FunctionConfig.isShowHotWallpaper = isShowHotWallpaper;
	}
	
	public static boolean isShowWidgetTab()
	{
		return isShowWidgetTab;
	}
	
	public static void setShowWidgetTab(
			boolean isShowWidgetTab )
	{
		FunctionConfig.isShowWidgetTab = isShowWidgetTab;
	}
	
	public static boolean isShowHotWidget()
	{
		return isShowHotWidget;
	}
	
	public static void setShowHotWidget(
			boolean isShowHotWidget )
	{
		FunctionConfig.isShowHotWidget = isShowHotWidget;
	}
	
	public static boolean isShowHotFont()
	{
		return isShowHotFont;
	}
	
	public static void setShowHotFont(
			boolean isShowHotFont )
	{
		FunctionConfig.isShowHotFont = isShowHotFont;
	}
	
	public static void setDisableSetWallpaperDimensions(
			boolean disableSetWallpaperDimensions )
	{
		FunctionConfig.disable_set_wallpaper_dimensions = disableSetWallpaperDimensions;
	}
	
	public static boolean getDisableSetWallpaperDimensions()
	{
		return FunctionConfig.disable_set_wallpaper_dimensions;
	}
	
	public static String getCooeePayID(
			int price )
	{
		int p = price / 100;
		if( p < 10 )
		{
			return "U0" + p;
		}
		else
		{
			return "U" + p;
		}
	}
	
	public static String getSmsPurchasedPayID(
			int price )
	{
		String LEASE_PAYCODE = null;
		if( price > 0 )
		{
			if( price / 100 < 10 )
				LEASE_PAYCODE = "3000029163830" + price / 100;
			else if( price / 100 < 100 && price / 100 >= 10 )
				LEASE_PAYCODE = "300002916383" + price / 100;
		}
		return LEASE_PAYCODE;
	}
	
	public static boolean isLiveWallpaperShow()
	{
		return isLiveWallpaperShow;
	}
	
	public static void setLiveWallpaperShow(
			boolean isLiveWallpaperShow )
	{
		FunctionConfig.isLiveWallpaperShow = isLiveWallpaperShow;
	}
	
	public static boolean isEnable_background_configuration_tab()
	{
		return enable_background_configuration_tab;
	}
	
	public static void setEnable_background_configuration_tab(
			boolean enable_background_configuration_tab )
	{
		FunctionConfig.enable_background_configuration_tab = enable_background_configuration_tab;
	}
	
	public static boolean isThemeVisible()
	{
		return themeVisible;
	}
	
	public static void setThemeVisible(
			boolean themeVisible )
	{
		FunctionConfig.themeVisible = themeVisible;
	}
	
	public static String getTab_sequence()
	{
		return tab_sequence;
	}
	
	public static void setTab_sequence(
			String tab_sequence )
	{
		FunctionConfig.tab_sequence = tab_sequence;
	}
	
	public static String getTabdefaultHighlight()
	{
		return tab_default_highlight;
	}
	
	public static void setTabdefaultHighlight(
			String tab_default_highlight )
	{
		FunctionConfig.tab_default_highlight = tab_default_highlight;
	}
	
	public static boolean isStatictoIcon()
	{
		return isStatictoIcon;
	}
	
	public static void setStatictoIcon(
			boolean isStatictoIcon )
	{
		FunctionConfig.isStatictoIcon = isStatictoIcon;
	}
	
	public static boolean isLockwallpaperShow()
	{
		return lockwallpaperShow;
	}
	
	public static void setLockwallpaperShow(
			boolean lockwallpaperShow )
	{
		FunctionConfig.lockwallpaperShow = lockwallpaperShow;
	}
	
	public static String getCustomLockWallpaperPath()
	{
		return customLockWallpaperPath;
	}
	
	public static void setCustomLockWallpaperPath(
			String customLockWallpaperPath )
	{
		FunctionConfig.customLockWallpaperPath = customLockWallpaperPath;
	}
	
	public static boolean isEnable_topwise_style()
	{
		return enable_topwise_style;
	}
	
	public static void setEnable_topwise_style(
			boolean enable_topwise_style )
	{
		FunctionConfig.enable_topwise_style = enable_topwise_style;
	}
	
	public static boolean isEnable_hedafeng_style()
	{
		return enable_hedafeng_style;
	}
	
	public static void setEnable_hedafeng_style(
			boolean enable_hedafeng_style )
	{
		FunctionConfig.enable_hedafeng_style = enable_hedafeng_style;
	}
	
	public static boolean isEnable_CheckLockMode()
	{
		return enable_check_lock_mode;
	}
	
	public static void setEnable_CheckLockMode(
			boolean enable_check_lock_mode )
	{
		FunctionConfig.enable_check_lock_mode = enable_check_lock_mode;
	}
	
	public static boolean isShowSystemLockInLocal()
	{
		return is_show_systemlock_in_local;
	}
	
	public static void setIsShowSystemLockInLocal(
			boolean is_show_systemlock_in_local )
	{
		FunctionConfig.is_show_systemlock_in_local = is_show_systemlock_in_local;
	}
	
	public static boolean isEnable_tophard_style()
	{
		return enable_tophard_style;
	}
	
	public static void setEnable_tophard_style(
			boolean enable_tophard_style )
	{
		FunctionConfig.enable_tophard_style = enable_tophard_style;
	}
	
	public static boolean isEnable_manual_update()
	{
		return enable_manual_update;
	}
	
	public static void setEnable_manual_update(
			boolean enable_manual_update )
	{
		FunctionConfig.enable_manual_update = enable_manual_update;
	}
	
	public static int getGridWidth()
	{
		return gridWidth;
	}
	
	public static int getGridHeight()
	{
		return gridHeight;
	}
	
	public static boolean isEnable_add_widget()
	{
		return enable_add_widget;
	}
	
	public static void setEnable_add_widget(
			boolean enable_add_widget )
	{
		FunctionConfig.enable_add_widget = enable_add_widget;
	}
	
	public static String getWallpapers_from_other_apk()
	{
		return wallpapers_from_other_apk;
	}
	
	public static void setWallpapers_from_other_apk(
			String wallpapers_from_other_apk )
	{
		FunctionConfig.wallpapers_from_other_apk = wallpapers_from_other_apk;
	}
	
	public static String getThemeApplyLauncherPackageName()
	{
		return theme_apply_launcher_package_name;
	}
	
	public static void setThemeApplyLauncherPackageName(
			String theme_apply_launcher_package_name )
	{
		FunctionConfig.theme_apply_launcher_package_name = theme_apply_launcher_package_name;
	}
	
	public static String getThemeApplyLauncherClassName()
	{
		return theme_apply_launcher_class_name;
	}
	
	public static void setThemeApplyLauncherClassName(
			String theme_apply_launcher_class_name )
	{
		FunctionConfig.theme_apply_launcher_class_name = theme_apply_launcher_class_name;
	}
	
	public static boolean isEnable_eastaeon_style()
	{
		return enable_eastaeon_style;
	}
	
	public static void setEnable_eastaeon_style(
			boolean enable_eastaeon_style )
	{
		FunctionConfig.enable_eastaeon_style = enable_eastaeon_style;
	}
	
	public static boolean isLangyitong_wallpaper_set()
	{
		return langyitong_wallpaper_set;
	}
	
	public static void setLangyitong_wallpaper_set(
			boolean langyitong_wallpaper_set )
	{
		FunctionConfig.langyitong_wallpaper_set = langyitong_wallpaper_set;
	}
	
	// @2014/11/21 gaominghui ADD START
	public static boolean isEnable_langyitong_theme_style()
	{
		return enable_langyitong_theme_style;
	}
	
	public static void setEnable_langyitong_theme_style(
			boolean enable_langyitong_theme_style )
	{
		FunctionConfig.enable_langyitong_theme_style = enable_langyitong_theme_style;
	}
	
	public static boolean isLangyitong_theme_order_set()
	{
		return langyitong_theme_order_set;
	}
	
	public static void setLangyitong_theme_order_set(
			boolean langyitong_theme_order_set )
	{
		FunctionConfig.langyitong_theme_order_set = langyitong_theme_order_set;
	}
	
	// @2014/11/21 gaominghui ADD END
	public static boolean isExitSystemProgress()
	{
		return themebox_system_exit;
	}
	
	public static void setIsExitSystemProgress(
			boolean themebox_system_exit )
	{
		FunctionConfig.themebox_system_exit = themebox_system_exit;
	}
	
	public static boolean isEnableShowPreviewWallpaper()
	{
		return enable_show_preview_wallpaper;
	}
	
	public static void setIsEnableShowPreviewWallpaper(
			boolean enable_show_preview_wallpaper )
	{
		FunctionConfig.enable_show_preview_wallpaper = enable_show_preview_wallpaper;
	}
	
	public static boolean isEnableShowApplyLockWallpaper()
	{
		return enable_show_apply_lock_wallpaper;
	}
	
	public static void setEnableShowApplyLockWallpaper(
			boolean enable_show_apply_lock_wallpaper )
	{
		FunctionConfig.enable_show_apply_lock_wallpaper = enable_show_apply_lock_wallpaper;
	}
	
	public static String getLockWallpaperPath()
	{
		return set_lockwallpaper_path;
	}
	
	public static void setLockWallpaperPath(
			String set_lockwallpaper_path )
	{
		FunctionConfig.set_lockwallpaper_path = set_lockwallpaper_path;
	}
	
	public static String getLocalDefaultFontPath()
	{
		return local_default_font_path;
	}
	
	public static void setLocalDefaultFontPath(
			String local_default_font_path )
	{
		FunctionConfig.local_default_font_path = local_default_font_path;
	}
	
	public static boolean isEnableDisclaimerDialog()
	{
		return enable_disclaimer_dialog;
	}
	
	public static void setEnableDisclaimerDialog(
			boolean enable_disclaimer_dialog )
	{
		FunctionConfig.enable_disclaimer_dialog = enable_disclaimer_dialog;
	}
	
	public static boolean isEnableStartLiveWallpaperPicker()
	{
		return enable_start_livewallpaper_picker;
	}
	
	public static void setEnableStartLiveWallpaperPicker(
			boolean enable_start_livewallpaper_picker )
	{
		FunctionConfig.enable_start_livewallpaper_picker = enable_start_livewallpaper_picker;
	}
	
	public static boolean isEnablePreviewWallpaperByAdapter()
	{
		return enable_preview_wallpaper_by_adapter;
	}
	
	public static void setEnablePreviewWallpaperByAdapter(
			boolean enable_preview_wallpaper_by_adapter )
	{
		FunctionConfig.enable_preview_wallpaper_by_adapter = enable_preview_wallpaper_by_adapter;
	}
	
	public static boolean isEnableSetwallpaperByGalleryClip()
	{
		return enable_setwallpaper_by_gallery_clip;
	}
	
	public static void setEnableSetwallpaperByGalleryClip(
			boolean enable_setwallpaper_by_gallery_clip )
	{
		FunctionConfig.enable_setwallpaper_by_gallery_clip = enable_setwallpaper_by_gallery_clip;
	}
	
	public static boolean isEnableDeleteCurrentDeskWallpaper()
	{
		return enable_delete_current_desk_wallpaper;
	}
	
	public static void setEnableDeleteCurrentDeskWallpaper(
			boolean enable_delete_current_desk_wallpaper )
	{
		FunctionConfig.enable_delete_current_desk_wallpaper = enable_delete_current_desk_wallpaper;
	}
	
	public static boolean isEnableStartActivityByAsync()
	{
		return enable_startactivity_by_async;
	}
	
	public static void setEnableStartActivityByAsync(
			boolean enable_startactivity_by_async )
	{
		FunctionConfig.enable_startactivity_by_async = enable_startactivity_by_async;
	}
	
	public static boolean isEnableShowCurrentWallpaperFlag()
	{
		return enable_show_current_wallpaper_flag;
	}
	
	public static void setEnableShowCurrentWallpaperFlag(
			boolean enable_show_current_wallpaper_flag )
	{
		FunctionConfig.enable_show_current_wallpaper_flag = enable_show_current_wallpaper_flag;
	}
	
	public static boolean isEnableWallpaperClipByScale()
	{
		return enable_wallpaper_clip_by_systemwallpaper_scale;
	}
	
	public static void setEnableWallpaperClipByScale(
			boolean enable_wallpaper_clip_by_systemwallpaper_scale )
	{
		FunctionConfig.enable_wallpaper_clip_by_systemwallpaper_scale = enable_wallpaper_clip_by_systemwallpaper_scale;
	}
	
	public static boolean isEnableMoveTaskBackAfterSetDeskWallpaper()
	{
		return enable_move_task_back_after_setdeskwallpaper;
	}
	
	public static void setEnableMoveTaskBackAfterSetDeskWallpaper(
			boolean enable_move_task_back_after_setdeskwallpaper )
	{
		FunctionConfig.enable_move_task_back_after_setdeskwallpaper = enable_move_task_back_after_setdeskwallpaper;
	}
	
	// @gaominghui 2014/12/15 ADD START 设置视频壁纸开关
	public static boolean isEnableShowVideoWallpaper()
	{
		return enable_show_video_wallpaper;
	}
	
	public static void setEnableShowVideoWallpaper(
			boolean enable_show_video_wallpaper )
	{
		FunctionConfig.enable_show_video_wallpaper = enable_show_video_wallpaper;
	}
	
	//  @gaominghui 2014/12/15 ADD END
	// @gaominghui2014/12/18 ADD START（兴软）去掉系统锁屏或者第三方锁屏是否支持设置锁屏壁纸的判断
	public static boolean isRemove_enable_support_lockwallpaper_judge()
	{
		return remove_enable_support_lockwallpaper_judge;
	}
	
	public static void setRemove_enable_support_lockwallpaper_judge(
			boolean remove_enable_support_lockwallpaper_judge )
	{
		FunctionConfig.remove_enable_support_lockwallpaper_judge = remove_enable_support_lockwallpaper_judge;
	}
	
	// @gaominghui014/12/18 ADD END
	// @gaominghui2015/07/8 ADD START
	public static boolean isIs_show_local_gallery()
	{
		return is_show_local_gallery;
	}
	
	public static void setIs_show_local_gallery(
			boolean is_show_local_gallery )
	{
		FunctionConfig.is_show_local_gallery = is_show_local_gallery;
	}
	
	public static boolean isShow_local_livewallpaper()
	{
		return show_local_livewallpaper;
	}
	
	public static void setShow_local_livewallpaper(
			boolean show_local_livewallpaper )
	{
		FunctionConfig.show_local_livewallpaper = show_local_livewallpaper;
	}
	
	// @gaominghui2015/07/8 ADD END
	// @gaominghui2015/07/09 ADD START
	public static String getCustom_theme_path_brzh()
	{
		return custom_theme_path_brzh;
	}
	
	public static void setCustom_theme_path_brzh(
			String custom_theme_path_brzh )
	{
		FunctionConfig.custom_theme_path_brzh = custom_theme_path_brzh;
	}
	
	// @gaominghui2015/07/09 ADD END
	// @gaominghui2015/08/31 ADD START
	public static boolean isEnable_apply_desktopwallpaper_lockwallpaper()
	{
		return enable_apply_desktopwallpaper_lockwallpaper;
	}
	
	public static void setEnable_apply_desktopwallpaper_lockwallpaper(
			boolean enable_apply_desktopwallpaper_lockwallpaper )
	{
		FunctionConfig.enable_apply_desktopwallpaper_lockwallpaper = enable_apply_desktopwallpaper_lockwallpaper;
		Log.i( "andy" , "enable_apply_desktopwallpaper_lockwallpaper 000= " + FunctionConfig.enable_apply_desktopwallpaper_lockwallpaper );
	}
	
	// @gaominghui2015/08/31 ADD END
	// @2015/11/12 ADD START
	/**
	 * @return the brzhSortThemeList
	 */
	public static ArrayList<String> getBrzhSortThemeList()
	{
		return brzhSortThemeList;
	}
	
	/**
	 * @param brzhSortThemeList the brzhSortThemeList to set
	 */
	public static void setBrzhSortThemeList(
			ArrayList<String> brzhSortThemeList )
	{
		FunctionConfig.brzhSortThemeList = brzhSortThemeList;
	}
	
	// @2015/11/12 ADD END
	// @2015/11/16 ADD START
	/**
	 * @return the enable_local_thumb_preview_path
	 */
	public static boolean isEnable_local_thumb_preview_path()
	{
		return enable_local_thumb_preview_path;
	}
	
	/**
	 * @param enable_local_thumb_preview_path the enable_local_thumb_preview_path to set
	 */
	public static void setEnable_local_thumb_preview_path(
			boolean enable_local_thumb_preview_path )
	{
		FunctionConfig.enable_local_thumb_preview_path = enable_local_thumb_preview_path;
		//Log.i( "FunctionConfig" , "FunctionConfig.enable_local_thumb_preview_path = "+FunctionConfig.enable_local_thumb_preview_path);
	}
	
	// @2015/11/16 ADD END
	// @2015/11/20 ADD START
	/**
	 * @return the inatall_silently_ThemeApk
	 */
	public static boolean isInatall_silently_ThemeApk()
	{
		return inatall_silently_ThemeApk;
	}
	
	/**
	 * @param inatall_silently_ThemeApk the inatall_silently_ThemeApk to set
	 */
	public static void setInatall_silently_ThemeApk(
			boolean inatall_silently_ThemeApk )
	{
		FunctionConfig.inatall_silently_ThemeApk = inatall_silently_ThemeApk;
	}
	
	// @2015/11/20 ADD END
	// @2015/11/20 ADD START
	/**
	 * @return the brzh_setWaitBackgroundView
	 */
	public static boolean isBrzh_setWaitBackgroundView()
	{
		return brzh_setWaitBackgroundView;
	}
	
	/**
	 * @param brzh_setWaitBackgroundView the brzh_setWaitBackgroundView to set
	 */
	public static void setBrzh_setWaitBackgroundView(
			boolean brzh_setWaitBackgroundView )
	{
		FunctionConfig.brzh_setWaitBackgroundView = brzh_setWaitBackgroundView;
	}
	
	// @2015/11/20 ADD END
	// @gaominghui2016/01/08 ADD START brzh需求：从系统设置界面进入美化中心需要配置的内置桌面的数据库名字参数
	/**
	 * @return the launcher_pub_provider_authority
	 */
	public static String getLauncher_pub_provider_authority()
	{
		return launcher_pub_provider_authority;
	}
	
	/**
	 * @param launcher_pub_provider_authority the launcher_pub_provider_authority to set
	 */
	public static void setLauncher_pub_provider_authority(
			String launcher_pub_provider_authority )
	{
		FunctionConfig.launcher_pub_provider_authority = launcher_pub_provider_authority;
	}
	
	// @gaominghui2016/01/08 ADD END
	// @gaominghui2016/01/21 ADD START
	/**
	 * @return the apply_theme_show_toast
	 */
	public static boolean isApply_theme_show_toast()
	{
		return apply_theme_show_toast;
	}
	
	/**
	 * @param apply_theme_show_toast the apply_theme_show_toast to set
	 */
	public static void setApply_theme_show_toast(
			boolean apply_theme_show_toast )
	{
		FunctionConfig.apply_theme_show_toast = apply_theme_show_toast;
	}
	
	// @gaominghui2016/01/21 ADD END
	// gaominghui@2016/06/20 ADD START
	/**
	 * @return the default_theme_show_front
	 */
	public static boolean isDefault_theme_show_front()
	{
		return default_theme_show_front;
	}
	
	/**
	 * @param default_theme_show_front the default_theme_show_front to set
	 */
	public static void setDefault_theme_show_front(
			boolean default_theme_show_front )
	{
		FunctionConfig.default_theme_show_front = default_theme_show_front;
	}
	
	// gaominghui@2016/06/20 ADD END
	// gaominghui@2016/12/09 ADD START 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
	/**
	 * @return the cx_lockwallpaper_show
	 */
	public static boolean isCx_lockwallpaper_show()
	{
		return cx_lockwallpaper_show;
	}
	
	/**
	 * @param cx_lockwallpaper_show the cx_lockwallpaper_show to set
	 */
	public static void setCx_lockwallpaper_show(
			boolean cx_lockwallpaper_show )
	{
		FunctionConfig.cx_lockwallpaper_show = cx_lockwallpaper_show;
	}
	
	// gaominghui@2016/12/09 ADD END 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
	// gaominghui@2017/05/09 ADD START 智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
	/**
	 * @return the enable_wallpaper_clip_by_systemgallery
	 */
	public static boolean isEnable_wallpaper_clip_by_systemgallery()
	{
		return enable_wallpaper_clip_by_systemgallery;
	}
	
	/**
	 * @param enable_wallpaper_clip_by_systemgallery the enable_wallpaper_clip_by_systemgallery to set
	 */
	public static void setEnable_wallpaper_clip_by_systemgallery(
			boolean enable_wallpaper_clip_by_systemgallery )
	{
		FunctionConfig.enable_wallpaper_clip_by_systemgallery = enable_wallpaper_clip_by_systemgallery;
	}
	// gaominghui@2017/05/09 ADD END 智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
}
