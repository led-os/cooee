package com.cooee.framework.config.defaultConfig;


import java.util.ArrayList;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;

import com.cooee.framework.config.ConfigUtils;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.Category.CategoryConstant;


public class BaseDefaultConfig
{
	
	public static boolean SWITCH_ENABLE_DEBUG = false;//是否打开调试开关。打开后打印某些log以追踪bug。(i_0011034、i_0011035、i_0011156、i_0011202、c_0003076、c_0003080、c_0003400)
	//桌面核心、抽屉形式随意切换 , change by shlt@2015/01/23 ADD START
	public static final int LAUNCHER_STYLE_CORE = 0;//桌面模式，单层。
	public static final int LAUNCHER_STYLE_DRAWER = 1;//桌面模式，双层。
	public static int CONFIG_LAUNCHER_STYLE = LAUNCHER_STYLE_CORE;//桌面模式。0为单层，1为双层。默认为0。
	public static final String CONFIG_LAUNCHER_STYLE_KEY = "config_launcher_style_key";
	public static boolean ENABLE_HOTSEAT_FUNCTION_BUTTON = false;//底边栏功能页按钮的开关，true为显示，false为不显示，默认为false
	public static boolean ENABLE_HOTSEAT_ARROW_TIP = true;//底边栏功能页箭头开关，true为显示，false为不显示，默认为true
	//桌面核心、抽屉形式随意切换 , change by shlt@2015/01/23 ADD END
	public static String CONFIG_DEFAULT_THEME_PACKAGE_NAME = null;
	public static final String THEME_ICON_FOLDER = "theme/icon/80";
	//zhujieping add,主题中文件夹图标的位置
	public static final String THEME_FOLDER_ICON_DIR = "theme/folder/widget-folder-bg.png";
	//添加智能分类功能 , change by shlt@2015/02/09 ADD START
	public static int CONFIG_CATEGORY_TYPE = CategoryConstant.CAN_CATEGORY;//0:禁止，1：显示，2：可运营出来
	public static boolean SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE = false;//智能分类的文件夹中，是否支持显示推荐应用。 true为支持，false为不支持。默认为true。
	public static int CONFIG_CATEGORY_FOLDER_START_ADD_SCREENS_ID = 1;//智能分类后，智能分类文件夹从哪一页屏幕开始添加。0表示第一页。
	//添加智能分类功能 , change by shlt@2015/02/09 ADD END
	//	public static boolean SWITCH_ENABLE_SHOW_APPBAR_IN_APPLIST = false;//双层模式下，在主菜单中是否显示“应用”和“小组件”两个tab页的开关，true为显示两个tab页，false为只显示“应用”页，默认为false。
	public static boolean SWITCH_ENABLE_SHOW_MARKET_BUTTON_IN_APPBAR = false;//双层模式下，在主菜单的appbar中是否显示“应用市场”(包含“Intent.CATEGORY_APP_MARKET”属性的窗口)入口。true为显示，false为不显示。默认为false。
	public static boolean SWITCH_ENABLE_DEBUG_FPS = false;//是否显示fps
	public static boolean SWITCH_ENABLE_FINGER_EFFECT = false;//是否打开魔法手指效果功能。
	public static boolean SWITCH_CATEGORY_VISIABLE = false;//cheyingkun add	//桌面设置是否显示智能分类这一项
	//	public static boolean SWITCH_ENABLE_SEARCH_BAR = true; //WangLei add  //c_0003035 是否显示桌面顶部搜索框
	public static ArrayList<ComponentName> mHideAppList = new ArrayList<ComponentName>();//xiatian add	//桌面支持配置隐藏特定的activity界面。
	public static boolean SWITCH_ENABLE_TITLE_SHADOW = true;//xiatian add	//图标名称和文件夹名称，是否显示文字阴影。true为显示（详细配置见<style name="WorkspaceIcon">），false为不显示，默认为true。
	//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
	public static final int ITEM_STYLE_NORMAL = 0;//桌面图标显示的样式：item的图标和文字分离，图标在item的上半部，文字在item的下半部。
	public static final int ITEM_STYLE_ICON_EXTENDS_INTO_TITLE = 1;//桌面图标显示的样式：item的图标和文字重叠（图标是一个有图片形式区域和文字显示区域的大图），图标延伸至文字的下方显示（图标类似view的background，但是四周有留白）。
	public static int CONFIG_ITEM_STYLE = ITEM_STYLE_NORMAL;
	public static float ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE = 1f;
	//xiatian add end
	public static HashMap<ComponentName , Integer> mAppReplaceTitleList = new HashMap<ComponentName , Integer>();//xiatian add	//桌面支持配置特定的activity的显示名称。
	public static ComponentName CONFIG_CUSTOMER_WALLPAPER_COMPONENT_NAME = null;//xiatian add	//飞利浦需求，将美化中心改为他们的壁纸设置。
	public static boolean SWITCH_ENABLE_INSTALL_OPERATEICON_INFOLDER = false;
	//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
	public static final int DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE = 0;//默认主页和worspace的页数（第几页）绑定，和celllayout无关。
	public static final int DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT = 1;//默认主页和worspace的celllayout绑定。
	public static int CONFIG_WORKSPACE_DEFAULT_PAGE_STYLE = DEFAULT_PAGE_STYLE_BIND_WITH_CELLLAYOUT_INDEX_IN_WORKSPACE;
	//xiatian add end
	protected static Resources mResources;
	protected static Context mContext;
	public static boolean SWITCH_ENABLE_DISCLAIMER = false;// cheyingkun add //免责声明布局(是否显示免责声明。true为显示；false为不显示。默认为false。 关闭)
	public static ArrayList<ComponentName> mHideWidgetList = new ArrayList<ComponentName>();//xiatian add	//桌面支持配置隐藏特定的widget插件。
	public static ArrayList<ComponentName> mHideShortcutList = new ArrayList<ComponentName>();//xiatian add	//桌面支持配置隐藏特定的快捷方式插件。
	public static boolean SWITCH_ENABLE_DYNAMIC_ICON_DELETE = true;//是否允许桌面运营虚图标、虚链接删除
	public static boolean SWITCH_ENABLE_DOWNLOAD_CONFIRM_DIALOG = true;//点击虚图标下载，是否弹框提示
	//xiatian add start	//桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
	//【说明】现在运营的方式为：在桌面配置需要运营的包名类名和时间间隔。当前时间和手机注册时间（现在每个手机在第一次联网时，会上传数据到我们的服务器，服务器会记录下该时间，该时间为手机注册时间。）的差值大于时间间隔时，会在桌面（从第一页找空位）显示该配置的应用。
	public static HashMap<ComponentName , Long> mDelayShowAppList = new HashMap<ComponentName , Long>();
	public static final String ALLREADY_DELAY_SHOW_APP_LIST_KEY = "allready_delay_show_app_list_key";
	//xiatian add end
	;
	public static boolean SWITCH_ENABLE_ENLARGE_ICON_SIZE_WHEN_SOURCE_SIZE_LESS_THEN_DEST_SIZE = false;//cheyingkun add	//当图标尺寸小于目标尺寸时，是否放大图标尺寸（放大会导致图标模糊）。true为放大；false为不放大。默认为false。
	public static boolean SWITCH_ENABLE_CLINGS = true;//cheyingkun add	//是否显示新手引导
	//cheyingkun add start	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
	//【说明】启动页面样式。0为默认启动页（全屏的一个loading界面。颜色、文字和图片均可配置），1为进度圈启动页，-1为不显示loading界面。（默认启动页时不显示新手引导）
	public static final int LOADING_PAGE_STYLE_NONE = -1;//不显示loading界面
	public static final int LOADING_PAGE_STYLE_DEFAULT = 0;//默认启动页（全屏的一个loading界面。颜色、文字和图片均可配置）
	public static final int LOADING_PAGE_STYLE_PROGRESS = 1;//进度圈启动页
	public static int CONFIG_LOADING_PAGE_STYLE = LOADING_PAGE_STYLE_DEFAULT;
	//cheyingkun add end
	;
	//xiatian add start	//需求：编辑模式底边栏配置打开特定界面。预留两个可配置的button，这两个button配置intent即可打开特定界面（详见“BaseDefaultConfig”中说明）。
	//【说明】
	//	1、目前编辑模式的底边栏有四个按钮：美化中心、小组件、系统设置和桌面设置。现在预留两个可配置的button，这两个button配置包类名即可打开特定界面。
	//	2、配置步骤：
	//		2.1、这两个入口在“\res\layout”目录文件overview_panel.xml中配置，可参考已经存在的四个入口配置。
	//		2.2、“android:id=”这个参数一定要是“overview_panel_button_fallback_1_id”或者“overview_panel_button_fallback_2_id”
	//		2.3、“android:drawableTop”这个参数需要参考“overview_panel_button_beauty_center_selector.xml”创建文件“overview_panel_button_fallback_1_selector.xml”或者“overview_panel_button_fallback_2_selector.xml”
	//		2.4、需要参考“res\drawable-（不同分辨率）”相应目录下“overview_panel_button_beauty_center_icon_focus”和“overview_panel_button_beauty_center_icon_normal”，添加两张图片，并修改2.2中添加的xml文件中的图片名称
	//		2.5、“android:text”这个参数需要添加新的字符串
	//		2.6、“android:onClick”这个参数需要对应的写为“enterOverviewPanelButtonFallback_1”或者“enterOverviewPanelButtonFallback_2”
	//		2.7、需要配置配置项“config_overview_panel_button_fallback_1_intent”和“config_overview_panel_button_fallback_2_intent”
	//	3、两个预留入口的位置，可调整。
	public static Intent CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_1_INTENT = null;
	public static Intent CONFIG_OVERVIEW_PANEL_BUTTON_FALLBACK_2_INTENT = null;
	//xiatian add end
	;
	public static boolean SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE = false;//xiatian add	//底边栏图标是否显示名称。true为显示名称；false为不显示。默认为false。
	/**加载桌面过程中,加载手机应用是否切页到添加应用的那一页 默认false 不切页*/
	public static boolean SWITCH_ENABLE_BIND_ITEMS_ANIMATE_IN_LOADING = false;//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页
	public static boolean SWITCH_ENABLE_CUSTOMER_PHILIPS_WALLPAPER_CHANGED_NOTIFY = false;//cheyingkun add	//是否监听飞利浦壁纸改变广播【c_0003456】
	//xiatian add start	//Workspace中void moveToDefaultScreen(boolean animate )方法里，不进行“ getCurrentPage() != mDefaultPage ”判断。true为不判断，false为判断。默认为false。
	//【备注】
	//	打开桌面循环切页的前提下，默认配置中不配置插件或者找不到配置的插件时，
	//	从第一页向左切页到最后一页完成后PagedView的protected boolean computeScrollHelper()方法中， 
	//	mScroller.computeScrollOffset() 一直为true（切页完成后，应改为false）,
	//	导致Workspace中void moveToDefaultScreen(boolean animate )方法里的判断“ getCurrentPage() != mDefaultPage ”不成立，
	//	从而无法回到主页。
	public static boolean SWITCH_ENABLE_FORCE_SNAP_TO_DEFAULT_PAGE = false;
	public static boolean SWITCH_ENABLE_SHOW_APP_AUTO_CREATE_SHORTCUT = true;//cheyingkun add	//是否显示应用自动创建的快捷方式。true显示；false不显示。默认true。【c_0003466】
	public static boolean SWITCH_ENABLE_SHOW_SHORTCUT_IN_WIDGET_LIST = true;//cheyingkun add	//是否在小部件界面中显示快捷方式。true显示；false不显示。默认true。
	public static boolean SWITCH_ENABLE_UMENG = true;//xiatian add	//需求：运营友盟（详见“OperateUmeng”中说明）。
	public static String CONFIG_ONRESUME_BROADCAST_ACTION = null;//xiatian add	//在桌面onResume的时候，发送广播通知客户。配置为空则不发送广播。默认为空。	
	public static String CONFIG_ONSTOP_BROADCAST_ACTION = null;//xiatian add	//在桌面onStop的时候，发送广播通知客户。配置为空则不发送广播。默认为空。	
	public static boolean SWITCH_ENABLE_SHOW_CAMERA_PAGE = true;//lvjiangbin add	//是否显示相机页 默认显示 true
	public static boolean SWITCH_ENABLE_SHOW_MUSIC_PAGE = true;//lvjiangbin add	//是否显示音乐页 默认显示 true
	public static boolean SWITCH_ENABLE_SHOW_MEDIA_PAGE = true;//lvjiangbin add	//是否显示功能页默认显示 true
	// zhangjin@2016/03/29 ADD START
	public static boolean HERUNXIN_BIG_LAUNCHER = false;
	// zhangjin@2016/03/29 ADD END
	//yangmengchao  add start  //需求：迅虎增加桌面小部件时间日期格式样式。0为默认样式，1为迅虎样式。默认为0
	public static final int TIMER_WIDGET_DATE_STYLE_DEFAULT = 0;//	默认桌面时间小部件日期格式样式
	public static final int TIMER_WIDGET_DATE_STYLE_XH = 1; // 迅虎桌面时间小部件日期格式样式
	public static int CONFIG_TIMER_WIDGET_DATE_STYLE = TIMER_WIDGET_DATE_STYLE_DEFAULT;
	//yangmengchao  add end
	// zhangjin@2015/12/10 ADD START
	public static boolean LAUNCHER_UPDATE = true;
	// zhangjin@2015/12/10 ADD END
	//cheyingkun add start	//解决“改变系统字体后，飞利浦图标样式下，文件夹和图标名称偏移”的问题。【c_0003610】
	public static float SYSTEM_FONT_SIZE_SMALL = 0.85f;
	public static float SYSTEM_FONT_SIZE_NORMAL = 1.0f;
	public static float SYSTEM_FONT_SIZE_LARGE = 1.15f;
	public static float SYSTEM_FONT_SIZE_HUGE = 1.3f;
	//cheyingkun add end
	public static boolean SWITCH_ENABLE_SHORTCUT_WIDGET_NAME_FOLLOW_SYSTEM_LANGUAGE = true;//cheyingkun add	//快捷方式名称是否跟随系统语言变化。true为跟随系统语言变化；false为不跟随。默认true。【c_0003657】
	public static String CONFIG_CUSTOM_WALLPAPERS_PATH = null;//xiatian add	//德盛伟业需求：本地化默认壁纸路径。客户可配置的桌面壁纸路径，如"/system/wallpapers"，再在该路径下放置客户的壁纸图片。配置为空则显示"\assets\launcher\wallpapers"中的壁纸。
	public static String CONFIG_CUSTOM_DEFAULT_WALLPAPER_NAME = null;//cheyingkun add	//默认壁纸本地化。【c_0003753】
	public static boolean SWITCH_ENABLE_STATUS_BAR_AND_NAVIGATION_BAR_TRANSPARENT = false;//cheyingkun add	//是否开启“状态栏透明”和“导航栏透明”效果，安卓4.4以上有效。
	public static boolean SWITCH_ENABLE_CUSTOM_LAYOUT = false;//cheyinkgun add start	//自定义桌面布局
	// @gaominghui2016/01/05 ADD START
	public static boolean SWITCH_ENABLE_SHOW_ICON_TITLE_FADE_OUT = false;//和兴六部 android5.0以上版本实现桌面图标标题过长，渐隐的效果。true默认android5.0以上文字渐隐，false默认文字过长后面加省略号，默认false 
	// @gaominghui2016/01/05 ADD END
	//cheyingkun add start	//解决“文件夹原生层叠效果，第一次拖动图标生成文件夹时，桌面重启”的问题。【i_0013310】
	public static final int FOLDER_ICON_PREVIEW_OVERLAP_KITKAT = 0;//4.4的层叠样式。（效果图详见“Phenix桌面“配置项”说明.pdf”）
	public static final int FOLDER_ICON_PREVIEW_GRIDS = 1;//宫格样式
	public static final int FOLDER_ICON_PREVIEW_CIRCLE_ANDROID7 = 2;//7.1的样式（1、文件夹背景图为圆形；2、文件夹内小图标超出文件夹背景的部分，不显示）
	public static final int FOLDER_ICON_PREVIEW_OVERLAP_MARSHMALLOW = 3;//6.0的层叠样式。（效果图详见“Phenix桌面“配置项”说明.pdf”）
	public static int CONFIG_FOLDER_ICON_PREVIEW_STYLE = FOLDER_ICON_PREVIEW_GRIDS;
	//cheyingkun add end
	public static boolean SWITCH_ENABLE_OVERVIEW_PANEL_TEXT_HINT = false;//cheyingkun add	//编辑模式是否显示提示信息（提示信息内容为“拖动页面可改变页面位置”）。true为显示；false为不显示。默认false。【c_0004055】
	//cheyingkun add start	//phenix1.1稳定版移植酷生活
	//是否打开-1页
	public static boolean SWITCH_ENABLE_FAVORITES = true;
	//普通页是否显示桌面顶部搜索框，true时显示，false时隐藏lvjiangbin add
	public static boolean SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE = true;
	//负一屏是否显示桌面顶部搜索框，true时显示，false时隐藏lvjiangbin add
	public static boolean SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE = true;
	//cheyinkgun add end
	public static boolean SWITCH_ENABLE_SEARCH_BAR_SHOW_VOICE_BUTTON = false;//cheyingkun add	//搜索栏是否支持显示语音搜索的按钮。true为支持；false为不支持。默认true。
	//chenchen  add start
	public static boolean NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION = false;//手机不插Sim卡时桌面不显示Sim卡应用图标
	public static ArrayList<ComponentName> mHideSimCardList = new ArrayList<ComponentName>();//配置所需的所有包类名
	//chenchen  add end
	// YANGTIANYU@2016/06/30 ADD START
	/**相机页广告显示开关*/
	public static boolean SWITCH_ENABLE_CAMERAPAGE_AD_SHOW = false;
	/**音乐页广告显示开关*/
	public static boolean SWITCH_ENABLE_MUSICPAGE_AD_SHOW = false;
	// YANGTIANYU@2016/06/30 ADD END
	public static boolean SWITCH_ENABLE_TIMER_WIDGET_SECOND_HAND_FLASH = false;//lvjiangbin add 时钟插件中，小时和分钟之间的“两个点（秒针）”是否闪烁。true为闪烁；false为不闪烁。默认true。
	public static boolean SWITCH_ENABLE_REMOVE_SPACE_IN_APP_TITLE = true;//cheyingkun add	//是否除去应用名称中的空格。true为去除空格；false为不去除空格。默认true。【c_0004348】
	public static boolean SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE = true;//xiatian add	//设置默认桌面引导（是否支持"设置默认桌面引导"功能。true为支持；false为不支持。默认为true。）
	public static boolean SWITCH_ENABLE_MTK_SET_WALLPAPER = false;//lvjangbin add//MTK 设置单屏壁纸的时候，第二次启动需要重新设置大小，否则会被拉伸。
	public static String CONFIG_CUSTOM_THEME_PATH = null;
	//xiatian start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
	//	public static final String CUSTOM_DEFAULT_CONFIG = "/system/launcher/launcher_default_config.xml";//xiatian del
	//xiatian add start
	public static final String CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT = "/system/launcher";
	public static String CUSTOM_DEFAULT_CONFIG_PATH = CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
	public static final String CUSTOM_DEFAULT_CONFIG_FILE_NAME = "launcher_default_config.xml";
	//xiatian add end
	//xiatian end
	public static ConfigUtils mConfigUtils;
	public static String CONFIG_CUSTOM_OPERATE_PATH = null;
	public static boolean SWITCH_ENABLE_OVERVIEW_FREESCROLL = false;//cheyingkun add	//编辑模式下，滑动页面松手后是否自动切页。true为自动切页；false为不自动切页。默认为false。
	public static boolean SWITCH_ENABLE_OVERVIEW_SHOW_PAGEINDICATOR = false;//cheyingkun add	//编辑模式下，是否显示页面指示器。true为显示；false为不显示。默认为false。
	public static float CONFIG_PAGEINDICATOR_SCALE = 0.5f;//cheyingkun add	//非当前页面的页面指示器的缩放比。默认为0.5。
	public static boolean SWITCH_ENABLE_PRESS_NUMS_OR_STAR_OR_POUND_TO_DAIL = false;//xiatian add	//是否支持"点击数字键打开拨号界面"的功能。true为支持，false为不支持。默认false。
	public static boolean SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH = false;//xiatian add	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
	public static boolean SWITCH_ENABLE_WORKSPACE_ICON_HIGHLIGHT_WHEN_SELECTED = true;//cheyingkun add	//按键选中图标时，图标是否高亮。true为高亮，false为不高亮。默认true。【c_0004474】
	//xiatian add start	//添加配置项“config_search_bar_type”，搜索栏中搜索的配置参数。0为酷搜，1为安卓的全局搜索。默认为0。（详见BaseDefaultConfig.java中的“SEARCH_BAR_STYLE_XXX”）
	//1、“酷搜”的备注如下：
	//	（1）搜索按钮图标为search_bar_search_button_icon_fallback_selector
	//	（2）是否显示语音按钮,由“switch_enable_search_bar_show_voice_button”控制
	//	（3）显示语音按钮时,语音按钮的图标为search_bar_voice_button_icon_fallback
	//2、“安卓的全局搜索”的备注如下：
	//	（1）搜索按钮图标：优先到支持搜索的apk中查找（见Launcher.java中updateGlobalSearchIcon()），找不到的话则为search_bar_search_button_icon_fallback_selector
	//	（2）是否显示语音按钮,由“switch_enable_search_bar_show_voice_button”和“手机是否安装支持语音搜索的apk”控制
	//	（3）显示语音按钮时,语音按钮的图标：优先到支持搜索的apk中查找（见Launcher.java中updateVoiceSearchIcon(boolean searchVisible )），找不到的话则为search_bar_voice_button_icon_fallback	
	public static final int SEARCH_BAR_STYLE_COOEE = 0;//搜索栏使用酷搜。
	public static final int SEARCH_BAR_STYLE_GLOBAL_SEARCH = 1;//搜索栏使用安卓的全局搜索。
	public static int CONFIG_SEARCH_BAR_STYLE = SEARCH_BAR_STYLE_COOEE;
	//xiatian add end
	//gaominghui add start
	public static boolean SWITCH_ENABLE_SHOW_BEAUTYCENTER_THEME_TAB = true;//是否允许显示美化中心主题tab页，默认true；
	public static boolean SWITCH_ENABLE_SHOW_BEAUTYCENTER_LOCK_TAB = true;//是否允许显示美化中心锁屏tab页，默认true；
	public static boolean SWITCH_ENABLE_SHOW_BEAUTYCENTER_WALLPAPER_TAB = true;//是否允许显示美化中心壁纸tab页，默认true；
	//gaominghui add end
	public static boolean SWITCH_ENABLE_SHOW_LAUNCHER_STYLE_MENU_IN_LAUNCHER_SETTING = true;
	public static boolean SWITCH_ONE_KEY_CHANGE_WALLPAPER = false;//zhujieping add,一键换壁纸的开关，如果关闭，不需要初始化，若打开，需要将开关打开，同时配置图标和manifest
	public static boolean SWITCH_ENABLE_CATEGORY_SHOW_NOTIFICATION = true;//xiatian add	//智能分类功能开启后，是否允许在通知栏显示建议智能分类的通知。 true为允许显示通知，false为不允许显示通知。默认为true。
	public static final int APPLIST_BAR_STYLE_NO_BAR = -1;//不显示菜单栏
	public static final int APPLIST_BAR_STYLE_TAB = 0;//4.4的tabhost
	public static final int APPLIST_BAR_STYLE_S5 = 1;//仿S5：1、菜单栏右侧显示“三个点”，点击后打开一个menu菜单；2、menu菜单有三个选项（排序、隐藏应用、编辑应用）
	public static final int APPLIST_BAR_STYLE_TITLE = 2;//菜单栏显示“应用”两个字
	//zhujieping add start	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。
	public static final int APPLIST_BAR_STYLE_S6 = 3;//仿S6：1、菜单栏平铺按钮（搜索、A-Z、编辑）；2、点击“搜索”支持打开特定界面；3、点击“A-Z”按照名称排序；4.点击编辑，进入卸载模式
	//zhujieping add end
	//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项4。4在主菜单上显示搜素栏。
	public static final int APPLIST_BAR_STYLE_SEARCH_BAR = 4;
	//zhujieping add end
	//zhujieping add start //拓展配置项“config_applistbar_style”，添加可配置项5。5在主菜单上方最左边显示“应用”，点击弹出选择排序的dialog。
	public static final int APPLIST_BAR_STYLE_SORT_APP = 5;
	//zhujieping add end
	public static int CONFIG_APPLIST_BAR_STYLE = APPLIST_BAR_STYLE_NO_BAR;
	public static boolean LAST_INSTALLED_APP_SORT_ON_HEAD = false;
	//xiatian add start	//meun键点击后（menu键的onKeyUp事件当做点击事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）或退出编辑模式（当前是编辑模式）；1为打开“最近任务”界面；2为打开“竖直列表”样式的桌面菜单。默认为0。
	public static final int MENU_CLICK_STYLE_NONE = -1;//不处理menu键的click事件。
	public static final int MENU_CLICK_STYLE_ENTER_OR_EXIT_EDIT_MODE = 0;//进入编辑模式（当前不是编辑模式）或退出编辑模式（当前是编辑模式）。
	public static final int MENU_CLICK_STYLE_OPEN_RECENTS_ACTIVITY = 1;//打开“最近任务”界面。
	public static final int MENU_CLICK_STYLE_WORKSPACE_MENU_VERTICAL_LIST = 2;//“竖直列表”样式的桌面菜单。	//xiatian add	//需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
	public static int CONFIG_MENU_CLICK_STYLE = MENU_CLICK_STYLE_ENTER_OR_EXIT_EDIT_MODE;
	//xiatian add end
	;
	//xiatian add start	//meun键长按后（menu键的onKeyDown事件中判断mKeyEvent.isLongPress()为true，则当做长按事件），响应不同的事件。-1为不处理；0为进入编辑模式（当前不是编辑模式）；1为打开“最近任务”界面。默认为-1。
	public static final int MENU_LONG_CLICK_STYLE_NONE = -1;//不处理menu键的long click事件。
	public static final int MENU_LONG_CLICK_STYLE_ENTER_EDIT_MODE = 0;//进入编辑模式。
	public static final int MENU_LONG_CLICK_STYLE_OPEN_RECENTS_ACTIVITY = 1;//打开“最近任务”界面。
	public static int CONFIG_MENU_LONG_CLICK_STYLE = MENU_LONG_CLICK_STYLE_NONE;
	//xiatian add end
	;
	public static boolean SWITCH_ENABLE_RESPONSE_ONKEYLISTENER = true;//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
	//xiatian add start	//编辑模式底边栏按钮配置为壁纸时，打开不同的壁纸选择界面。0为美化中心的壁纸tab；1为launcher3的壁纸选择界面；2为uni3的壁纸选择界面；3为客户自定义的壁纸选择界面；4为系统的选择壁纸应用的界面。5为编辑模式下的二级界面。默认为0。
	//1、“launcher3的壁纸选择界面”的备注如下：
	//	（1）一个壁纸的配置方式为两张图片：一张为设置壁纸的图片，一张为壁纸预览图。如：wallpaper00.png和wallpaper00_small.png。
	//	（2））壁纸优先读取“config_custom_wallpapers_path_launcher_3”配置的路径下的壁纸
	//	（3）若上述地址没有壁纸，则读取“system_wallpaper_directory”配置的路径下的壁纸
	//	（4）若上述地址没有壁纸，则读取“partner_wallpapers”中配置的壁纸（壁纸位于文件夹“res\drawable”）
	//	（5）需要在“AndroidManifest.xml”中，将“com.android.launcher3.WallpaperPickerActivity”的android:enabled设置为true	
	//	（6）需要在“AndroidManifest.xml”中，将“com.android.launcher3.WallpaperCropActivity”的android:enabled设置为true		
	//2、“uni3的的壁纸选择界面”的备注如下：
	//	（1）一个壁纸的配置方式为两张图片：一张为设置壁纸的图片，一张为壁纸预览图。如：wallpaper00.png和wallpaper00_small.png。
	//	（2）壁纸优先读取“config_custom_wallpapers_path”配置的路径下的壁纸
	//	（3）当“config_custom_wallpapers_path”配置为空或者配置的路径不存在，则读取目录“assets\launcher\wallpapers”中的壁纸	
	//	（4）会读取读取t卡根目录“Coco\Wallpaper\App”中的壁纸
	//	（5）需要在“AndroidManifest.xml”中，将“com.iLoong.launcher.desktop.WallpaperChooser”的android:enabled设置为true	
	//3、“客户自定义的的壁纸选择界面”的备注如下：
	//	（1）必须要配置config_customer_wallpaper_component_name	
	//4、“系统的选择壁纸应用的界面”的备注如下：
	//	（1）“pick_wallpaper_activity_title”为空时，使用默认名称
	public static final int EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_BEAUTY_CENTER = 0;//美化中心的壁纸tab
	public static final int EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_LAUNCHER3 = 1;//launcher3的壁纸选择界面
	public static final int EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_UNI3 = 2;//uni3的的壁纸选择界面
	public static final int EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_CUSTOMER = 3;//客户自定义的的壁纸选择界面
	public static final int EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_PICK_ACTIVITY = 4;//系统的选择壁纸应用的界面	//xiatian add	//需求：拓展配置项“config_edit_mode_button_enter_wallpaper_style”，添加可配置项4。4为打开“系统的选择壁纸应用的界面”。	
	public static final int EDIT_MODE_BUTTON_ENTER_WALLPAPER_SECONDARY_INTEFACE = 5;//进入编辑模式二级界面
	public static int CONFIG_EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE = EDIT_MODE_BUTTON_ENTER_WALLPAPER_STYLE_BEAUTY_CENTER;//
	//xiatian add end
	;
	//xiatian add start	//需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
	public static boolean SWITCH_ENABLE_OPERATE_EXPLORER = false;
	public static String CONFIG_OPERATE_EXPLORER_HOME_WEBSITE = "";
	//xiatian add end
	;
	public static int CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_APPLIST = -1;
	public static int CONFIG_ANIMATION_DURATION_WHEN_APPLIST_TO_WORKSPACE = -1;
	public static int CONFIG_ANIMATION_DURATION_WHEN_WORKSPACE_TO_EDITMODE = -1;
	public static int CONFIG_ANIMATION_DURATION_WHEN_EDITMODE_TO_WORKSPACE = -1;
	//xiatian add start	//meun键响应事件的类型。0为在onKeyDown和onKeyUp中响应事件（支持menu键点击和长按）；1为在onPrepareOptionsMenu中响应事件（不支持menu键长按）。默认为0。
	//【备注】
	//	1、客户手机（锐益，展讯6.0），偶数次点击menu键，高概率出现偶数次点击menu键，menu键消息传不到桌面（出现该情况后，放一段时间或者重启后，会自动恢复正常）【c_0004536】。
	//	2、我们给客户指出了问题点，但是客户找不到原因，这个算是我们的临时方案。
	public static final int MENU_KEY_STYLE_RESPONSE_IN_ON_KEY = 0;//onKeyDown和onKeyUp中响应事件（支持menu键点击和长按）
	public static final int MENU_KEY_STYLE_RESPONSE_IN_ON_PREPARE_OPTIONS_MENU = 1;//在onPrepareOptionsMenu中响应事件（不支持menu键长按）
	public static int CONFIG_MENU_KEY_STYLE = MENU_KEY_STYLE_RESPONSE_IN_ON_KEY;
	//xiatian add end
	;
	public static ArrayList<ComponentName> mAppsShowInApplist = new ArrayList<ComponentName>();//xiatian add	//主菜单支持配置显示特定activity。
	public static final int EDIT_MODE_BUTTON_ENTER_THEME_STYLE_BEAUTY_CENTER = 0;//美化中心的壁纸tab
	public static final int EDIT_MODE_BUTTON_ENTER_THEME_SECONDARY_INTEFACE = 1;//进入编辑模式二级界面
	public static int CONFIG_EDIT_MODE_BUTTON_ENTER_THEME_STYLE = EDIT_MODE_BUTTON_ENTER_THEME_STYLE_BEAUTY_CENTER;
	public static boolean SWITCH_ENABLE_SET_HOME_PAGE_IN_OVERVIEW_MODE = false;//gaominghui add  //添加配置项“switch_enable_set_home_page_in_overview_mode”，是否支持编辑模式设置home页 的功能。true为支持，false为不支持。默认为false。
	public static String CONFIG_WINDOW_LOST_FOCUS_BROADCAST_ACTION = "";
	public static String CONFIG_WINDOW_GET_FOCUS_BROADCAST_ACTION = "";
	//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	public static HashMap<String , Integer> mConfigFuntionPagesPosition = new HashMap<String , Integer>();
	public static final String FUNCTION_PAGES_POSITION_ITEM_KEY_FAVORITES_PAGE = "酷生活";
	public static final String FUNCTION_PAGES_POSITION_ITEM_KEY_CAMERA_PAGE = "相机页";
	public static final String FUNCTION_PAGES_POSITION_ITEM_KEY_MUSIC_PAGE = "音乐页";
	public static final int FUNCTION_PAGES_POSITION_INDEX_KEY_LEFT_OF_NORMAL_PAGE_1 = -1;//普通页面左边的第一页
	public static final int FUNCTION_PAGES_POSITION_INDEX_KEY_LEFT_OF_NORMAL_PAGE_2 = -2;//普通页面左边的第二页（在“普通页面左边的第一页”的左边）
	public static final int FUNCTION_PAGES_POSITION_INDEX_KEY_LEFT_OF_NORMAL_PAGE_3 = -3;//普通页面左边的第三页
	public static final int FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_1 = 1;//普通页面右边的第一页
	public static final int FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_2 = 2;//普通页面右边的第二页（在“普通页面右边的第一页”的右边）
	public static final int FUNCTION_PAGES_POSITION_INDEX_KEY_RIGHT_OF_NORMAL_PAGE_3 = 3;//普通页面右边的第三页
	public static final String FUNCTION_PAGES_POSITION_KEY_IN_SHARED_PREFERENCES = "config_funtion_pages_position_key_in_sp";
	//xiatian add end
	;
	public static int APPLIST_SYTLE_KITKAT = 0;//4.4的主菜单样式：1、应用分页面显示；2、有菜单栏（可配置不同风格，详见：config_applistbar_style）
	public static int APPLIST_SYTLE_MARSHMALLOW = 1;//6.0的主菜单样式：1、应用显示在一个可以上下滑动的页面；2、有搜索栏（可配置不同风格，详见：switch_enable_marshmallow_mainmenu_search）
	//zhujieping add start  //拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。	
	public static int APPLIST_SYTLE_NOUGAT = 2;//7.0的主菜单样式：1、同6.0主菜单样式； 2、应用根据首字母进行排列显示，首字母不同的用分隔线隔开，每段开头显示当前首字母；3、应用右侧列出所有应用的首字母，并随着应用滑动高亮相应的首字母”
	//zhujieping add end
	public static int CONFIG_APPLIST_STYLE = APPLIST_SYTLE_KITKAT;
	//zhujieping add start //当主菜单配置为7.0样式时，true为显示常用应用，false为不显示，CONFIG_APPLIST_STYLE =2才有效果
	public static boolean SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS = true;
	//zhujieping add end
	;
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public static final int APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT = 0;
	public static final int APPLIST_IN_AND_OUT_ANIM_STYLE_NOUGAT = 1;
	public static final int APPLIST_IN_AND_OUT_ANIM_STYLE_S8 = 2;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	public static int CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE = APPLIST_IN_AND_OUT_ANIM_STYLE_KITKAT;
	//zhujieping add end
	;
	public static String SCROLL_BY_BROADCAST = null;//xiatian add	//通知桌面切页：“晨想”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerByBroadcast.java”中的备注。
	public static boolean SWITCH_ENABLE_CUSTOMER_DSWY_PROXIMITY_SENSOR_SNAP_PAGE = false;//xiatian add	//通知桌面切页：“德盛伟业”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerDSWY.java”中的备注。
	public static boolean SWITCH_ENABLE_CUSTOMER_RY_PROXIMITY_SENSOR_SNAP_PAGE = false;//xiatian add	//通知桌面切页：“锐益”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerRY.java”中的备注。
	public static boolean SWITCH_ENABLE_CUSTOMER_XH_FINGER_PRINT_SCROLL = false;//xiatian add	//通知桌面切页：“讯虎”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerCustomerXH.java”中的备注。
	//xiatian add start	//需求：支持某些特定按键触发桌面切页。
	public static ArrayList<Integer> mConfigKeyEventNotifySnapToLeft = new ArrayList<Integer>();
	public static ArrayList<Integer> mConfigKeyEventNotifySnapToRight = new ArrayList<Integer>();
	//xiatian add end
	//chenliang add start	//添加配置项“switch_enable_customer_lm_set_lockwallpaper”，增加锁屏壁纸的功能（进入uni3桌面壁纸，点击设置，弹出“设置桌面”、“设置锁屏”和“同时设置”选项）。true为显示，false不显示。默认为false。备注：该功能需要系统底层支持。
	public static boolean SWITCH_ENABLE_CUSTOMER_LM_SET_LOCKWALLPAPER = false;
	//chenliang add end
	;
	public static boolean XUNHU_SENSOR = false;//gaominghui add  //添加配置项“xunhu_sensor”,是否支持"讯虎定制特殊传感器切页"的功能。true为支持，false为不支持。默认false。
	public static boolean XUNHU_PROXIMITY_SENSOR_SCROLL = false;//gaominghui add //添加配置项“xunhu_proximity_sensor_scroll”,是否支持"讯虎定制普通光感切页"的功能。true为支持，false为不支持。默认false。
	public static boolean SWITCH_ENABLE_RESPONSE_BLANK_OF_LONGCLICK = true;//gaominghui add	//添加配置项“switch_enable_response_blank_of_longclick”，桌面空白处是否响应长按消息。true为响应长按，false为不响应长按。默认true。
	//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
	public static boolean SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE = false;
	public static boolean SWITCH_ENABLE_SHOW_WORKSPACE_SCROLL_TYPE_IN_LAUNCHER_SETTINGS = false;
	public static final String CONFIG_WORKSPACE_SCROLL_TYPE_KEY = "config_workspace_scroll_type_key";
	//xiatian add end
	;
	//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
	public static boolean SWITCH_ENABLE_APPLIST_LOOP_SLIDE = false;
	public static boolean SWITCH_ENABLE_SHOW_APPLIST_SCROLL_TYPE_IN_LAUNCHER_SETTINGS = false;
	public static final String CONFIG_APPLIST_SCROLL_TYPE_KEY = "config_applist_scroll_type_key";
	//xiatian add end
	;
	//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
	public static boolean SWITCH_ENABLE_WIDGET_LOOP_SLIDE = false;
	public static boolean SWITCH_ENABLE_SHOW_WIDGET_SCROLL_TYPE_IN_LAUNCHER_SETTINGS = false;
	public static final String CONFIG_WIDGET_SCROLL_TYPE_KEY = "config_widget_scroll_type_key";
	//xiatian add end
	;
	public static boolean FINISH_ACTIVITY_WHEN_SET_UNI3_WALLPAPER_SUCCESSFULLY = true;//zhujieping add	//添加配置项“finish_activity_when_set_uni3_wallpaper_successfully”，当com.iLoong.launcher.desktop.WallpaperChooser配置enable为true时，true为关闭activity，false为不关闭activity，弹出设置成功的toast。	
	public static boolean SWITCH_ENABLE_EXIT_OVERVIEW_MODE_WHEN_APPLY_THEME_FROM_BEAUTYCENTER = false;//添加配置项“switch_enable_exit_overview_mode_when_apply_theme_from_beautycenter” ,编辑模式进入美化中心应用主题，应用主题的同时是否退出编辑模式，true退出，false不退出，默认false。//gaomignhui add
	public static boolean SWITCH_ENABLE_EXTEND_DROP_BAR_AREA = false;//xiatian add	//添加配置项“switch_enable_extend_drop_bar_area”，是否支持扩大“垃圾筐”和“应用信息框”的响应区域。true时为“1、单独显示一个时：上为屏幕顶端、左为搜索框左边框、右为搜索框右边框；2、显示两个时：上为屏幕顶端、宽度均分搜索框”；false时为“图标和文字区域的边界”。默认false。
	//xiatian add start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
	public static boolean SWITCH_ENABLE_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH = false;
	public static final String CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_KEY = "ro.lxt.useuichange";//模式关键字
	public static final int CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST = 0;//“读取广播”模式
	public static final int CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_NV_MAP = 1;//“读取nv映射表”模式
	public static final String CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_ACTION = "cooee_phenix_customer_lxt_change_custom_config_path_style_broadcast_action";
	public static final String CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_PATH = "cooee_phenix_customer_lxt_change_custom_config_path_style_broadcast_path";
	public static final String CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_FILE_DIR = "/cooee/launcher/phenix";
	public static final String CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_FILE_NAME = "CustomerLXTChangeCustomConfigPathTemp";
	public static final String CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_NV_MAP_KEY_PATH = "persist.sys.bootanimation";//“读取nv映射表”模式的映射表的关键字	
	//xiatian add end
	;
	public static final String CUSTOM_WALLPAPER_PATH_LAUNCHER_3_KEY = "config_custom_wallpapers_path_launcher_3";//xiatian add	//添加配置项“config_custom_wallpapers_path_launcher_3”，launcher3壁纸选择界面的壁纸路径的本地化配置。默认为空。
	public static final String LAUNCHER_3_WALLPAPER_PREVIEW_STYLE_KEY = "config_custom_wallpapers_preview_style_launcher_3";//fulijuan add //  //添加配置项“config_launcher_3_wallpapers_preview_style”，launcher3壁纸选择界面的壁纸预览图显示样式。0为可拖动且居中显示 ，1为不可拖动且居中显示。默认为0。 
	public static boolean SWITCH_ENABLE_CUSTOMER_LJ_NOTIFY_APPLY_THEME = false;//gaominghui add //添加配置项“switch_enable_customer_lj_notify_apply_theme”，应用主题后，是否通知客户主题已经更换，true为通知，false为不通知，默认为false。【c_0004704】。
	public static int[] mConfigEmptyScreenIdArrayInDrawer = null;//zhujieping add  //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	public static int[] mConfigEmptyScreenIdArrayInCore = null;//zhujieping add  //添加配置项“config_empty_screen_id_in_core”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	public static boolean SWITCH_ENABLE_DRAG_ITEM_PUSH_NORMAL_ITEM_IN_WORKSPACE = true;//xiatian add	添加配置项“switch_enable_drag_item_push_other_item_in_workspace”，是否支持“被拖动的桌面图标（应用图标、文件夹、插件），推动其他图标”的功能。true为支持；false为不支持。默认true。
	public static boolean SWITCH_ENABLE_HOTSEAT_ALLAPPS_BUTTON_SHOW_TITLE_WHEN_HOTSEAT_ITEM_SHOW_TITLE = true;//yangmengchao add       //添加配置项“switch_enable_hotseat_allapps_button_show_title_when_hotseat_item_show_title”，底边栏显示图标名称前提下，主菜单入口图标是否显示名称。true为显示名称；false为不显示。默认为true。
	public static boolean SWITCH_ENABLE_EFFECT_IN_FUNCTION_PAGES = false;//yangmengchao add //添加配置项“switch_enable_effect_in_function_pages”，功能页是否支持切页特效。true为支持；false为不支持。默认为false。
	
	public static String getString(
			int resID )
	{
		if( mConfigUtils != null )
		{
			return mConfigUtils.getString( mResources.getResourceEntryName( resID ) , mResources.getString( resID ) );
		}
		return mResources.getString( resID );
	}
	
	public static int getInt(
			int resID )
	{
		if( mConfigUtils != null )
		{
			return mConfigUtils.getInteger( mResources.getResourceEntryName( resID ) , mResources.getInteger( resID ) );
		}
		return mResources.getInteger( resID );
	}
	
	public static float getFloatDimension(
			int resID )
	{
		if( mConfigUtils != null )
		{
			return mConfigUtils.getDimension( mResources.getResourceEntryName( resID ) , mResources.getDimension( resID ) );
		}
		return mResources.getDimension( resID );
	}
	
	public static boolean getBoolean(
			int resID )
	{
		if( mConfigUtils != null )
		{
			return mConfigUtils.getBoolean( mResources.getResourceEntryName( resID ) , mResources.getBoolean( resID ) );
		}
		return mResources.getBoolean( resID );
	}
	
	public static int getIntDimension(
			int resID )
	{
		return (int)getFloatDimension( resID );
	}
	
	public static String[] getStringArray(
			int resID )
	{
		if( mConfigUtils != null )
		{
			ArrayList<String> array = mConfigUtils.getStringArray( mResources.getResourceEntryName( resID ) );
			if( array != null )
			{
				return (String[])array.toArray( new String[array.size()] );
			}
		}
		return mResources.getStringArray( resID );
	}
	
	public static int getDimensionPixelSize(
			int resID )
	{
		if( mConfigUtils != null )
		{
			return mConfigUtils.getDimensionPixelSize( mResources.getResourceEntryName( resID ) , mResources.getDimensionPixelSize( resID ) );
		}
		return mResources.getDimensionPixelSize( resID );
	}
	
	//xiatian add start	//需求：本地化（配置文件）支持“integer-array”类型数据。
	public static int[] getIntArray(
			int resID )
	{
		if( mConfigUtils != null )
		{
			return mConfigUtils.getIntegerArray( mResources.getResourceEntryName( resID ) , mResources.getIntArray( resID ) );
		}
		return mResources.getIntArray( resID );
	}
	//xiatian add end
	;
	
	//xiatian add start	//需求：支持某些特定按键触发桌面切页。
	public static boolean isNeedSnapToLeft(
			KeyEvent mKeyEvent )
	{
		boolean mIsNeedSnapToLeft = false;
		if(
		//
		( mKeyEvent.getAction() == KeyEvent.ACTION_UP )
		//
		&& ( mConfigKeyEventNotifySnapToLeft.contains( mKeyEvent.getKeyCode() ) )
		//
		)
		{
			mIsNeedSnapToLeft = true;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( "mConfigKeyEventNotifySnapToLeft" , StringUtils.concat( "mIsNeedSnapToLeft=" , mIsNeedSnapToLeft ) );
		}
		return mIsNeedSnapToLeft;
	}
	
	public static boolean isNeedSnapToRight(
			KeyEvent mKeyEvent )
	{
		boolean mIsNeedSnapToRight = false;
		if(
		//
		( mKeyEvent.getAction() == KeyEvent.ACTION_UP )
		//
		&& ( mConfigKeyEventNotifySnapToRight.contains( mKeyEvent.getKeyCode() ) )
		//
		)
		{
			mIsNeedSnapToRight = true;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( "mConfigKeyEventNotifySnapToRight" , StringUtils.concat( "mIsNeedSnapToRight=" , mIsNeedSnapToRight ) );
		}
		return mIsNeedSnapToRight;
	}
	//xiatian add end
}
