package com.cooee.wallpaper.wrap;


/**读取config时，为了方便修改定义静态字符串。
 * 需要和host里的字符串统一
 * 为了防止插件和桌面依赖，分开写
 * */
public class WallpaperConfigString
{
	
	/**桌面图标大小*/
	public static final String LAUNCHER_ICON_SIZEPX = "launcherIconSizePx";
	/**桌面图标字体的大小*/
	public static final String LAUNCHER_ICON_TEXT_SIZE = "launcherIconTextSize";
	/**桌面图标、字之间的间距*/
	public static final String LAUNCHER_ICON_TEXT_PADDING = "launcherIconTextPadding";
	/**是否使用友盟统计*/
	public static final String ENABLE_UMENG = "enable_umeng";
	/**热更新是否有toast提示*/
	public static final String ENABLE_UPDATE_DEBUG = "enable_update_debug";
	/**是否显示广告*/
	public static final String ENABLE_ADS = "enable_ads";
	/**壁纸是否设置尺寸*/
	public static final String LAUNCHER_SET_WALLPAPER_DIMENSIONS = "disable_set_wallpaper_dimensions";
	/**默认壁纸的配置路径*/
	public static final String CUSTOM_WALLPAPERS_PATH = "custom_wallpapers_path";
	/**壁纸是否滑动*/
	public static String DISABLE_MOVE_WALLPAPER = "disable_move_wallpaper";
	/**使用缩小后的图片设壁纸（设置大壁纸时因oom失败时可打开）*/
	public static String WHEN_CHANGE_THEME_OUTOFMEMORY = "when_change_theme_outofmemory";
	/**在设置壁纸前设置尺寸（解决某些手机上设置壁纸时黑边的问题）*/
	public static String ENABLE_SET_WALLPAPERDIM_BEFORE_SET_WALLPAPER = "enable_set_wallpaperDim_before_set_wallpaper";
	/**MTK部分手机设置单屏尺寸重启手机后，手机会自动设置壁纸尺寸为屏高x屏高导致壁纸被拉伸，此时需重新设置壁纸尺寸为单屏*/
	public static String MTK_SETWALLPAPERSIZE = "MTK_setWallpaperSize";
	/**在线壁纸的来源 */
	public static String ONLINE_WALLPAPER_FROM = "online_wallpaper_from";
}
