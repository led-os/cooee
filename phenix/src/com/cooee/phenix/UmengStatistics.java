package com.cooee.phenix;


// cheyingkun add Whole file //添加友盟统计自定义事件
/**
 * 友盟统计自定义事件id
 * @author cheyingkun
 */
public class UmengStatistics
{
	
	/**搜索栏*/
	public static final String ENTER_SEARCH_BAR = "enter_search_bar";
	/**编辑模式*/
	public static final String ENTER_EDIT_MODE = "enter_edit_mode";
	/**打开Phenix工具文件夹*/
	public static final String ENTER_PHENIX_TOOLS_FOLDER = "enter_phenix_tools_folder";
	/**美化中心图标*/
	public static final String ENTER_BEAUTY_CENTER_BY_ICON = "enter_beauty_center_by_icon";
	/**智能分类图标*/
	public static final String ENTER_CATEGORY_BY_ICON = "enter_category_by_icon";
	/**桌面设置图标*/
	public static final String ENTER_LAUNCHER_SETTING_BY_ICON = "enter_launcher_setting_by_icon";
	/**编辑模式美化中心*/
	public static final String ENTER_BEAUTY_CENTER_BY_EDIT_MODE = "enter_beauty_center_by_edit_mode";
	/**编辑模式小部件*/
	public static final String ENTER_WIDGETS_BY_EDIT_MODE = "enter_widgets_by_edit_mode";
	/**编辑模式系统设置*/
	public static final String ENTER_SETTING_BY_EDIT_MODE = "enter_setting_by_edit_mode";
	/**编辑模式桌面设置*/
	public static final String ENTER_LAUNCHER_SETTING_BY_EDIT_MODE = "enter_launcher_setting_by_edit_mode";
	/**桌面设置切页特效*/
	public static final String ENTER_DESKTOP_SLIDE_BY_LAUNCHER_SETTING = "enter_desktop_slide_by_launcher_setting";
	/**桌面设置桌面模式*/
	public static final String ENTER_LAUNCHER_STYLEBY_LAUNCHER_SETTING = "enter_launcher_style_by_launcher_setting";
	/**桌面设置智能分类*/
	public static final String ENTER_CATEGORY_BY_LAUNCHER_SETTING = "enter_category_by_launcher_setting";
	/**猜你喜欢图标*/
	public static final String ENTER_GUESS_YOU_LIKE = "enter_guess_you_like";//cheyingkun add	//猜你喜欢添加友盟统计
	//cheyingkun add start	//自更新添加友盟统计
	/**自更新*/
	public static final String UPDATE_BY_SELF = "update_by_self";
	/**自更新title*/
	public static final String UPDATE_BY_SELF_TITLE = "自更新";
	/**自更新对话框*/
	public static final String UPDATE_BY_SELF_DIALOG = "自更新_对话框";
	/**自更新图标*/
	public static final String UPDATE_BY_SELF_ICON = "自更新_桌面图标";
	/**自更新(桌面设置里的)*/
	public static final String UPDATE_BY_SELF_LAUNCHER_SETTING = "自更新_桌面设置";
	//cheyingkun add end	//自更新添加友盟统计
	//cheyingkun add start	//自更新完善友盟统计
	/**自更新下载*/
	public static final String UPDATE_BY_SELF_DOWN = "update_by_self_down";
	/**自更新下载完安装*/
	public static final String UPDATE_BY_SELF_INSTALL = "update_by_self_install";
	//cheyingkun add end
	//cheyingkun add start	//自更新请求数据统计
	/**自更新手动请求数据*/
	public static final String UPDATE_BY_SELF_MANUAL_REQUEST = "update_by_self_manual_request";
	/**自更新手动请求数据(成功)*/
	public static final String UPDATE_BY_SELF_MANUAL_REQUEST_SUCCESS = "update_by_self_manual_request_success";
	/**自更新手动请求数据(有新版本)*/
	public static final String UPDATE_BY_SELF_MANUAL_REQUEST_HAS_NEW_VERSION = "update_by_self_manual_request_has_new_version";
	/**自更新自动请求数据*/
	public static final String UPDATE_BY_SELF_AUTO_REQUEST = "update_by_self_auto_request";
	/**自更新自动请求数据(成功)*/
	public static final String UPDATE_BY_SELF_AUTO_REQUEST_SUCCESS = "update_by_self_auto_request_success";
	/**自更新自动请求数据(有新版本)*/
	public static final String UPDATE_BY_SELF_AUTO_REQUEST_HAS_NEW_VERSION = "update_by_self_auto_request_has_new_version";
	//cheyingkun add end
	//cheyingkun add start	//一键换壁纸(友盟统计)
	/**点击一键换壁纸图标*/
	public static final String ENTER_ONE_KEY_CHANGE_WALLPAPER = "enter_one_key_change_wallpaper";
	/**使用网络壁纸*/
	public static final String ENTER_ONE_KEY_CHANGE_NETWORK_WALLPAPER = "enter_one_key_change_network_wallpaper";
	/**使用本地壁纸*/
	public static final String ENTER_ONE_KEY_CHANGE_NATIVE_WALLPAPER = "enter_one_key_change_native_wallpaper";
	/**换回去*/
	public static final String ENTER_ONE_KEY_CHANGE_WALLPAPER_BACK = "enter_one_key_change_wallpaper_back";
	/**保存到本地*/
	public static final String ENTER_ONE_KEY_CHANGE_WALLPAPER_SAVE = "enter_one_key_change_wallpaper_save";
	/**更多(美化中心)*/
	public static final String ENTER_ONE_KEY_CHANGE_WALLPAPER_MORE = "enter_one_key_change_wallpaper_more";
	//cheyingkun add end
	// YANGTIANYU@2016/06/30 ADD START
	// 专属页统计字段
	/**进入音乐页*/
	public static final String MUSIC_PAGE_IN = "music_page_in";
	/**点击歌曲播放按钮 
	 * @Deprecated  音乐页点击歌曲播放的状态不好判断是在音乐页点击播放，还是进入音乐播放器播放，经产品确认后去掉该统计点 
	 */
	@Deprecated
	public static final String MUSIC_CHANGE_PLAY_STATE = "music_change_play_state";
	/**进入相机页*/
	public static final String CAMERA_PAGE_IN = "camera_page_in";
	/**拍照*/
	public static final String CAMERA_TAKE_PICTURE = "camera_take_picture";
	/**点击删除按钮*/
	public static final String DELETE_CLICK = "delete_click";
	// YANGTIANYU@2016/06/30 ADD END
	//cheyingkun add start	//添加运营酷生活umeng统计和内部统计
	/**运营酷生活key*/
	public static final String NOTIFY_FAVORITES_SWITCH = "notify_favorites_switch";
	/**打开酷生活key*/
	public static final String NOTIFY_FAVORITES_SWITCH_OPEN = "notify_favorites_switch_open";
	/**关闭酷生活key*/
	public static final String NOTIFY_FAVORITES_SWITCH_CLOSE = "notify_favorites_switch_close";
	//cheyingkun add end
	//cheyingkun add start	//第一次运行桌面时,统计酷生活开关默认配置(umeng统计和内部统计)
	/**默认是否显示酷生活页面的key*/
	public static final String SWITCH_ENABLE_FAVORITES_DEFAULT = "switch_enable_favorites_default";
	//cheyingkun add end
	;
	//xiatian add start	//“运营浏览器主页”的功能：针对浏览器主页运营，添加友盟统计和内部统计（从uni3移植过来）。
	/**桌面启动时浏览器主页运营已打开的次数*/
	public static final String ENABLE_OPERATE_EXPLORER = "enable_operate_explorer";
	/**运营打开次数*/
	public static final String OPERATE_ON_TIMES = "operative_on_times";
	/**运营关闭次数*/
	public static final String OPERATE_OFF_TIMES = "operative_off_times";
	/**浏览器点击次数*/
	public static final String BROWSER_BOOT_TIMES = "browser_boot_times";
	/**浏览器点击次数事件中表示是否运营的参数*/
	public static final String KEY_ENABLE_OPERATE = "enable_operate";
	/**浏览器点击次数事件中表示点击的浏览器的包名的参数*/
	public static final String KEY_BROWSER_PACKAGE = "browser_package";
	//xiatian add end
	;
}
